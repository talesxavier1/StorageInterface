package br.com.tx.storageInterface.Utils;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;

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
}

enum OSEnum {
	LINUX, WINDOWS, OTHERS
}
