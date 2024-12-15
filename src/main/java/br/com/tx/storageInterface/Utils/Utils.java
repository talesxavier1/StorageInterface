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
}

enum OSEnum {
	LINUX, WINDOWS, OTHERS
}
