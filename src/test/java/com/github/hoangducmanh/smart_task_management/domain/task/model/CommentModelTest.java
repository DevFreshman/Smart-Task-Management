package com.github.hoangducmanh.smart_task_management.domain.task.model;

import com.github.hoangducmanh.smart_task_management.domain.task.exception.CommentDeleteException;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommentModelTest {

    // BR: Comment creation sets content and initial audit state.
    @Test
    void create_shouldInitializeCommentFieldsAndAuditInfo() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 15, 10, 0);

        Comment comment = Comment.create(
            CommentId.fromString("18acc644-a858-426e-93da-6f56866ebfee"),
            UserId.fromString("a6f9f6a1-c2a9-48d7-b4f4-f431f73388cd"),
            TaskId.fromString("f55ace4e-b2db-46a5-ab5c-cc63366db670"),
            new ContentComment("Please update the deadline."),
            now
        );

        assertEquals("Please update the deadline.", comment.getContent().content());
        assertEquals(now, comment.getAuditInfo().createdAt());
        assertEquals(now, comment.getAuditInfo().updatedAt());
        assertEquals(null, comment.getAuditInfo().deletedAt());
    }

    // BR: Comment can be deleted once; repeated delete is invalid.
    @Test
    void delete_shouldSetDeletedAtAndThrowWhenDeletingTwice() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 15, 10, 0);
        Comment comment = Comment.create(
            CommentId.fromString("18acc644-a858-426e-93da-6f56866ebfee"),
            UserId.fromString("a6f9f6a1-c2a9-48d7-b4f4-f431f73388cd"),
            TaskId.fromString("f55ace4e-b2db-46a5-ab5c-cc63366db670"),
            new ContentComment("Please update the deadline."),
            now
        );

        LocalDateTime deletedAt = now.plusMinutes(5);
        comment.delete(deletedAt);

        assertNotNull(comment.getAuditInfo().deletedAt());
        assertEquals(deletedAt, comment.getAuditInfo().deletedAt());
        assertThrows(CommentDeleteException.class, () -> comment.delete(deletedAt.plusMinutes(1)));
    }
}
