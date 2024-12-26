package br.com.tx.storageInterface.drivers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import br.com.tx.storageInterface.models.CredentialInfosModel;
import br.com.tx.storageInterface.services.GoogleCredentialService;

public class GoogleGmailDrive {


	public static Gmail getDrive(String mainAccount) throws IOException, GeneralSecurityException {
		GoogleCredentialService credentialService = new GoogleCredentialService(mainAccount, "GMAIL");
		CredentialInfosModel credentialInfosModel = credentialService.getCredentials();
		Gmail newDrive = new Gmail.Builder(credentialInfosModel.getHttpTransport(), GsonFactory.getDefaultInstance(), credentialInfosModel.getCredential()).setApplicationName("GoogleDrive" + "-" + mainAccount).build();
		return newDrive;
	}

	public static Message createMessage(String recipientEmail, String subject, String bodyText) throws Exception {
		MimeMessage mimeMessage = createMimeMessage(recipientEmail, "me", subject, bodyText);
		Message message = createMessageWithMimeMessage(mimeMessage);
		return message;
	}
	
	private static MimeMessage createMimeMessage(String to, String from, String subject, String bodyText) throws Exception {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);
		email.setFrom(new InternetAddress(from));
		email.addRecipient(RecipientType.TO, new InternetAddress(to));
		email.setSubject(subject);
		email.setText(bodyText);

		return email;
	}

	private static Message createMessageWithMimeMessage(MimeMessage emailContent) throws Exception {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		emailContent.writeTo(buffer);
		byte[] bytes = buffer.toByteArray();
		String encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);

		Message message = new Message();
		message.setRaw(encodedEmail);

		return message;
	}

}
