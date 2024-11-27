package br.com.tx.storageInterface.models;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

@Document(collection = "Files")
public class FileModel implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@JsonProperty(access = Access.WRITE_ONLY)
	private String _id;

	@JsonProperty(access = Access.WRITE_ONLY)
	private String keyID;

	@JsonProperty(access = Access.WRITE_ONLY)
	private FileInfoModel fileInfoModel;

	private String key;
	private String name;
	private Date dateCreated;
	private boolean isDirectory;
	private long size;
	private boolean hasSubDirectories;

	public FileModel() {
		this._id = UUID.randomUUID().toString();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public boolean getIsDirectory() {
		return isDirectory;
	}

	public void setIsDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public boolean isHasSubDirectories() {
		return hasSubDirectories;
	}
	public void setHasSubDirectories(boolean hasSubDirectories) {
		this.hasSubDirectories = hasSubDirectories;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getKeyID() {
		return keyID;
	}

	public void setKeyID(String keyID) {
		this.keyID = keyID;
	}

	public FileInfoModel getFileInfoModel() {
		return fileInfoModel;
	}

	public void setFileInfoModel(FileInfoModel fileInfoModel) {
		this.fileInfoModel = fileInfoModel;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
}
