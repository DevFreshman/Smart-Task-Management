package com.github.hoangducmanh.smart_task_management.domain.shared;

import java.util.List;

public record PageResult<T>(List<T> items, long totalItems, int totalPages, int currentPage, int pageSize) {
    public boolean hasNextPage() {
        return this.currentPage < this.totalPages;
    }
}
