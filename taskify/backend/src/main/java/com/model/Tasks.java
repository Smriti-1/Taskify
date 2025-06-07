package com.model;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Component
@Scope("prototype")
public class Tasks {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long taskId;

    private String title;
    private String description;
    private String status;
    private String priority;
    private String category;

    private LocalDateTime createdTime;
    private LocalDateTime dueDate;

    @Column(length = 1000)
    private String userRemark;

    @Column(length = 1000)
    private String adminRemark;

    private Boolean isCompletedByUser = false;
    private Boolean isApprovedByAdmin = false;

    private Integer assignedAdminId;

    private Integer assignedToUserId;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = true)
    private Admin admin;

    // Constructors
    public Tasks() {
    }

    public Tasks(Long taskId, String title, String description, String status, String priority, String category,
                 LocalDateTime createdTime, LocalDateTime dueDate, String userRemark, String adminRemark,
                 Boolean isCompletedByUser, Boolean isApprovedByAdmin, Integer assignedAdminId,
                 Users user, Admin admin , Integer assignedToUserId) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.category = category;
        this.createdTime = createdTime;
        this.dueDate = dueDate;
        this.userRemark = userRemark;
        this.adminRemark = adminRemark;
        this.isCompletedByUser = isCompletedByUser;
        this.isApprovedByAdmin = isApprovedByAdmin;
        this.assignedAdminId = assignedAdminId;
        this.assignedToUserId = assignedToUserId;
        this.user = user;
        this.admin = admin;
    }

    // Getters and Setters

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getUserRemark() {
        return userRemark;
    }

    public void setUserRemark(String userRemark) {
        this.userRemark = userRemark;
    }

    public String getAdminRemark() {
        return adminRemark;
    }

    public void setAdminRemark(String adminRemark) {
        this.adminRemark = adminRemark;
    }

    public Boolean getIsCompletedByUser() {
        return isCompletedByUser;
    }

    public void setIsCompletedByUser(Boolean isCompletedByUser) {
        this.isCompletedByUser = isCompletedByUser;
    }

    public Boolean getIsApprovedByAdmin() {
        return isApprovedByAdmin;
    }

    public void setIsApprovedByAdmin(Boolean isApprovedByAdmin) {
        this.isApprovedByAdmin = isApprovedByAdmin;
    }

    public Integer getAssignedAdminId() {
        return assignedAdminId;
    }

    public void setAssignedAdminId(Integer assignedAdminId) {
        this.assignedAdminId = assignedAdminId;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

	public Integer getAssignedToUserId() {
		return assignedToUserId;
	}

	public void setAssignedToUserId(Integer assignedToUserId) {
		this.assignedToUserId = assignedToUserId;
	}

//	@Override
//	public String toString() {
//		return "Tasks [taskId=" + taskId + ", title=" + title + ", description=" + description + ", status=" + status
//				+ ", priority=" + priority + ", category=" + category + ", createdTime=" + createdTime + ", dueDate="
//				+ dueDate + ", userRemark=" + userRemark + ", adminRemark=" + adminRemark + ", isCompletedByUser="
//				+ isCompletedByUser + ", isApprovedByAdmin=" + isApprovedByAdmin + ", assignedAdminId="
//				+ assignedAdminId + ", user=" + user + ", admin=" + admin + "]";
//	}


}
