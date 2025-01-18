package br.com.tx.storageInterface.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.tx.storageInterface.repository.IDriveConfigsRepository;
import br.com.tx.storageInterface.repository.IDriveFileInfoRepository;
import br.com.tx.storageInterface.repository.IFilesRepository;
import br.com.tx.storageInterface.repository.ITempChunckPartInfoRepository;
import br.com.tx.storageInterface.repository.ITempFileRepository;

/** Interfaces do MongoDB */
@Service
public class MongoDBService {

	/** Interface da collection DriveConfigs */
	private IDriveConfigsRepository driveConfigsRepository;
	/** Interface da collection Files */
	private IFilesRepository filesRepository;
	/** Interface da collection DriveFileInfo */
	private IDriveFileInfoRepository driveFileInfoRepository;
	/** Interface da collection PartsChunks */
	private ITempChunckPartInfoRepository tempChunckPartInfoRepository;
	/** Interface da collection TempFiles */
	private ITempFileRepository tempFileRepository;


	@Autowired
	public MongoDBService(
			IDriveConfigsRepository driveConfigsRepository, 
			IFilesRepository filesRepository,
			IDriveFileInfoRepository driveFileInfoRepository, 
			ITempChunckPartInfoRepository tempChunckPartInfoRepository, 
			ITempFileRepository tempFileRepository
	) {
		this.driveConfigsRepository = driveConfigsRepository;
		this.filesRepository = filesRepository;
		this.driveFileInfoRepository = driveFileInfoRepository;
		this.tempChunckPartInfoRepository = tempChunckPartInfoRepository;
		this.tempFileRepository = tempFileRepository;
	}

	/**
	 * @return Retorna a iterface da collection DriveConfigs
	 */
	public IDriveConfigsRepository getDriveConfigsRepository() {
		return driveConfigsRepository;
	}

	/**
	 * @return Retorna a iterface da collection Files
	 */
	public IFilesRepository getFilesRepository() {
		return filesRepository;
	}

	/**
	 * @return Retorna a iterface da collection DriveFileInfo
	 */
	public IDriveFileInfoRepository getDriveFileInfoRepository() {
		return driveFileInfoRepository;
	}

	/**
	 * @return Retorna a iterface da collection PartsChunks
	 */
	public ITempChunckPartInfoRepository getTempChunckPartInfoRepository() {
		return tempChunckPartInfoRepository;
	}

	/**
	 * @return Retorna a iterface da collection TempFiles
	 */
	public ITempFileRepository getTempFileRepository() {
		return tempFileRepository;
	}


}
