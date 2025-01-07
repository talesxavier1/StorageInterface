package br.com.tx.storageInterface.models;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Classe que representa o detalhamento da chunck de arquivo que está sendo enviada.
 */
@Document(collection = "PartsChunks")
public class ChunkMetadataModel implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String _id;
	
	/**
	 * ID do upload.
	 * É necessário quando um arquio é dividido em vários chunks e enviados em chamadas separadas.
	 */
	private String UploadId;
	/**
	 * É o mesmo campo UploadId, só está repetido para não dar problema no Mongo por conta do nome do campo iniciar com letra maiúscula.
	 */
	private String _uploadId;
	/**
	 * Nome do arquivo.
	 */
	private String FileName;
	/**
	 * index do chunk.
	 * Quando o arquivo é dividido em vários chunks, esse campo armazena o index do chunk.
	 * Se o Index for igual ao TotalCount, significa que é a última parte da sequencia dos chunks.
	 */
	private Integer Index;
	/**
	 * número de chunks.
	 * Quando o arquivo é dividido, esse campo armazena a quantidade de chunck total.
	 */
	private Integer TotalCount;
	/**
	 * Tamanho do chunk em bytes.
	 */
	private Long FileSize;
	/**
	 * bytes do arquivo enviado no chunk.
	 * Esse campo é útilizado para armazenar temporariamente os bytes do chunk no banco até que a última parte chegue.
	 */
	private byte[] partByte;


	public ChunkMetadataModel() {
	}

	public String getUploadId() {
		return UploadId;
	}

	public void setUploadId(String UploadId) {
		this.UploadId = UploadId;
	}

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
