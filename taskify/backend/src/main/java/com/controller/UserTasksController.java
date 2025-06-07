package com.controller;

import java.time.LocalDateTime;
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
import org.springframework.web.bind.annotation.RestController;

import com.dto.AdminDTO;
import com.model.Admin;
import com.model.Tasks;
import com.model.Users;
import com.repos.AdminRepo;
import com.repos.TasksRepo;
import com.repos.UserRepo;
import com.service.TaskOverDueScheduler;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/tasks")
public class UserTasksController {

	@Autowired
	private TasksRepo tasksRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private AdminRepo adminRepo;

	@Autowired
	private TaskOverDueScheduler taskOverdue;

//	=====Get All admins ======
	@GetMapping("/admins-list")
	public ResponseEntity<?> getallAdminsList(HttpSession session) {
		try {
			List<AdminDTO> alladmins = adminRepo.fetchAllAdminsWithIdNameEmail();
			return ResponseEntity.ok(alladmins);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error in fetching admin list: " + e.getMessage()));
		}
	}

//	=====Get All Tasks of user ======
	@GetMapping("/getall/user{id}")
	public ResponseEntity<?> getAllTaskOfUser(@PathVariable int id, HttpSession session) {
		String role = (String) session.getAttribute("role");
		Integer sessId = (Integer) session.getAttribute("user_id");

		try {
			if (role == null || sessId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			if ("User".equalsIgnoreCase(role) && sessId == id) {
				List<String> excludedStatuses = List.of("Completed", "Overdue", "Done", "Redo");
				List<Tasks> userTasks = tasksRepo.findByUserUserIdAndStatusNotInIgnoreCaseOrderByCreatedTimeDesc(id,
						excludedStatuses);
				return ResponseEntity.ok(Map.of("status", "success", "data", userTasks));
			}

			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("status", "error", "message", "Access denied"));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error fetching tasks: " + e.getMessage()));
		}
	}

//	===== creating task for a user ======
	@PostMapping("/create")
	public ResponseEntity<?> createTask(@RequestBody Tasks task, HttpSession session) {
		try {
			Object userIdObj = session.getAttribute("user_id");
			Object roleObj = session.getAttribute("role");

			if (userIdObj == null || roleObj == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			int loggedInUserId = (int) userIdObj;
			String role = roleObj.toString();

			if ("USER".equalsIgnoreCase(role)) {
				Optional<Users> user = userRepo.findById(loggedInUserId);
				Optional<Admin> assignedAdmin = adminRepo.findById(task.getAssignedAdminId());

				if (user.isEmpty()) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(Map.of("status", "error", "message", "User Not Found."));
				}

				if (assignedAdmin.isEmpty()) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(Map.of("status", "error", "message", "Assigned Admin doesn't exist."));
				}

				String requestedStatus = task.getStatus();
				if ("Completed".equalsIgnoreCase(requestedStatus) || "Redo".equalsIgnoreCase(requestedStatus)
						|| "Overdue".equalsIgnoreCase(requestedStatus)) {
					task.setStatus("Todo");
					task.setIsCompletedByUser(false);
				}
				if (("Done").equalsIgnoreCase(requestedStatus)) {
					task.setStatus(requestedStatus);
					task.setIsCompletedByUser(true);
				} else {
					task.setIsCompletedByUser(false);
				}

				task.setIsApprovedByAdmin(false);
				task.setUser(user.get());
				task.setAdmin(assignedAdmin.get());
				task.setCreatedTime(LocalDateTime.now());

				tasksRepo.save(task);

				return ResponseEntity.status(HttpStatus.CREATED)
						.body(Map.of("status", "success", "message", "Task Created Successfully!", "data", task));

			} else if ("ADMIN".equalsIgnoreCase(role)) {

				Optional<Admin> admin = adminRepo.findById(loggedInUserId);
				Optional<Users> assignedUser = userRepo.findById(task.getAssignedToUserId());

				if (admin.isEmpty()) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(Map.of("status", "error", "message", "User Not Found."));
				}

				if (assignedUser.isEmpty()) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(Map.of("status", "error", "message", "Assigned user doesn't exist."));
				}

