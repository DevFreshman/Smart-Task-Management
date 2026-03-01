package com.github.hoangducmanh.smart_task_management.domain.user.repository;

import java.util.Optional;

import com.github.hoangducmanh.smart_task_management.domain.user.model.Email;
import com.github.hoangducmanh.smart_task_management.domain.user.model.User;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public interface UserRepository {
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(Email email);
    User save(User user);
    boolean existsByEmail(Email email);
    boolean existsById(UserId id);
}
