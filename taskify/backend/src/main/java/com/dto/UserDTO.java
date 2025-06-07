package com.dto;

import java.time.LocalDate;

public class UserDTO {

	private int userId;
	private String firstname;
	private String lastname;
	private String email ;
	private LocalDate registerDate;
	private String phonenumber;
	private byte[] profileImg;

	public UserDTO() {
	}

	public UserDTO(int userId, String firstname, String lastname, String email, LocalDate registerDate, String phonenumber,
			byte[] profileImg) {
		this.userId = userId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.registerDate = registerDate;
		this.phonenumber = phonenumber;
		this.profileImg = profileImg;
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

	public LocalDate getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(LocalDate registerDate) {
		this.registerDate = registerDate;
	}

	public String getPhoneNum() {
		return phonenumber;
	}

	public void setPhoneNum(String phonenumber) {
		this.phonenumber = phonenumber;
	}

	public byte[] getProfileimg() {
		return profileImg;
	}

	public void setProfileimg(byte[] profileImg) {
		this.profileImg = profileImg;
	}


}