				String requestedStatus = task.getStatus();
				if ("Redo".equalsIgnoreCase(requestedStatus) || "Overdue".equalsIgnoreCase(requestedStatus)) {
					task.setStatus("Todo");
					task.setIsCompletedByUser(false);
					task.setIsApprovedByAdmin(false);
				}
				if ("Done".equalsIgnoreCase(requestedStatus)) {
					task.setStatus(requestedStatus);
					task.setIsCompletedByUser(true);
					task.setIsApprovedByAdmin(false);
				} else if ("Completed".equalsIgnoreCase(requestedStatus)) {
					task.setStatus(requestedStatus);
					task.setIsCompletedByUser(true);
					task.setIsApprovedByAdmin(true);
				}
				task.setUser(assignedUser.get());
				task.setAdmin(admin.get());
				task.setAssignedAdminId(admin.get().getAdminId());
				task.setCreatedTime(LocalDateTime.now());

				tasksRepo.save(task);

				return ResponseEntity.status(HttpStatus.CREATED)
						.body(Map.of("status", "success", "message", "Task Created Successfully!", "data", task));

			}

			else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("status", "error", "message", "Access Denied"));
			}

		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Unexpected error: " + ex.getMessage()));
		}
	}

//	=====Get TAsk by taskId/ ======
	@GetMapping("/get/{taskid}")
	public ResponseEntity<?> getTaskByID(@PathVariable Long taskid, HttpSession session) {
		String role = (String) session.getAttribute("role");
		Integer sessId = (Integer) session.getAttribute("user_id");

		if (role == null || sessId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("status", "error", "message", "User not logged in or session expired."));
		}

		try {
			Optional<Tasks> taskOptional = tasksRepo.findById(taskid);

			if (taskOptional.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("status", "error", "message", "Task not found."));
			}

			Tasks task = taskOptional.get();
			if ("USER".equalsIgnoreCase(role) && (task.getUser().getUserId() == (sessId))) {

				return ResponseEntity.ok(Map.of("status", "success", "data", task));

			} else if ("ADMIN".equalsIgnoreCase(role) && (task.getAssignedAdminId() == sessId)) {

				return ResponseEntity.ok(Map.of("status", "success", "data", task));
			} else {
				return ResponseEntity.ok(Map.of("status", "error", "message", "Access denied"));
			}

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error retrieving task: " + e.getMessage()));
		}
	}

