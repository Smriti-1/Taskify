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
@Component
@Scope("prototype")
public class Users {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "user_id")
	private int userId;

	private String firstname;
	private String lastname;
	@Column(unique = true)
	private String email;

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

	public Users() {
	}

	public Users(int userId, String firstname, String lastname, String email, String password, String role,
			String resetToken, LocalDateTime tokenExpiry, LocalDate registerDate, String phonenumber, byte[] profileImg) {
		this.userId = userId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
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

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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