package br.com.tx.storageInterface.models;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "DriveConfigs")
public class DriveConfigsModel {

	@Id
	private String _id;

	private String emailContaPrincipal;
	private String emailContaServico;
	private String strJsonClientSecret;

	public DriveConfigsModel() {
		this._id = UUID.randomUUID().toString();
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getEmailContaPrincipal() {
		return emailContaPrincipal;
	}

	public void setEmailContaPrincipal(String emailContaPrincipal) {
		this.emailContaPrincipal = emailContaPrincipal;
	}

	public String getEmailContaServico() {
		return emailContaServico;
	}

	public void setEmailContaServico(String emailContaServico) {
		this.emailContaServico = emailContaServico;
	}

	public String getStrJsonClientSecret() {
		return strJsonClientSecret;
	}

	public void setStrJsonClientSecret(String strJsonClientSecret) {
		this.strJsonClientSecret = strJsonClientSecret;
	}
}
