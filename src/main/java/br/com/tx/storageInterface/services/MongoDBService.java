package br.com.tx.storageInterface.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.tx.storageInterface.repository.IDriveConfigsRepository;
import br.com.tx.storageInterface.repository.IDriveFileInfoRepository;
import br.com.tx.storageInterface.repository.IFilesRepository;
import br.com.tx.storageInterface.repository.ITempChunckPartInfoRepository;
import br.com.tx.storageInterface.repository.ITempFileRepository;


@Service
public class MongoDBService {

	private IDriveConfigsRepository driveConfigsRepository;
	private IFilesRepository filesRepository;
	private IDriveFileInfoRepository driveFileInfoRepository;
	private ITempChunckPartInfoRepository tempChunckPartInfoRepository;
	private ITempFileRepository tempFileRepository;

	@Autowired
	public MongoDBService(
			IDriveConfigsRepository driveConfigsRepository, 
			IFilesRepository filesRepository,
			IDriveFileInfoRepository driveFileInfoRepository, 
			ITempChunckPartInfoRepository tempChunckPartInfoRepository, ITempFileRepository tempFileRepository
	) {
		this.driveConfigsRepository = driveConfigsRepository;
		this.filesRepository = filesRepository;
		this.driveFileInfoRepository = driveFileInfoRepository;
		this.tempChunckPartInfoRepository = tempChunckPartInfoRepository;
		this.tempFileRepository = tempFileRepository;
	}

	public IDriveConfigsRepository getDriveConfigsRepository() {
		return driveConfigsRepository;
	}

	public IFilesRepository getFilesRepository() {
		return filesRepository;
	}

	public IDriveFileInfoRepository getDriveFileInfoRepository() {
		return driveFileInfoRepository;
	}

	public ITempChunckPartInfoRepository getTempChunckPartInfoRepository() {
		return tempChunckPartInfoRepository;
	}

	public ITempFileRepository getTempFileRepository() {
		return tempFileRepository;
	}
}
