package com.github.hoangducmanh.smart_task_management.domain.user.model;

public enum UserRole {
    ADMIN("admin"),
    USER("user");
    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public static UserRole fromRoleName(String roleName) {
        for (UserRole role : values()) {
            if (role.roleName.equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role name: " + roleName);
    }
}
