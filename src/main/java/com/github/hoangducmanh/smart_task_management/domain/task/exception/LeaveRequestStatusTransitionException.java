package com.github.hoangducmanh.smart_task_management.domain.task.exception;

public class LeaveRequestStatusTransitionException extends TaskDomainException {
    public LeaveRequestStatusTransitionException(String message) {
        super(message);
    }
    public LeaveRequestStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }

}
