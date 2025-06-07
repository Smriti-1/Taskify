package com.dto;

import java.time.LocalDate;

public class AdminDTO {

    private int adminId;
    private String fullname;
    private String adminEmail;
    private LocalDate registerDate;
    private String phonenumber;
    private byte[] profileImg;

    public AdminDTO() {
    }

    public AdminDTO(int adminId, String fullname, String adminEmail) {
        this.adminId = adminId;
        this.fullname = fullname;
        this.adminEmail = adminEmail;
    }

    public AdminDTO(int adminId, String fullname, String adminEmail, LocalDate registerDate, String phonenumber, byte[] profileImg) {
        this.adminId = adminId;
        this.fullname = fullname;
        this.adminEmail = adminEmail;
        this.registerDate = registerDate;
        this.phonenumber = phonenumber;
        this.profileImg = profileImg;
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