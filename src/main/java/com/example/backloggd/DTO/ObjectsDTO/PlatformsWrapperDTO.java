package com.example.backloggd.DTO.ObjectsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlatformsWrapperDTO(
        @JsonProperty("platform")
        PlatformsDTO platforms
) {
}
