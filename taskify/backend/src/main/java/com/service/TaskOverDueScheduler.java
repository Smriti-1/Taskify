package com.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.model.Tasks;
import com.repos.TasksRepo;

@Service
public class TaskOverDueScheduler {

	@Autowired
	private TasksRepo taskRepo;

//	second minute hour day-of-month month day-of-week
	@Scheduled(cron = "0 0 0/2 * * *")
	public void schedulerForMarkingoverdue() {
		List<Integer> userIds = taskRepo.findDistinctUserUserIdsByStatusIn(List.of("Todo", "Inprogress", "Redo"));
		
		if(!userIds.isEmpty()) {		
			for (Integer userId : userIds) {
				System.out.println(userId);
				markUserTasksOverdue(userId);
			}
		}
	}

	public List<Tasks> markUserTasksOverdue(int userId) {
		List<String> pendingStatuses = List.of("Todo", "Redo", "Inprogress");
	    LocalDateTime now = LocalDateTime.now();

	    List<Tasks> toCheck = taskRepo.findByUserUserIdAndStatusInIgnoreCase(userId, pendingStatuses);

	    List<Tasks> overdueTasks = new ArrayList<>();
	    for (Tasks task : toCheck) {
	        if (task.getDueDate() != null && task.getDueDate().isBefore(now)) {
	            task.setStatus("Overdue");
	            overdueTasks.add(task);
	            System.out.println("Task " + task.getTaskId() + " marked as overdue.");
	        }
	    }

	    taskRepo.saveAll(overdueTasks);
	    List<Tasks> taskslist= taskRepo.findByUserUserIdAndStatusIgnoreCase(userId, "Overdue");
	   
	    return taskslist;
	}
}
