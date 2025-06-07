package com.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/taskify")
public class TaskifyDashboard {

	@Autowired
	private TasksRepo taskRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private AdminRepo adminRepo;

	@GetMapping("/user/dashboard")
	public ResponseEntity<Map<String, Object>> getDashboardData(@RequestParam int loggedUserId, HttpSession session) {
		try {
			Integer userId = (Integer) session.getAttribute("user_id");
			String role = (String) session.getAttribute("role");

			if (userId == null || role == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			if (loggedUserId == -1 || loggedUserId != userId) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "Access Denied"));
			}

			Optional<Users> userFound = userRepo.findById(userId);
			if (userFound.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User Not Found"));
			}

			long totalTasks = taskRepo.countByUserUserId(userId);
			long incompleteTasks = taskRepo.countByUserUserIdAndStatusIn(userId, List.of("Todo", "Inprogress"));
			long overdueTasks = taskRepo.countByUserUserIdAndDueDateBeforeAndStatusNot(userId, LocalDateTime.now(),
					"Done");
			long pendingApprovalTasks = taskRepo.countByUserUserIdAndIsApprovedByAdminFalseAndStatusIgnoreCase(userId,
					"Done");

			List<Tasks> highPriorityTasks = taskRepo.findTop5SortedByPriorityAndDueDate(userId);

			List<Tasks> redoHighPriorityTasks = taskRepo.findTop4ByUserUserIdAndStatusOrderByDueDateDesc(userId,
					"Redo");
			List<Tasks> todoInprogressTasks = taskRepo.findTop3ByUserUserIdAndStatusInOrderByDueDateDesc(userId,
					List.of("Todo", "Inprogress"));

			Map<String, Object> dashboardData = Map.of("totalTasks", totalTasks, "incompleteTasks", incompleteTasks,
					"overdueTasks", overdueTasks, "pendingApprovalTasks", pendingApprovalTasks, "highPriorityTasks",
					highPriorityTasks, "redoHighPriorityTasks", redoHighPriorityTasks, "todoInprogressTasks",
					todoInprogressTasks);

			return ResponseEntity.ok(dashboardData);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", e.getMessage()));
		}
	}

	@GetMapping("/admin/dashboard")
	public ResponseEntity<?> getAdminDashboardSummary(@RequestParam int adminId, HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer loggedAdminID = (Integer) session.getAttribute("user_id");

			if (role == null || loggedAdminID == null || !role.equalsIgnoreCase("admin") || adminId != loggedAdminID) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "Unauthorized access."));
			}

			Optional<Admin> adminFound = adminRepo.findById(loggedAdminID);
			if (adminFound.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("status", "error", "message", "No User Found."));
			}

			long totalAssignedTasks = taskRepo.countByAdminAdminIdAndAssignedToUserIdNotNull(adminId);
			long pendingApproval = taskRepo
					.countByAdminAdminIdAndAssignedToUserIdNotNullAndStatusIgnoreCaseAndIsCompletedByUserTrue(adminId,
							"Done");

			long todoInProgress = taskRepo.countByAdminAdminIdAndAssignedToUserIdNotNullAndStatusIn(adminId,
					List.of("Todo", "Inprogress"));

			long overdueTasks = taskRepo.countByAdminAdminIdAndAssignedToUserIdNotNullAndStatusIgnoreCase(adminId,
					"Overdue");
			List<Users> latestEmployees = userRepo.findTop4ByRoleOrderByRegisterDateDesc("user");

			List<Tasks> topRedoTasks = taskRepo
					.findTop5ByAdminAdminIdAndAssignedToUserIdNotNullAndStatusIgnoreCaseOrderByCreatedTimeDesc(adminId,
							"Redo");

			List<Tasks> latestAssignedTasks = taskRepo
					.findTop3ByAdminAdminIdAndAssignedToUserIdNotNullOrderByCreatedTimeDesc(adminId);

			return ResponseEntity.ok(Map.of("status", "success", "totalTasksAssigned", totalAssignedTasks,
					"pendingApprovalTasks", pendingApproval, "todoAndInprogressTasks", todoInProgress, "overdueTasks",
					overdueTasks, "redoTasks", topRedoTasks, "latestEmployees", latestEmployees, "latestAssignedTasks",
					latestAssignedTasks));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error: " + e.getMessage()));
		}
	}

}
