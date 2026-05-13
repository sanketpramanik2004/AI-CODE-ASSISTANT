package com.aibugfinder.backend.dto;

import java.time.Instant;

public record ApiError(String message, int status, Instant timestamp) {
}
