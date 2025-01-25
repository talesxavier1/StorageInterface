package br.com.tx.storageInterface.drivers;

import java.security.GeneralSecurityException;

import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;

import br.com.tx.storageInterface.enums.DriveContextEnum;
import br.com.tx.storageInterface.models.GoogleCredentialInfosModel;
import br.com.tx.storageInterface.services.GoogleCredentialService;
	
/** Classe responsável por gerenciar o drive para consumo do serviço google gmail.*/
public class GoogleGmailDrive {

	/**
	 * Retorna o drive autenticado com base na conta de email do serviço.
	 * 
	 * @param mainAccount Conta do serviço.
	 * @return Retorna o drive do Gmail.
	 * @throws GeneralSecurityException Quando não é possível concluir a autenticação.
	 */
	public static Gmail getDrive(String mainAccount) throws GeneralSecurityException {
		GoogleCredentialService credentialService = new GoogleCredentialService(mainAccount, DriveContextEnum.GOOGLE_GMAIL);
		GoogleCredentialInfosModel credentialInfosModel = credentialService.getCredentials();
		Gmail newDrive = new Gmail.Builder(credentialInfosModel.getHttpTransport(), GsonFactory.getDefaultInstance(), credentialInfosModel.getCredential()).setApplicationName("GoogleDrive" + "-" + mainAccount).build();
		return newDrive;
	}

}
