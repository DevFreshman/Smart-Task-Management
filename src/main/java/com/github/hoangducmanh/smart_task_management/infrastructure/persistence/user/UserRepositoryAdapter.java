package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.user;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.github.hoangducmanh.smart_task_management.domain.user.model.Email;
import com.github.hoangducmanh.smart_task_management.domain.user.model.User;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository userJpaRepository;
    private final UserPersistenceMapper userPersistenceMapper;
    
    @Override
    public Optional<User> findById(UserId id) {
        Objects.requireNonNull(id, "UserId cannot be null");
        return userJpaRepository.findByUserIdAndDeletedAtIsNull(id.value()).map(userPersistenceMapper::toDomain);
    }
    @Override
    public Optional<User> findByEmail(Email email) {
        Objects.requireNonNull(email, "Email cannot be null");
        return userJpaRepository.findByEmailAndDeletedAtIsNull(email.value()).map(userPersistenceMapper::toDomain);
    }

    @Override
    public User save(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        UserEntity entity = userPersistenceMapper.toEntity(user);
        UserEntity savedEntity = userJpaRepository.save(entity);
        return userPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByEmail(Email email) {
        Objects.requireNonNull(email, "Email cannot be null");
        return userJpaRepository.existsByEmailAndDeletedAtIsNull(email.value());
    }

    @Override
    public boolean existsById(UserId id) {
        Objects.requireNonNull(id, "UserId cannot be null");
        return userJpaRepository.existsByUserIdAndDeletedAtIsNull(id.value());
    }
    
}
