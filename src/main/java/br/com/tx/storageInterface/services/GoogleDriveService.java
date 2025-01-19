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

import br.com.tx.storageInterface.SpringContext;
import br.com.tx.storageInterface.Utils.FileHashUtil;
import br.com.tx.storageInterface.Utils.FilesUtils;
import br.com.tx.storageInterface.drivers.GoogleDriveDrive;
import br.com.tx.storageInterface.models.DriveFileInfoModel;

/** Classe resposável por manipular arquivos no googleDrive. */
public class GoogleDriveService {

	/** Instância do servico do mongoDB */
	private MongoDBService dbService;

	/** Drive do google Drive. */
	private Drive googleDriveDrive;

	/** Conta de email principal do google drive. */
	private String defaultAccout;

	/** Instância do redis para cache. */
	private RedisTemplate<String, String> redisTemplate;

	/**
	 * 
	 * @param redisTemplate Instâcia do redis para cache.
	 * @throws GeneralSecurityException Quando não é possível autenticar a conta google.
	 * @throws IOException Quando não é possivel criar algum arquivo necessário para a autenticação com o google.
	 */
	public GoogleDriveService(RedisTemplate<String, String> redisTemplate) throws GeneralSecurityException, IOException {
		var springContext = SpringContext.getSpringContext();
		this.dbService = springContext.getBean(MongoDBService.class);
		this.defaultAccout = "npcpk1999.drive01@gmail.com";

		this.googleDriveDrive = GoogleDriveDrive.getDrive(this.defaultAccout);

		this.redisTemplate = redisTemplate;
	}
	
	/**
	 * Função responsável por fazer o download de um arquivo armazenado no google
	 * Drive.
	 * 
	 * @param driveFileID ID do arquivo no Google Drive.
	 * @param fileName    Nome do arquivo que vai ser criado localmente. Caso não
	 *                    seja passado, o arquivo será criado com o nome original
	 *                    dele.
	 * @return Retorna o Path do arquivo temporário criado.
	 */
	public String downloadFile(String driveFileID, String fileName) {

		if (fileName == null) {
			DriveFileInfoModel driveFileInfo = this.dbService.getDriveFileInfoRepository().findBy_id(driveFileID);
			if (driveFileInfo == null) {
				throw new NullPointerException("DriveFileInfoModel não encontrado.");
			}
			fileName = driveFileInfo.getFileName();
		}

		if (fileName == null) {
			System.out.println("GoogleDrive.downloadFile() -  file name não passado.");
			fileName = UUID.randomUUID().toString();
		}

		if (driveFileID == null) {
			throw new NullPointerException("driveFileID nulo.");
		}

		String tempFIlePath = null;
		try {
			String cachedContent = (String) redisTemplate.opsForValue().get("FILE-B64-" + driveFileID);
			if (cachedContent != null) {
				byte[] bContent = Base64.getDecoder().decode(cachedContent);
				tempFIlePath = FilesUtils.createTempFile(fileName, bContent, 60);
			} else {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				googleDriveDrive.files().get(driveFileID).executeMediaAndDownloadTo(outputStream);
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

	/**
	 * Função responsável por retornar o conteúdo do arquivo armazenado no google
	 * drive.
	 * 
	 * @deprecated Função não é mais utilizada, porque os arquivos de texto estão
	 *             sendo enviados para o google gmail.
	 * 
	 * @param fileID ID do aqruivo no google drive.
	 * @return Conteúdo do arquivo no google Drive.
	 */
	@Deprecated
	public String getFileContent(String fileID) {
		String cachedContent = (String) redisTemplate.opsForValue().get("FILE-STR-CONTENT-" + fileID);
		if (cachedContent != null) {
			return cachedContent;
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			this.googleDriveDrive.files().get(fileID).executeMediaAndDownloadTo(outputStream);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		String result = outputStream.toString(StandardCharsets.UTF_8);
		redisTemplate.opsForValue().set("FILE-STR-CONTENT-" + fileID, result);

		return result;
	}

	/**
	 * Função responsável por fazer o upload do arquivo no google drive.
	 * 
	 * @param chunk          Arquivo.
	 * @param originFileName Nome do arquivo.
	 * @return Retorna o ID do arquivo no google drive.
	 * @throws IOException              Quando não é possível calcular o hash md5 do
	 *                                  arquivo ou enviar o arquivo para o google
	 *                                  Drive.
	 * @throws NoSuchAlgorithmException Quando não é possível calcular o hash md5 do
	 *                                  arquivo
	 */
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

		var uploadResult = this.googleDriveDrive.files().create(fileMetadata, mediaContent).setFields("id").execute();

		FilesUtils.tryDeleteFile(tempFileDir);
		
		var newDriveFileInfo = new DriveFileInfoModel();
		newDriveFileInfo.setFileHash(fileMD5Hash);
		newDriveFileInfo.set_id(uploadResult.getId());
		newDriveFileInfo.setFileName(newFileName);
		this.dbService.getDriveFileInfoRepository().insert(newDriveFileInfo);

		
		return uploadResult.getId();

	}

	/**
	 * Função que verifica se existe registro de upload do arquivo no google drive
	 * pelo hashmd5.
	 * 
	 * @deprecated Não é necessário, pois a função findDriveFileInfo substituiu
	 *             essa.
	 * @param fileMD5Hash HashMD5 do arquivo.
	 * @return Retorna true caso encontre algum registro de upload no banco.
	 */
	@Deprecated
	public boolean fileExistInDrive(String fileMD5Hash) {
		var fileModel = dbService.getDriveFileInfoRepository().countByFileHash(fileMD5Hash);

		if (fileModel > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Função que procura as informaçõe de upload de um arquivo com base no ID ou
	 * hash do arquivo.
	 * 
	 * 
	 * @param id          ID do arquivo no banco.
	 * @param fileMD5Hash hashmd5 do aqruivo.
	 * @return Retorna a classe DriveFileInfoModel.
	 */
	public DriveFileInfoModel findDriveFileInfo(String id, String fileMD5Hash) {
		if (id != null) {
			return dbService.getDriveFileInfoRepository().findBy_id(id);
		} else if (fileMD5Hash != null) {
			return dbService.getDriveFileInfoRepository().findByFileHash(fileMD5Hash);
		}
		return null;
	}

	/**
	 * @return Retorna a conta google pricipal do drive.
	 */
	public String getDefaultAccout() {
		return defaultAccout;
	}
}
