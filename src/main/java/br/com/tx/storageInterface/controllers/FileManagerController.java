package br.com.tx.storageInterface.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import br.com.tx.storageInterface.Utils.Utils;
import br.com.tx.storageInterface.enums.FileManagerGetComandEum;
import br.com.tx.storageInterface.enums.FileManagerPostComandEum;
import br.com.tx.storageInterface.models.ArgumentsModel;
import br.com.tx.storageInterface.models.FileModel;
import br.com.tx.storageInterface.models.ResponseContentModel;
import br.com.tx.storageInterface.services.FileManagerService;



@RestController
@RequestMapping("/file-manager")
public class FileManagerController {


	@Autowired
	private FileManagerService fileManagerService;

	@GetMapping("/")
	public ResponseEntity<ResponseContentModel> fileManagerGet(
			@RequestParam FileManagerGetComandEum command,
			@RequestHeader String processID,
			@RequestHeader String processVersionID,
			@RequestHeader String packageID,
			@RequestHeader String packageVersionID,
			@RequestParam String arguments,
			@RequestHeader(required = false) String tempDirID
	) {
		ResponseContentModel response = new ResponseContentModel();
		ArgumentsModel argumentsModel = new Gson().fromJson(arguments, ArgumentsModel.class);
		argumentsModel.init();

		if (command == FileManagerGetComandEum.GetDirContents) {
			
			FileModel[] tempResult = this.fileManagerService.getTempDirContent(argumentsModel, processID, processVersionID, packageID, tempDirID);
			if(tempResult.length > 0) {
				response.setSuccess(true);
				response.setResult(tempResult);
			} else {
				FileModel[] result = this.fileManagerService.getDirContent(argumentsModel, processID, processVersionID, packageID, packageVersionID);
				response.setSuccess(true);
				response.setResult(result);
				
				if (Utils.stringHasValue(tempDirID) && result.length > 0) {
					this.fileManagerService.createTempDirContent(processID, processVersionID, packageID, packageVersionID, tempDirID);
				}

			}
		}

		if (command == FileManagerGetComandEum.GetFileContent) {
			String result = this.fileManagerService.getFileContent(argumentsModel);
			response.setStrResult(result);
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@PostMapping(value = "/", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<ResponseContentModel> fileManagerPost(
			@RequestPart MultipartFile chunk, 
			@RequestHeader String arguments, 
			@RequestHeader FileManagerPostComandEum command,
			@RequestHeader String processID,
			@RequestHeader String processVersionID,
			@RequestHeader String packageID,
			@RequestHeader String tempDirID
	) {

		ResponseContentModel response = new ResponseContentModel();
		ArgumentsModel argumentsModel = new Gson().fromJson(arguments, ArgumentsModel.class);
		argumentsModel.init();

		if (command == FileManagerPostComandEum.UploadChunk) {
			boolean result = this.fileManagerService.UploadChunk(argumentsModel, chunk , processID, processVersionID, packageID, tempDirID);
			if (!result) {
				response.setSuccess(false);
				response.setErrorText("Não foi possível fazer o upload do arquivo.");
			}
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping(value = "/Download")
	public ResponseEntity<InputStreamResource> fileManagerGet(
			@RequestHeader String processID,
			@RequestHeader String processVersionID,
			@RequestHeader String packageID,
			@RequestHeader String arguments
	) throws IOException {
		ArgumentsModel argumentsModel = new Gson().fromJson(arguments, ArgumentsModel.class);
		argumentsModel.init();
		
		String tempFilePath = this.fileManagerService.download(argumentsModel, processID, processVersionID, packageID);
		if (tempFilePath == null) {
			return ResponseEntity.notFound().build();
		}
		
		File file = new File(tempFilePath);
		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + argumentsModel.getName());

		return ResponseEntity.ok().headers(headers).contentLength(file.length()).body(resource);
	}
	

	@PostMapping(value = "/")
	@Transactional
	public ResponseEntity<ResponseContentModel> fileManagerPost(
			@RequestHeader String processID,
			@RequestHeader String processVersionID,
			@RequestHeader String packageID,
			@RequestHeader String packageVersionID,
			@RequestHeader String tempDirID,
			@RequestParam String arguments,
			@RequestParam FileManagerPostComandEum command
	) {
		ResponseContentModel response = new ResponseContentModel();
		ArgumentsModel argumentsModel = new Gson().fromJson(arguments, ArgumentsModel.class);
		argumentsModel.init();

		if (command == FileManagerPostComandEum.CreateDir) {
			boolean result = this.fileManagerService.createDir(argumentsModel, processID, processVersionID, packageID, packageVersionID, tempDirID);
			if (!result) {
				response.setSuccess(false);
				response.setErrorText("Não foi possível criar o diretório.");
			}
		} else if (command == FileManagerPostComandEum.Rename) {
			boolean result = this.fileManagerService.renameFile(argumentsModel, processID, processVersionID, packageID, tempDirID);
			if (!result) {
				response.setSuccess(false);
				response.setErrorText("Não foi possível renomear arquivo ou diretório.");
			}
		} else if (command == FileManagerPostComandEum.Copy) {
			boolean result = this.fileManagerService.copy(argumentsModel, processID, processVersionID, packageID, tempDirID);

			if (!result) {
				response.setSuccess(false);
				response.setErrorText("Não foi possível copiar arquivo ou diretório.");
			}
		} else if (command == FileManagerPostComandEum.Remove) {
			boolean result = this.fileManagerService.logicalDeletion(argumentsModel, processID, processVersionID, packageID, tempDirID);

			if (!result) {
				response.setSuccess(false);
				response.setErrorText("Não foi possível copiar arquivo ou diretório.");
			}
		} else if (command == FileManagerPostComandEum.Move) {
			boolean result = this.fileManagerService.move(argumentsModel, processID, processVersionID, packageID, tempDirID);

			if (!result) {
				response.setSuccess(false);
				response.setErrorText("Não foi possível mover arquivo ou diretório.");
			}
		} else if (command == FileManagerPostComandEum.ClearTempDir) {
			boolean result = this.fileManagerService.deleteTempDir(tempDirID);

			if (!result) {
				response.setSuccess(false);
				response.setErrorText("Não foi possível deletar diretório temporário.");
			}

		} else if (command == FileManagerPostComandEum.PubTempDir) {
			String result = this.fileManagerService.pubTempDir(tempDirID);

			if (result == null) {
				response.setSuccess(false);
				response.setErrorText("Não foi possível publicar diretório temporário.");
			}
			else {
				Map<String, String> responseMap = new HashMap<String, String>();
				responseMap.put("newPackageVersionID", result);
				response.setStrResult(new Gson().toJson(responseMap));
			}
		}


		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