//	=====Upadting tAsk by taskId ====
	@PostMapping("/update/task={id}")
	public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Tasks task, HttpSession session) {
		Object userIdObj = session.getAttribute("user_id");
		Object roleObj = session.getAttribute("role");

		if (userIdObj == null || roleObj == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("status", "error", "message", "User not logged in or session expired."));
		}

		int loggedUserID = (int) userIdObj;
		String role = roleObj.toString();

		try {
			Optional<Tasks> existingTaskOpt = tasksRepo.findById(id);
			if (existingTaskOpt.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("status", "error", "message", "Task not found."));
			}

			Tasks existingTask = existingTaskOpt.get();

			// USER
			if ("USER".equalsIgnoreCase(role) && existingTask.getUser().getUserId() == loggedUserID) {

				Optional<Users> userOpt = userRepo.findById(loggedUserID);
				Optional<Admin> adminOpt = adminRepo.findById(task.getAssignedAdminId());

				if (userOpt.isEmpty()) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(Map.of("status", "error", "message", "User not found."));
				}
				if (adminOpt.isEmpty()) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(Map.of("status", "error", "message", "Assigned admin not found."));
				}
				String status = task.getStatus();
				if ("Done".equalsIgnoreCase(status)) {
					existingTask.setStatus("Done");
					existingTask.setIsCompletedByUser(true);
				} else if ("Completed".equalsIgnoreCase(status)) {
					existingTask.setStatus("Todo");
					existingTask.setIsCompletedByUser(false);
				} else {
					existingTask.setStatus(status);
					existingTask.setIsCompletedByUser(false);
				}

				existingTask.setIsApprovedByAdmin(false);
				existingTask.setTitle(task.getTitle());
				existingTask.setDescription(task.getDescription());
				existingTask.setPriority(task.getPriority());
				existingTask.setCategory(task.getCategory());
				existingTask.setDueDate(task.getDueDate());
				existingTask.setUserRemark(task.getUserRemark());
				existingTask.setUser(userOpt.get());
				existingTask.setAdmin(adminOpt.get());
				existingTask.setAssignedAdminId(adminOpt.get().getAdminId());

				tasksRepo.save(existingTask);
				return ResponseEntity.ok(Map.of("status", "success", "message", "Task updated successfully."));
			}

			// ADMIN
			else if ("ADMIN".equalsIgnoreCase(role)) {
				Optional<Admin> adminOpt = adminRepo.findById(loggedUserID);
				Optional<Users> userOpt = userRepo.findById(existingTask.getAssignedToUserId());

				if (adminOpt.isEmpty()) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(Map.of("status", "error", "message", "Admin not found."));
				}

				if (userOpt.isEmpty()) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(Map.of("status", "error", "message", "Assigned user doesn't exist."));
				}

				String status = task.getStatus();
				if ("Redo".equalsIgnoreCase(status) || "Overdue".equalsIgnoreCase(status)) {
					task.setStatus("Todo");
					task.setIsCompletedByUser(false);
					task.setIsApprovedByAdmin(false);
				}
				if ("Done".equalsIgnoreCase(status)) {
					task.setStatus(status);
					task.setIsCompletedByUser(true);
					task.setIsApprovedByAdmin(false);
				} else if ("Completed".equalsIgnoreCase(status)) {
					task.setStatus(status);
					task.setIsCompletedByUser(true);
					task.setIsApprovedByAdmin(true);
				}
//	            return ResponseEntity.ok(existingTask);

				existingTask.setIsCompletedByUser(false);
				existingTask.setIsApprovedByAdmin(false);
				existingTask.setTitle(task.getTitle());
				existingTask.setDescription(task.getDescription());
				existingTask.setPriority(task.getPriority());
				existingTask.setCategory(task.getCategory());
				existingTask.setDueDate(task.getDueDate());
				existingTask.setAdminRemark(task.getAdminRemark());

				existingTask.setUser(userOpt.get());
				existingTask.setAdmin(adminOpt.get());
				existingTask.setAssignedAdminId(adminOpt.get().getAdminId());

				tasksRepo.save(existingTask);
				return ResponseEntity.ok(Map.of("status", "success", "message", "Task updated successfully."));
			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("status", "error", "message", "Access denied."));
			}

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "An error occurred: " + e.getMessage()));
		}
	}

