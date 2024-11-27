package br.com.tx.storageInterface.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import br.com.tx.storageInterface.models.ChunkMetadataModel;

public interface ITempChunckPartInfoRepository extends MongoRepository<ChunkMetadataModel, String> {

	@Query(value = "{ '_uploadId': ?0}", count = true)
	public long countChunckParts(String UploadId);

	@Query(value = "{ '_uploadId': ?0}")
	public ChunkMetadataModel[] getChunkMetadataByUploadId(String UploadId);

}
