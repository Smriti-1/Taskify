package com.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dto.UserDTO;
import com.model.Users;

@Repository
public interface UserRepo extends JpaRepository<Users, Integer> {

	boolean existsByEmail(String email);

	Optional<Users> findByEmail(String email);

	Optional<Users> findByRole(String role);

	Optional<Users> findByResetToken(String reset_token);

	@Query("SELECT new com.dto.UserDTO(u.userId, u.firstname, u.lastname , u.email , u.registerDate , u.phonenumber , u.profileImg) FROM Users u WHERE u.userId = :id")
	UserDTO fetchUserDetailFromID(@Param("id") int id);

	@Query("SELECT new com.dto.UserDTO(u.userId, u.firstname, u.lastname , u.email , u.registerDate , u.phonenumber , u.profileImg) FROM Users u ")
	List<UserDTO> fetchAllUserDetail();

	@Query("SELECT u FROM Users u WHERE " + "LOWER(u.firstname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(u.lastname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "u.phonenumber LIKE CONCAT('%', :keyword, '%')")
	List<Users> searchByNameEmailOrPhone(String keyword);

	List<Users> findTop4ByRoleOrderByRegisterDateDesc(String string);
}
