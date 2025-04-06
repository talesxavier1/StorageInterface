package br.com.tx.storageInterface.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.naming.directory.NoSuchAttributeException;

import org.apache.avalon.framework.parameters.ParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import br.com.tx.storageInterface.Utils.Utils;
import br.com.tx.storageInterface.enums.DriveContextEnum;
import br.com.tx.storageInterface.enums.ScriptModuleTypeEnum;
import br.com.tx.storageInterface.models.APIContextModel;
import br.com.tx.storageInterface.models.ArgumentsModel;
import br.com.tx.storageInterface.models.ChunkMetadataModel;
import br.com.tx.storageInterface.models.FileInfoModel;
import br.com.tx.storageInterface.models.FileModel;
import br.com.tx.storageInterface.models.FilelHierarchyModel;
import br.com.tx.storageInterface.models.PathInfoModel;
import br.com.tx.storageInterface.models.TempFileModel;

@Service
public class FileManagerService {
	
	/**
	 * Função responsável por criar um registro que represente um novo doretório.
	 * Também atualiza o diretório pai, caso haja.
	 * 
	 * @param argumentsModel   Argumentos.
	 * @param processID        ID do processo atual.
	 * @param processVersionID ID da versão do processo atual.
	 * @param packageID        ID do pacote ou atividade atual.
	 * @param packageVersionID ID da versão do pacote ou atividade atual.
	 * @param tempDirID        ID do doretorio temporário.
	 * @return Quando o diretório é criado retorna true
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean createDir(ArgumentsModel argumentsModel, String processID, String processVersionID , String packageID, String packageVersionID, String tempDirID) {
		
		FileInfoModel newFileInfoModel = new FileInfoModel();
		newFileInfoModel.setDefaultAccount("");
		newFileInfoModel.setDeleted(false);
		newFileInfoModel.setPackageID(packageID);
		newFileInfoModel.setProcessID(processID);
		newFileInfoModel.setProcessVersionID(processVersionID);
		newFileInfoModel.setPackageVersionID(packageVersionID);
		
		TempFileModel newTempFileModel = new TempFileModel();
		newTempFileModel.setKeyID(UUID.randomUUID().toString());
		newTempFileModel.setFileInfoModel(newFileInfoModel);
		newTempFileModel.setTempDirID(tempDirID);
		newTempFileModel.setTempDirDate(Utils.getDateNow());
		newTempFileModel.setName(argumentsModel.getName());
		newTempFileModel.setDateCreated(Utils.getDateNow());
		newTempFileModel.setIsDirectory(true);
		newTempFileModel.setSize(0);
		newTempFileModel.setHasSubDirectories(false);
		newTempFileModel.setScriptUnique(false);
		
		PathInfoModel[] pathInfoModels = argumentsModel.getPathInfo();
		String parentKey = null;
		String parentKeyID = null;
		/* Quando tem array de pathInfo, isso significa que o novo diretório é filho de algum outro diretório. */
		if (pathInfoModels.length > 0) {
			parentKey = pathInfoModels[pathInfoModels.length - 1].getKey(); /* parentKey é sempre a última Key do array de PathInfo. */
			String[] parentKeySplit = parentKey.split("/");
			parentKeyID = parentKeySplit[parentKeySplit.length - 1]; /* parentKeyID é sempre o último ID da sequecial de IDS separados por barra. */
			newFileInfoModel.setParentKey(parentKey);
			
			/* A key do novo diretório é formada pela key do diretório pai mais o keyID (UUID novo) do diretorio que está sendo criado.*/
			newTempFileModel.setKey(String.format("%s/%s", parentKey, newTempFileModel.getKeyID()));
		} 
		/* Quando não tem pathInfo significa que o novo diretório não tem um diretório pai.*/
		else {
			newFileInfoModel.setParentKey("");
			newTempFileModel.setKey(newTempFileModel.getKeyID());
		}

		try {
			/* Se o novo diretório tem um diretório pai, o diretório pai é atualizado com a informação que existe um doretório filho.*/
			boolean haveParentID = Utils.stringHasValue(parentKeyID);
			if (haveParentID) {
				TempFileModel parentFileModel = dbService.getTempFileRepository().findByKeyIDAndTempDirID(parentKeyID, tempDirID);
				if (parentFileModel == null) {
					throw new NullPointerException("Não foi possível encontrar diretório pai para atualização.");
				}
				parentFileModel.setHasSubDirectories(true);
				dbService.getTempFileRepository().save(parentFileModel);
			}
			
			
			dbService.getTempFileRepository().insert(newTempFileModel);
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return false;
		}

