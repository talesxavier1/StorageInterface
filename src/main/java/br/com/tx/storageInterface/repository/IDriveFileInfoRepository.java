package br.com.tx.storageInterface.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.tx.storageInterface.models.DriveFileInfoModel;

public interface IDriveFileInfoRepository extends MongoRepository<DriveFileInfoModel, String> {

	DriveFileInfoModel findBy_id(String _id);

	DriveFileInfoModel findByFileHash(String fileHash);

	long countByFileHash(String fileHash);
}
