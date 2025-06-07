package com.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.dto.UserDTO;
import com.model.Admin;
import com.model.Tasks;
import com.model.Users;
import com.repos.AdminRepo;
import com.repos.TasksRepo;
import com.repos.UserRepo;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/admin")
public class AdminTasksController {

	@Autowired
	private AdminRepo adminRepo;
	@Autowired
	private UserRepo usersRepository;

	@Autowired
	private TasksRepo taskRepo;

	// fetch all users
	@GetMapping("/users-list")
	public ResponseEntity<?> getAllUsers(HttpSession session) {

		try {
			String role = (String) session.getAttribute("role");
			Integer loggedId = (Integer) session.getAttribute("user_id");

			if (role == null || loggedId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "Not logged in or session expired."));
			}

			List<UserDTO> users = usersRepository.fetchAllUserDetail();
			return ResponseEntity.ok(Map.of("status", "success", "data", users));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error in fetching: " + e.getMessage()));
		}
	}

//	fetch all admin related task
	@GetMapping("/assigned-tasks/admin={id}")
	public ResponseEntity<?> getAllAdminCreatedtasks(HttpSession session, @PathVariable int id) {

		try {
			String role = (String) session.getAttribute("role");
			Integer loggedId = (Integer) session.getAttribute("user_id");

			if (role == null || loggedId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "Not logged in or session expired."));
			}

			Optional<Admin> adminFound = adminRepo.findById(id);

			if (adminFound.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("status", "error", "message", "Admin doesn't exists!!!"));
			}
			List<Tasks> assignedtask = taskRepo.findTasksAssignedToUsersByAdmin(id);
			return ResponseEntity.ok(Map.of("status", "success", "data", assignedtask));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error in fetching: " + e.getMessage()));
		}
	}

	@GetMapping("/all-pending-for-approval")
	public ResponseEntity<?> fetchAllPendingApprovalTasks(@RequestParam int adminid, HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer userId = (Integer) session.getAttribute("user_id");

			if (role == null || userId == null || userId != adminid) {
//				System.out.println("+++++++++++++2" + adminid);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			Optional<Admin> adminFound = adminRepo.findById(adminid);
			if (adminFound.isEmpty()) {
//				System.out.println("+++++++++++++3" + adminFound);
				return ResponseEntity.ok(Map.of("status", "error", "message", "Admin Not Found"));
			}

			List<Tasks> tasks = taskRepo
					.findByAdminAdminIdAndAssignedAdminIdAndStatusIgnoreCaseAndIsApprovedByAdminFalse(adminid, adminid,
							"done");
//			System.out.println("+++++++++++++4" + tasks);

			return ResponseEntity.ok(Map.of("status", "success", "data", tasks));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error while filtering tasks: " + e.getMessage()));
		}
	}

	@GetMapping("/all-approved-tasks")
	public ResponseEntity<?> fetchAllApprovedTasks(@RequestParam int adminid, HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer userId = (Integer) session.getAttribute("user_id");

			if (role == null || userId == null || userId != adminid) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			Optional<Admin> adminFound = adminRepo.findById(adminid);
			if (adminFound.isEmpty()) {
				return ResponseEntity.ok(Map.of("status", "error", "message", "Admin Not Found"));
			}

			List<Tasks> tasks = taskRepo
					.findByAdminAdminIdAndAssignedAdminIdAndStatusIgnoreCaseAndIsApprovedByAdminTrueAndIsCompletedByUserTrue(
							adminid, adminid, "Completed");
			return ResponseEntity.ok(Map.of("status", "success", "data", tasks));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error while filtering tasks: " + e.getMessage()));
		}
	}

	@PostMapping("/approve-task/taskId={taskId}")
	public ResponseEntity<?> approveTask(@PathVariable("taskId") Long taskId, @RequestBody Tasks task,
			HttpSession session) {

		try {
			String role = (String) session.getAttribute("role");
			Integer userId = (Integer) session.getAttribute("user_id");

			if (role == null || userId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			Optional<Tasks> existingTaskOpt = taskRepo.findById(taskId);
			if (existingTaskOpt.isEmpty()) {
				return ResponseEntity.ok(Map.of("status", "error", "message", "Task not found."));
			}

			Tasks existingTask = existingTaskOpt.get();

			if (!userId.equals(existingTask.getAdmin().getAdminId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("status", "error", "message", "You are not authorized to approve this task."));
			}

			List<Tasks> pendingTasks = taskRepo
					.findByAdminAdminIdAndAssignedAdminIdAndIsCompletedByUserTrueAndIsApprovedByAdminFalse(userId,
							userId);
			if (pendingTasks.isEmpty()) {
				return ResponseEntity
						.ok(Map.of("status", "error", "message", "No tasks pending approval for this admin."));
			}

			existingTask.setStatus(task.getStatus());
			existingTask.setAdminRemark(task.getAdminRemark());

			if (task.getStatus().equalsIgnoreCase("Redo")) {
				existingTask.setIsApprovedByAdmin(false);
				existingTask.setIsCompletedByUser(false);
			} else {
				existingTask.setIsApprovedByAdmin(true);
			}

			Tasks updatedTask = taskRepo.save(existingTask);

			return ResponseEntity.ok(Map.of("status", "success", "message",
					task.getStatus().equalsIgnoreCase("Redo") ? "Task sent back for redo successfully."
							: "Task approved successfully.",
					"task", updatedTask));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error : " + e.getMessage()));
		}
	}

	@DeleteMapping("/user-delete/{userID}")
	public ResponseEntity<?> deleteEmployeeAndtasks(@PathVariable int userID, HttpSession session) {
		String role = (String) session.getAttribute("role");
		Integer sessId = (Integer) session.getAttribute("user_id");

		if (role == null || sessId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("status", "error", "message", "User not logged in or session expired."));
		}

		try {
			Optional<Users> userFound = usersRepository.findById(userID);
			if (userFound.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("status", "error", "message", "User not found."));
			}

			Users user = userFound.get();
			List<Tasks> userTasks = taskRepo.findByUserUserId(userID);
			if(!userTasks.isEmpty()) {
				taskRepo.deleteAll(userTasks);
			}
			usersRepository.delete(user);
			
			return ResponseEntity.ok(Map.of("status", "success", "message", "User deleted successfully!"));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "An error occurred: " + e.getMessage()));
		}
	}
}