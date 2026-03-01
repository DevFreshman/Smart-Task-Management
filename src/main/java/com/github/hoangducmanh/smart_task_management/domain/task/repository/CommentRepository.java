package com.github.hoangducmanh.smart_task_management.domain.task.repository;

import java.util.Optional;

import com.github.hoangducmanh.smart_task_management.domain.shared.PageResult;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Comment;
import com.github.hoangducmanh.smart_task_management.domain.task.model.CommentId;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;

public interface CommentRepository {
    PageResult<Comment> findByTaskId(TaskId taskId, int page, int size);
    Optional<Comment> findById(CommentId commentId);
    Comment save(Comment comment);
}
