package br.com.tx.storageInterface.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.tx.storageInterface.models.DriveConfigsModel;

public interface IDriveConfigsRepository extends MongoRepository<DriveConfigsModel, String> {
	public DriveConfigsModel getByEmailContaPrincipal(String emailContaPrincipal);

}