		return true;
	}
	
	/**
	 * Função responsável por buscar o conteúdo publicado de um diretório.
	 * 
	 * @param argumentsModel   Argumentos.
	 * @param processID        ID do processo atual.
	 * @param processVersionID ID da versão do processo atual.
	 * @param packageID        ID do pacote ou atividade atual.
	 * @param packageVersionID ID da versão do pacote ou atividade atual.
	 * @param scriptModule     Enumerador que indica se estamos tratando um script
	 *                         Único ou uma estrutura de arquivos e pastas.
	 * @param pageable         paginação da collection
	 * @return FileModel[] Retorna um array com as informações das pasatas e
	 *         arquivos do diretório.
	 */
	public FileModel[] getDirContent(ArgumentsModel argumentsModel, APIContextModel apiContextModel, ScriptModuleTypeEnum scriptModule, Pageable pageable) {
		PathInfoModel[] pathInfoModels = argumentsModel.getPathInfo();
		String key = "";
		if (pathInfoModels.length > 0) {
			key = pathInfoModels[pathInfoModels.length - 1].getKey();
		}

		boolean isUniqueScript;
		if (scriptModule == ScriptModuleTypeEnum.UNIQUE_SCRIPT) {
			isUniqueScript = true;
		} else {
			isUniqueScript = false;
		}

		FileModel[] result;
		if (apiContextModel.getProcessVersionID() != null) {
			result = dbService.getFilesRepository().findFiles(
					apiContextModel.getProcessID(), 
					apiContextModel.getProcessVersionID(), 
					apiContextModel.getPackageID(), 
					key,
					apiContextModel.getPackageVersionID(), 
					isUniqueScript, 
					pageable
			).toArray(new FileModel[0]);
		} 
		/* Quando o JsonSchema está utilizando a StorageInterface a versão do pacote é desconsiderada*/
		else {
			result = dbService.getFilesRepository().findFiles(
							apiContextModel.getProcessID(), 
							apiContextModel.getProcessVersionID(), 
							apiContextModel.getPackageID(), 
							key, 
							isUniqueScript, 
							pageable
			).toArray(new FileModel[0]);
		}

		return result;
	}
	
	/**
	 * Função responsável por buscar o conteúdo temporário de um diretório.
	 * 
	 * @param argumentsModel  Argumentos.
	 * @param apiContextModel IDs de contexto da requisição.
	 * @param scriptModule    Enumerador que indica se estamos tratando um script
	 *                        Único ou uma estrutura de arquivos e pastas.
	 * @param pageable        paginação da collection
	 * @return
	 */
	public TempFileModel[] getTempDirContent(ArgumentsModel argumentsModel, APIContextModel apiContextModel, ScriptModuleTypeEnum scriptModule, Pageable pageable) {

			if (!Utils.stringHasValue(apiContextModel.getTempDirID())) {
				return new TempFileModel[0];
			}
			PathInfoModel[] pathInfoModels = argumentsModel.getPathInfo();
			String key = "";
			if (pathInfoModels.length > 0) {
				key = pathInfoModels[pathInfoModels.length - 1].getKey();
			}

			boolean isUniqueScript;
			if (scriptModule == ScriptModuleTypeEnum.UNIQUE_SCRIPT) {
				isUniqueScript = true;
			} else {
				isUniqueScript = false;
			}

			TempFileModel[] result = dbService.getTempFileRepository().findTempFiles(
					apiContextModel.getProcessID(), 
					apiContextModel.getProcessVersionID(),
					apiContextModel.getPackageID(),
					key, 
					apiContextModel.getTempDirID(), 
					isUniqueScript, 
					pageable).toArray(new TempFileModel[0]);
			return result;
	}

	/**
	 * Função responsável por consultar a existência de um diretório temporário.
	 * 
	 * @param apiContextModel IDs de contexto da requisição.
	 * @return boolean Retorna true quando existe.
	 */
	public boolean tempDirExist(APIContextModel apiContextModel) {
		return dbService.getTempFileRepository().tempDirExistV2(apiContextModel.getTempDirID());
	}

	/**
	 * Função responsável por criar um diretório temporário.
	 * 
	 * @param apiContextModel IDs de contexto da requisição.
	 * @return Retorna true quando criado.
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean createTempDirContent(APIContextModel apiContextModel) {
		
		FileModel[] result = this.dbService.getFilesRepository().findBypackageID(
				apiContextModel.getProcessID(), 
				apiContextModel.getProcessVersionID(),
				apiContextModel.getPackageID(), 
				apiContextModel.getPackageVersionID()
		);
		
		List<FileModel> listResult = Arrays.asList(result);

		List<TempFileModel> newListResult = listResult.stream().map(VALUE -> {
			Gson gson = new Gson();
			String strFileModel = gson.toJson(VALUE);
			TempFileModel newTempFileModel = gson.fromJson(strFileModel, TempFileModel.class);
			
			newTempFileModel.set_id(UUID.randomUUID().toString());
			newTempFileModel.setTempDirID(apiContextModel.getTempDirID());
			newTempFileModel.setTempDirDate(Utils.getDateNow());
			return newTempFileModel;
		}).toList();

		this.dbService.getTempFileRepository().insert(newListResult);

		return true;
	}

	/**
	 * Função responsável por buscar o conteúdo de um arquivo de texto.
	 * 
	 * @param argumentsModel  Argumentos da requisição.
	 * @param apiContextModel IDs de contexto da requisição.
	 * @return Retorna o conteúdo do arquivo texto.
	 * 
	 * @throws NoSuchAttributeException Quando não é possível obter o keyID da classe argumentsModel.
	 * @throws GeneralSecurityException Quando não é possível fazer a autenticação no serviço do google.
	 * @throws IOException Quando o arquivo que está sendo acessado não está armazenado no google gmail.
	 */
	public String getFileContent(ArgumentsModel argumentsModel, APIContextModel apiContextModel) throws NoSuchAttributeException, GeneralSecurityException, IOException {
		PathInfoModel[] pathInfoModels = argumentsModel.getPathInfo();

			String keyID = null;
			if (pathInfoModels.length > 0) {
				String key = pathInfoModels[pathInfoModels.length - 1].getKey();
				String[] splitKey = key.split("/");
				keyID = splitKey[splitKey.length - 1];
			}
			if (!Utils.stringHasValue(keyID)) {
				throw new NoSuchAttributeException("Não foi possível obter o keyID do pathInfoModels.");
			}

			FileModel fileModel = null;

			if (Utils.stringHasValue(apiContextModel.getTempDirID())) {
				fileModel = dbService.getTempFileRepository().findByKeyIDAndTempDirID(keyID, apiContextModel.getTempDirID());
			}
			if (fileModel == null) {
				fileModel = dbService.getFilesRepository().findByKeyID(keyID);
			}
			if (fileModel == null) {
				throw new NoSuchAttributeException("Não foi possível obter o fileModel de keyID: " + keyID);
			}
			
			if (fileModel.getFileInfoModel().getStorageType() != DriveContextEnum.GOOGLE_GMAIL) {
				throw new IOException("Arquivo foi armazenado no drive. não é possível obter o conteúdo.");
			}
			
			String fileDriveID = fileModel.getFileInfoModel().getFileDriveID();
			if (!Utils.stringHasValue(fileDriveID)) {
				throw new NoSuchAttributeException("Não foi possível obter o fileDriveID do fileModel _id: " + fileModel.get_id());
			}
			
			GoogleGmailService drive = new GoogleGmailService();
			String result = drive.getMessage(fileModel.getFileInfoModel().getFileDriveID());
			return result;
	}

	/**
	 * Função resposável por renomear um diretório ou arquivo.
	 * 
	 * @param argumentsModel  Argumentos da requisião.
	 * @param apiContextModel IDs da requisição.
	 * @return Retorna true quando foi alterado.
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean renameFile(ArgumentsModel argumentsModel, APIContextModel apiContextModel) {
		
		PathInfoModel[] pathInfoModels = argumentsModel.getPathInfo();
		if (pathInfoModels.length == 0) { return false; }

		String fileKey = pathInfoModels[pathInfoModels.length - 1].getKey();
		if(!Utils.stringHasValue(fileKey)) { return false; }
		
		String[] keySplit = fileKey.split("/");
		String keyID = keySplit[keySplit.length - 1];
		if(!Utils.stringHasValue(keyID)) { return false; }
		
		TempFileModel fileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(keyID, apiContextModel.getTempDirID());
		if(fileModel == null) { return false; }
		
		fileModel.setName(argumentsModel.getName());
		this.dbService.getTempFileRepository().save(fileModel);
		
		return true;
	}

	/**
	 * Função resonsável por fazer a exclusão lógica de um diretório ou arquivo.
	 * 
	 * @param argumentsModel  Argumentos da requisição.
	 * @param apiContextModel IDs de contexto da requisição.
	 * @return Retorna true quando excluído.
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean logicalDeletion(ArgumentsModel argumentsModel, APIContextModel apiContextModel) {
		PathInfoModel[] pathInfoModels = argumentsModel.getPathInfo();
		String key = pathInfoModels[pathInfoModels.length - 1].getKey();
		String[] splitKey = key.split("/");
		String keyID = splitKey[splitKey.length - 1];
		
		TempFileModel fileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(keyID, apiContextModel.getTempDirID());
		if(fileModel == null) {return false;}
		
		FilelHierarchyModel filelHierarchyModel = buildFilelHierarchy(apiContextModel, fileModel);
		logicalDeleteHierarchyModel(filelHierarchyModel);
		
		String parentKey = fileModel.getFileInfoModel().getParentKey();
		if (Utils.stringHasValue(parentKey)) {
			long parentChildsQuantity = this.dbService.getTempFileRepository().countByKey(
					apiContextModel.getProcessID(), 
					apiContextModel.getProcessVersionID(),
					apiContextModel.getPackageID(), 
					parentKey, 
					apiContextModel.getTempDirID()
			);
			if (parentChildsQuantity > 0 && fileModel.getIsDirectory()) {
				String[] splitParentKey = parentKey.split("/");
				String parentkeyID = splitParentKey[splitParentKey.length - 1];
				TempFileModel parentFileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(parentkeyID, apiContextModel.getTempDirID());
				parentFileModel.setHasSubDirectories(false);
				this.dbService.getTempFileRepository().save(parentFileModel);
			}	
		}
		

		return true;
	}
	
	/**
	 * Função responsável por fazer a cópia de um arquivo ou diretório.
	 * 
	 * @param argumentsModel  Argumentos da requisição.
	 * @param apiContextModel IDs de contexto da requisição.
	 * @return Retorna true quando copiado.
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean copy(ArgumentsModel argumentsModel, APIContextModel apiContextModel) {

		try {
			PathInfoModel destinationPathInfoModel = null;
			if (argumentsModel.getDestinationPathInfo().length > 0) {
				var destinations = argumentsModel.getDestinationPathInfo();
				destinationPathInfoModel = destinations[destinations.length - 1];
			}


			PathInfoModel sourcePathInfo = null;
			if (argumentsModel.getSourcePathInfo().length > 0) {
				var sources = argumentsModel.getSourcePathInfo();
				sourcePathInfo = sources[sources.length - 1];
			}
			if(sourcePathInfo == null) { return false; }

			FilelHierarchyModel filelHierarchyModel = buildFilelHierarchy(
					apiContextModel,
					sourcePathInfo.getKey(), 
					null
			);

			if (destinationPathInfoModel != null) {
				
				if (argumentsModel.getIsSourceIsDirectory()) {
					String destinationKey = destinationPathInfoModel.getKey();
					String[] destinationKeySplit = destinationKey.split("/");
					String destinationKeyID = destinationKeySplit[destinationKeySplit.length - 1];

					TempFileModel destinationFileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(destinationKeyID, apiContextModel.getTempDirID());
					if (destinationFileModel != null) {
						destinationFileModel.setHasSubDirectories(true);
						this.dbService.getTempFileRepository().save(destinationFileModel);
					}
				}
				
				filelHierarchyModel.updateIDAndHierarchy(UUID.randomUUID().toString(), destinationPathInfoModel.getKey());
				filelHierarchyModel.updateKeyAndHierarchy(destinationPathInfoModel.getKey());
			} else {
				filelHierarchyModel.updateIDAndHierarchy(UUID.randomUUID().toString(), null);
				filelHierarchyModel.updateKeyAndHierarchy("");
			}
			insertFilelHierarchyModel(filelHierarchyModel);
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}

		return true;
	}

	/**
	 * Função resposável por mover um arquivo ou diretório.
	 * 
	 * @param argumentsModel  Argumantos da requisição.
	 * @param apiContextModel IDs de contexto da requisição.
	 * @return retorna true quando movido.
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean move(ArgumentsModel argumentsModel, APIContextModel apiContextModel) {

		try {
			PathInfoModel destinationPathInfoModel = null;
			if (argumentsModel.getDestinationPathInfo().length > 0) {
				var destinations = argumentsModel.getDestinationPathInfo();
				destinationPathInfoModel = destinations[destinations.length - 1];
			}


			PathInfoModel sourcePathInfo = null;
			if (argumentsModel.getSourcePathInfo().length > 0) {
				var sources = argumentsModel.getSourcePathInfo();
				sourcePathInfo = sources[sources.length - 1];
			}
			if(sourcePathInfo == null) { return false; }
			
			FilelHierarchyModel filelHierarchyModel = buildFilelHierarchy(apiContextModel, sourcePathInfo.getKey(), null);
			String oldParentKey = filelHierarchyModel.getSourceFileModel().getFileInfoModel().getParentKey();
			
			if (destinationPathInfoModel != null) {
				String destinationKey = destinationPathInfoModel.getKey();
				String[] destinationKeySplit = destinationKey.split("/");
				String destinationKeyID = destinationKeySplit[destinationKeySplit.length - 1];

				TempFileModel destinationFileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(destinationKeyID, apiContextModel.getTempDirID());
				boolean sourceFileModelIsDorectory = filelHierarchyModel.getSourceFileModel().getIsDirectory();
				destinationFileModel.setHasSubDirectories(sourceFileModelIsDorectory);
				this.dbService.getTempFileRepository().save(destinationFileModel);

				filelHierarchyModel.updateParentKeyAndHierarchy(destinationPathInfoModel.getKey());
			} else {
				filelHierarchyModel.updateParentKeyAndHierarchy("");
			}

			this.savetFilelHierarchyModel(filelHierarchyModel);

			if (Utils.stringHasValue(oldParentKey)) {
				long parentChildsQuantity = this.dbService.getTempFileRepository().countByParentKey(
						apiContextModel.getProcessID(), 
						apiContextModel.getProcessVersionID(),
						apiContextModel.getPackageID(), 
						oldParentKey, 
						apiContextModel.getTempDirID()
				);
				if(parentChildsQuantity == 0 && filelHierarchyModel.getSourceFileModel().getIsDirectory()) {
					String[] splitParentKey = oldParentKey.split("/");
					String parentKeyID = splitParentKey[splitParentKey.length - 1];
					TempFileModel parentFileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(parentKeyID, apiContextModel.getTempDirID());
					parentFileModel.setHasSubDirectories(false);
					this.dbService.getTempFileRepository().save(parentFileModel);
				}	
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return false;
		}

		return true;
	}
	
	/**
	 * Função responsável por fazer o upload de um arquivo. 
	 * Quando a requisição manda uma parte do chunck a função armazena essa parte até que a ultima chegue para  que o upload seja feito.
	 * 
	 * @param argumentsModel  Argumentos da requisição.
	 * @param chunk           Chunck do arquivo.
	 * @param apiContextModel IDs de contexto da requisição.
	 * @return Retorna true quando o arquivo é enviado para o drive ou quando a
	 *         parte do arquivo é armazenada no banco.
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean uploadChunk(ArgumentsModel argumentsModel, MultipartFile chunk, APIContextModel apiContextModel) {
		try {
			if (argumentsModel.getClassChunkMetadata().getTotalCount() > 1) {
				this.appendPartChuck(chunk, argumentsModel.getClassChunkMetadata());

				boolean completed = this.partChunksCompleted(argumentsModel.getClassChunkMetadata());
				if (!completed) {
					return true;
				}
				
				chunk = getMergedChunksParts(argumentsModel.getClassChunkMetadata().getUploadId(), chunk);
			}
			
			String mimeType = Utils.getFileMimeType(chunk);

			String fileDriveID = null;
			String storageDefaultAccout = null;
			DriveContextEnum storageType = null;
			if (mimeType.contains("text")) {
				GoogleGmailService drive = new GoogleGmailService();
				var bChuk = chunk.getBytes();
				var strChunk = new String(bChuk, "UTF-8");

				fileDriveID = drive.addMessage(strChunk, argumentsModel.getClassChunkMetadata().getFileName());
				storageDefaultAccout = drive.getDefaultAccout();
				storageType = DriveContextEnum.GOOGLE_GMAIL;
			} else {
				GoogleDriveService drive = new GoogleDriveService();
				fileDriveID = drive.uploadFile(chunk, argumentsModel.getClassChunkMetadata().getFileName());
				storageDefaultAccout = drive.getDefaultAccout();
				storageType = DriveContextEnum.GOOGLE_DRIVE;
			}

			PathInfoModel destinationPathInfoModel = null;
			if (argumentsModel.getDestinationPathInfo().length > 0) {
				var destinations = argumentsModel.getDestinationPathInfo();
				destinationPathInfoModel = destinations[destinations.length - 1];
			}
				
			
			FileInfoModel newFileInfoModel = new FileInfoModel();
			newFileInfoModel.setProcessID(apiContextModel.getProcessID());
			newFileInfoModel.setProcessVersionID(apiContextModel.getProcessVersionID());
			newFileInfoModel.setPackageID(apiContextModel.getPackageID());
			newFileInfoModel.setDefaultAccount(storageDefaultAccout);
			newFileInfoModel.setDeleted(false);
			newFileInfoModel.setStorageType(storageType);
			
			if (destinationPathInfoModel != null) {
				newFileInfoModel.setParentKey(destinationPathInfoModel.getKey());
			} else {
				newFileInfoModel.setParentKey("");
			}
			newFileInfoModel.setFileDriveID(fileDriveID);

			TempFileModel newTempFileModel = new TempFileModel();
			newTempFileModel.setFileInfoModel(newFileInfoModel);
			newTempFileModel.setKeyID(UUID.randomUUID().toString());
			if (destinationPathInfoModel != null) {
				newTempFileModel.setKey(destinationPathInfoModel.getKey() + "/" + newTempFileModel.getKeyID());
			} else {
				newTempFileModel.setKey(newTempFileModel.getKeyID());
			}
			newTempFileModel.setName(argumentsModel.getClassChunkMetadata().getFileName());
			newTempFileModel.setDateCreated(Utils.getDateNow());
			newTempFileModel.setIsDirectory(false);
			newTempFileModel.setSize(argumentsModel.getClassChunkMetadata().getFileSize());
			newTempFileModel.setHasSubDirectories(false);
			newTempFileModel.setTempDirDate(Utils.getDateNow());
			newTempFileModel.setTempDirID(apiContextModel.getTempDirID());
			newTempFileModel.setScriptUnique(false);

			this.dbService.getTempFileRepository().insert(newTempFileModel);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return false;
		}
	}
	
	/**
	 * Função responsável por fazer o dowload do arquivo no drive.
	 * 
	 * @param argumentsModel  Argumentos da requisição.
	 * @param apiContextModel IDs de contexto da requisição.
	 * @return Retorna o path do arquivo temporário criado.
	 * @throws GeneralSecurityException Quando não é possível fazer a autenticação no gmail.
	 */
	public String download(ArgumentsModel argumentsModel, APIContextModel apiContextModel) throws GeneralSecurityException {

		PathInfoModel[] infoModels = argumentsModel.getPathInfo();
		PathInfoModel pathInfoModel = infoModels[infoModels.length - 1];
		String fileKey = pathInfoModel.getKey();
		String[] fileKeySplit = fileKey.split("/");
		String fileKeyID = fileKeySplit[fileKeySplit.length - 1];
		
		String driveFileID = null;
		String fileName = null;
		DriveContextEnum storageType = null;
		
		TempFileModel tempFileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(fileKeyID, apiContextModel.getTempDirID());
		if(tempFileModel != null) {
			driveFileID = tempFileModel.getFileInfoModel().getFileDriveID();
			fileName = tempFileModel.getName();
			storageType = tempFileModel.getFileInfoModel().getStorageType();
		} else {
			FileModel fileModel = this.dbService.getFilesRepository().findByKeyID(fileKeyID);
			driveFileID = fileModel.getFileInfoModel().getFileDriveID();
			fileName = fileModel.getName();
			storageType = fileModel.getFileInfoModel().getStorageType();
		}


		String tempFilePath = null;
		if (storageType == DriveContextEnum.GOOGLE_DRIVE) {
			GoogleDriveService drive = new GoogleDriveService();
			tempFilePath = drive.downloadFile(driveFileID, fileName);
		} else if (storageType == DriveContextEnum.GOOGLE_GMAIL) {
			GoogleGmailService gmailService = new GoogleGmailService();
			tempFilePath = gmailService.getMessageFile(driveFileID, fileName);
		}
		
		return tempFilePath;
	}

	/**
	 * Função responsável por fazer o update do conteúdo de um arquivo de texto.
	 * 
	 * @param argumentsModel  Argumentos da requisição.
	 * @param chunk           Chunk do arquivo.
	 * @param apiContextModel IDs de contexto da requisição.
	 * @return Retorna true quando o conteúdo foi atualizado.
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean updateFileContent(ArgumentsModel argumentsModel, MultipartFile chunk, APIContextModel apiContextModel) {

		try {
			PathInfoModel[] infoModels = argumentsModel.getPathInfo();
			PathInfoModel pathInfoModel = infoModels[infoModels.length - 1];
			String fileKey = pathInfoModel.getKey();
			String[] fileKeySplit = fileKey.split("/");
			String fileKeyID = fileKeySplit[fileKeySplit.length - 1];

			TempFileModel tempFile = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(fileKeyID, apiContextModel.getTempDirID());
			if (tempFile == null) {
				throw new NoSuchAttributeException("Não foi possível encontrar tempFile.");
			}

			String mimeType = Utils.getFileMimeType(chunk);
			if (!mimeType.contains("text")) {
				throw new ParameterException("Não é possível atializar o conteúdo. Tipo de conteúdo recebido: " + mimeType);
			}

			GoogleGmailService drive = new GoogleGmailService();
			var bChuk = chunk.getBytes();
			var strChunk = new String(bChuk, "UTF-8");

			var fileDriveID = drive.addMessage(strChunk, argumentsModel.getClassChunkMetadata().getFileName());
			tempFile.getFileInfoModel().setFileDriveID(fileDriveID);

			this.dbService.getTempFileRepository().save(tempFile);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return false;
		}

	}

	/**
	 * Função responsável por fazer o insert de TempFileModel.
	 * 
	 * @param filelHierarchyModel Hierarquia de TempFileModel.
	 */
	public void insertFilelHierarchyModel(FilelHierarchyModel filelHierarchyModel) {
		dbService.getTempFileRepository().insert(filelHierarchyModel.getSourceFileModel());
		for (FilelHierarchyModel child : filelHierarchyModel.getChildsFileModel()) {
			insertFilelHierarchyModel(child);
		}
	}

	/**
	 * Função responsável por fazer o save de TempFileModel
	 * 
	 * @param filelHierarchyModel Hierarquia de TempFileModel.
	 */
	public void savetFilelHierarchyModel(FilelHierarchyModel filelHierarchyModel) {
		dbService.getTempFileRepository().save(filelHierarchyModel.getSourceFileModel());
		for (FilelHierarchyModel child : filelHierarchyModel.getChildsFileModel()) {
			savetFilelHierarchyModel(child);
		}
	}

	/**
	 * Função responsável por fazer a exclusão lógica de TempFileModel
	 * 
	 * @param filelHierarchyModel Hierarquia de TempFileModel.
	 */
	public void logicalDeleteHierarchyModel(FilelHierarchyModel filelHierarchyModel) {
		TempFileModel fileModel = filelHierarchyModel.getSourceFileModel();
		fileModel.getFileInfoModel().setDeleted(true);
		this.dbService.getTempFileRepository().save(fileModel);

		for (FilelHierarchyModel child : filelHierarchyModel.getChildsFileModel()) {
			logicalDeleteHierarchyModel(child);
		}
	}
    
	/**
	 * Função responsável por fazer a exclusão de todos os arquivos e diretórios temporários.
	 * 
	 * @param apiContextModel IDs de contexto da requisição.
	 * @return retorna true quando deletados.
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteTempDir(APIContextModel apiContextModel) {
		try {
			this.dbService.getTempFileRepository().deleteByTempDirId(apiContextModel.getTempDirID());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return false;
		}
	}
	
	/**
	 * Funcção responsável por publicar um diretório temporário.
	 * 
	 * @param apiContextModel IDs de contexto da requisição.
	 * @return Retorna o novo PackageVersionID.
	 */
	@Transactional(rollbackFor = Exception.class)
	public String pubTempDir(APIContextModel apiContextModel) {
		try {
			TempFileModel[] findResult = this.dbService.getTempFileRepository().findByTempDirID(apiContextModel.getTempDirID());
			if (findResult.length == 0) {
				throw new Error("TempDir content não encontrados.");
			}

			var newPackageVersionID = UUID.randomUUID().toString();
			List<FileModel> newResult = Arrays.asList(findResult).stream().map(VALUE -> {
				Gson gson = new Gson();
				String strTempFileModel = gson.toJson(VALUE);
				FileModel newFileModel = gson.fromJson(strTempFileModel, FileModel.class);

				newFileModel.setDateCreated(Utils.getDateNow());
				newFileModel.set_id(UUID.randomUUID().toString());
				newFileModel.getFileInfoModel().setPackageVersionID(newPackageVersionID);
				return newFileModel;
			}).toList();

			this.dbService.getFilesRepository().insert(newResult);

			boolean result = this.deleteTempDir(apiContextModel);
			if (!result) {
				throw new Error("Não foi possível deletar diretorio temporário.");
			}
			return newPackageVersionID;
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return null;
		}
	}

	/**
	 * Função responsável por salvar um arquivo de texto no googleGmail.
	 * 
	 * @param argumentsModel  Argumentos da requisição.
	 * @param chunk           Arquivo.
	 * @param apiContextModel IDs de contextp da requisição
	 * @return Retorna truen quando salvo.
	 * @throws IOException Quando não é possível obter o mimetype ou obter os bytes do chunck.
	 * @throws GeneralSecurityException Quando não é possível fazer a autenticação no google Gmail.
	 */
	public boolean saveFileContent(ArgumentsModel argumentsModel, MultipartFile chunk, APIContextModel apiContextModel)
			throws IOException,  GeneralSecurityException {
	
			String mimeType = Utils.getFileMimeType(chunk);
			if (!mimeType.contains("text")) {
				throw new IOException("Não é possível atualizar o conteúdo. Tipo de conteúdo recebido: " + mimeType);
			}

			GoogleGmailService drive = new GoogleGmailService();
			var bChuk = chunk.getBytes();
			var strChunk = new String(bChuk, "UTF-8");

			var fileDriveID = drive.addMessage(strChunk, argumentsModel.getClassChunkMetadata().getFileName());

			FileInfoModel newFileInfoModel = new FileInfoModel();
			newFileInfoModel.setProcessID(apiContextModel.getProcessID());
			newFileInfoModel.setProcessVersionID(apiContextModel.getProcessVersionID());
			newFileInfoModel.setPackageID(apiContextModel.getPackageID());
			newFileInfoModel.setDefaultAccount(drive.getDefaultAccout());
			newFileInfoModel.setDeleted(false);
			newFileInfoModel.setStorageType(DriveContextEnum.GOOGLE_GMAIL);
			newFileInfoModel.setParentKey("");
			newFileInfoModel.setFileDriveID(fileDriveID);

			TempFileModel newTempFileModel = new TempFileModel();
			newTempFileModel.setFileInfoModel(newFileInfoModel);
			newTempFileModel.setKeyID(UUID.randomUUID().toString());
			newTempFileModel.setKey(newTempFileModel.getKeyID());
			newTempFileModel.setName(argumentsModel.getClassChunkMetadata().getFileName());
			newTempFileModel.setDateCreated(Utils.getDateNow());
			newTempFileModel.setIsDirectory(false);
			newTempFileModel.setSize(argumentsModel.getClassChunkMetadata().getFileSize());
			newTempFileModel.setHasSubDirectories(false);
			newTempFileModel.setTempDirDate(Utils.getDateNow());
			newTempFileModel.setTempDirID(apiContextModel.getTempDirID());
			newTempFileModel.setScriptUnique(true);

			dbService.getTempFileRepository().save(newTempFileModel);

		return true;
	}

	// ------------------------------------------------ PRIVATE ------------------------------------------------ //	
	@Autowired
	private MongoDBService dbService;

	/**
	 * Função responsável por montar uma hierarquia de TempFiles com base em uma key
	 * ou KeyID
	 * 
	 * @param apiContextModel IDs de contexto da requisição.
	 * @param key             Key do diretório ou arquivo.
	 * @param keyID           KeyID do diretório ou arquivo.
	 * @return Retorna o FilelHierarchyModel
	 */
	private FilelHierarchyModel buildFilelHierarchy(APIContextModel apiContextModel, String key, String keyID) {
		if (!Utils.stringHasValue(key) && !Utils.stringHasValue(keyID)) {
			throw new NullPointerException("Não é posível busca FileModel. keyID e KEY nulos");
		}

		String fileDirID = keyID;
		if (fileDirID == null || fileDirID.isBlank()) {
			String[] splitKey = key.split("/");
			fileDirID = splitKey[splitKey.length - 1];
		}
		
		TempFileModel sourceFileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(fileDirID, apiContextModel.getTempDirID());
		if(sourceFileModel == null) {return null;}
		
		return this.buildFilelHierarchy(apiContextModel, sourceFileModel);
	}

	/**
	 * Função responsável por montar uma hierarquia de TempFiles com base em um TempFileModel
	 * 
	 * @param apiContextModel IDs de contexto da requisição.
	 * @param sourceFileModel TempFileModel pai 
	 * @return Retorna o FilelHierarchyModel
	 */ 
	private FilelHierarchyModel buildFilelHierarchy(APIContextModel apiContextModel, TempFileModel sourceFileModel) {
		
		var newFilelHierarchyModel = new FilelHierarchyModel();
		newFilelHierarchyModel.setSourceFileModel(sourceFileModel);
		
		var childsFileModelList = new ArrayList<FilelHierarchyModel>();
		TempFileModel[] childsFileModel = this.dbService.getTempFileRepository().findTempFiles(
				apiContextModel.getProcessID(), 
				apiContextModel.getProcessVersionID(),
				apiContextModel.getPackageID(), 
				sourceFileModel.getKey(), 
				apiContextModel.getTempDirID()
		);
		for (TempFileModel child : childsFileModel) {
			var childFilelHierarchyModel = buildFilelHierarchy(apiContextModel, child);
			childsFileModelList.add(childFilelHierarchyModel);
		}
		newFilelHierarchyModel.setChildsFileModel(childsFileModelList);

		return newFilelHierarchyModel;
	}

	/**
	 * Função responsável por arquivar o chunck part no mongodb.
	 * 
	 * @param chunk              chunk part
	 * @param chunkMetadataModel Classe com as informaçõe do chuck
	 * @return retorna true quando salvo.
	 * @throws IOException Quando não é possível obter os bytes do chuck
	 */
	private boolean appendPartChuck(MultipartFile chunk, ChunkMetadataModel chunkMetadataModel) throws IOException {
		chunkMetadataModel.setPartByte(chunk.getBytes());
		this.dbService.getTempChunckPartInfoRepository().insert(chunkMetadataModel);
		return true;
	}

	/**
	 * Função responsável por verificar se todas as partes dos chucks chegaram.
	 * 
	 * @param chunkMetadataModel Propriedades do chuck atual.
	 * @return retorna true quando todos os chuck estão no mongo.
	 */
	private boolean partChunksCompleted(ChunkMetadataModel chunkMetadataModel) {
		long result = this.dbService.getTempChunckPartInfoRepository().countChunckParts(chunkMetadataModel.getUploadId());
		if(result == chunkMetadataModel.getTotalCount()) {
			return true;
		}
		return false;
	}

	/**
	 * Função responsável por unir os chucks parts.
	 * 
	 * @param UploadId  Id de upload dos chucks
	 * @param lastChunk último chuk.
	 * @return Retorna um MultipartFile unificado.
	 * @throws IOException Quando não é possível obter os bytes do chuck
	 */
	private MultipartFile getMergedChunksParts(String UploadId, MultipartFile lastChunk) throws IOException {
		ChunkMetadataModel[] results = this.dbService.getTempChunckPartInfoRepository().getChunkMetadataByUploadId(UploadId);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		for (ChunkMetadataModel result : results) {
            outputStream.write(result.getPartByte());
        }
		

		MultipartFile mergedMultipartFile = new MockMultipartFile(lastChunk.getName(), lastChunk.getOriginalFilename(), lastChunk.getContentType(), outputStream.toByteArray());
		
		return mergedMultipartFile;
	}




	
	
}
