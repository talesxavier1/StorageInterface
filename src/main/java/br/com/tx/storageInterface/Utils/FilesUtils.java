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

/**
 * Classe com as funções básicas para manipular os arquivos da aplicação.
 */
public class FilesUtils {
	/** Separador de path do sistema atual. */
	private static final String SEPARATOR = Utils.getCurrentOSSeparator();
	/** Diretório temporário do sistema atual. */
	private static final String TEMP_DIR_PATH = Utils.getOSTempDir();
	/** Nome da pasta que será criada no diretório temporário */
	private static final String TEMP_FOLDER_NAME = UUID.randomUUID().toString() + "-" + "StorageInterface";
	/** Path do diretório temporário final. */
	private static final String FINAL_TEMP_PATH = TEMP_DIR_PATH + TEMP_FOLDER_NAME;

	static {
		checkExistTempFolder();
	}

	/**
	 * Função que tenta obter um arquivo com base em um path.
	 * 
	 * @param stringPath Path do arquivo.
	 * @return Retorna o Path caso encontrado e null caso não encontrado.
	 */
	public static Path tryGetPath(String stringPath) {
		try {
			return Paths.get(stringPath);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Função que tenta apagar um arquivo com base em um path.
	 * 
	 * @param stringPath Path do arquivo.
	 */
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

	/**
	 * Cria um arquivo temporário e com o tempo em segundos para ser apagado.
	 * 
	 * @param fileName        Nome do arquivo.
	 * @param fileBytes       Arquivo em Bytes.
	 * @param fileSecTimeTemp Tempo em segundos para o arquivo ser apagado.
	 * @return Retorna o path do arquivo criado.
	 */
	public static String createTempFile(String fileName, byte[] fileBytes, long fileSecTimeTemp) {
		String filePath = createFile(fileName, fileBytes);
		startDeleteScheduler(fileSecTimeTemp, filePath);
		return filePath;
	}

	/**
	 * Cria um arquivo temporário sem tempo para ser ecluído.
	 * 
	 * @param fileName  Nome do arquivo
	 * @param fileBytes Arquivo em bytes.
	 * @return Retorna o path do arquivo criado.
	 */
	public static String createTempFile(String fileName, byte[] fileBytes) {
		String filePath = createFile(fileName, fileBytes);
		return filePath;
	}

	/**
	 * Cria um .json com base em uma String.
	 * O arquivo é criado sem tempo para ser deletado.
	 * 
	 * @param fileName   Nome do arquivo.
	 * @param jsonString conteúdo do Json.
	 * @return Retorna o path do arquivo criado.
	 */
	public static String createJsonFileFromString(String fileName, String jsonString) {
		var finalPath = createJsonFile(fileName, jsonString, null);
		return finalPath;

	}

	/**
	 * Cria um .json com schedule para ser apagado com base em uma String.
	 * 
	 * @param fileName        Nome do arquivo.
	 * @param jsonString      Conteúdo do Json.
	 * @param fileSecTimeTemp Tempo em segundos para o arquivo ser deletado.
	 * @return Retorna o path do arquivo criado.
	 */
	public static String createJsonFileFromString(String fileName, String jsonString, long fileSecTimeTemp) {
		String finalPath = createJsonFile(fileName, jsonString, null);
		if (finalPath != null) {
			startDeleteScheduler(fileSecTimeTemp, finalPath);
		}
		return finalPath;
	}

	/**
	 * Cria um .json com base em um Object no diretório temporário sem Schedule para ser apagado.
	 * 
	 * @param fileName nome do arquivo.
	 * @param object   Arquivo.
	 * @return Retorna o path do arquivo criado.
	 */
	public static String createJsonFileFromObject(String fileName, Object object) {
		var finalPath = createJsonFile(fileName, null, object);
		return finalPath;
	}
	
	/**
	 * Cria um .json com base em um Object no diretório temporário com Schedule para ser apagado.
	 * 
	 * @param fileName        Nomde do arquivo.
	 * @param object          Arquivo.
	 * @param fileSecTimeTemp Tempo em segunso para o arquivo ser apagado.
	 * @return Retorna o path do arquivo criado.
	 */
	public static String createJsonFileFromObject(String fileName, Object object, long fileSecTimeTemp) {
		String finalPath = createJsonFile(fileName, null, object);
		if (finalPath != null) {
			startDeleteScheduler(fileSecTimeTemp, finalPath);
		}
		return finalPath;
	}

	// ----------------------------------------- Private ----------------------------------------- //
	/**
	 * Verifica se o a pasta foi criada no diretório temporário.
	 * Cria caso não exista.
	 */
	private static void checkExistTempFolder() {
		File directory = new File(TEMP_DIR_PATH + SEPARATOR + TEMP_FOLDER_NAME);
		if (!directory.exists()) {
			directory.mkdir();
			System.out.println("Diretório temporario " + FINAL_TEMP_PATH);
		}
	}

	/**
	 * Inicia um scheduler para deletar um arquivo.
	 * 
	 * @param secTime        Tempo em segundos.
	 * @param fileStringPath Path do arquivo que será apagado.
	 */
	private static void startDeleteScheduler(long secTime, String fileStringPath) {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.schedule(() -> {
			FilesUtils.tryDeleteFile(fileStringPath);
		}, secTime, TimeUnit.SECONDS);
	}

	/**
	 * Cria um arquivo no diretório temporário..
	 * 
	 * @param fileName  Nome do arquivo.
	 * @param fileBytes Arquivo em Bytes.
	 * @return retorna o path do arquivo criado.
	 */
	private static String createFile(String fileName, byte[] fileBytes) {
		String filePath = FINAL_TEMP_PATH + SEPARATOR + UUID.randomUUID().toString() + fileName;
		File file = new File(filePath);

		try (var os = new FileOutputStream(file)) {
			os.write(fileBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return filePath;
	}

	/**
	 * Cria um .json no diretório temporário.
	 * Se jsonString for passado passado o arquivo é criado com base nele.
	 * Se object for passado o arquivo é criado com base nele.
	 * 
	 * 
	 * @param fileName   Nome do arquivo
	 * @param jsonString Conteúdo do arquivo em string.
	 * @param object     Conteúdo do arquivo em Object.
	 * @return retorna o path do arquivo criado.
	 */
	private static String createJsonFile(String fileName, String jsonString, Object object) {
		String filePath = FINAL_TEMP_PATH + SEPARATOR + UUID.randomUUID().toString() + fileName;
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
