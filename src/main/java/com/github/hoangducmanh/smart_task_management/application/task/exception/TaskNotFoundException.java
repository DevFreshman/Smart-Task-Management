
package com.github.hoangducmanh.smart_task_management.application.task.exception;

public class TaskNotFoundException extends TaskApplicationException {

    public TaskNotFoundException(String message) {
        super(message, TaskErrorCode.TASK_NOT_FOUND);
    }

}