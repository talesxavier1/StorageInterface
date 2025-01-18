package br.com.tx.storageInterface.models;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Classe responsavel por representar arquivos ou diretórios temporários.
 * Classe tem os mesmo campos que o modelo FileModel com o acréscimo de campos de controle.
 */
@Document(collection = "TempFiles")
public class TempFileModel extends FileModel implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * ID do diretório temporário.
	 * Todos os diretórios e arquivos temporários vão ter esse ID igual.
	 */
	private String tempDirID;
	/**
	 * Data de criação do diretório temporário.
	 */
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
