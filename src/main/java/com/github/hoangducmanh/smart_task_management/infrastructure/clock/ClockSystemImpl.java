package com.github.hoangducmanh.smart_task_management.infrastructure.clock;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.github.hoangducmanh.smart_task_management.application.TimeProvider;

@Component
public class ClockSystemImpl implements TimeProvider {
    @Override
    public Instant now() {
        return Instant.now(Clock.systemUTC());
    }

}
