package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import com.github.hoangducmanh.smart_task_management.application.ClockSystem;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.LogoutCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogoutUseCaseTest {

    private RefreshTokenRepository refreshTokenRepository;
    private ClockSystem clockSystem;
    private LogoutUseCase logoutUseCase;

    @BeforeEach
    void setUp() {
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        clockSystem = mock(ClockSystem.class);
        logoutUseCase = new LogoutUseCase(refreshTokenRepository, clockSystem);
    }

    // Case: Logout thành công.
    // Verify flow: lấy userId từ command, gọi revokeByUserId(userId, now) với thời gian từ clockSystem.now().
    // Đảm bảo tất cả refresh token của user bị revoke tại thời điểm logout.
    @Test
    void execute_shouldRevokeAllRefreshTokensForUser() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Instant now = Instant.parse("2026-03-03T10:15:00Z");
        LogoutCommand command = LogoutCommand.of(userId);

        when(clockSystem.now()).thenReturn(now);

        logoutUseCase.execute(command);

        verify(refreshTokenRepository).revokeByUserId(userId, now);
        verify(clockSystem).now();
    }

    // Case: Logout với userId khác nhau.
    // Verify revokeByUserId nhận đúng userId từ command, không bị hardcode hay nhầm lẫn.
    @Test
    void execute_shouldPassCorrectUserIdFromCommand() {
        UUID userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        Instant now = Instant.parse("2026-06-15T14:30:00Z");
        LogoutCommand command = LogoutCommand.of(userId);

        when(clockSystem.now()).thenReturn(now);

        logoutUseCase.execute(command);

        verify(refreshTokenRepository).revokeByUserId(userId, now);
    }
}
