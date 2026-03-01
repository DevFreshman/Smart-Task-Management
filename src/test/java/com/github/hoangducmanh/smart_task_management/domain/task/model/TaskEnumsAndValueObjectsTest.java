package com.github.hoangducmanh.smart_task_management.domain.task.model;

import com.github.hoangducmanh.smart_task_management.domain.task.exception.ContentExceedException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.DescriptionOverLimitException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskStatusTransitionException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TitleOverLimitException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskEnumsAndValueObjectsTest {

    // OV: TaskStatus display text + parse behavior.
    @Test
    void taskStatus_shouldExposeDisplayNameAndParseFromDisplayName() {
        assertEquals("To Do", TaskStatus.TODO.getDisplayName());
        assertEquals(TaskStatus.TODO, TaskStatus.fromDisplayName("to do"));
        assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.fromDisplayName("In Progress"));
        assertEquals(TaskStatus.COMPLETED, TaskStatus.fromDisplayName("COMPLETED"));
        assertThrows(IllegalArgumentException.class, () -> TaskStatus.fromDisplayName("unknown"));
    }

    // BR: TaskStatus valid transition flow.
    @Test
    void taskStatus_shouldApplyValidTransitions() {
        assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.TODO.updateStatus(TaskStatus.IN_PROGRESS));
        assertEquals(TaskStatus.COMPLETED, TaskStatus.IN_PROGRESS.updateStatus(TaskStatus.COMPLETED));
        assertEquals(TaskStatus.COMPLETED, TaskStatus.COMPLETED.updateStatus(TaskStatus.COMPLETED));
    }

    // BR: TaskStatus invalid transitions are blocked.
    @Test
    void taskStatus_shouldThrowOnInvalidTransitions() {
        assertThrows(TaskStatusTransitionException.class, () -> TaskStatus.TODO.updateStatus(TaskStatus.COMPLETED));
        assertThrows(TaskStatusTransitionException.class, () -> TaskStatus.IN_PROGRESS.updateStatus(TaskStatus.TODO));
        assertThrows(TaskStatusTransitionException.class, () -> TaskStatus.COMPLETED.updateStatus(TaskStatus.TODO));
    }

    // OV: LeaveRequestStatus display text mapping.
    @Test
    void leaveRequestStatus_shouldExposeDisplayName() {
        assertEquals("Pending", LeaveRequestStatus.PENDING.getDisplayName());
        assertEquals("Approved", LeaveRequestStatus.APPROVED.getDisplayName());
        assertEquals("Rejected", LeaveRequestStatus.REJECTED.getDisplayName());
        assertEquals("Expired", LeaveRequestStatus.EXPIRED.getDisplayName());
    }

    // OV: TaskPriority display text mapping.
    @Test
    void taskPriority_shouldExposeDisplayName() {
        assertEquals("Low", TaskPriority.LOW.getDisplayName());
        assertEquals("Medium", TaskPriority.MEDIUM.getDisplayName());
        assertEquals("High", TaskPriority.HIGH.getDisplayName());
        assertEquals("Critical", TaskPriority.CRITICAL.getDisplayName());
    }

    // OV: TaskFilter current behavior is field storage only.
    @Test
    void taskFilter_shouldStoreProvidedFields() {
        LocalDateTime from = LocalDateTime.of(2026, 2, 1, 8, 0);
        LocalDateTime to = LocalDateTime.of(2026, 2, 20, 8, 0);

        TaskFilter filter = new TaskFilter(TaskStatus.IN_PROGRESS, TaskPriority.HIGH, from, to);

        assertEquals(TaskStatus.IN_PROGRESS, filter.status());
        assertEquals(TaskPriority.HIGH, filter.priority());
        assertEquals(from, filter.deadlineFrom());
        assertEquals(to, filter.deadlineTo());
    }

    // OV: Title boundary/required validation.
    @Test
    void title_shouldAllowValidValueAndThrowForNullBlankAndOverLimit() {
        Title title = new Title("Build release pipeline");
        assertEquals("Build release pipeline", title.value());

        assertThrows(IllegalArgumentException.class, () -> new Title(null));
        assertThrows(IllegalArgumentException.class, () -> new Title("   "));
        assertThrows(TitleOverLimitException.class, () -> new Title("a".repeat(201)));
    }

    // OV: Description allows null, validates max length.
    @Test
    void description_shouldAllowNullAndValidValueAndThrowWhenTooLong() {
        Description description = new Description("Simple description");
        Description nullDescription = new Description(null);

        assertEquals("Simple description", description.value());
        assertEquals(null, nullDescription.value());
        assertThrows(DescriptionOverLimitException.class, () -> new Description("a".repeat(1001)));
    }

    // OV: ContentComment required + max length validation.
    @Test
    void contentComment_shouldValidateBoundaries() {
        ContentComment contentComment = new ContentComment("Looks good");
        assertEquals("Looks good", contentComment.content());

        assertThrows(IllegalArgumentException.class, () -> new ContentComment(null));
        assertThrows(IllegalArgumentException.class, () -> new ContentComment("  "));
        assertThrows(ContentExceedException.class, () -> new ContentComment("a".repeat(1001)));
    }

    // OV: ReasonLeave required + max length validation.
    @Test
    void reasonLeave_shouldValidateBoundaries() {
        ReasonLeave reasonLeave = new ReasonLeave("Family emergency");
        assertEquals("Family emergency", reasonLeave.reason());

        assertThrows(IllegalArgumentException.class, () -> new ReasonLeave(null));
        assertThrows(IllegalArgumentException.class, () -> new ReasonLeave("  "));
        assertThrows(ContentExceedException.class, () -> new ReasonLeave("a".repeat(501)));
    }

    // OV: TaskId parse/format/generate validation.
    @Test
    void taskId_shouldSupportGenerateFromStringAsStringAndToString() {
        String raw = "f18b8e8e-5ac7-4a54-b9ac-c6f25ac54314";
        TaskId fromString = TaskId.fromString(raw);
        TaskId generated = TaskId.generate();

        assertEquals(raw, fromString.asString());
        assertEquals("TaskId=[" + raw + "]", fromString.toString());
        assertNotNull(generated.value());
        assertThrows(IllegalArgumentException.class, () -> new TaskId(null));
        assertThrows(IllegalArgumentException.class, () -> TaskId.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> TaskId.fromString(" "));
        assertThrows(IllegalArgumentException.class, () -> TaskId.fromString("invalid"));
    }

    // OV: CommentId factory/parse/generate validation.
    @Test
    void commentId_shouldSupportOfGenerateFromStringAndValidateInput() {
        UUID uuid = UUID.fromString("74259031-b5f9-41c0-ae5a-3f4ad41ef87f");
        CommentId of = CommentId.of(uuid);
        CommentId generated = CommentId.generate();
        CommentId fromString = CommentId.fromString(uuid.toString());

        assertEquals(uuid, of.value());
        assertEquals(uuid, fromString.value());
        assertNotNull(generated.value());
        assertThrows(IllegalArgumentException.class, () -> new CommentId(null));
        assertThrows(IllegalArgumentException.class, () -> CommentId.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> CommentId.fromString(" "));
        assertThrows(IllegalArgumentException.class, () -> CommentId.fromString("invalid"));
    }

    // OV: LeaveRequestId factory/parse/generate validation.
    @Test
    void leaveRequestId_shouldSupportOfGenerateFromStringAndValidateInput() {
        UUID uuid = UUID.fromString("5f8e955f-3df9-43f9-b560-f8b6f8f7de01");
        LeaveRequestId of = LeaveRequestId.of(uuid);
        LeaveRequestId generated = LeaveRequestId.generate();
        LeaveRequestId fromString = LeaveRequestId.fromString(uuid.toString());

        assertEquals(uuid, of.value());
        assertEquals(uuid, fromString.value());
        assertNotNull(generated.value());
        assertThrows(IllegalArgumentException.class, () -> new LeaveRequestId(null));
        assertThrows(IllegalArgumentException.class, () -> LeaveRequestId.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> LeaveRequestId.fromString(" "));
        assertThrows(IllegalArgumentException.class, () -> LeaveRequestId.fromString("invalid"));
    }
}
