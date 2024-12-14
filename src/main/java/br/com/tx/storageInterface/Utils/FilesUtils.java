package br.com.tx.storageInterface.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class FilesUtils {
	private static final String PATH = "src/main/resources/temp/";

	static {
		var a = "";
		String tempDir = System.getProperty("java.io.tmpdir") + "/temp/";
		var b = "";
	}

	public static Path tryGetPath(String stringPath) {
		try {
			return Paths.get(stringPath);
		} catch (Exception e) {
			return null;
		}
	}

	public static void tryDeleteFile(String stringPath) {
		var filePath = tryGetPath(stringPath);
		if (filePath != null) {
			try {
				java.nio.file.Files.deleteIfExists(filePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String createTempFile(String fileName, byte[] fileBytes, long fileSecTimeTemp) {
		String filePath = createFile(fileName, fileBytes);
		startDeleteScheduler(fileSecTimeTemp, filePath);
		return filePath;
	}

	public static String createTempFile(String fileName, byte[] fileBytes) {
		String filePath = createFile(fileName, fileBytes);
		return filePath;
	}

	public static String createJsonFileFromString(String fileName, String jsonString) {
		var finalPath = createJsonFile(fileName, jsonString, null);
		return finalPath;

	}

	public static String createJsonFileFromString(String fileName, String jsonString, long fileSecTimeTemp) {
		String finalPath = createJsonFile(fileName, jsonString, null);
		if (finalPath != null) {
			startDeleteScheduler(fileSecTimeTemp, finalPath);
		}
		return finalPath;
	}

	public static String createJsonFileFromObject(String fileName, Object object) {
		var finalPath = createJsonFile(fileName, null, object);
		return finalPath;

	}

	public static String createJsonFileFromObject(String fileName, Object object, long fileSecTimeTemp) {
		String finalPath = createJsonFile(fileName, null, object);
		if (finalPath != null) {
			startDeleteScheduler(fileSecTimeTemp, finalPath);
		}
		return finalPath;
	}

	public static Map<String, String> listTempFiles() {
		checkExistTempFolder();
		File directory = new File(PATH);
		String[] filesName = directory.list();

		Map<String, String> filesMap = new HashMap<String, String>();
		for (String name : filesName) {
			String[] splitName = name.split("-");
			filesMap.put(splitName[0], PATH + "/" + name);
		}
		
		return filesMap;
	}

	// ----------------------------------------- Private ----------------------------------------- //
	private static void checkExistTempFolder() {
		File directory = new File(PATH);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	private static void startDeleteScheduler(long secTime, String fileStringPath) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.schedule(() -> {
			FilesUtils.tryDeleteFile(fileStringPath);
		}, secTime, TimeUnit.SECONDS);
	}

	private static String createFile(String fileName, byte[] fileBytes) {
		checkExistTempFolder();
		String filePath = PATH + fileName;
		File file = new File(filePath);

		try (var os = new FileOutputStream(file)) {
			os.write(fileBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return filePath;
	}

	private static String createJsonFile(String fileName, String jsonString, Object object) {
		checkExistTempFolder();
		String filePath = PATH + fileName;
		if (jsonString != null) {
			JsonElement jsonElement = JsonParser.parseString(jsonString);

			try (FileWriter file = new FileWriter(filePath)) {
				new Gson().toJson(jsonElement, file);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

		} else {
			try (FileWriter file = new FileWriter(filePath)) {
				new Gson().toJson(object, file);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		return filePath;
	}
}
