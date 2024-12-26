package br.com.tx.storageInterface.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import br.com.tx.storageInterface.SpringContext;
import br.com.tx.storageInterface.Utils.Utils;
import br.com.tx.storageInterface.models.CredentialInfosModel;
import br.com.tx.storageInterface.models.DriveConfigsModel;

public class GoogleCredentialService {

	private MongoDBService dbService;
	private String emailContaPrincipal;
	private String scope;
	
	public GoogleCredentialService(String emailContaPrincipal, String scope) {
		var springContext = SpringContext.getSpringContext();
		this.dbService = springContext.getBean(MongoDBService.class);
		this.emailContaPrincipal = emailContaPrincipal;
		this.scope = scope;
	}

	public CredentialInfosModel getCredentials() {

		DriveConfigsModel config = this.getConfig();
		var result = new CredentialInfosModel();
		try {
			NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			result.setHttpTransport(httpTransport);

			var builder = new GoogleCredential.Builder();
			builder.setTransport(httpTransport);
			builder.setJsonFactory(GsonFactory.getDefaultInstance());
			builder.setClientSecrets(config.getClientID(), config.getClientSecret());

			Credential credential = builder.build();
			credential.setRefreshToken(config.getRefreshToken());

			/* ---------------------  atualização do AccessToken ---------------------*/
			// No bloco sincronizado, é verificado novamente se o token está vencido. ( outra thread pode ter atualizado).
			if (!this.checkTokenValidity(config)) {
				synchronized (GoogleCredentialService.class) {
					config = this.getConfig();
					if (!this.checkTokenValidity(config)) {
						credential.refreshToken();
						config.setAccessToken(credential.getAccessToken());
						config.setExpiresIn(credential.getExpiresInSeconds());
						config.setTokenGeneratedTimestamp(Utils.getDateNow().getTime());
						this.dbService.getDriveConfigsRepository().save(config);
					}

				}
			}
			/* ---------------------------------------------------------------------- */
			credential.setAccessToken(config.getAccessToken());

			
			result.setCredential(credential);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}
	
	private boolean checkTokenValidity(DriveConfigsModel config) {

		long currentTime = Utils.getDateNow().getTime();
		long tokenValiditySeconds = (config.getExpiresIn() - 120) * 1000;
		long tokenTime = config.getTokenGeneratedTimestamp();

		if ((tokenTime + tokenValiditySeconds) < currentTime) {
			return false;
		}

		return true;
	}
	
	private DriveConfigsModel getConfig() {
		var config = this.dbService.getDriveConfigsRepository().getConfig(this.emailContaPrincipal, this.scope);

		if (config == null) {
			throw new NullPointerException(String.format("Não foi possível encontrar registro de configuraçoes para emailContaPrincipal: %s e scope: %s", emailContaPrincipal, scope));
		}
		return config;
	}
}