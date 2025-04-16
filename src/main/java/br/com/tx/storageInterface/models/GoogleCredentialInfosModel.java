package br.com.tx.storageInterface.models;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;

/** Classe de crendenciais google. */
public class GoogleCredentialInfosModel {

	/** credencial google. */
	private Credential credential;

	/** NetHttpTransport google. */
	private NetHttpTransport httpTransport;

	public Credential getCredential() {
		return credential;
	}

	public void setCredential(Credential credential) {
		this.credential = credential;
	}

	public NetHttpTransport getHttpTransport() {
		return httpTransport;
	}

	public void setHttpTransport(NetHttpTransport httpTransport) {
		this.httpTransport = httpTransport;
	}

}
