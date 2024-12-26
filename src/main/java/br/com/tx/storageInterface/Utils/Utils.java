package br.com.tx.storageInterface.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

public class Utils {

	public static boolean stringHasValue(String value) {
		if (value == null || value.isBlank() || value.isEmpty()) {
			return false;
		}
		return true;
	}

	public static Date getDateNow() {
		return Date.from(Instant.now(Clock.systemUTC()));
	}

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

	public static String getCurrentOSSeparator() {
		return System.getProperty("file.separator");
	}

	public static String getOSTempDir() {
		OSEnum os = getCurrentOS();
		var tempPath = System.getProperty("java.io.tmpdir");
		if (os == OSEnum.LINUX) {
			return tempPath + getCurrentOSSeparator();
		} else {
			return tempPath;
		}
	}

	public static String getFileMimeType(MultipartFile file) throws IOException {
		Tika tika = new Tika();
		String mimeType = tika.detect(file.getInputStream());
		return mimeType;
	}

	public static String getFileMimeType(InputStream inputStream) throws IOException {
		Tika tika = new Tika();
		String mimeType = tika.detect(inputStream);
		return mimeType;
	}
}

enum OSEnum {
	LINUX, WINDOWS, OTHERS
}
