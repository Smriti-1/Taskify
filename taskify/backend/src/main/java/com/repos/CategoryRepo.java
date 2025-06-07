package com.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.model.Category;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Integer> {

	List<Category> findByCategoryNameContainingIgnoreCaseOrCategoryDescriptionContainingIgnoreCase(String name, String description);


	Optional<Category> findByCatId(Long catId);



}
