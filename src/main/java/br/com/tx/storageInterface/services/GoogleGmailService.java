package br.com.tx.storageInterface.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

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

/** Classe com as fuções para manipular os registros do Gmail. */
public class GoogleGmailService {

	/** Drive do Gmail. */
	private Gmail googleGmailDrive;
	/** Conta Google principal. */
	private String defaultAccout;

	/** Instância do Redis para chache. */
	private RedisTemplate<String, String> redisTemplate;

	/** Instância do servisos do mongoDB */
	private MongoDBService dbService;

	/**
	 * 
	 * @param redisTemplate Instânia do Redis para cache.
	 * @throws GeneralSecurityException Quado não é pissível obter o Drive do Gmail.
	 * @throws IOException Quando algum arquivo necessário para criar o drive do Gmail não pode ser lido.
	 */
	public GoogleGmailService(RedisTemplate<String, String> redisTemplate) throws GeneralSecurityException, IOException {
		var springContext = SpringContext.getSpringContext();
		this.dbService = springContext.getBean(MongoDBService.class);
		this.defaultAccout = "npcpk1999.drive01@gmail.com";
		this.googleGmailDrive = GoogleGmailDrive.getDrive(this.defaultAccout);
		this.redisTemplate = redisTemplate;
	}
	
	/**
	 * Cria um Draft no Gmail. E armazena as informações do arquivo na collection DriveFileInfo.
	 * 
	 * @param value    Conteúdo do Draft.
	 * @param fileName Nome do arquivo ou descrição do conteúdo.
	 * @return Retorna o ID do Draft criado no Gmail. Retorna null quando não é possível criar o Draft.
	 */
	public String addMessage(String value, String fileName) {
		try {
			String messageHash = FileHashUtil.generateMD5Hash(value);
			DriveFileInfoModel createdMessage = this.getDriveFileInfoByHash(messageHash);
			if (createdMessage != null) {
				return createdMessage.get_id();
			}

			Message message = createMessage(this.defaultAccout, fileName, value); // |||||||||||||||||||||||||||||||||||||||||||||||||||||||||
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

	/**
	 * Consulta um Draft no Gmail com base no ID da mensagem
	 * 
	 * @param fileID ID do Draft no Gmail.
	 * @return Retorna o conteúdo do Draft
	 */
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

	/**
	 * Busca o conteúdo do Draft e cria um arquivo com base no retorno.
	 * 
	 * @param fileID   ID do Draft no Gmail.
	 * @param fileName Nome do arquivo que vai ser criado. Quando não é passado, o arquivo é criado com o mesmo nome que foi enviado no momento do upload.
	 * @return Retorna o Path do arquivo criado. Retorna null quando não é possível criar o arquivo.
	 */
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

	/**
	 * @return Retorna o email da conta Google.
	 */
	public String getDefaultAccout() {
		return defaultAccout;
	}

	/**
	 * Busca as informações do arquivo com base no hash md5 dele.
	 * 
	 * @param hash hashmd5 do arquivo ou texto.
	 * @return Retorna as informaçoes do arquivo.
	 */
	private DriveFileInfoModel getDriveFileInfoByHash(String hash) {
		DriveFileInfoModel result = dbService.getDriveFileInfoRepository().findByFileHash(hash);
		return result;
	}

	/**
	 * @param message Mensagem.
	 * @return Retorna o conteúdo do body de uma Message. Retorna null quando não é possível obter o Body.
	 */
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

	/**
	 * Busca um draft no gmail com base no ID da mensagem.
	 * 
	 * @param fileID ID do draft no gmail.
	 * @return Retorna o Draft.
	 * @throws IOException Quando não é possível executar a consulta utilizando o drive do Gmail.
	 */
	private Draft getDraft(String fileID) throws IOException {
		Draft draftResult = this.googleGmailDrive.users().drafts().get(this.defaultAccout, fileID).execute();
		return draftResult;
	}

	/**
	 * Busca um a Message de um draft no gmail.
	 * 
	 * @param fileID fileID ID do draft no gmail.
	 * @return Retorna a Message de um Draft.
	 * @throws IOException Quando não é possível executar a consulta utilizando o drive do Gmail.
	 */
	private Message getDraftMessage(String fileID) throws IOException {
		Draft draftResult = this.getDraft(fileID);
		Message message = draftResult.getMessage();
		return message;
	}

	/**
	 * Cria a classe Message que o google aceita para a criação de um email.
	 * 
	 * @param recipientEmail Destinatário.
	 * @param subject        Assunto.
	 * @param bodyText       Conteúdo da mensagem.
	 * @return retorna a class Message.
	 * @throws Exception lançado quando ocorre algum erro ao tentar gravar o conteúdo da mensagem na class.
	 */
	private Message createMessage(String recipientEmail, String subject, String bodyText) throws Exception {
		MimeMessage mimeMessage = createMimeMessage(recipientEmail, "me", subject, bodyText);
		Message message = createMessageWithMimeMessage(mimeMessage);
		return message;
	}

	/**
	 * Cria a classe MimeMessage. Base para criar a classe Message.
	 * 
	 * @param to       Destino da mensagem.
	 * @param from     Origem da mensagem.
	 * @param subject  assunto.
	 * @param bodyText conteúdo.
	 * @return retora a classe MimeMessage
	 * @throws Exception lançado quando ocorre algum erro ao tentar gravar o conteúdo da mensagem na class.
	 */
	private MimeMessage createMimeMessage(String to, String from, String subject, String bodyText) throws Exception {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);
		email.setFrom(new InternetAddress(from));
		email.addRecipient(RecipientType.TO, new InternetAddress(to));
		email.setSubject(subject);
		email.setText(bodyText);

		return email;
	}

	/**
	 * Cria uma Message com base em uma MimeMessage.
	 * 
	 * @param emailContent MimeMessage
	 * @return retora a classe Message montada.
	 * @throws Exception lançado quando ocorre algum erro ao tentar gravar o conteúdo da mensagem na class.
	 */
	private Message createMessageWithMimeMessage(MimeMessage emailContent) throws Exception {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		emailContent.writeTo(buffer);
		byte[] bytes = buffer.toByteArray();
		String encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);

		Message message = new Message();
		message.setRaw(encodedEmail);

		return message;
	}

}
