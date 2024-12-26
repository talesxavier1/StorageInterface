package br.com.tx.storageInterface.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import br.com.tx.storageInterface.models.DriveConfigsModel;

public interface IDriveConfigsRepository extends MongoRepository<DriveConfigsModel, String> {

	@Query("{ 'emailContaPrincipal': ?0, 'scope': ?1}")
	public DriveConfigsModel getConfig(String emailContaPrincipal, String scope);

	// public DriveConfigsModel getByEmailContaPrincipal(String
	// emailContaPrincipal);

}