//	delete task
	@DeleteMapping("/delete/{taskid}")
	public ResponseEntity<?> deleteTask(@PathVariable Long taskid, HttpSession session) {
		String role = (String) session.getAttribute("role");
		Integer sessId = (Integer) session.getAttribute("user_id");

		if (role == null || sessId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("status", "error", "message", "User not logged in or session expired."));
		}

		try {
			Optional<Tasks> taskOpt = tasksRepo.findById(taskid);
			if (taskOpt.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("status", "error", "message", "Task not found."));
			}

			Tasks task = taskOpt.get();

			if ("USER".equalsIgnoreCase(role)) {
				if (task.getUser().getUserId() != sessId) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN)
							.body(Map.of("status", "error", "message", "Not Allowed to delete this task."));
				}
			}

			else if ("ADMIN".equalsIgnoreCase(role)) {
				boolean isCreator = task.getAdmin().getAdminId() == sessId;
				boolean isAssigned = task.getAssignedAdminId() != null && task.getAssignedAdminId() == sessId;

				if (!isCreator && !isAssigned) {
					return ResponseEntity.status(HttpStatus.FORBIDDEN)
							.body(Map.of("status", "error", "message", "Not Allowed to delete this task."));
				}
			}

			else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("status", "error", "message", "Access Denied"));
			}

			if((task.getIsApprovedByAdmin() == true && "Completed".equalsIgnoreCase(task.getStatus()) )|| "Redo".equalsIgnoreCase(task.getStatus())) {
				return ResponseEntity.ok(Map.of("status", "success", "message", "Task cannot be deleted!"));
			}
			tasksRepo.delete(task);
			return ResponseEntity.ok(Map.of("status", "success", "message", "Task deleted successfully!"));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "An error occurred: " + e.getMessage()));
		}
	}

//	===== Getting tAsk of Done status ====
	@GetMapping("/waitingforapproval/user{id}")
	public ResponseEntity<?> getAllDoneTasksOfUser(@PathVariable int id, HttpSession session) {
		String role = (String) session.getAttribute("role");
		Integer sessId = (Integer) session.getAttribute("user_id");

		try {
			if (role == null || sessId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			if (tasksRepo.findByUserUserId(id) != null) {
				List<Tasks> userTasks = tasksRepo.findByUserUserIdAndStatusOrderByCreatedTimeDesc(id, "Done");
				return ResponseEntity.ok(Map.of("status", "success", "data", userTasks));
			}
			return ResponseEntity.ok(Map.of("status", "success", "message", "No task found"));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error fetching tasks: " + e.getMessage()));
		}
	}

//	===== getting tAsk of Completed status ====
	@GetMapping("/approved/user{id}")
	public ResponseEntity<?> getAllApprovedTasksOfUser(@PathVariable int id, HttpSession session) {
		String role = (String) session.getAttribute("role");
		Integer sessId = (Integer) session.getAttribute("user_id");

		try {
			if (role == null || sessId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}
				List<Tasks> userTasks = tasksRepo.findByUserUserIdAndIsApprovedByAdminTrueAndIsCompletedByUserTrueAndStatusOrderByCreatedTimeDesc(id, "Completed");
				return ResponseEntity.ok(Map.of("status", "success", "data", userTasks));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error fetching tasks: " + e.getMessage()));
		}
	}

//	===== Getting overdue tasks ====
	@GetMapping("/getoverdue/user{id}")
	public ResponseEntity<?> getOverdueTaskOfUser(@PathVariable int id, HttpSession session) {
		String role = (String) session.getAttribute("role");
		Integer sessId = (Integer) session.getAttribute("user_id");

		try {
			if (role == null || sessId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			if ("User".equalsIgnoreCase(role) && sessId == id) {
				List<Tasks> userTasks = taskOverdue.markUserTasksOverdue(id);
				return ResponseEntity.ok(Map.of("status", "success", "data", userTasks));
			}

			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("status", "error", "message", "Access denied"));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error fetching tasks: " + e.getMessage()));
		}
	}

//	get redo tasks
	@GetMapping("/getredo/user{id}")
	public ResponseEntity<?> getRedoTaskOfUser(@PathVariable int id, HttpSession session) {
		String role = (String) session.getAttribute("role");
		Integer sessId = (Integer) session.getAttribute("user_id");

		try {
			if (role == null || sessId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "User not logged in or session expired."));
			}

			if ("User".equalsIgnoreCase(role) && sessId == id) {
				List<Tasks> userTasks = tasksRepo.findByUserUserIdAndStatusOrderByCreatedTimeDesc(id , "Redo");
				return ResponseEntity.ok(Map.of("status", "success", "data", userTasks));
			}

			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("status", "error", "message", "Access denied"));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error fetching tasks: " + e.getMessage()));
		}
	}

}