package com.example.backloggd.DTO;

import java.util.List;

public record UserProfileDTO(
        String userName,
        List<ReviewSummaryDTO> reviews
) {}

