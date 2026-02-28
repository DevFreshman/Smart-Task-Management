package com.github.hoangducmanh.smart_task_management.domain.task.exception;

public class CommentDeleteException extends TaskDomainException {
    public CommentDeleteException(String message) {
        super(message);
    }
    public CommentDeleteException(String message, Throwable cause) {
        super(message, cause);
    }

}
