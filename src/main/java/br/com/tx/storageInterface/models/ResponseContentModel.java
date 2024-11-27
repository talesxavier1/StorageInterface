package br.com.tx.storageInterface.models;

import java.io.Serializable;

public class ResponseContentModel implements Serializable {
	private static final long serialVersionUID = 1L;


	private boolean success = true;
	private String errorText;
	private FileModel[] result;
	private String strResult;

	public String getStrResult() {
		return strResult;
	}

	public void setStrResult(String strResult) {
		this.strResult = strResult;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public FileModel[] getResult() {
		return result;
	}

	public void setResult(FileModel[] result) {
		this.result = result;
	}

}
