package br.com.tx.storageInterface.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import br.com.tx.storageInterface.enums.DriveContextEnum;



@Document(collection = "DriveFileInfo")
public class DriveFileInfoModel {

	@Id
	private String _id;
	private String fileHash;
	private String defaultAccount;
	private String fileName;
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

