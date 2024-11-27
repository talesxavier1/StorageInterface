package br.com.tx.storageInterface.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import br.com.tx.storageInterface.SpringContext;
import br.com.tx.storageInterface.Utils.FilesUtils;
import br.com.tx.storageInterface.models.DriveConfigsModel;

public class GoogleDriveService {

	private static Map<String, Drive> services = new HashMap<String, Drive>();
	private static MongoDBService mongoDBService;

	public static Drive getGoogleDriveService(String mainAccount) throws IOException, GeneralSecurityException {
		var initializedDrive = services.get(mainAccount);
		if (initializedDrive != null) {
			return initializedDrive;
		}

		if (mongoDBService == null) {
			var springContext = SpringContext.getSpringContext();
			mongoDBService = springContext.getBean(MongoDBService.class);
		}

		return initGoogleDriveService(mainAccount);

	}

	private static Drive initGoogleDriveService(String mainAccount) {

		DriveConfigsModel driveConfigsModel = mongoDBService.getDriveConfigsRepository().getByEmailContaPrincipal(mainAccount);
		if (driveConfigsModel == null) {
			throw new NullPointerException("Não foi possível encontrar configurações para a conta " + mainAccount);
		}

		String newFileName = String.format("%s-%s", UUID.randomUUID().toString(), "client_secret.json");
		String tempFilePath = FilesUtils.createJsonFileFromString(newFileName, driveConfigsModel.getStrJsonClientSecret());
		if (tempFilePath == null) {
			throw new NullPointerException("Não foi possível criar arquivo temporário de configurações.");
		}

		File initialFile = new File(tempFilePath);
		Drive newDrive = null;
	
		try(InputStream inputStream = new FileInputStream(initialFile)){
			GoogleCredential credential = GoogleCredential.fromStream(inputStream).createScoped(Collections.singleton(DriveScopes.DRIVE));
			NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			newDrive = new Drive.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), credential).setApplicationName("GoogleDrive"+"-"+mainAccount).build();
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		} finally {
			FilesUtils.tryDeleteFile(tempFilePath);
		}
	
		if (newDrive != null) {
			services.put(mainAccount, newDrive);
		} else {
			throw new NullPointerException("Não foi possível criar conexão com GoogleDrive.");
		}

		return newDrive;
	}

}
