package br.com.tx.storageInterface.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.web.multipart.MultipartFile;

/** Classe com funçoes para calcular o hash dos arquivos. */
public class FileHashUtil {

	/**
	 * Função para calcular o Hash MD5 de um arquivo com base em um MultipartFile.
	 * 
	 * @param file Arquivo.
	 * @return Retorna o HashMD5 do arquivo.
	 * @throws IOException              Quando não é possível ler o arquivo.
	 * @throws NoSuchAlgorithmException Quando ão é possível obter o algorítimo do MD5
	 */
	public static String generateMD5Hash(MultipartFile file) throws IOException, NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		try (InputStream inputStream = file.getInputStream()) {
			byte[] buffer = new byte[8192]; // 8KB buffer
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				digest.update(buffer, 0, bytesRead);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (byte b : digest.digest()) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	/**
	 * Função para calcular o hash md5 de uma string.
	 * 
	 * @param value valor.
	 * @return Retorna o hash md5 da string
	 * @throws NoSuchAlgorithmException Quando ão é possível obter o algorítimo do MD5
	 */
	public static String generateMD5Hash(String value) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] messageDigest = md.digest(value.getBytes());
		StringBuilder hexString = new StringBuilder();
		for (byte b : messageDigest) {
			hexString.append(String.format("%02x", b));
		}
		return hexString.toString();
	}

}
