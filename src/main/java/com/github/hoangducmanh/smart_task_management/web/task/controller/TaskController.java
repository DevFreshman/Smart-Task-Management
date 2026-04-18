package com.github.hoangducmanh.smart_task_management.web.task.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.github.hoangducmanh.smart_task_management.application.task.dto.command.DeleteTaskCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.PageResult;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskStatusResult;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskSummaryResult;
import com.github.hoangducmanh.smart_task_management.application.task.port.in.ChangeTaskStatusPort;
import com.github.hoangducmanh.smart_task_management.application.task.port.in.DeleteTaskPort;
import com.github.hoangducmanh.smart_task_management.application.task.port.in.GetListTaskByFilterPort;
import com.github.hoangducmanh.smart_task_management.application.task.port.in.GetTaskByIdPort;
import com.github.hoangducmanh.smart_task_management.infrastructure.security.AuthenticateUser;
import com.github.hoangducmanh.smart_task_management.web.task.dto.request.ChangeStatusRequest;
import com.github.hoangducmanh.smart_task_management.web.task.dto.request.TaskFilterRequest;
import com.github.hoangducmanh.smart_task_management.web.task.dto.response.ChangeStatusResponse;
import com.github.hoangducmanh.smart_task_management.web.task.dto.response.PageResponse;
import com.github.hoangducmanh.smart_task_management.web.task.dto.response.TaskSummaryResponse;
import com.github.hoangducmanh.smart_task_management.web.task.mapper.QueryMapper;
import com.github.hoangducmanh.smart_task_management.web.task.mapper.UpdateMapper;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/tasks")
public class TaskController {

    private final GetTaskByIdPort getTaskById;
    private final GetListTaskByFilterPort getListTask;
    private final DeleteTaskPort deleteTask;
    private final ChangeTaskStatusPort changeTaskStatus;

    @GetMapping("/get/{id}")
    public ResponseEntity<TaskSummaryResponse> getTaskById(@PathVariable UUID id) {
        TaskSummaryResult taskResult = getTaskById.execute(id);
        TaskSummaryResponse response = QueryMapper.toResponse(taskResult);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/get/list")
    public ResponseEntity<PageResponse<TaskSummaryResponse>> getTaskList(@ModelAttribute @Valid TaskFilterRequest request) {
        PageResult<TaskSummaryResult> taskListResult = getListTask.execute(QueryMapper.toFilterQuery(request), request.page(), request.size());
        PageResponse<TaskSummaryResponse> response = QueryMapper.toPageResponse(taskListResult);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTaskId(@PathVariable("id") UUID taskId, @AuthenticationPrincipal AuthenticateUser authenticateUser) {
        DeleteTaskCommand deleteTaskCommand = new DeleteTaskCommand(taskId, authenticateUser.userId());
        deleteTask.execute(deleteTaskCommand);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/update/{id}/status")
    public ResponseEntity<ChangeStatusResponse> changeTaskStatus(
        @PathVariable("id") UUID taskId,
        @RequestBody @Valid ChangeStatusRequest request, 
        @AuthenticationPrincipal AuthenticateUser authenticateUser) {

        TaskStatusResult result = changeTaskStatus.execute(UpdateMapper.toChangeTaskStatusCommand(request, taskId, authenticateUser.userId()));

        ChangeStatusResponse response = UpdateMapper.toChangeStatusResponse(result);
        
        return ResponseEntity.ok(response);

    }
    
}
