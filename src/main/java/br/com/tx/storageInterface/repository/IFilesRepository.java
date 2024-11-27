package br.com.tx.storageInterface.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import br.com.tx.storageInterface.models.FileModel;

public interface IFilesRepository extends MongoRepository<FileModel, String> {

	@Query("{ 'fileInfoModel.processID': ?0, 'fileInfoModel.processVersionID': ?1, 'fileInfoModel.packageID': ?2, 'fileInfoModel.parentKey': ?3, 'fileInfoModel.packageVersionID': ?4, 'fileInfoModel.deleted': false }")
	public FileModel[] findFiles(String processID, String processVersionID, String packageID, String parentKey, String packageVersionID);
	
	@Query("{ 'fileInfoModel.processID': ?0, 'fileInfoModel.processVersionID': ?1, 'fileInfoModel.packageID': ?2, 'fileInfoModel.parentKey': ?3,'fileInfoModel.tempDirID': ?4, 'fileInfoModel.packageVersionID': ?5, 'fileInfoModel.deleted': false,  }")
	public FileModel[] findTempFiles(String processID, String processVersionID, String packageID, String parentKey, String tempDirID, String packageVersionID);

	@Query("{ 'fileInfoModel.processID': ?0, 'fileInfoModel.processVersionID': ?1, 'fileInfoModel.packageID': ?2, 'fileInfoModel.packageVersionID': ?3, 'fileInfoModel.deleted': false}")
	public FileModel[] findBypackageID(String processID, String processVersionID, String packageID, String packageVersionID);

	@Query("{ 'keyID': ?0 }")
	public FileModel findByKeyID(String keyID);

	@Query("{ 'keyID': ?0, 'fileInfoModel.tempDirID': ?1, 'fileInfoModel.deleted': false}")
	public FileModel findByKeyIDAndTempDirID(String keyID, String tempDirID);

	@Query("{ 'fileInfoModel.tempDirID': ?0, 'fileInfoModel.deleted': false}")
	public FileModel[] findByTempDirID(String tempDirID);

	@Query(value = "{ 'fileInfoModel.processID': ?0, 'fileInfoModel.processVersionID': ?1, 'fileInfoModel.packageID': ?2, 'fileInfoModel.parentKey': ?3, 'fileInfoModel.tempDirID': ?4, 'fileInfoModel.deleted': false, 'isDirectory': true }", count = true)
	public long countByParentKey(String processID, String processVersionID, String packageID, String parentKey, String tempDirID);

	@Query(value = "{ 'fileInfoModel.tempDirID': ?0 }", delete = true)
	public void deleteByTempDirId(String tempDirID);
}
