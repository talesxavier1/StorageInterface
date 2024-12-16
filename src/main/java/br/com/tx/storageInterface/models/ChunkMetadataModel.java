package br.com.tx.storageInterface.models;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "PartsChunks")
public class ChunkMetadataModel implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String _id;

	private String UploadId;
	private String _uploadId;
	private String FileName;
	private Integer Index;
	private Integer TotalCount;
	private Long FileSize;
	private byte[] partByte;


	public ChunkMetadataModel() {
	}

	public String getUploadId() {
		return UploadId;
	}

//	public void setUploadId(String uploadId) {
//		uploadId = uploadId;
//	}

	public String getFileName() {
		return FileName;
	}

	public void setFileName(String fileName) {
		FileName = fileName;
	}

	public Integer getIndex() {
		return Index;
	}

	public void setIndex(Integer index) {
		Index = index;
	}

	public Integer getTotalCount() {
		return TotalCount;
	}

	public void setTotalCount(Integer totalCount) {
		TotalCount = totalCount;
	}

	public Long getFileSize() {
		return FileSize;
	}

	public void setFileSize(Long fileSize) {
		FileSize = fileSize;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public byte[] getPartByte() {
		return partByte;
	}

	public void setPartByte(byte[] partByte) {
		this.partByte = partByte;
	}

	public String get_uploadId() {
		return _uploadId;
	}

	public void set_uploadId(String _uploadId) {
		this._uploadId = _uploadId;
	}

}
