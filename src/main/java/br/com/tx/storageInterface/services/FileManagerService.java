package br.com.tx.storageInterface.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import br.com.tx.storageInterface.Utils.Utils;
import br.com.tx.storageInterface.models.ArgumentsModel;
import br.com.tx.storageInterface.models.ChunkMetadataModel;
import br.com.tx.storageInterface.models.FileInfoModel;
import br.com.tx.storageInterface.models.FileModel;
import br.com.tx.storageInterface.models.FilelHierarchyModel;
import br.com.tx.storageInterface.models.PathInfoModel;
import br.com.tx.storageInterface.models.TempFileModel;

@Service
public class FileManagerService {
	
	@Autowired
	private MongoDBService dbService;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
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
		
		PathInfoModel[] pathInfoModels = argumentsModel.getPathInfo();
		String parentKey = null;
		String parentKeyID = null;
		if (pathInfoModels.length > 0) {
			parentKey = pathInfoModels[pathInfoModels.length - 1].getKey();
			String[] parentKeySplit = parentKey.split("/");
			parentKeyID = parentKeySplit[parentKeySplit.length - 1];
			newFileInfoModel.setParentKey(parentKey);

			newTempFileModel.setKey(String.format("%s/%s", parentKey, newTempFileModel.getKeyID()));
		} else {
			newFileInfoModel.setParentKey("");
			newTempFileModel.setKey(newTempFileModel.getKeyID());
		}

		try {
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
			return false;
		}

