package br.com.tx.storageInterface.models;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "TempFiles")
public class TempFileModel extends FileModel implements Serializable {

	private static final long serialVersionUID = 1L;
	private String tempDirID;
	private Date tempDirDate;

	public String getTempDirID() {
		return tempDirID;
	}

	public void setTempDirID(String tempDirID) {
		this.tempDirID = tempDirID;
	}

	public Date getTempDirDate() {
		return tempDirDate;
	}

	public void setTempDirDate(Date tempDirDate) {
		this.tempDirDate = tempDirDate;
	}

	public TempFileModel() {
		super();
	}
}
