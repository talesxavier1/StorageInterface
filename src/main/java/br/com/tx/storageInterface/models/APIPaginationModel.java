package br.com.tx.storageInterface.models;

import jakarta.validation.constraints.NotNull;

public class APIPaginationModel {

	@NotNull
	private Integer take;
	@NotNull
	private Integer page;

	public Integer getTake() {
		return take;
	}

	public void setTake(Integer take) {
		this.take = take;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

}
