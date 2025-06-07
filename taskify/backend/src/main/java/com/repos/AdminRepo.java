package com.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dto.AdminDTO;
import com.model.Admin;

@Repository
public interface AdminRepo extends JpaRepository<Admin, Integer> {

    boolean existsByAdminEmail(String adminEmail);
    Optional<Admin> findByAdminEmail(String adminEmail);
    Optional<Admin> findByResetToken(String reset_token);
    Optional<Admin> findByRole(String role);

    @Query("SELECT new com.dto.AdminDTO(a.adminId, a.fullname, a.adminEmail) FROM Admin a")
    List<AdminDTO> fetchAllAdminsWithIdNameEmail();

    @Query("SELECT new com.dto.AdminDTO(u.adminId, u.fullname, u.adminEmail, u.registerDate, u.phonenumber, u.profileImg) FROM Admin u WHERE u.adminId = :id")
    AdminDTO fetchAdminDetailFromID(@Param("id") int id);

}
