package br.com.tx.storageInterface.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import br.com.tx.storageInterface.SpringContext;
import br.com.tx.storageInterface.Utils.Utils;
import br.com.tx.storageInterface.enums.DriveContextEnum;
import br.com.tx.storageInterface.models.GoogleCredentialInfosModel;
import br.com.tx.storageInterface.models.DriveConfigsModel;

/** Classe resposável por gereciar as credenciais google. */
public class GoogleCredentialService {

	/** Instancia do serviço do mongodb */
	private MongoDBService dbService;

	/** Conta de email da credencial. */
	private String emailContaPrincipal;

	/** Enum que define qual é o tipo de armazenamento que será utilizado. */
	private DriveContextEnum driveContext;
	
	/**
	 * 
	 * @param emailContaPrincipal Endereço de email da conta do serviço.
	 * @param driveContext        Tipo de de serviço de armazenamento.
	 */
	public GoogleCredentialService(String emailContaPrincipal, DriveContextEnum driveContext) {
		var springContext = SpringContext.getSpringContext();
		this.dbService = springContext.getBean(MongoDBService.class);
		this.emailContaPrincipal = emailContaPrincipal;
		this.driveContext = driveContext;
	}

	/**
	 * Função que monta a credencial necessário para a autenticação com o google.
	 * 
	 * @return retorna a classe CredentialInfosModel que é necessária para a autenticação do serviço google.
	 */
	public GoogleCredentialInfosModel getCredentials() {

		DriveConfigsModel config = this.getConfig();
		var result = new GoogleCredentialInfosModel();
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
	
	/**
	 * Função verifica se o token armazenado ainda é válido.
	 * 
	 * @param config classe com as informações da autenticação OAuth do google.
	 * @return Retorna true quando está válido.
	 */
	private boolean checkTokenValidity(DriveConfigsModel config) {

		long currentTime = Utils.getDateNow().getTime();
		long tokenValiditySeconds = (config.getExpiresIn() - 120) * 1000;
		long tokenTime = config.getTokenGeneratedTimestamp();

		if ((tokenTime + tokenValiditySeconds) < currentTime) {
			return false;
		}

		return true;
	}
		
	/**
	 * Função responsável por buscar o registro de configuração do serviço do
	 * google.
	 * 
	 * @return Retorna a classe de configuração.
	 */
	private DriveConfigsModel getConfig() {
		var config = this.dbService.getDriveConfigsRepository().getConfig(this.emailContaPrincipal, this.driveContext);

		if (config == null) {
			throw new NullPointerException(String.format("Não foi possível encontrar registro de configuraçoes para emailContaPrincipal: %s e scope: %s", emailContaPrincipal, driveContext));
		}
		return config;
	}
}