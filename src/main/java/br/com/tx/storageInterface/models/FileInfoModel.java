package br.com.tx.storageInterface.models;

import java.io.Serializable;

import br.com.tx.storageInterface.enums.DriveContextEnum;

/**
 * Classe responsael por armazenar detalhes da pasta ou arquivo.
 */
public class FileInfoModel implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * ID do processo atual.
	 */
	private String processID;

	/**
	 * ID da versão do processo atual.
	 */
	private String processVersionID;

	/**
	 * ID do pacote ou atividade atual.
	 */
	private String packageID;

	/**
	 * Id da ersão do pacote ou atividade.
	 */
	private String packageVersionID;

	/**
	 * indica em que conta as informações do arquivo foram salvas.
	 */
	private String defaultAccount;

	/**
	 * Indica se o registro foi deletado.
	 */
	private boolean deleted;

	/**
	 * KeyID do diretório pais da pasta ou arquivo.
	 */
	private String parentKey;

	/**
	 * ID do arquivo no storage.
	 */
	private String fileDriveID;

	/**
	 * Indica em qual serviço de storage a informação do arquivo foi armazenada.
	 */
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
