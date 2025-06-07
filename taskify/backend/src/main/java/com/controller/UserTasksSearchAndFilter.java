package com.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.model.Tasks;
import com.repos.TasksRepo;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/tasks")
public class UserTasksSearchAndFilter {

	@Autowired
	private TasksRepo tasksRepo;

	// seacrh on the basis of keyword in title , desc , cate etc
	@GetMapping("/search")
	public ResponseEntity<?> searchTasks(@RequestParam String keyword, @RequestParam String status,
			HttpSession session) {

		String role = (String) session.getAttribute("role");
		Integer userId = (Integer) session.getAttribute("user_id");

		if (role == null || userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("status", "error", "message", "User not logged in or session expired."));
		}
		List<String> statusList;
		if ("incomplete".equalsIgnoreCase(status)) {
			statusList = List.of("Todo", "Inprogress");
		} else if ("done".equalsIgnoreCase(status)) {
			statusList = List.of("Done");
		} else if ("redo".equalsIgnoreCase(status)) {
			statusList = List.of("Redo");
		}else if ("overdue".equalsIgnoreCase(status)) {
			statusList = List.of("Overdue");
		} else {
			return ResponseEntity.ok(Map.of("status", "error", "message", "Invalid status filter"));
		}

		List<Tasks> tasks = tasksRepo.searchByKeywordUserIdAndStatus(keyword, userId, statusList);

		if (tasks.isEmpty()) {
			return ResponseEntity.ok(Map.of("status", "success", "data", tasks, "message", "No matching tasks found."));
		}

		return ResponseEntity.ok(Map.of("status", "success", "data", tasks));
	}

//	===== Filtering Task by sorting ====
	@GetMapping("/filter-by-sort")
	public ResponseEntity<?> filterTasksBasedOnSort(@RequestParam String tasksStatus, @RequestParam String sort,
			HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer userId = (Integer) session.getAttribute("user_id");

			if (role == null || userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			List<String> statusList;
			if ("incomplete".equalsIgnoreCase(tasksStatus)) {
				statusList = List.of("Todo", "Inprogress");
			} else if ("done".equalsIgnoreCase(tasksStatus)) {
				statusList = List.of("Done");
			} else if ("redo".equalsIgnoreCase(tasksStatus)) {
				statusList = List.of("Redo");
			} else if ("overdue".equalsIgnoreCase(tasksStatus)) {
				statusList = List.of("Overdue");
			}else {
				return ResponseEntity.ok(Map.of("status", "error", "message", "Invalid filter."));
			}

			Sort sortOrder;
			switch (sort.toLowerCase()) {
			case "newest":
				sortOrder = Sort.by(Sort.Direction.DESC, "createdTime");
				break;
			case "oldest":
				sortOrder = Sort.by(Sort.Direction.ASC, "createdTime");
				break;
			case "title-asc":
				sortOrder = Sort.by(Sort.Direction.ASC, "title");
				break;
			case "title-desc":
				sortOrder = Sort.by(Sort.Direction.DESC, "title");
				break;
			default:
				return ResponseEntity.ok(Map.of("status", "error", "message", "Invalid sort option."));
			}

			List<Tasks> tasks = tasksRepo.findByUserUserIdAndStatusIn(userId, statusList, sortOrder);
			return ResponseEntity.ok(Map.of("status", "success", "data", tasks));
		} catch (Exception e)

		{
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", e.getMessage()));
		}
	}

//	===== Filtering task by priority ====
	@GetMapping("/filter-by-priority")
	public ResponseEntity<?> filterTasksByPriority(@RequestParam String tasksStatus, @RequestParam String priority,
			HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer userId = (Integer) session.getAttribute("user_id");

			if (role == null || userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			List<String> statusList;
			if ("incomplete".equalsIgnoreCase(tasksStatus)) {
				statusList = List.of("Todo", "Inprogress");
			} else if ("done".equalsIgnoreCase(tasksStatus)) {
				statusList = List.of("Done");
			} else if ("redo".equalsIgnoreCase(tasksStatus)) {
				statusList = List.of("Redo");
			} else if ("overdue".equalsIgnoreCase(tasksStatus)) {
				statusList = List.of("Overdue");
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("status", "error", "message", "Invalid filter."));
			}

			List<Tasks> tasks = tasksRepo.findByUserUserIdAndStatusInAndPriorityIgnoreCase(userId, statusList,
					priority);

			if (tasks.isEmpty()) {
				return ResponseEntity
						.ok(Map.of("status", "success", "message", "No tasks found with the given priority."));
			}

			return ResponseEntity.ok(Map.of("status", "success", "data", tasks));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", e.getMessage()));
		}
	}

	@GetMapping("/filter-by-status")
	public ResponseEntity<?> filterTasksByStatus(@RequestParam String status, HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer userId = (Integer) session.getAttribute("user_id");

			if (role == null || userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			List<Tasks> tasks = tasksRepo.findByUserUserIdAndStatusIgnoreCaseOrderByCreatedTimeDesc(userId, status);

			if (tasks.isEmpty()) {
				return ResponseEntity
						.ok(Map.of("status", "success", "message", "No tasks found with the given status."));
			}

			return ResponseEntity.ok(Map.of("status", "success", "data", tasks));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error while filtering tasks: " + e.getMessage()));
		}
	}

	
}