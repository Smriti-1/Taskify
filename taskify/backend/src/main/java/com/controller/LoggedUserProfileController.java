package com.controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dto.AdminDTO;
import com.dto.UserDTO;
import com.model.Admin;
import com.model.Users;
import com.repos.AdminRepo;
import com.repos.UserRepo;

import jakarta.servlet.http.HttpSession;

@RestController
public class LoggedUserProfileController {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private AdminRepo adminRepo;

	@Autowired
	private PasswordEncoder passwordEncode;

	// GET Profile Endpoint
	@PostMapping("/user/profile/{id}")
	public ResponseEntity<?> getProfile(@PathVariable int id, HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer loggedUserID = (Integer) session.getAttribute("user_id");

			if (role == null || loggedUserID == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "Unauthorized access."));
			}

			if (("USER".equalsIgnoreCase(role)) && (loggedUserID == id)) {

				UserDTO user = userRepo.fetchUserDetailFromID(loggedUserID);
				String base64Img = user.getProfileimg() != null
						? "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(user.getProfileimg())
						: null;

				Map<String, Object> profileData = new HashMap<>();
				profileData.put("firstname", user.getFirstname());
				profileData.put("lastname", user.getLastname());
				profileData.put("email", user.getEmail());
				profileData.put("phonenumber", user.getPhoneNum());
				profileData.put("profileImg", base64Img);

				return ResponseEntity.ok(Map.of("status", "success", "data", profileData));

			} else if (("ADMIN".equalsIgnoreCase(role)) && (loggedUserID == id)) {

				AdminDTO admin = adminRepo.fetchAdminDetailFromID(loggedUserID);
				String base64Img = admin.getProfileImg() != null
						? "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(admin.getProfileImg())
						: null;

				Map<String, Object> profileData = new HashMap<>();
				profileData.put("fullname", admin.getFullname());
				profileData.put("email", admin.getAdminEmail());
				profileData.put("phonenumber", admin.getPhonenumber());
				profileData.put("profileImg", base64Img);

				return ResponseEntity.ok(Map.of("status", "success", "data", profileData));
			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("status", "error", "message", "Access denied or invalid role/ID!!!"));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", e.getMessage()));
		}
	}

	// POST Save Profile + Image for user
	@PostMapping(value = "/user/uploadProfileImage/{id}", consumes = "multipart/form-data")
	public ResponseEntity<?> uploadProfileImage(@PathVariable int id, @RequestParam String firstname,
			@RequestParam String lastname, @RequestParam String email, @RequestParam String phonenumber,
			@RequestParam(required = false) MultipartFile profileImg, HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer loggedUserID = (Integer) session.getAttribute("user_id");

			if (role == null || loggedUserID == null || loggedUserID != id) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "Unauthorized access"));
			} else if ("USER".equalsIgnoreCase(role)) {
				Users user = userRepo.findById(id).orElse(null);
				if (user == null) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(Map.of("status", "error", "message", "User not found"));
				}

				user.setFirstname(firstname);
				user.setLastname(lastname);
				user.setEmail(email);
				user.setPhonenumber(phonenumber);

				if (profileImg != null && !profileImg.isEmpty()) {
					user.setProfileImg(profileImg.getBytes());
				} else {
					user.setProfileImg(new byte[0]);
				}

				userRepo.save(user);

				return ResponseEntity.ok(Map.of("status", "success", "message", "Profile updated successfully"));
			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("status", "error", "message", "Access Denied"));
			}

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error: " + e.getMessage()));
		}
	}

	// POST Save Profile + Image for admin
	@PostMapping(value = "/admin/uploadProfileImage/{id}", consumes = "multipart/form-data")
	public ResponseEntity<?> uploadProfileImageAdmin(@PathVariable int id, @RequestParam String fullname,
			@RequestParam String email, @RequestParam String phonenumber,
			@RequestParam(required = false) MultipartFile profileImg, HttpSession session) {
		try {
			String role = (String) session.getAttribute("role");
			Integer loggedAdminId = (Integer) session.getAttribute("user_id");

			if (role == null || loggedAdminId == null || loggedAdminId != id) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "Unauthorized access"));
			} else if ("ADMIN".equalsIgnoreCase(role)) {
				Admin admin = adminRepo.findById(id).orElse(null);
				if (admin == null) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(Map.of("status", "error", "message", "Admin not found"));
				}

				admin.setFullname(fullname);
				admin.setAdminEmail(email);
				admin.setPhonenumber(phonenumber);

				if (profileImg != null && !profileImg.isEmpty()) {
					admin.setProfileImg(profileImg.getBytes());
				} else {
					admin.setProfileImg(new byte[0]);
				}

				adminRepo.save(admin);

				return ResponseEntity.ok(Map.of("status", "success", "message", "Profile updated successfully"));
			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("status", "error", "message", "Access Denied"));
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error: " + e.getMessage()));
		}
	}

	@PostMapping("/user={id}/change-password")
	public ResponseEntity<?> chnagePasswordthrProfile(@PathVariable int id, @RequestBody Map<String, String> response,
			HttpSession session) {

		String role = (String) session.getAttribute("role");
		Integer loggedUserID = (Integer) session.getAttribute("user_id");
		try {

			if (role == null || loggedUserID == null || loggedUserID != id) {

				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Map.of("status", "error", "message", "Unauthorized access"));
			} else if ("USER".equalsIgnoreCase(role)) {
				Users user = userRepo.findById(id).orElse(null);
				if (user == null) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(Map.of("status", "error", "message", "User not found"));
				}

				String oldPassword = response.get("oldpassword");
				String newPassword = response.get("newpassword");

				if (!passwordEncode.matches(oldPassword, user.getPassword())) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(Map.of("status", "error", "message", "Old password is incorrect"));
				}

				user.setPassword(passwordEncode.encode(newPassword));
				userRepo.save(user);
				session.invalidate();
				return ResponseEntity.ok(Map.of("status", "success", "message", "Password updated successfully"));

			} else if ("ADMIN".equalsIgnoreCase(role)) {
				Admin admin = adminRepo.findById(id).orElse(null);
				if (admin == null) {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body(Map.of("status", "error", "message", "User not found"));
				}

				String oldPassword = response.get("oldpassword");
				String newPassword = response.get("newpassword");

				if (!passwordEncode.matches(oldPassword, admin.getPassword())) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(Map.of("status", "error", "message", "Old password is incorrect"));
				}

				admin.setPassword(passwordEncode.encode(newPassword));
				adminRepo.save(admin);
				session.invalidate();
				return ResponseEntity.ok(Map.of("status", "success", "message", "Password updated successfully"));

			} else {
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("status", "error", "message", "Access Denied"));
			}

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Error: " + e.getMessage()));
		}

	}
}
