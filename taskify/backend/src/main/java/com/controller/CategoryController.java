package com.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.model.Category;
import com.repos.CategoryRepo;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/admin/categories")
public class CategoryController {

	@Autowired
	private CategoryRepo catRepo;

	@PostMapping("/add-category")
	public ResponseEntity<?> addCategory(@RequestBody Category category, HttpSession session) {

		String role = (String) session.getAttribute("role");
		if ("ADMIN".equalsIgnoreCase(role)) {
			try {
				catRepo.save(category);
				return ResponseEntity.status(HttpStatus.CREATED)
						.body(Map.of("status", "success", "message", "Category Created Successfully!!!"));
			} catch (Exception e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of("status", "error", "message", e.getMessage()));
			}
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("status", "error", "message", "Access Denied"));
		}

	}

	@PostMapping("/update-category/{id}")
	public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category category,
			HttpSession session) {
		String role = (String) session.getAttribute("role");
		if ("ADMIN".equalsIgnoreCase(role)) {
			try {

				Category existingCategory = catRepo.findByCatId(id)
						.orElseThrow(() -> new RuntimeException("Category not found"));
				existingCategory.setCategoryName(category.getCategoryName());
				existingCategory.setCatgoryDescription(category.getCategoryDescription());

				catRepo.save(existingCategory);

				return ResponseEntity.status(HttpStatus.OK)
						.body(Map.of("status", "success", "message", "Category Updated Successfully"));
			} catch (Exception e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of("status", "error", "message", e.getMessage()));
			}
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("status", "error", "message", "Access Denied"));
		}
	}

	@DeleteMapping("/delete-category/{id}")
	public ResponseEntity<?> deleteCategory(@PathVariable Long id, HttpSession session) {
		String role = (String) session.getAttribute("role");
		if ("ADMIN".equalsIgnoreCase(role)) {

			try {
				Category existingCategory = catRepo.findByCatId(id)
						.orElseThrow(() -> new RuntimeException("Category with ID " + id + " not found"));

				catRepo.delete(existingCategory);

				return ResponseEntity.status(HttpStatus.OK)
						.body(Map.of("status", "success", "message", "Category Deleted Successfully!!!"));
			} catch (Exception e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of("status", "error", "message", e.getMessage()));
			}
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("status", "error", "message", "Access Denied"));
		}
	}

	@GetMapping("/get-all")
	public ResponseEntity<?> getAllCategories() {
		return ResponseEntity.ok(catRepo.findAll());
	}

	@GetMapping("/search")
	public ResponseEntity<List<Category>> searchCategories(@RequestParam String keyword) {
		return ResponseEntity.ok(catRepo
				.findByCategoryNameContainingIgnoreCaseOrCategoryDescriptionContainingIgnoreCase(keyword, keyword));
	}
}
