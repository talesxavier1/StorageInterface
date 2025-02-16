package br.com.tx.storageInterface.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.directory.NoSuchAttributeException;

import org.apache.avalon.framework.parameters.ParameterException;
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
import br.com.tx.storageInterface.enums.ScriptModuleTypeEnum;
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
			@RequestHeader(required = false) String packageVersionID, /* Quando não enviado, a aplicaçã retorna todos os arquivos independete da versão. */
			@RequestParam String arguments,
			@RequestHeader(required = false) String tempDirID,
			@RequestHeader(required = false) ScriptModuleTypeEnum scriptModule
	) {
		ResponseContentModel response = new ResponseContentModel();
		ArgumentsModel argumentsModel = new Gson().fromJson(arguments, ArgumentsModel.class);
		argumentsModel.init();

		try {
			if (command == FileManagerGetComandEum.GetDirContents) {
				FileModel[] tempResult = this.fileManagerService.getTempDirContent(argumentsModel, processID, processVersionID, packageID, tempDirID, scriptModule);
				boolean tempDirExist = this.fileManagerService.tempDirExist(tempDirID);
				if (tempResult.length > 0 || tempDirExist) {
					response.setSuccess(true);
					response.setResult(tempResult);
				} else {
					FileModel[] result = this.fileManagerService.getDirContent(argumentsModel, processID, processVersionID, packageID, packageVersionID, scriptModule);
					response.setSuccess(true);
					response.setResult(result);

					/* Cria o doiretorio temporário somente quando é FILE_MANAGER. pq quando é script único, a função de SaveUniqueFileContent já salva o script como temporário. */
					if (Utils.stringHasValue(tempDirID) && result.length > 0 && scriptModule == ScriptModuleTypeEnum.FILE_MANAGER) {
						this.fileManagerService.createTempDirContent(processID, processVersionID, packageID, packageVersionID, tempDirID);
					}
				}
			}

			if (command == FileManagerGetComandEum.GetFileContent) {

				String result = this.fileManagerService.getFileContent(argumentsModel, tempDirID);
				response.setStrResult(result);
			}
		} catch (NoSuchAttributeException | GeneralSecurityException | IOException e) {
			e.printStackTrace();
			response.setErrorText("Não foi possível consultar o contaúdo.");
			response.setSuccess(false);
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
		
		try {
			if (command == FileManagerPostComandEum.UploadChunk) {
				try {
					var result = this.fileManagerService.uploadChunk(argumentsModel, chunk, processID, processVersionID, packageID, tempDirID);
					if (!result) {
						response.setErrorText("Não foi possível fazer o upload.");
						response.setSuccess(false);
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new Exception("Não foi possível fazer o upload.");
				}
			} else if (command == FileManagerPostComandEum.UpdateFileContent) {
				try {
					var result = this.fileManagerService.updateFileContent(argumentsModel, chunk, processID, processVersionID, packageID, tempDirID);
					if (!result) {
						response.setErrorText("Não foi possível atualizar o arquivo.");
						response.setSuccess(false);
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new Exception("Não foi possível atualizar o arquivo.");
				}
			} else if (command == FileManagerPostComandEum.SaveUniqueFileContent) {
				try {
					var result = this.fileManagerService.saveFileContent(argumentsModel, chunk, processID, processVersionID, packageID, tempDirID);
					if (!result) {
						response.setErrorText("Não foi possível salvar o conteúdo.");
						response.setSuccess(false);
					}
				} catch (ParameterException | IOException | GeneralSecurityException e) {
					e.printStackTrace();
					throw new Exception("Não foi possível salvar o conteúdo.");
				}
			}

		} catch (Exception e) {
			response.setErrorText(e.getMessage());
			response.setSuccess(false);
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
	@GetMapping(value = "/Download")
	public ResponseEntity<InputStreamResource> fileManagerGet(
			@RequestHeader String processID,
			@RequestHeader String processVersionID,
			@RequestHeader String packageID,
			@RequestHeader String arguments,
			@RequestHeader(required = false) String tempDirID
	) throws GeneralSecurityException, IOException {
		ArgumentsModel argumentsModel = new Gson().fromJson(arguments, ArgumentsModel.class);
		argumentsModel.init();

		try {
			String tempFilePath = this.fileManagerService.download(argumentsModel, processID, processVersionID, packageID, tempDirID);
			
			File file = new File(tempFilePath);
			InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + argumentsModel.getName());

			return ResponseEntity.ok().headers(headers).contentLength(file.length()).body(resource);
		} catch (GeneralSecurityException | IOException e) {
			return ResponseEntity.notFound().build();
		}
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
		try {
			if (command == FileManagerPostComandEum.CreateDir) {
				try {
					boolean result = this.fileManagerService.createDir(argumentsModel, processID, processVersionID, packageID, packageVersionID, tempDirID);
					if (!result) {
						response.setSuccess(false);
						response.setErrorText("Não foi possível criar o diretório.");
					}
				} catch (Exception e) {
					throw new Exception("Não foi possível criar o diretório.");
				}
			} else if (command == FileManagerPostComandEum.Rename) {
				try {
					boolean result = this.fileManagerService.renameFile(argumentsModel, processID, processVersionID, packageID, tempDirID);
					if (!result) {
						response.setSuccess(false);
						response.setErrorText("Não foi possível renomear arquivo ou diretório.");
					}
				} catch (Exception e) {
					throw new Exception("Não foi possível renomear arquivo ou diretório.");
				}

			} else if (command == FileManagerPostComandEum.Copy) {
				try {
					boolean result = this.fileManagerService.copy(argumentsModel, processID, processVersionID, packageID, tempDirID);

					if (!result) {
						response.setSuccess(false);
						response.setErrorText("Não foi possível copiar arquivo ou diretório.");
					}
				} catch (Exception e) {
					throw new Exception("Não foi possível copiar arquivo ou diretório.");
				}
			} else if (command == FileManagerPostComandEum.Remove) {
				try {
					boolean result = this.fileManagerService.logicalDeletion(argumentsModel, processID, processVersionID, packageID, tempDirID);

					if (!result) {
						response.setSuccess(false);
						response.setErrorText("Não foi possível copiar arquivo ou diretório.");
					}
				} catch (Exception e) {
					throw new Exception("Não foi possível copiar arquivo ou diretório.");
				}
			} else if (command == FileManagerPostComandEum.Move) {
				try {
					boolean result = this.fileManagerService.move(argumentsModel, processID, processVersionID, packageID, tempDirID);

					if (!result) {
						response.setSuccess(false);
						response.setErrorText("Não foi possível mover arquivo ou diretório.");
					}
				} catch (Exception e) {
					throw new Exception("Não foi possível mover arquivo ou diretório.");
				}

			} else if (command == FileManagerPostComandEum.ClearTempDir) {
				try {
					boolean result = this.fileManagerService.deleteTempDir(tempDirID);

					if (!result) {
						response.setSuccess(false);
						response.setErrorText("Não foi possível deletar diretório temporário.");
					}
				} catch (Exception e) {
					throw new Exception("Não foi possível deletar diretório temporário.");
				}
			} else if (command == FileManagerPostComandEum.PubTempDir) {
				try {
					String result = this.fileManagerService.pubTempDir(tempDirID);

					if (result == null) {
						response.setSuccess(false);
						response.setErrorText("Não foi possível publicar diretório temporário.");
					} else {
						Map<String, String> responseMap = new HashMap<String, String>();
						responseMap.put("newPackageVersionID", result);
						response.setStrResult(new Gson().toJson(responseMap));
					}
				} catch (Exception e) {
					throw new Exception("Não foi possível publicar diretório temporário.");
				}
			}
		} catch (Exception e) {
			response.setErrorText(e.getMessage());
			response.setSuccess(false);
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
