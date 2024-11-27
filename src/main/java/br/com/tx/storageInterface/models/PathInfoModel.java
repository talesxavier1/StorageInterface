package br.com.tx.storageInterface.models;

import java.io.Serializable;

public class PathInfoModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private String key;
	private String name;

	public PathInfoModel() {

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
}
