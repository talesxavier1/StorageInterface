package br.com.tx.storageInterface.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import br.com.tx.storageInterface.enums.DriveContextEnum;



@Document(collection = "DriveFileInfo")
/** Classe que representa um registro salvo em algum serviço de armazenamento. */
public class DriveFileInfoModel {

	@Id
	/** Id do registro. (Id retornado no momento do envio para o serviço de armazenamento.) */
	private String _id;

	/** Hash do conteúdo ou do arquivo. */
	private String fileHash;

	/** Conta de serviço onde o arquivo foi armazenado. */
	private String defaultAccount;

	/** Nome do arquivo no momento do envio. */
	private String fileName;

	/** Serviço de armazenamento usado. */
	private DriveContextEnum driveContext;

	public String getDefaultAccount() {
		return defaultAccount;
	}

	public void setDefaultAccount(String contaPrincipal) {
		this.defaultAccount = contaPrincipal;
	}

	public String getFileHash() {
		return fileHash;
	}

	public void setFileHash(String fileHash) {
		this.fileHash = fileHash;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public DriveContextEnum getDriveContext() {
		return driveContext;
	}

	public void setDriveContext(DriveContextEnum driveContext) {
		this.driveContext = driveContext;
	}
}

