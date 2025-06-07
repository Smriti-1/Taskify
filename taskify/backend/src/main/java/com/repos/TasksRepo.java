package com.repos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.model.Tasks;

@Repository
public interface TasksRepo extends JpaRepository<Tasks, Long> {
	List<Tasks> findByUserUserId(int user_id);

	List<Tasks> findByAssignedAdminId(int adminId);

	@Query("SELECT t FROM Tasks t WHERE t.user.userId = :userId AND LOWER(t.status) NOT IN :excludedStatuses ORDER BY t.createdTime DESC")
	List<Tasks> findByUserUserIdAndStatusNotInIgnoreCaseOrderByCreatedTimeDesc(int userId,
			List<String> excludedStatuses);

	List<Tasks> findByUserUserIdAndStatusOrderByCreatedTimeDesc(int id, String status);

	@Query("SELECT t FROM Tasks t " + "WHERE t.user.userId = :userId " + " AND LOWER(t.status) IN :statusList "
			+ " AND (" + "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(t.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(t.userRemark) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(t.adminRemark) LIKE LOWER(CONCAT('%', :keyword, '%'))" + ") " + "ORDER BY t.createdTime DESC")

	List<Tasks> searchByKeywordUserIdAndStatus(@Param("keyword") String keyword, @Param("userId") int userId,
			@Param("statusList") List<String> statusList);

	List<Tasks> findByUserUserIdAndStatusIn(Integer userId, List<String> status, Sort sort);

	List<Tasks> findByUserUserIdAndStatusInAndPriorityIgnoreCase(Integer userId, List<String> statusList,
			String priority);

	List<Tasks> findByUserUserIdAndStatusIgnoreCaseOrderByCreatedTimeDesc(Integer userId, String status);

	@Query("SELECT DISTINCT t.user.userId FROM Tasks t WHERE LOWER(t.status) IN :statuses")
	List<Integer> findDistinctUserUserIdsByStatusIn(@Param("statuses") List<String> statuses);

	List<Tasks> findByUserUserIdAndStatusInIgnoreCase(int userId, List<String> pendingStatuses);

	List<Tasks> findByUserUserIdAndStatusIgnoreCase(int userId, String string);

	List<Tasks> findByUserUserIdAndIsApprovedByAdminTrueAndIsCompletedByUserTrueAndStatusOrderByCreatedTimeDesc(int id,
			String string);

//	admin	
	@Query("SELECT t FROM Tasks t WHERE t.assignedToUserId IS NOT NULL AND (t.admin.adminId = :adminId OR t.assignedAdminId = :adminId)")
	List<Tasks> findTasksAssignedToUsersByAdmin(@Param("adminId") int adminId);

	@Query("SELECT t FROM Tasks t WHERE " + "t.admin.adminId = :adminId AND " + "t.assignedToUserId IS NOT NULL AND "
			+ "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	List<Tasks> searchByAdminAndTitleOrDescription(@Param("adminId") int adminId, @Param("keyword") String keyword);

	List<Tasks> findByAdminAdminIdAndAssignedToUserIdIsNotNullAndStatusIn(Integer adminId, Sort sort,
			List<String> status);

	List<Tasks> findByAdminAdminIdAndAssignedToUserIdIsNotNullAndPriorityIgnoreCaseAndStatusInIgnoreCase(
			Integer adminId, String priority, List<String> status);

	List<Tasks> findByAdminAdminIdAndAssignedToUserIdIsNotNullAndStatusIgnoreCaseOrderByCreatedTimeDesc(Integer adminId,
			String status);

	List<Tasks> findByAdminAdminIdAndAssignedToUserIdAndUserEmailIgnoreCaseOrderByCreatedTimeDesc(Integer adminId,
			int userId, String email);

	List<Tasks> findByAdminAdminIdAndAssignedAdminIdAndStatusIgnoreCaseAndIsApprovedByAdminFalse(Integer adminId,
			Integer assignedAdminId, String status);

	List<Tasks> findByAdminAdminIdAndAssignedAdminIdAndIsCompletedByUserTrueAndIsApprovedByAdminFalse(int adminId,
			int assignedAdminId);

	List<Tasks> findByAdminAdminIdAndAssignedAdminIdAndStatusIgnoreCaseAndIsApprovedByAdminTrueAndIsCompletedByUserTrue(
			int adminId, int assignedAdminId, String status);

	@Query("SELECT t FROM Tasks t WHERE t.admin.adminId = :adminId AND t.status = :status AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER"
			+ "(CONCAT('%', :keyword, '%')))")
	List<Tasks> searchByAdminAndTitleOrDescriptionForReviewAndApproved(@Param("adminId") int adminId,
			@Param("keyword") String keyword, @Param("status") String status);

//	user-dashbaord

	long countByUserUserId(Integer userId);

	long countByUserUserIdAndStatusIn(Integer userId, List<String> statuses);

	long countByUserUserIdAndDueDateBeforeAndStatusNot(Integer userId, LocalDateTime dueDate, String status);

	long countByUserUserIdAndIsApprovedByAdminFalseAndStatusIgnoreCase(Integer userId, String status);

	List<Tasks> findTop4ByUserUserIdAndStatusOrderByDueDateDesc(Integer userId, String status);

	List<Tasks> findTop3ByUserUserIdAndStatusInOrderByDueDateDesc(Integer userId, List<String> statuses);

	@Query(value = "SELECT * FROM tasks WHERE user_id = :userId ORDER BY CASE WHEN priority = 'High' THEN 1 WHEN priority = 'Normal' THEN 2 WHEN priority = 'Low' THEN 3 ELSE 4 END, due_date DESC LIMIT 5 ", nativeQuery = true)
	List<Tasks> findTop5SortedByPriorityAndDueDate(@Param("userId") Integer userId);

	List<Tasks> findByAdminAdminIdAndAssignedToUserIdAndUserEmailIgnoreCaseAndStatusInOrderByCreatedTimeDesc(
			Integer userId, int empId, String email, List<String> statusList);

//	admin dashboard
	long countByAdminAdminIdAndAssignedToUserIdNotNull(int adminId);

	long countByAdminAdminIdAndAssignedToUserIdNotNullAndStatusIgnoreCaseAndIsCompletedByUserTrue(int adminId,
			String string);

	long countByAdminAdminIdAndAssignedToUserIdNotNullAndStatusIn(int adminId, List<String> of);

	long countByAdminAdminIdAndAssignedToUserIdNotNullAndStatusIgnoreCase(int adminId, String string);

	List<Tasks> findTop3ByAdminAdminIdAndAssignedToUserIdNotNullOrderByCreatedTimeDesc(int adminId);

	List<Tasks> findTop5ByAdminAdminIdAndAssignedToUserIdNotNullAndStatusIgnoreCaseOrderByCreatedTimeDesc(int adminId,
			String string);

}
