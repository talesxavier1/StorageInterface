package br.com.tx.storageInterface.drivers;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;

import br.com.tx.storageInterface.models.CredentialInfosModel;
import br.com.tx.storageInterface.services.GoogleCredentialService;

public class GoogleDriveDrive {

	public static Drive getDrive(String mainAccount) throws IOException, GeneralSecurityException {
		GoogleCredentialService credentialService = new GoogleCredentialService(mainAccount, "DRIVE");
		CredentialInfosModel credentialInfosModel = credentialService.getCredentials();
		Drive newDrive = new Drive.Builder(credentialInfosModel.getHttpTransport(), GsonFactory.getDefaultInstance(), credentialInfosModel.getCredential()).setApplicationName("GoogleDrive"+ "-" + mainAccount).build();
		
		return newDrive;

	}
}
