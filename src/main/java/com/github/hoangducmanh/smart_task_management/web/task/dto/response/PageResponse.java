package com.github.hoangducmanh.smart_task_management.web.task.dto.response;

import java.util.List;

public record PageResponse<T>(
    List<T> items, 
    long totalItems, 
    int totalPages, 
    int currentPage, 
    int pageSize
) {

}
