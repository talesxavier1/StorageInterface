package br.com.tx.storageInterface.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class FilesUtils {
	private static final String PATH = "src/main/resources/temp/";
	private static final String SEPARATOR = Utils.getCurrentOSSeparator();
	private static final String TEMP_DIR_PATH = System.getProperty("java.io.tmpdir");
	private static final String TEMP_FOLDER_NAME = UUID.randomUUID().toString() + "-" + "StorageInterface";
	private static final String FINAL_TEMP_PATH = TEMP_DIR_PATH + TEMP_FOLDER_NAME;

	static {
		checkExistTempFolder();
		System.out.println("--------- Diretório tempoário: " + FINAL_TEMP_PATH);
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

	/*
	 * public static Map<String, String> listTempFiles_() { File directory = new
	 * File(FINAL_TEMP_PATH); String[] filesName = directory.list();
	 * 
	 * Map<String, String> filesMap = new HashMap<String, String>(); for (String
	 * name : filesName) { String[] splitName = name.split("-");
	 * filesMap.put(splitName[0], PATH + "/" + name); }
	 * 
	 * return filesMap; }
	 */

	// ----------------------------------------- Private ----------------------------------------- //
	private static void checkExistTempFolder() {
		File directory = new File(TEMP_DIR_PATH + SEPARATOR + TEMP_FOLDER_NAME);
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
		String filePath = FINAL_TEMP_PATH + SEPARATOR + fileName;
		File file = new File(filePath);

		try (var os = new FileOutputStream(file)) {
			os.write(fileBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return filePath;
	}

	private static String createJsonFile(String fileName, String jsonString, Object object) {
		String filePath = FINAL_TEMP_PATH + SEPARATOR + fileName;
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
