package br.com.tx.storageInterface.models;

import java.util.List;
import java.util.UUID;

import br.com.tx.storageInterface.Utils.Utils;

/* Classe usada para organizar os TempFileModel em hierarquia */
public class FilelHierarchyModel {

	/* Arquivo ou pasta pai */
	private TempFileModel sourceFileModel;
	/* Lista de arquivos ou pastas filhas */
	private List<FilelHierarchyModel> childsFileModel;

	/**
	 * Atualiza o setKeyID do sourceFileModel e todos seus filhos.
	 * 
	 * @param newID     Novo ID do sourceFileModel
	 * @param parentKey Novo parentKey do sourceFileModel. (Pode ser passado nulo quando não tem pai.)
	 * @throws Exception lançado quando parâmetro newID não é passado.
	 */
	public void updateIDAndHierarchy(String newID, String parentKey) throws Exception {

		if(!Utils.stringHasValue(newID)) {throw new Exception("Campo ID vazio ou nulo.");}
		
		this.sourceFileModel.setKeyID(newID);
		this.sourceFileModel.set_id(UUID.randomUUID().toString());
		
		String newParentKey = Utils.stringHasValue(parentKey) ? parentKey : "";
		if (Utils.stringHasValue(newParentKey)) {
			this.sourceFileModel.setKey(parentKey + "/" + this.sourceFileModel.getKeyID());
		} else {
			this.sourceFileModel.setKey(this.sourceFileModel.getKeyID());
		}

		this.sourceFileModel.getFileInfoModel().setParentKey(newParentKey);

		for (FilelHierarchyModel child : this.childsFileModel) {
			child.updateIDAndHierarchy(UUID.randomUUID().toString(), sourceFileModel.getKey());
		}
	}

	/**
	 * Atualiza a Key do sourceFileModel e de todos os filhos.
	 * 
	 * @param key Nova Key.
	 */
	public void updateKeyAndHierarchy(String key) {
		
		if (!Utils.stringHasValue(key)) {
			this.sourceFileModel.setKey(sourceFileModel.getKeyID());
		} else {
			this.sourceFileModel.setKey(key + "/" + sourceFileModel.getKeyID());
		}
		
		for (FilelHierarchyModel child : this.childsFileModel) {
			child.updateKeyAndHierarchy(this.sourceFileModel.getKey());
		}
	}

	/**
	 * Atualiza o parentKey do sourceFileModel e de todos seus filhos.
	 * 
	 * @param parentKey nova parentKey
	 */
	public void updateParentKeyAndHierarchy(String parentKey) {
		if (!Utils.stringHasValue(parentKey)) {
			this.sourceFileModel.getFileInfoModel().setParentKey("");
			this.sourceFileModel.setKey(this.sourceFileModel.getKeyID());
		} else {
			this.sourceFileModel.getFileInfoModel().setParentKey(parentKey);
			this.sourceFileModel.setKey(parentKey + "/" + this.sourceFileModel.getKeyID());
		}

		for (FilelHierarchyModel child : this.childsFileModel) {
			child.updateParentKeyAndHierarchy(this.sourceFileModel.getKey());
		}
	}

	public TempFileModel getSourceFileModel() {
		return sourceFileModel;
	}

	public void setSourceFileModel(TempFileModel sourceFileModel) {
		this.sourceFileModel = sourceFileModel;
	}

	public List<FilelHierarchyModel> getChildsFileModel() {
		return childsFileModel;
	}

	public void setChildsFileModel(List<FilelHierarchyModel> childsFileModel) {
		this.childsFileModel = childsFileModel;
	}

}
