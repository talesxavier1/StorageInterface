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
}
