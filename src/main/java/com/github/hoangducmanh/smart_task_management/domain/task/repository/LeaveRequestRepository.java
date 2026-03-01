package com.github.hoangducmanh.smart_task_management.domain.task.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.github.hoangducmanh.smart_task_management.domain.shared.PageResult;
import com.github.hoangducmanh.smart_task_management.domain.task.model.LeaveRequest;
import com.github.hoangducmanh.smart_task_management.domain.task.model.LeaveRequestId;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public interface LeaveRequestRepository {
    PageResult<LeaveRequest> findByTaskId(TaskId taskId, int page, int size);
    Optional<LeaveRequest> findByLeaveRequestId(LeaveRequestId leaveRequestId);
    LeaveRequest save(LeaveRequest leaveRequest);
    boolean existsPendingByAssigneeIdAndTaskId(TaskId taskId, UserId userId);
    List<LeaveRequest> findExpiredPendingRequests(LocalDateTime now);
    List<LeaveRequest> findPendingByTaskId(TaskId taskId);
}
