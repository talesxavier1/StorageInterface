package br.com.tx.storageInterface.models;

import java.io.Serializable;

import com.google.gson.Gson;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Modelo que representa o detalhamento do diretório ou arquivo recebido do front.
 */
public class ArgumentsModel implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Sequencia de PathInfo sendo a última do diretório ou arquivo atual e a
	 * penúltima a do diretorio pai, caso haja.
	 */
	private PathInfoModel[] pathInfo;
	/**
	 * Indica se o arquivo é um diretório ou não.
	 */
	private boolean isDirectory;
	/**
	 * Nome do arquivo ou diretório.
	 */
	private String name;
	/**
	 * PathInfo informado quando uma ação de movimentação ou cópia de diretório ou arquivo está sendo executado.
	 * Representa o PathInfo de destino da ação.
	 */
	private PathInfoModel[] destinationPathInfo;
	
	/**
	 * PathInfo informado quando uma ação de movimentação ou cópia de diretório ou arquivo está sendo executado.
	 * Representa o PathInfo de origem da ação.
	 */
	private PathInfoModel[] sourcePathInfo;

	/**
	 * Json que representa a classe ChunkMetadataModel.
	 */
	//WARNING ** Manter o json do exemple igual ao json que representa a classe ChunkMetadataModel. **
	//WARNING ** a propriedade partByte pode ser ignorada. **
	@Schema(description = "JSON que representa a classe ChunkMetadataModel", example = "{\"UploadId\":\"\",\"FileName\":\"\",\"Index\":0,\"TotalCount\":0,\"FileSize\":0}")
	private String chunkMetadata;
	/**
	 * Classe com o detalhameto do chunk de arquivo enviado.
	 */
	@Schema(hidden = true)
	private ChunkMetadataModel classChunkMetadata;
	/**
	 * Indica que a classe está tratando de um diretório.
	 */
	private boolean sourceIsDirectory;
	
	/**
	 * Função responsável por iniciar a classe e tratar de campos que precisem de alguma lógica.
	 * 
	 * - Faz o parse do campo chunkMetadata e popula a classe ChunkMetadataModel.
	 * - Preeche o campo _uploadId da classe ChunkMetadataModel com base no valor do UploadId da propria classe ChunkMetadataModel.
	 */
	public void init() {
		if (chunkMetadata != null) {
			this.classChunkMetadata = new Gson().fromJson(chunkMetadata, ChunkMetadataModel.class);
			classChunkMetadata.set_uploadId(classChunkMetadata.getUploadId());
		}
	}

	public ChunkMetadataModel getClassChunkMetadata() {
		return this.classChunkMetadata;
	}

	public void setClassChunkMetadata(ChunkMetadataModel classChunkMetadata) {
		this.classChunkMetadata = classChunkMetadata;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getChunkMetadata() {
		return chunkMetadata;
	}

	public void setChunkMetadata(String chunkMetadata) {
		this.chunkMetadata = chunkMetadata;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public PathInfoModel[] getPathInfo() {
		return pathInfo;
	}

	public void setPathInfo(PathInfoModel[] pathInfo) {
		this.pathInfo = pathInfo;
	}

	public PathInfoModel[] getSourcePathInfo() {
		return sourcePathInfo;
	}

	public void setSourcePathInfo(PathInfoModel[] sourcePathInfo) {
		this.sourcePathInfo = sourcePathInfo;
	}

	public PathInfoModel[] getDestinationPathInfo() {
		return destinationPathInfo;
	}

	public void setDestinationPathInfo(PathInfoModel[] destinationPathInfo) {
		this.destinationPathInfo = destinationPathInfo;
	}

	public boolean getIsSourceIsDirectory() {
		return sourceIsDirectory;
	}

	public void setSourceIsDirectory(boolean sourceIsDirectory) {
		this.sourceIsDirectory = sourceIsDirectory;
	}

}
