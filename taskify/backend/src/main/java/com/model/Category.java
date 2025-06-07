package com.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Scope("prototype")
@Component
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long catId;

	private String categoryName;
    @Column(length = 1000)
	private String categoryDescription;

	public Category() {
	}

	public Category(Long catId, String categoryName, String categoryDescription) {
		this.catId = catId;
		this.categoryName = categoryName;
		this.categoryDescription = categoryDescription;
	}

	public Long getCatId() {
		return catId;
	}

	public void setCatId(Long catId) {
		this.catId = catId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getCategoryDescription() {
		return categoryDescription;
	}

	public void setCatgoryDescription(String categoryDescription) {
		this.categoryDescription = categoryDescription;
	}



}
