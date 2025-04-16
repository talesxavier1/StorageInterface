package br.com.tx.storageInterface.models;

import java.io.Serializable;

/**
 * Modelo que representa uma informação de path. Nome e sequencia de KeyIDs.
 */
public class PathInfoModel implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * Nesse campo temos uma sequencia de KeyIDs separados por barra 
	 * {keyID_1}/{keyID_2}/{keyID_3}/{keyID_4}...
	 * 		O último {keyID} sempre será o keyID do diretório representado pelo PathInfo.
	 * 		{keyID_1}
	 * 				{keyID_2}
	 * 						{keyID_3}
	 * 								{keyID_4}
	 */
	private String key;

	/**
	 * Nome do diretório.
	 */
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
