package br.com.tx.storageInterface.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Classe que representa os IDs de contexto da requisição.
 */
public class APIContextModel {


	/**
	 * ID do processo atual.
	 */
	@NotBlank
	@NotNull
	private String processID;

	/**
	 * ID da versão do processo atual.
	 */
	@NotBlank
	@NotNull
	private String processVersionID;

	/**
	 * ID do pacote ou atividade atual.
	 */
	@NotBlank
	@NotNull
	private String packageID;

	/**
	 * ID da versão do pacote ou atividade
	 * Quando não é enviado nos métodos de consulta, esse campo é desconsiderado na busca do banco.
	 */
	@NotBlank
	private String packageVersionID;
	
	/**
	 * ID do diretório temporário.
	 */
	@NotBlank
	@NotNull
	private String tempDirID;
	
	/**
	 * ID da assinatura.
	 */
	@NotBlank
	@NotNull
	private String assID;

	public String getProcessID() {
		return processID;
	}

	public void setProcessID(String processID) {
		this.processID = processID;
	}

	public String getProcessVersionID() {
		return processVersionID;
	}

	public void setProcessVersionID(String processVersionID) {
		this.processVersionID = processVersionID;
	}

	public String getPackageID() {
		return packageID;
	}

	public void setPackageID(String packageID) {
		this.packageID = packageID;
	}

	public String getPackageVersionID() {
		return packageVersionID;
	}

	public void setPackageVersionID(String packageVersionID) {
		this.packageVersionID = packageVersionID;
	}

	public String getTempDirID() {
		return tempDirID;
	}

	public void setTempDirID(String tempDirID) {
		this.tempDirID = tempDirID;
	}

	public String getAssID() {
		return assID;
	}

	public void setAssID(String assID) {
		this.assID = assID;
	}



}
