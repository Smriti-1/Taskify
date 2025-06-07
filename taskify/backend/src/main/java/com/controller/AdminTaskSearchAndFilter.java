package com.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.model.Admin;
import com.model.Tasks;
import com.model.Users;
import com.repos.AdminRepo;
import com.repos.TasksRepo;
import com.repos.UserRepo;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/admin")
public class AdminTaskSearchAndFilter {

	@Autowired
	private AdminRepo adminRepo;

	@Autowired
	private TasksRepo taskRepo;

	@Autowired
	private UserRepo usersRepo;
	
//	/review-assigned-tasks-search
	@GetMapping("/review-assigned-tasks-search")
	public ResponseEntity<?> searchReviewApprovedTasks(@RequestParam String keyword, @RequestParam int adminId, @RequestParam String status , HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer loggedId = (Integer) session.getAttribute("user_id");

			if (role == null || loggedId == null || adminId != loggedId) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "Not logged in or session expired."));
			}
			Optional<Admin> adminFound = adminRepo.findById(adminId);

			if (adminFound.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("status", "error", "message", "Admin doesn't exists!!!"));
			}
			List<Tasks> searchedTask = taskRepo.searchByAdminAndTitleOrDescriptionForReviewAndApproved(adminId, keyword , status);
			return ResponseEntity.ok(Map.of("status", "success", "data", searchedTask));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error in fetching: " + e.getMessage()));
		}
	}
	
	@GetMapping("/assigned-tasks-search")
	public ResponseEntity<?> searchTask(@RequestParam String keyword, @RequestParam int adminId, HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer loggedId = (Integer) session.getAttribute("user_id");

			if (role == null || loggedId == null || adminId != loggedId) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "Not logged in or session expired."));
			}
			Optional<Admin> adminFound = adminRepo.findById(adminId);

			if (adminFound.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("status", "error", "message", "Admin doesn't exists!!!"));
			}
			List<Tasks> searchedTask = taskRepo.searchByAdminAndTitleOrDescription(adminId, keyword);

			return ResponseEntity.ok(Map.of("status", "success", "data", searchedTask));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error in fetching: " + e.getMessage()));
		}
	}

