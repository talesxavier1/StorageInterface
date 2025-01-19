package br.com.tx.storageInterface.models;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import br.com.tx.storageInterface.enums.DriveContextEnum;

@Document(collection = "DriveConfigs")
public class DriveConfigsModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String _id;

	private String emailContaPrincipal;
	private DriveContextEnum driveContext;
	private String refreshToken;
	private long expiresIn;
	private long tokenGeneratedTimestamp;
	private String accessToken;
	private String clientID;
	private String clientSecret;

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}


	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

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

	public long getTokenGeneratedTimestamp() {
		return tokenGeneratedTimestamp;
	}

	public void setTokenGeneratedTimestamp(long tokenGeneratedTimestamp) {
		this.tokenGeneratedTimestamp = tokenGeneratedTimestamp;
	}

	public long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public DriveContextEnum getDriveContext() {
		return driveContext;
	}

	public void setDriveContext(DriveContextEnum driveContext) {
		this.driveContext = driveContext;
	}
}
