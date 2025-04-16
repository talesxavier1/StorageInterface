package br.com.tx.storageInterface.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.directory.NoSuchAttributeException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import br.com.tx.storageInterface.Utils.ClassUtils;
import br.com.tx.storageInterface.Utils.Utils;
import br.com.tx.storageInterface.enums.FileManagerGetComandEum;
import br.com.tx.storageInterface.enums.FileManagerPostComandEum;
import br.com.tx.storageInterface.enums.ScriptModuleTypeEnum;
import br.com.tx.storageInterface.models.APIContextModel;
import br.com.tx.storageInterface.models.APIPaginationModel;
import br.com.tx.storageInterface.models.ArgumentsModel;
import br.com.tx.storageInterface.models.ChunkMetadataModel;
import br.com.tx.storageInterface.models.FileModel;
import br.com.tx.storageInterface.models.ResponseContentModel;
import br.com.tx.storageInterface.services.FileManagerService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/file-manager")
public class FileManagerController {

	@Autowired
	private FileManagerService fileManagerService;

	@GetMapping("/")
	public ResponseEntity<ResponseContentModel> fileManagerGet(
			@RequestParam FileManagerGetComandEum command,
			@RequestHeader(required = false) ScriptModuleTypeEnum scriptModule,
			@Parameter(description = "APIPaginationModel", content = @Content(schema = @Schema(implementation = APIPaginationModel.class))) 
			@RequestHeader(required = false) String strPagination,
			@Parameter(description = "ArgumentsModel", content = @Content(schema = @Schema(implementation = ArgumentsModel.class))) 
			@RequestParam String arguments,
			@Parameter(description = "ApiContextModel", content = @Content(schema = @Schema(implementation = APIContextModel.class))) 
			@RequestHeader String strAPIContextModel
	) {

		ResponseContentModel response = new ResponseContentModel();

		APIContextModel apiContextModel = new Gson().fromJson(strAPIContextModel, APIContextModel.class);
		ArgumentsModel argumentsModel = new Gson().fromJson(arguments, ArgumentsModel.class);
		ChunkMetadataModel chunkMetadataModel = new Gson().fromJson(argumentsModel.getChunkMetadata(), ChunkMetadataModel.class);
		argumentsModel.setClassChunkMetadata(chunkMetadataModel);
		APIPaginationModel apiPaginationModel = null;
		if (strPagination != null) {
			apiPaginationModel = new Gson().fromJson(strPagination, APIPaginationModel.class);
		}

		boolean allOK = true;
		String errorMessage = "";

		var apiContextModelValidate = new ClassUtils<APIContextModel>().validClass(apiContextModel);
		if (apiContextModelValidate.get("STATUS").equals("NOK")) {
			allOK = false;
			errorMessage = apiContextModelValidate.get("MESSAGE");
		}

		var argumentsModelValidate = new ClassUtils<ArgumentsModel>().validClass(argumentsModel);
		if (argumentsModelValidate.get("STATUS").equals("NOK")) {
			allOK = false;
			errorMessage = argumentsModelValidate.get("MESSAGE");
		}

		if (chunkMetadataModel != null) {
			var chunkMetadataModelValidate = new ClassUtils<ChunkMetadataModel>().validClass(chunkMetadataModel);
			if (chunkMetadataModelValidate.get("STATUS").equals("NOK")) {
				allOK = false;
				errorMessage = chunkMetadataModelValidate.get("MESSAGE");
			}
		}

		if (apiPaginationModel != null) {
			var apiPaginationModelValidate = new ClassUtils<APIPaginationModel>().validClass(apiPaginationModel);
			if (apiPaginationModelValidate.get("STATUS").equals("NOK")) {
				allOK = false;
				errorMessage = apiPaginationModelValidate.get("MESSAGE");
			}
		}

		if (!allOK) {
			response.setSuccess(false);
			response.setErrorText(errorMessage);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}

		try {
			if (command == FileManagerGetComandEum.GetDirContents) {
				Pageable pageable = null;

				if (apiPaginationModel != null) {
					pageable = PageRequest.of(apiPaginationModel.getPage(), apiPaginationModel.getTake(), Sort.by("dateCreated").descending());
				}

				FileModel[] tempResult = this.fileManagerService.getTempDirContent(argumentsModel, apiContextModel, scriptModule, pageable);
				boolean tempDirExist = this.fileManagerService.tempDirExist(apiContextModel);
				if (tempResult.length > 0 || tempDirExist) {
					response.setSuccess(true);
					response.setResult(tempResult);
				} else {
					FileModel[] result = this.fileManagerService.getDirContent(argumentsModel, apiContextModel, scriptModule, pageable);
					response.setSuccess(true);
					response.setResult(result);

					/*
					 * Cria o doiretorio temporário somente quando é FILE_MANAGER. pq quando é
					 * script único, a função de SaveUniqueFileContent já salva o script como
					 * temporário.
					 */
					if (Utils.stringHasValue(apiContextModel.getTempDirID()) && result.length > 0 && scriptModule == ScriptModuleTypeEnum.FILE_MANAGER) {
						this.fileManagerService.createTempDirContent(apiContextModel);
					}
				}
			}

			if (command == FileManagerGetComandEum.GetFileContent) {
				String result = this.fileManagerService.getFileContent(argumentsModel, apiContextModel);
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
			@Parameter(description = "ApiContextModel", content = @Content(schema = @Schema(implementation = ArgumentsModel.class))) 
			@RequestHeader String arguments,
			@RequestHeader FileManagerPostComandEum command,
			@Parameter(description = "ApiContextModel", content = @Content(schema = @Schema(implementation = APIContextModel.class)))
			@RequestHeader String strAPIContextModel
		) {

		ResponseContentModel response = new ResponseContentModel();

		APIContextModel apiContextModel = new Gson().fromJson(strAPIContextModel, APIContextModel.class);

		ArgumentsModel argumentsModel = new Gson().fromJson(arguments, ArgumentsModel.class);
		ChunkMetadataModel chunkMetadataModel = new Gson().fromJson(argumentsModel.getChunkMetadata(), ChunkMetadataModel.class);
		argumentsModel.setClassChunkMetadata(chunkMetadataModel);

		boolean allOK = true;
		String errorMessage = "";

		var apiContextModelValidate = new ClassUtils<APIContextModel>().validClass(apiContextModel);
		if (apiContextModelValidate.get("STATUS").equals("NOK")) {
			allOK = false;
			errorMessage = apiContextModelValidate.get("MESSAGE");
		}

		var argumentsModelValidate = new ClassUtils<ArgumentsModel>().validClass(argumentsModel);
		if (argumentsModelValidate.get("STATUS").equals("NOK")) {
			allOK = false;
			errorMessage = argumentsModelValidate.get("MESSAGE");
		}

		var chunkMetadataModelValidate = new ClassUtils<ChunkMetadataModel>().validClass(chunkMetadataModel);
		if (chunkMetadataModelValidate.get("STATUS").equals("NOK")) {
			allOK = false;
			errorMessage = chunkMetadataModelValidate.get("MESSAGE");
		}

		if (!allOK) {
			response.setSuccess(false);
			response.setErrorText(errorMessage);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}

		try {
			if (command == FileManagerPostComandEum.UploadChunk) {
				try {
					var result = this.fileManagerService.uploadChunk(argumentsModel, chunk, apiContextModel);
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
					var result = this.fileManagerService.updateFileContent(argumentsModel, chunk, apiContextModel);
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
					var result = this.fileManagerService.saveFileContent(argumentsModel, chunk, apiContextModel);
					if (!result) {
						response.setErrorText("Não foi possível salvar o conteúdo.");
						response.setSuccess(false);
					}
				} catch (IOException | GeneralSecurityException e) {
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
			@Parameter(description = "ApiContextModel", content = @Content(schema = @Schema(implementation = ArgumentsModel.class))) 
			@RequestHeader String arguments,
			@Parameter(description = "ApiContextModel", content = @Content(schema = @Schema(implementation = APIContextModel.class))) 
			@RequestHeader String strAPIContextModel) {
		APIContextModel apiContextModel = new Gson().fromJson(strAPIContextModel, APIContextModel.class);

		ArgumentsModel argumentsModel = new Gson().fromJson(arguments, ArgumentsModel.class);
		ChunkMetadataModel chunkMetadataModel = new Gson().fromJson(argumentsModel.getChunkMetadata(),
				ChunkMetadataModel.class);
		argumentsModel.setClassChunkMetadata(chunkMetadataModel);

		boolean allOK = true;

		var apiContextModelValidate = new ClassUtils<APIContextModel>().validClass(apiContextModel);
		if (apiContextModelValidate.get("STATUS").equals("NOK")) {
			allOK = false;
		}

		var argumentsModelValidate = new ClassUtils<ArgumentsModel>().validClass(argumentsModel);
		if (argumentsModelValidate.get("STATUS").equals("NOK")) {
			allOK = false;
		}
		if (chunkMetadataModel != null) {
			var chunkMetadataModelValidate = new ClassUtils<ChunkMetadataModel>().validClass(chunkMetadataModel);
			if (chunkMetadataModelValidate.get("STATUS").equals("NOK")) {
				allOK = false;
			}
		}

		if (!allOK) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		try {
			String tempFilePath = this.fileManagerService.download(argumentsModel, apiContextModel);

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
			@RequestParam FileManagerPostComandEum command,
			@Parameter(description = "ApiContextModel", content = @Content(schema = @Schema(implementation = ArgumentsModel.class))) 
			@RequestParam String arguments,
			@Parameter(description = "ApiContextModel", content = @Content(schema = @Schema(implementation = APIContextModel.class))) 
			@RequestHeader String strAPIContextModel) {
		ResponseContentModel response = new ResponseContentModel();

		APIContextModel apiContextModel = new Gson().fromJson(strAPIContextModel, APIContextModel.class);

		ArgumentsModel argumentsModel = new Gson().fromJson(arguments, ArgumentsModel.class);
		ChunkMetadataModel chunkMetadataModel = new Gson().fromJson(argumentsModel.getChunkMetadata(), ChunkMetadataModel.class);
		argumentsModel.setClassChunkMetadata(chunkMetadataModel);

		boolean allOK = true;
		String errorMessage = "";

		var apiContextModelValidate = new ClassUtils<APIContextModel>().validClass(apiContextModel);
		if (apiContextModelValidate.get("STATUS").equals("NOK")) {
			allOK = false;
			errorMessage = apiContextModelValidate.get("MESSAGE");
		}

		var argumentsModelValidate = new ClassUtils<ArgumentsModel>().validClass(argumentsModel);
		if (argumentsModelValidate.get("STATUS").equals("NOK")) {
			allOK = false;
			errorMessage = argumentsModelValidate.get("MESSAGE");
		}

		if (chunkMetadataModel != null) {
			var chunkMetadataModelValidate = new ClassUtils<ChunkMetadataModel>().validClass(chunkMetadataModel);
			if (chunkMetadataModelValidate.get("STATUS").equals("NOK")) {
				allOK = false;
				errorMessage = chunkMetadataModelValidate.get("MESSAGE");
			}
		}

		if (!allOK) {
			response.setSuccess(false);
			response.setErrorText(errorMessage);
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}

		try {
			if (command == FileManagerPostComandEum.CreateDir) {
				try {
					boolean result = this.fileManagerService.createDir(
							argumentsModel,
							apiContextModel.getProcessID(),
							apiContextModel.getProcessVersionID(),
							apiContextModel.getPackageID(),
							apiContextModel.getPackageVersionID(),
							apiContextModel.getTempDirID());
					if (!result) {
						response.setSuccess(false);
						response.setErrorText("Não foi possível criar o diretório.");
					}
				} catch (Exception e) {
					throw new Exception("Não foi possível criar o diretório.");
				}
			} else if (command == FileManagerPostComandEum.Rename) {
				try {
					boolean result = this.fileManagerService.renameFile(argumentsModel, apiContextModel);
					if (!result) {
						response.setSuccess(false);
						response.setErrorText("Não foi possível renomear arquivo ou diretório.");
					}
				} catch (Exception e) {
					throw new Exception("Não foi possível renomear arquivo ou diretório.");
				}

			} else if (command == FileManagerPostComandEum.Copy) {
				try {
					boolean result = this.fileManagerService.copy(argumentsModel, apiContextModel);

					if (!result) {
						response.setSuccess(false);
						response.setErrorText("Não foi possível copiar arquivo ou diretório.");
					}
				} catch (Exception e) {
					throw new Exception("Não foi possível copiar arquivo ou diretório.");
				}
			} else if (command == FileManagerPostComandEum.Remove) {
				try {
					boolean result = this.fileManagerService.logicalDeletion(argumentsModel, apiContextModel);

					if (!result) {
						response.setSuccess(false);
						response.setErrorText("Não foi possível copiar arquivo ou diretório.");
					}
				} catch (Exception e) {
					throw new Exception("Não foi possível copiar arquivo ou diretório.");
				}
			} else if (command == FileManagerPostComandEum.Move) {
				try {
					boolean result = this.fileManagerService.move(argumentsModel, apiContextModel);

					if (!result) {
						response.setSuccess(false);
						response.setErrorText("Não foi possível mover arquivo ou diretório.");
					}
				} catch (Exception e) {
					throw new Exception("Não foi possível mover arquivo ou diretório.");
				}

			} else if (command == FileManagerPostComandEum.ClearTempDir) {
				try {
					boolean result = this.fileManagerService.deleteTempDir(apiContextModel);

					if (!result) {
						response.setSuccess(false);
						response.setErrorText("Não foi possível deletar diretório temporário.");
					}
				} catch (Exception e) {
					throw new Exception("Não foi possível deletar diretório temporário.");
				}
			} else if (command == FileManagerPostComandEum.PubTempDir) {
				try {
					String result = this.fileManagerService.pubTempDir(apiContextModel);

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
