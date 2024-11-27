package br.com.tx.storageInterface.models;

import java.io.Serializable;

import com.google.gson.Gson;

public class ArgumentsModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private PathInfoModel[] pathInfo;
	private boolean isDirectory;
	private String name;
	private PathInfoModel[] destinationPathInfo;
	private PathInfoModel[] sourcePathInfo;
	private String chunkMetadata;
	private ChunkMetadataModel classChunkMetadata;
	private boolean sourceIsDirectory;

	public void init() {
		if (chunkMetadata != null) {
			this.classChunkMetadata = new Gson().fromJson(chunkMetadata, ChunkMetadataModel.class);
			classChunkMetadata.set_uploadId(classChunkMetadata.getUploadId());
		}
	}

	public ChunkMetadataModel getClassChunkMetadata() {
		return this.classChunkMetadata;
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
