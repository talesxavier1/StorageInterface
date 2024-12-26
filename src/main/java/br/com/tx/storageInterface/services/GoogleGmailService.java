package br.com.tx.storageInterface.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

import org.springframework.data.redis.core.RedisTemplate;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;

import br.com.tx.storageInterface.SpringContext;
import br.com.tx.storageInterface.Utils.FileHashUtil;
import br.com.tx.storageInterface.Utils.FilesUtils;
import br.com.tx.storageInterface.drivers.GoogleGmailDrive;
import br.com.tx.storageInterface.enums.DriveContextEnum;
import br.com.tx.storageInterface.models.DriveFileInfoModel;

public class GoogleGmailService {

	private Gmail googleGmailDrive;
	private String defaultAccout;

	private RedisTemplate<String, String> redisTemplate;
	private MongoDBService dbService;

	public GoogleGmailService(RedisTemplate<String, String> redisTemplate) throws GeneralSecurityException, IOException {
		var springContext = SpringContext.getSpringContext();
		this.dbService = springContext.getBean(MongoDBService.class);
		this.defaultAccout = "npcpk1999.drive01@gmail.com";
		this.googleGmailDrive = GoogleGmailDrive.getDrive(this.defaultAccout);
		this.redisTemplate = redisTemplate;
	}
	
	public String addMessage(String value, String fileName) {
		try {
			String messageHash = FileHashUtil.generateMD5Hash(value);
			DriveFileInfoModel createdMessage = this.getDriveFileInfoByHash(messageHash);
			if (createdMessage != null) {
				return createdMessage.get_id();
			}

			Message message = GoogleGmailDrive.createMessage(this.defaultAccout, fileName, value);
			Draft draft = new Draft();
			draft.setMessage(message);
			Draft draftResult = this.googleGmailDrive.users().drafts().create(this.defaultAccout, draft).execute();

			String draftID = draftResult.getId();
			if (draftID == null) {
				throw new NullPointerException("Draft criado não retornou ID.");
			}
			
			var driveFileInfoModel = new DriveFileInfoModel();
			driveFileInfoModel.set_id(draftID);
			driveFileInfoModel.setFileHash(messageHash);
			driveFileInfoModel.setFileName(fileName);
			driveFileInfoModel.setDefaultAccount(this.defaultAccout);
			driveFileInfoModel.setDriveContext(DriveContextEnum.GOOGLE_GMAIL);
			dbService.getDriveFileInfoRepository().insert(driveFileInfoModel);
			
			return draftID;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getMessage(String fileID) {


		String cachedContent = (String) redisTemplate.opsForValue().get("FILE-STR-CONTENT-" + fileID);
		if (cachedContent != null) {
			return cachedContent;
		}

		Message message = null;
		try {
			message = this.getDraftMessage(fileID);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String bData = this.getMessageBodyFromMesage(message);
		byte[] decoded = null;
		try {
			decoded = Base64.getDecoder().decode(bData);
		} catch (Exception e) {
			try {
				decoded = Base64.getUrlDecoder().decode(bData);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		String decodedMessage = new String(decoded, StandardCharsets.UTF_8);

		return decodedMessage;
	}

	public String getMessageFile(String fileID, String fileName) {

		String tempFIlePath = null;
		try {

			if (fileName == null) {
				DriveFileInfoModel driveFileInfo = this.dbService.getDriveFileInfoRepository().findBy_id(fileID);
				if (driveFileInfo == null) {
					throw new NullPointerException("DriveFileInfoModel não encontrado.");
				}
				fileName = driveFileInfo.getFileName();
			}

			String cachedContent = (String) redisTemplate.opsForValue().get("STORAGE_INTERFACE-FILE-B64-" + fileID);
			if (cachedContent != null) {
				byte[] bContent = Base64.getDecoder().decode(cachedContent);
				tempFIlePath = FilesUtils.createTempFile(fileName, bContent, 60);
			} else {

				Message message = this.getDraftMessage(fileID);
				
				String bData = this.getMessageBodyFromMesage(message);
				byte[] decoded = Base64.getDecoder().decode(bData);

				tempFIlePath = FilesUtils.createTempFile(fileName, decoded, 60);
				redisTemplate.opsForValue().set("STORAGE_INTERFACE-FILE-B64-" + fileID, bData);

			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return tempFIlePath;
	}

	public String getDefaultAccout() {
		return defaultAccout;
	}


	private DriveFileInfoModel getDriveFileInfoByID(String fileID) {
		DriveFileInfoModel result = dbService.getDriveFileInfoRepository().findBy_id(fileID);
		return result;
	}

	private DriveFileInfoModel getDriveFileInfoByHash(String hash) {
		DriveFileInfoModel result = dbService.getDriveFileInfoRepository().findByFileHash(hash);
		return result;
	}

	private String getMessageBodyFromMesage(Message message) {
		try {
			MessagePart messagePart = message.getPayload();
			if (messagePart == null) {
				throw new NullPointerException("messagePart não econtrado.");
			}

			MessagePartBody messagePartBody = messagePart.getBody();
			if (messagePartBody == null) {
				throw new NullPointerException("messagePartBody não econtrado.");
			}

			String bData = messagePartBody.getData();
			if (bData == null) {
				throw new NullPointerException("bData não econtrado.");
			}

			return bData;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	private Draft getDraft(String fileID) throws IOException {
		Draft draftResult = this.googleGmailDrive.users().drafts().get(this.defaultAccout, fileID).execute();
		return draftResult;
	}

	private Message getDraftMessage(String fileID) throws IOException {
		Draft draftResult = this.getDraft(fileID);
		Message message = draftResult.getMessage();
		return message;
	}

}
