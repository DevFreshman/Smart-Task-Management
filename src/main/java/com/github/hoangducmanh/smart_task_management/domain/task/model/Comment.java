package com.github.hoangducmanh.smart_task_management.domain.task.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.github.hoangducmanh.smart_task_management.domain.shared.AuditInfo;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.CommentDeleteException;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public class Comment {
    private final CommentId id;
    private final UserId authorId;
    private final TaskId taskId; 
    private ContentComment content;
    private AuditInfo auditInfo;

    private Comment(CommentId id, UserId authorId, TaskId taskId, ContentComment content, AuditInfo auditInfo) {
        this.id = Objects.requireNonNull(id, "Comment ID cannot be null");
        this.authorId = Objects.requireNonNull(authorId, "Author ID cannot be null");
        this.taskId = Objects.requireNonNull(taskId, "Task ID cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.auditInfo = Objects.requireNonNull(auditInfo, "Audit info cannot be null");
    }

    public CommentId getId() {
        return id;
    }
    public UserId getAuthorId() {
        return authorId;
    }
    public TaskId getTaskId() {
        return taskId;
    }
    public ContentComment getContent() {
        return content;
    }
    public AuditInfo getAuditInfo() {
        return auditInfo;
    }

    public static Comment create(CommentId id, UserId authorId, TaskId taskId, ContentComment content, LocalDateTime now) {
        return new Comment(id, authorId, taskId, content, AuditInfo.create(now));
    }

    public void delete(LocalDateTime now) {
    Objects.requireNonNull(now, "now cannot be null");
    if (auditInfo.isDeleted()) {
        throw new CommentDeleteException("Comment is already deleted");
    }
    this.auditInfo = this.auditInfo.delete(now);
    }
}
