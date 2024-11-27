package br.com.tx.storageInterface.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

import br.com.tx.storageInterface.SpringContext;
import br.com.tx.storageInterface.Utils.FileHashUtil;
import br.com.tx.storageInterface.Utils.FilesUtils;
import br.com.tx.storageInterface.models.DriveFileInfoModel;


public class GoogleDrive {

	private MongoDBService dbService;
	private Drive googleDriveService;
	private String defaultAccout;
	private RedisTemplate<String, String> redisTemplate;

	public GoogleDrive(RedisTemplate<String, String> redisTemplate) throws GeneralSecurityException, IOException {
		var springContext = SpringContext.getSpringContext();
		this.dbService = springContext.getBean(MongoDBService.class);

		this.defaultAccout = "npcpk1999.drive01@gmail.com";

		this.googleDriveService = GoogleDriveService.getGoogleDriveService(this.defaultAccout);

		this.redisTemplate = redisTemplate;
	}

	public String downloadFile(String driveFileID, String fileName) {
		
		String tempFIlePath = null;
		try {
			String cachedContent = (String) redisTemplate.opsForValue().get("FILE-B64-" + driveFileID);
			if (cachedContent != null) {
				byte[] bContent = Base64.getDecoder().decode(cachedContent);
				tempFIlePath = FilesUtils.createTempFile(fileName, bContent, 60);
			} else {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				googleDriveService.files().get(driveFileID).executeMediaAndDownloadTo(outputStream);
				byte[] fileContent = outputStream.toByteArray();

				tempFIlePath = FilesUtils.createTempFile(fileName, fileContent, 60);

				String encodedContent = Base64.getEncoder().encodeToString(fileContent);
				redisTemplate.opsForValue().set("FILE-B64-" + driveFileID, encodedContent);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return tempFIlePath;
	}

	public String getFileContent(String fileID) {
		String cachedContent = (String) redisTemplate.opsForValue().get("FILE-STR-CONTENT-" + fileID);
		if (cachedContent != null) {
			return cachedContent;
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			this.googleDriveService.files().get(fileID).executeMediaAndDownloadTo(outputStream);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		String result = outputStream.toString(StandardCharsets.UTF_8);
		redisTemplate.opsForValue().set("FILE-STR-CONTENT-" + fileID, result);

		return result;
	}

	public String uploadFile(MultipartFile chunk, String originFileName) throws IOException, NoSuchAlgorithmException {
		String fileMD5Hash = FileHashUtil.generateMD5Hash(chunk);
		var driveFileInfoResultFind = findDriveFileInfo(null, fileMD5Hash);
		if (driveFileInfoResultFind != null) {
			return driveFileInfoResultFind.get_id();
		}
		
		String newFileName = String.format("%s-%s", UUID.randomUUID().toString(), originFileName);
		File fileMetadata = new File();
		fileMetadata.setName(newFileName);

		String tempFileDir = FilesUtils.createTempFile(newFileName, chunk.getBytes());
		var file = new java.io.File(tempFileDir);

		FileContent mediaContent = new FileContent(chunk.getContentType(), file);

		var uploadResult = this.googleDriveService.files().create(fileMetadata, mediaContent).setFields("id").execute();

		Permission permission = new Permission().setType("user").setRole("writer").setEmailAddress("npcpk1999.drive01@gmail.com");
		this.googleDriveService.permissions().create(uploadResult.getId(), permission).setFields("id").execute();
		FilesUtils.tryDeleteFile(tempFileDir);
		
		var newDriveFileInfo = new DriveFileInfoModel();
		newDriveFileInfo.setFileHash(fileMD5Hash);
		newDriveFileInfo.set_id(uploadResult.getId());
		newDriveFileInfo.setFileName(newFileName);
		this.dbService.getDriveFileInfoRepository().insert(newDriveFileInfo);
		
		return uploadResult.getId();

	}

	public boolean fileExistInDrive(String fileMD5Hash) {
		var fileModel = dbService.getDriveFileInfoRepository().countByFileHash(fileMD5Hash);

		if (fileModel > 0) {
			return true;
		}
		return false;
	}

	public DriveFileInfoModel findDriveFileInfo(String id, String fileMD5Hash) {
		if (id != null) {
			return dbService.getDriveFileInfoRepository().findBy_id(id);
		} else if (fileMD5Hash != null) {
			return dbService.getDriveFileInfoRepository().findByFileHash(fileMD5Hash);
		}
		return null;
	}

	public String getDefaultAccout() {
		return defaultAccout;
	}
}
