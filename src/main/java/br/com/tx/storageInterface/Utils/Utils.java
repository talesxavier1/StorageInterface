package br.com.tx.storageInterface.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

public class Utils {

	/**
	 * Função que verifica se a string tem valor ou não.
	 * Verifica se é null, Vazio ou branco.
	 * @param value
	 * @return Retorna true quando válido.
	 */
	public static boolean stringHasValue(String value) {
		if (value == null || value.isBlank() || value.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * @return Retorna um Date com a data atual em UTC.
	 */
	public static Date getDateNow() {
		return Date.from(Instant.now(Clock.systemUTC()));
	}

	/**
	 * Verifica o sistema operacional atual.
	 * @return Retona o enumerador OSEnum o sistema identificado.
	 */
	public static OSEnum getCurrentOS() {
		String os = System.getProperty("os.name").toLowerCase();

		if (os.contains("win")) {
			return OSEnum.WINDOWS;
		} else if (os.contains("nux")) {
			return OSEnum.LINUX;
		} else {
			return OSEnum.OTHERS;
		}
	}
	
	/**
	 * Função que retorna o separador de URI do sistema atual.
	 * @return Retorna o separador de URI do sistema atual.
	 */
	public static String getCurrentOSSeparator() {
		return System.getProperty("file.separator");
	}

	/**
	 * Função que obtem o diretório temporário do sistema atual.
	 * @return Retorna o path do diretório temporário.
	 */
	public static String getOSTempDir() {
		OSEnum os = getCurrentOS();
		var tempPath = System.getProperty("java.io.tmpdir");
		if (os == OSEnum.LINUX) {
			return tempPath + getCurrentOSSeparator();
		} else {
			return tempPath;
		}
	}

	/**
	 * Função usada para obter o mimeType de um arquivo.
	 * @param file Arquivo que deve ser analisado.
	 * @return Retorna o MimType do arquivo.
	 * @throws IOException Quando a leitura do arquivo não for realizada.
	 */
	public static String getFileMimeType(MultipartFile file) throws IOException {
		Tika tika = new Tika();
		String mimeType = tika.detect(file.getInputStream());
		return mimeType;
	}

	/**
	 * Função usada para obter o mimeType de um arquivo.
	 * 
	 * @param file Arquivo que deve ser analisado.
	 * @return Retorna o MimType do arquivo.
	 * @throws IOException Quando a leitura do arquivo não for realizada.
	 */
	public static String getFileMimeType(InputStream inputStream) throws IOException {
		Tika tika = new Tika();
		String mimeType = tika.detect(inputStream);
		return mimeType;
	}
}

enum OSEnum {
	LINUX, WINDOWS, OTHERS
}
