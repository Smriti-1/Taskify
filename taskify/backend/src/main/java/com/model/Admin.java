package com.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;

@Entity
@Scope("prototype")
@Component
public class Admin {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name ="admin_id")
	private int adminId;

	private String fullname;
	@Column(unique = true)
	private String adminEmail;

	private String password;
	private String role;


	@Column(name = "reset_token")
	private String resetToken;
	private LocalDateTime tokenExpiry;

	@Column(name = "creation_date")
	private LocalDate registerDate;
	private String phonenumber;

	@Lob
    private byte[] profileImg;

	public Admin() {
	}

	public Admin(int adminId, String fullname, String adminEmail, String password, String role, String resetToken,
			LocalDateTime tokenExpiry, LocalDate registerDate, String phonenumber, byte[] profileImg) {
		this.adminId = adminId;
		this.fullname = fullname;
		this.adminEmail = adminEmail;
		this.password = password;
		this.role = role;
		this.resetToken = resetToken;
		this.tokenExpiry = tokenExpiry;
		this.registerDate = registerDate;
		this.phonenumber = phonenumber;
		this.profileImg = profileImg;
	}

	@PrePersist
	private void onCreate() {
		this.registerDate = LocalDate.now(); // Sets the current date (without time)
	}

	public int getAdminId() {
		return adminId;
	}

	public void setAdminId(int adminId) {
		this.adminId = adminId;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getResetToken() {
		return resetToken;
	}

	public void setResetToken(String resetToken) {
		this.resetToken = resetToken;
	}

	public LocalDateTime getTokenExpiry() {
		return tokenExpiry;
	}

	public void setTokenExpiry(LocalDateTime tokenExpiry) {
		this.tokenExpiry = tokenExpiry;
	}

	public LocalDate getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(LocalDate registerDate) {
		this.registerDate = registerDate;
	}

	public String getPhonenumber() {
		return phonenumber;
	}

	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}

	public byte[] getProfileImg() {
		return profileImg;
	}

	public void setProfileImg(byte[] profileImg) {
		this.profileImg = profileImg;
	}


}
