package com.github.hoangducmanh.smart_task_management.domain.user.model;

public enum Role {
    ADMIN("admin"),
    USER("user");
    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public static Role fromRoleName(String roleName) {
        for (Role role : values()) {
            if (role.roleName.equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role name: " + roleName);
    }
}
