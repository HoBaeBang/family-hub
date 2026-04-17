package com.familyhub.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TempCodeRequest(@NotBlank String code) {}
