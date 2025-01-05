package br.com.tx.storageInterface.models;

import java.io.Serializable;

import br.com.tx.storageInterface.enums.DriveContextEnum;

public class FileInfoModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private String processID;
	private String processVersionID;
	private String packageID;
	private String packageVersionID;
	private String defaultAccount;
	private boolean deleted;
	private String parentKey;
	private String fileDriveID;
	private DriveContextEnum storageType;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getProcessID() {
		return processID;
	}

	public void setProcessID(String processID) {
		this.processID = processID;
	}

	public String getProcessVersionID() {
		return processVersionID;
	}

	public void setProcessVersionID(String processVersion) {
		this.processVersionID = processVersion;
	}

	public String getPackageID() {
		return packageID;
	}

	public void setPackageID(String packageID) {
		this.packageID = packageID;
	}

	public String getDefaultAccount() {
		return defaultAccount;
	}

	public void setDefaultAccount(String defaultAccount) {
		this.defaultAccount = defaultAccount;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getParentKey() {
		return parentKey;
	}

	public void setParentKey(String parentKey) {
		this.parentKey = parentKey;
	}

	public String getFileDriveID() {
		return fileDriveID;
	}

	public void setFileDriveID(String fileDriveID) {
		this.fileDriveID = fileDriveID;
	}


	public String getPackageVersionID() {
		return packageVersionID;
	}

	public void setPackageVersionID(String packageVersionID) {
		this.packageVersionID = packageVersionID;
	}

	public DriveContextEnum getStorageType() {
		return storageType;
	}

	public void setStorageType(DriveContextEnum storageType) {
		this.storageType = storageType;
	}

}
