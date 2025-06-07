package com.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.model.Admin;
import com.model.Users;
import com.repos.AdminRepo;
import com.repos.UserRepo;
import com.service.AuthRequestDTO;
import com.service.SendMailService;

import jakarta.servlet.http.HttpSession;

@RestController
public class AuthController {

	@Autowired
	private UserRepo repo;

	@Autowired
	private AdminRepo adminRepo;
	@Autowired
	private SendMailService emailService;

	@Autowired
	private PasswordEncoder passwordEncode;

	public AuthController(UserRepo repo, AdminRepo adminRepo, SendMailService emailService,
			PasswordEncoder passwordEncoder) {
		this.repo = repo;
		this.adminRepo = adminRepo;
		this.emailService = emailService;
		this.passwordEncode = passwordEncoder;
	}

//	=========== Signup
	@PostMapping("/register")
	public ResponseEntity<Object> resgisterUser(@RequestBody Users user, HttpSession session) {
		if (repo.existsByEmail(user.getEmail())) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("status", "error", "message", "Email Already Exists!!!"));
		}
		try {
			user.setPassword(passwordEncode.encode(user.getPassword()));
			user.setRole("user");
			Users saveduser = repo.save(user);

			session.setAttribute("user_id", saveduser.getUserId());
			session.setAttribute("role", saveduser.getRole());

			return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", "ok", "message",
					"Registered Successfully!!!", "role", saveduser.getRole(), "userId", saveduser.getUserId()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", e.getMessage()));
		}
	}

	// =========== Login
	@PostMapping("/login")
	public ResponseEntity<Object> loginUser(@RequestBody AuthRequestDTO dto, HttpSession session) {
		try {
			Optional<Users> userOpt = repo.findByEmail(dto.getLoginEmail());
			Optional<Admin> adminOpt = adminRepo.findByAdminEmail(dto.getLoginEmail());

			if (userOpt.isPresent()) {

				Users found = userOpt.get();
				if (!passwordEncode.matches(dto.getLoginPassword(), found.getPassword())) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
							.body(Map.of("status", "error", "message", "Incorrect Password!!!"));
				}
				session.setAttribute("user_id", found.getUserId());
				session.setAttribute("role", found.getRole());
//				System.out.println("Login session ID: " + session.getId());

				return ResponseEntity.ok(Map.of("status", "success", "message", "Login Succesfully!!!", "role",
						found.getRole(), "userId", found.getUserId()));

			} else if (adminOpt.isPresent()) {

				Admin adminFound = adminOpt.get();
				if (!passwordEncode.matches(dto.getLoginPassword(), adminFound.getPassword())) {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
							.body(Map.of("status", "error", "message", "Incorrect Password!!!"));
				}

				session.setAttribute("user_id", adminFound.getAdminId());
				session.setAttribute("role", adminFound.getRole());

				return ResponseEntity.ok(Map.of("status", "success", "message", "Login Succesfully!!!", "role",
						adminFound.getRole(), "userId", adminFound.getAdminId()));

			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("status", "error", "message", "Email Not Registered!!!"));
			}

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", e.getMessage()));
		}

	}

//	=========== Forgot password
	@PostMapping("/forgot-password")
	public ResponseEntity<Object> forgotpassword(@RequestBody Map<String, Object> resBody) {

		String email = String.valueOf(resBody.get("email")).trim();
		String host = String.valueOf(resBody.get("host")).trim();

		Optional<Users> userfound = repo.findByEmail(email);
		Optional<Admin> adminFound = adminRepo.findByAdminEmail(email);
		String token = UUID.randomUUID().toString();
		try {

			if (userfound.isPresent()) {
				Users user = userfound.get();
				user.setResetToken(token);
				user.setTokenExpiry(LocalDateTime.now().plusMinutes(10));
				repo.save(user);
				String reset_link = host + "/ResetPassword.html?token=" + token;

				emailService.sendResetPasswordEmail(user.getFirstname(), email, reset_link);

				return ResponseEntity.status(HttpStatus.OK)
						.body(Map.of("status", "success", "message", "Reset link sent to your email"));
			} else if (adminFound.isPresent()) {

				Admin admin = adminFound.get();
				admin.setResetToken(token);
				admin.setTokenExpiry(LocalDateTime.now().plusMinutes(10));
				adminRepo.save(admin);
				String reset_link = host + "/ResetPassword.html?token=" + token;

				emailService.sendResetPasswordEmail(admin.getFullname(), email, reset_link);

				return ResponseEntity.status(HttpStatus.OK)
						.body(Map.of("status", "success", "message", "Reset link sent to your email"));

			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("status", "error", "message", "Email Not Registered!!!"));
			}

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", e.getMessage()));
		}
	}

//	=========== Reset password
	@PostMapping("/reset-password")
	public ResponseEntity<Object> resetPassword(@RequestBody Map<String, String> response) {
		try {
			String token = response.get("token");
			String newPassword = response.get("newpassword");

			Optional<Users> userOtp = repo.findByResetToken(token);
			Optional<Admin> adminFound = adminRepo.findByResetToken(token);

			if (userOtp.isPresent()) {
				Users user = userOtp.get();
				if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(Map.of("status", "error", "message", "Token has expired"));
				}
				user.setPassword(passwordEncode.encode(newPassword));
				user.setResetToken(null);
				user.setTokenExpiry(null);
				repo.save(user);

				return ResponseEntity.ok(Map.of("status", "success", "message", "Password reset successfully"));

			} else if (adminFound.isPresent()) {
				Admin admin = adminFound.get();
				if (admin.getTokenExpiry().isBefore(LocalDateTime.now())) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST)
							.body(Map.of("status", "error", "message", "Token has expired"));
				}

				admin.setPassword(passwordEncode.encode(newPassword));
				admin.setResetToken(null);
				admin.setTokenExpiry(null);
				adminRepo.save(admin);

				return ResponseEntity.ok(Map.of("status", "success", "message", "Password reset successfully"));

			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("status", "error", "message", "Invalid or expired token"));
			}

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", e.getMessage()));
		}
	}

//	===== logout
	@GetMapping("/logout")
	public ResponseEntity<?> logout(HttpSession session) {
		session.invalidate();
		return ResponseEntity.ok(Map.of("status", "ok", "message", "Logged Out Successfully!!!"));
	}

}
