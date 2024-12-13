package br.com.tx.storageInterface.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import br.com.tx.storageInterface.models.TempFileModel;

public interface ITempFileRepository extends MongoRepository<TempFileModel, String> {

	@Query("{ 'keyID': ?0, 'tempDirID': ?1}")
	public TempFileModel findByKeyIDAndTempDirID(String keyID, String tempDirID);

	@Query("{ 'fileInfoModel.processID': ?0, 'fileInfoModel.processVersionID': ?1, 'fileInfoModel.packageID': ?2, 'fileInfoModel.parentKey': ?3,'tempDirID': ?4, 'fileInfoModel.deleted': false }")
	public TempFileModel[] findTempFiles(String processID, String processVersionID, String packageID, String parentKey, String tempDirID);
	
	@Query("{ 'fileInfoModel.processID': ?0, 'fileInfoModel.processVersionID': ?1, 'fileInfoModel.packageID': ?2, 'fileInfoModel.packageVersionID': ?3}")
	public TempFileModel[] findBypackageID(String processID, String processVersionID, String packageID, String packageVersionID);

	@Query("{ 'tempDirID': ?0}")
	public TempFileModel[] findByTempDirID(String tempDirID);

	@Query(value = "{ 'tempDirID': ?0 }", delete = true)
	public void deleteByTempDirId(String tempDirID);
	
	@Query(value = "{ 'fileInfoModel.processID': ?0, 'fileInfoModel.processVersionID': ?1, 'fileInfoModel.packageID': ?2, 'fileInfoModel.parentKey': ?3, 'tempDirID': ?4, 'fileInfoModel.deleted': false, 'isDirectory': true }", count = true)
	public long countByParentKey(String processID, String processVersionID, String packageID, String parentKey, String tempDirID);

	@Query(value = "{ 'fileInfoModel.processID': ?0, 'fileInfoModel.processVersionID': ?1, 'fileInfoModel.packageID': ?2, 'key': ?3, 'tempDirID': ?4, 'fileInfoModel.deleted': false, 'isDirectory': true }", count = true)
	public long countByKey(String processID, String processVersionID, String packageID, String key, String tempDirID);

	@Query(value = "{ 'tempDirID': ?0, 'fileInfoModel.deleted': false }", exists = true)
	public boolean tempDirExist(String tempDirID);
}