		return true;
	}
	
	public FileModel[] getDirContent(ArgumentsModel argumentsModel, String processID, String processVersionID, String packageID, String packageVersionID) {
		try {
			PathInfoModel[] pathInfoModels = argumentsModel.getPathInfo();
			String key = "";
			if (pathInfoModels.length > 0) {
				key = pathInfoModels[pathInfoModels.length - 1].getKey();
			}

			FileModel[] result = dbService.getFilesRepository().findFiles(processID, processVersionID, packageID, key, packageVersionID);

			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public TempFileModel[] getTempDirContent(ArgumentsModel argumentsModel, String processID, String processVersionID, String packageID, String tempDirID) {
		try {
			if (!Utils.stringHasValue(tempDirID)) {
				return new TempFileModel[0];
			}
			PathInfoModel[] pathInfoModels = argumentsModel.getPathInfo();
			String key = "";
			if (pathInfoModels.length > 0) {
				key = pathInfoModels[pathInfoModels.length - 1].getKey();
			}

			TempFileModel[] result = dbService.getTempFileRepository().findTempFiles(processID, processVersionID, packageID, key, tempDirID);

			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
		
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean createTempDirContent(String processID, String processVersionID, String packageID,String packageVersionID, String tempDirID) {
		
		FileModel[] result = this.dbService.getFilesRepository().findBypackageID(processID, processVersionID, packageID, packageVersionID);
		
		List<FileModel> listResult = Arrays.asList(result);

		List<TempFileModel> newListResult = listResult.stream().map(VALUE -> {
			Gson gson = new Gson();
			String strFileModel = gson.toJson(VALUE);
			TempFileModel newTempFileModel = gson.fromJson(strFileModel, TempFileModel.class);
			
			newTempFileModel.set_id(UUID.randomUUID().toString());
			newTempFileModel.setTempDirID(tempDirID);
			newTempFileModel.setTempDirDate(Utils.getDateNow());
			return newTempFileModel;
		}).toList();

		this.dbService.getTempFileRepository().insert(newListResult);

		return true;
	}

	public String getFileContent(ArgumentsModel argumentsModel) {
		PathInfoModel[] pathInfoModels = argumentsModel.getPathInfo();
		String id = null;
		if (pathInfoModels.length > 0) {
			String key = pathInfoModels[pathInfoModels.length - 1].getKey();
			String[] splitKey = key.split("/");
			id = splitKey[splitKey.length - 1];
		}
		
		if (id == null || id.isBlank()) {
			return "";
		}

		FileModel fileModel = dbService.getFilesRepository().findByKeyID(id);
		if (fileModel == null) {
			return "";
		}

		try {
			GoogleDrive drive = new GoogleDrive(redisTemplate);
			String result = drive.getFileContent(fileModel.getFileInfoModel().getFileDriveID());
			return result;
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean renameFile(ArgumentsModel argumentsModel, String processID, String processVersionID, String packageID, String tempDirID) {
		
		PathInfoModel[] pathInfoModels = argumentsModel.getPathInfo();
		if (pathInfoModels.length == 0) { return false; }

		String fileKey = pathInfoModels[pathInfoModels.length - 1].getKey();
		if(!Utils.stringHasValue(fileKey)) { return false; }
		
		String[] keySplit = fileKey.split("/");
		String keyID = keySplit[keySplit.length - 1];
		if(!Utils.stringHasValue(keyID)) { return false; }
		
		TempFileModel fileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(keyID, tempDirID);
		if(fileModel == null) { return false; }
		
		fileModel.setName(argumentsModel.getName());
		this.dbService.getTempFileRepository().save(fileModel);
		
		return true;
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean logicalDeletion(ArgumentsModel argumentsModel, String processID, String processVersionID, String packageID, String tempDirID) {
		PathInfoModel[] pathInfoModels = argumentsModel.getPathInfo();
		String key = pathInfoModels[pathInfoModels.length - 1].getKey();
		String[] splitKey = key.split("/");
		String keyID = splitKey[splitKey.length - 1];
		
		TempFileModel fileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(keyID, tempDirID);
		if(fileModel == null) {return false;}
		
		FilelHierarchyModel filelHierarchyModel =  buildFilelHierarchy(processID, processVersionID, packageID, fileModel, tempDirID);
		logicalDeleteHierarchyModel(filelHierarchyModel);
		
		String parentKey = fileModel.getFileInfoModel().getParentKey();
		if (Utils.stringHasValue(parentKey)) {
			long parentChildsQuantity = this.dbService.getFilesRepository().countByParentKey(processID, processVersionID, packageID, parentKey, tempDirID);
			if(parentChildsQuantity == 0 && fileModel.getIsDirectory()) {
				String[] splitParentKey = parentKey.split("/");
				String parentkeyID = splitParentKey[splitParentKey.length - 1];
				FileModel parentFileModel = this.dbService.getFilesRepository().findByKeyIDAndTempDirID(parentkeyID, tempDirID);
				parentFileModel.setHasSubDirectories(false);
				this.dbService.getFilesRepository().save(parentFileModel);
			}	
		}
		

		return true;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public boolean copy(ArgumentsModel argumentsModel, String processID, String processVersionID, String packageID, String tempDirID) {

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

			FilelHierarchyModel filelHierarchyModel = buildFilelHierarchy(processID, processVersionID, packageID, sourcePathInfo.getKey(), null, tempDirID);

			if (destinationPathInfoModel != null) {
				
				if (argumentsModel.getIsSourceIsDirectory()) {
					String destinationKey = destinationPathInfoModel.getKey();
					String[] destinationKeySplit = destinationKey.split("/");
					String destinationKeyID = destinationKeySplit[destinationKeySplit.length - 1];

					TempFileModel destinationFileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(destinationKeyID, tempDirID);
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
			return false;
		}

		return true;
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean move(ArgumentsModel argumentsModel, String processID, String processVersionID, String packageID, String tempDirID) {

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
			
			FilelHierarchyModel filelHierarchyModel = buildFilelHierarchy(processID, processVersionID, packageID, sourcePathInfo.getKey(), null, tempDirID);
			String oldParentKey = filelHierarchyModel.getSourceFileModel().getFileInfoModel().getParentKey();
			
			if (destinationPathInfoModel != null) {
				String destinationKey = destinationPathInfoModel.getKey();
				String[] destinationKeySplit = destinationKey.split("/");
				String destinationKeyID = destinationKeySplit[destinationKeySplit.length - 1];

				TempFileModel destinationFileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(destinationKeyID, tempDirID);
				boolean sourceFileModelIsDorectory = filelHierarchyModel.getSourceFileModel().getIsDirectory();
				destinationFileModel.setHasSubDirectories(sourceFileModelIsDorectory);
				this.dbService.getTempFileRepository().save(destinationFileModel);

				filelHierarchyModel.updateParentKeyAndHierarchy(destinationPathInfoModel.getKey());
			} else {
				filelHierarchyModel.updateParentKeyAndHierarchy("");
			}

			this.savetFilelHierarchyModel(filelHierarchyModel);

			if (Utils.stringHasValue(oldParentKey)) {
				long parentChildsQuantity = this.dbService.getTempFileRepository().countByParentKey(processID, processVersionID, packageID, oldParentKey, tempDirID);
				if(parentChildsQuantity == 0 && filelHierarchyModel.getSourceFileModel().getIsDirectory()) {
					String[] splitParentKey = oldParentKey.split("/");
					String parentKeyID = splitParentKey[splitParentKey.length - 1];
					TempFileModel parentFileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(parentKeyID, tempDirID);
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
	
	@Transactional(rollbackFor = Exception.class)
	public boolean UploadChunk(ArgumentsModel argumentsModel, MultipartFile chunk, String processID, String processVersionID, String packageID, String tempDirID) {
		try {
			if (argumentsModel.getClassChunkMetadata().getTotalCount() > 1) {
				this.appendPartChunck(chunk, argumentsModel.getClassChunkMetadata());

				boolean completed = this.partChunksCompleted(argumentsModel.getClassChunkMetadata());
				if (!completed) {
					return true;
				}
				
				chunk = getMergedChunksParts(argumentsModel.getClassChunkMetadata().getUploadId(), chunk);
			}
			

			GoogleDrive drive = new GoogleDrive(null);
			String fileDriveID = drive.uploadFile(chunk, argumentsModel.getClassChunkMetadata().getFileName());

			PathInfoModel destinationPathInfoModel = null;
			if (argumentsModel.getDestinationPathInfo().length > 0) {
				var destinations = argumentsModel.getDestinationPathInfo();
				destinationPathInfoModel = destinations[destinations.length - 1];
			}
				
			
			FileInfoModel newFileInfoModel = new FileInfoModel();
			newFileInfoModel.setProcessID(processID);
			newFileInfoModel.setProcessVersionID(processVersionID);
			newFileInfoModel.setPackageID(packageID);
			newFileInfoModel.setDefaultAccount(drive.getDefaultAccout());
			newFileInfoModel.setDeleted(false);
			
//			newFileInfoModel.setIsTempDir(true);
//			newFileInfoModel.setTempDirDate(Utils.getDateNow());
//			newFileInfoModel.setTempDirID(tempDirID);
			
			if (destinationPathInfoModel != null) {
				newFileInfoModel.setParentKey(destinationPathInfoModel.getKey());
			} else {
				newFileInfoModel.setParentKey("");
			}
			newFileInfoModel.setFileDriveID(fileDriveID);

			FileModel newFileModel = new FileModel();
			newFileModel.setFileInfoModel(newFileInfoModel);
			newFileModel.setKeyID(UUID.randomUUID().toString());
			if (destinationPathInfoModel != null) {
				newFileModel.setKey(destinationPathInfoModel.getKey() + "/" + newFileModel.getKeyID());
			} else {
				newFileModel.setKey(newFileModel.getKeyID());
			}
			newFileModel.setName(argumentsModel.getClassChunkMetadata().getFileName());
			newFileModel.setDateCreated(Utils.getDateNow());
			newFileModel.setIsDirectory(false);
			newFileModel.setSize(argumentsModel.getClassChunkMetadata().getFileSize());
			newFileModel.setHasSubDirectories(false);

			this.dbService.getFilesRepository().insert(newFileModel);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return false;
		}
	}
	
	public String download(ArgumentsModel argumentsModel, String processID, String processVersionID, String packageID) {

		PathInfoModel[] infoModels = argumentsModel.getPathInfo();
		PathInfoModel pathInfoModel = infoModels[infoModels.length - 1];
		String fileKey = pathInfoModel.getKey();
		String[] fileKeySplit = fileKey.split("/");
		String fileKeyID = fileKeySplit[fileKeySplit.length - 1];

		FileModel fileModel = this.dbService.getFilesRepository().findByKeyID(fileKeyID);
		String driveFileID = fileModel.getFileInfoModel().getFileDriveID();
		
		String tempFilePath = null;
		try {
			GoogleDrive drive = new GoogleDrive(redisTemplate);
			tempFilePath = drive.downloadFile(driveFileID, fileModel.getName());
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
			return null;
		}
		return tempFilePath;
	}

	public void insertFilelHierarchyModel(FilelHierarchyModel filelHierarchyModel) {
		dbService.getTempFileRepository().insert(filelHierarchyModel.getSourceFileModel());
		for (FilelHierarchyModel child : filelHierarchyModel.getChildsFileModel()) {
			insertFilelHierarchyModel(child);
		}
	}

	public void savetFilelHierarchyModel(FilelHierarchyModel filelHierarchyModel) {
		dbService.getTempFileRepository().save(filelHierarchyModel.getSourceFileModel());
		for (FilelHierarchyModel child : filelHierarchyModel.getChildsFileModel()) {
			savetFilelHierarchyModel(child);
		}
	}

	public void logicalDeleteHierarchyModel(FilelHierarchyModel filelHierarchyModel) {
		FileModel fileModel = filelHierarchyModel.getSourceFileModel();
		fileModel.getFileInfoModel().setDeleted(true);
		this.dbService.getFilesRepository().save(fileModel);

		for (FilelHierarchyModel child : filelHierarchyModel.getChildsFileModel()) {
			logicalDeleteHierarchyModel(child);
		}
	}
    
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteTempDir(String tempDirID) {
		try {
			this.dbService.getTempFileRepository().deleteByTempDirId(tempDirID);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return false;
		}
	}
	
	@Transactional(rollbackFor = Exception.class)
	public String pubTempDir(String tempDirID) {
		try {
			TempFileModel[] findResult = this.dbService.getTempFileRepository().findByTempDirID(tempDirID);
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

			boolean result = this.deleteTempDir(tempDirID);
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

	// ------------------------------------------------ PRIVATE ------------------------------------------------ //	
	private FilelHierarchyModel buildFilelHierarchy(String processID, String processVersionID, String packageID, String key, String keyID, String tempDirID) {
		if (!Utils.stringHasValue(key) && !Utils.stringHasValue(keyID)) {
			throw new NullPointerException("Não é posível busca FileModel. keyID e KEY nulos");
		}

		String fileDirID = keyID;
		if (fileDirID == null || fileDirID.isBlank()) {
			String[] splitKey = key.split("/");
			fileDirID = splitKey[splitKey.length - 1];
		}
		
		TempFileModel sourceFileModel = this.dbService.getTempFileRepository().findByKeyIDAndTempDirID(fileDirID, tempDirID);
		if(sourceFileModel == null) {return null;}
		
		return this.buildFilelHierarchy(processID, processVersionID, packageID, sourceFileModel, tempDirID);
	}

	private FilelHierarchyModel buildFilelHierarchy(String processID, String processVersionID, String packageID, TempFileModel sourceFileModel, String tempDirID) {
		
		var newFilelHierarchyModel = new FilelHierarchyModel();
		newFilelHierarchyModel.setSourceFileModel(sourceFileModel);
		
		var childsFileModelList = new ArrayList<FilelHierarchyModel>();
		TempFileModel[] childsFileModel = this.dbService.getTempFileRepository().findTempFiles(processID, processVersionID, packageID, sourceFileModel.getKey(), tempDirID);
		for (TempFileModel child : childsFileModel) {
			var childFilelHierarchyModel = buildFilelHierarchy(processID, processVersionID, packageID, child, tempDirID );
			childsFileModelList.add(childFilelHierarchyModel);
		}
		newFilelHierarchyModel.setChildsFileModel(childsFileModelList);

		return newFilelHierarchyModel;
	}

	private boolean appendPartChunck(MultipartFile chunk, ChunkMetadataModel chunkMetadataModel) {
		try {
			chunkMetadataModel.setPartByte(chunk.getBytes());
			this.dbService.getTempChunckPartInfoRepository().insert(chunkMetadataModel);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean partChunksCompleted(ChunkMetadataModel chunkMetadataModel) {
		long result = this.dbService.getTempChunckPartInfoRepository().countChunckParts(chunkMetadataModel.getUploadId());
		if(result == chunkMetadataModel.getTotalCount()) {
			return true;
		}
		return false;
	}

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
