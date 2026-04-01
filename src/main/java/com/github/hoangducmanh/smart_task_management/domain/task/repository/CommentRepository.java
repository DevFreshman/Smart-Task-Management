package com.github.hoangducmanh.smart_task_management.domain.task.repository;

import java.util.Optional;

import com.github.hoangducmanh.smart_task_management.domain.task.model.Comment;
import com.github.hoangducmanh.smart_task_management.domain.task.model.CommentId;

public interface CommentRepository {
    Optional<Comment> findById(CommentId commentId);
    Comment save(Comment comment);
}