//	filetr by sort
	@GetMapping("/filter-by-sort")
	public ResponseEntity<?> filterTasksBasedOnSort(@RequestParam String sort, @RequestParam String taskStatus,
			HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer adminId = (Integer) session.getAttribute("user_id");

			if (role == null || adminId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			List<String> statusList;
			if ("assigned".equalsIgnoreCase(taskStatus)) {
				statusList = List.of("Todo", "Inprogress", "Overdue", "Redo", "Completed", "Done");
			} else if ("done".equalsIgnoreCase(taskStatus)) {
				statusList = List.of("Done");
			} else if ("completed".equalsIgnoreCase(taskStatus)) {
				statusList = List.of("Completed");
			} else {
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
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("status", "error", "message", "Invalid sort option."));
			}

			List<Tasks> tasks = taskRepo.findByAdminAdminIdAndAssignedToUserIdIsNotNullAndStatusIn(adminId, sortOrder,
					statusList);

			return ResponseEntity.ok(Map.of("status", "success", "data", tasks));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", e.getMessage()));
		}
	}

	@GetMapping("/filter-by-priority")
	public ResponseEntity<?> filterTasksByPriority(@RequestParam String priority, @RequestParam String taskStatus,
			HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer userId = (Integer) session.getAttribute("user_id");

			if (role == null || userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}
			List<String> statusList;
			if ("assigned".equalsIgnoreCase(taskStatus)) {
				statusList = List.of("Todo", "Inprogress", "Overdue", "Redo", "Completed", "Done");
			} else if ("done".equalsIgnoreCase(taskStatus)) {
				statusList = List.of("Done");
			} else if ("completed".equalsIgnoreCase(taskStatus)) {
				statusList = List.of("Completed");
			} else {
				return ResponseEntity.ok(Map.of("status", "error", "message", "Invalid filter."));
			}

			List<Tasks> tasks = taskRepo
					.findByAdminAdminIdAndAssignedToUserIdIsNotNullAndPriorityIgnoreCaseAndStatusInIgnoreCase(userId,
							priority, statusList);

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
	public ResponseEntity<?> filterTasksByStatus(@RequestParam String status,
			@RequestParam(required = false) String taskStatus, HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer userId = (Integer) session.getAttribute("user_id");

			if (role == null || userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			List<Tasks> tasks = taskRepo
					.findByAdminAdminIdAndAssignedToUserIdIsNotNullAndStatusIgnoreCaseOrderByCreatedTimeDesc(userId,
							status);

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

	@GetMapping("/filter-by-email")
	public ResponseEntity<?> filterTasksByStatus(@RequestParam String email, @RequestParam int empId,
			@RequestParam String taskStatus, HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer userId = (Integer) session.getAttribute("user_id");

			if (role == null || userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}
			List<String> statusList;
			if ("assigned".equalsIgnoreCase(taskStatus)) {
				statusList = List.of("Todo", "Inprogress", "Overdue", "Redo", "Completed", "Done");
			} else if ("done".equalsIgnoreCase(taskStatus)) {
				statusList = List.of("Done");
			} else if ("completed".equalsIgnoreCase(taskStatus)) {
				statusList = List.of("Completed");
			} else {
				return ResponseEntity.ok(Map.of("status", "error", "message", "Invalid filter."));
			}

			List<Tasks> tasks = taskRepo
					.findByAdminAdminIdAndAssignedToUserIdAndUserEmailIgnoreCaseAndStatusInOrderByCreatedTimeDesc(userId, empId, email ,statusList);

			if (tasks.isEmpty()) {
				return ResponseEntity
						.ok(Map.of("status", "success", "message", "No tasks have been assigned to this user."));
			}

			return ResponseEntity.ok(Map.of("status", "success", "data", tasks));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error while filtering tasks: " + e.getMessage()));
		}
	}

	@GetMapping("/filter-users")
	public ResponseEntity<?> filterUsers(@RequestParam String sort, HttpSession session) {
	    try {
	        String role = (String) session.getAttribute("role");
	        Integer adminId = (Integer) session.getAttribute("user_id");

	        if (role == null || adminId == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body(Map.of("status", "error", "message", "User not logged in or session expired."));
	        }

	        Sort sortOrder;

	        switch (sort.toLowerCase()) {
	            case "newest" -> sortOrder = Sort.by(Sort.Direction.DESC, "registerDate");
	            case "oldest" -> sortOrder = Sort.by(Sort.Direction.ASC, "registerDate");
	            case "name-asc" -> sortOrder = Sort.by(Sort.Direction.ASC, "firstname");
	            case "name-desc" -> sortOrder = Sort.by(Sort.Direction.DESC, "firstname");
	            case "email-esc" -> sortOrder = Sort.by(Sort.Direction.ASC, "email");
	            case "email-desc" -> sortOrder = Sort.by(Sort.Direction.DESC, "email");
	            case "phn-asc" -> sortOrder = Sort.by(Sort.Direction.ASC, "phonenumber");
	            case "phn-desc" -> sortOrder = Sort.by(Sort.Direction.DESC, "phonenumber");
	            default -> {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                        .body(Map.of("status", "error", "message", "Invalid sort option."));
	            }
	        }

	        List<Users> sortedUsers = usersRepo.findAll(sortOrder);
	        return ResponseEntity.ok(Map.of("status", "success", "data", sortedUsers));

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("status", "error", "message", "Error while filtering users: " + e.getMessage()));
	    }
	}

	@GetMapping("/search-users")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword , HttpSession session) {
        try {
        	String role = (String) session.getAttribute("role");
	        Integer adminId = (Integer) session.getAttribute("user_id");

	        if (role == null || adminId == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body(Map.of("status", "error", "message", "User not logged in or session expired."));
	        }
	        
            List<Users> result = usersRepo.searchByNameEmailOrPhone(keyword);
            if (result.isEmpty()) {
                return ResponseEntity.ok(Map.of("status", "success", "message", "No users found."));
            }
            
            return ResponseEntity.ok(Map.of("status", "success", "data", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "Search failed: " + e.getMessage()));
        }
    }
	
}