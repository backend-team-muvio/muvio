package org.cyberrealm.tech.muvio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record MediaVibeRequestDto(
        @NotNull @NotBlank String vibe,
        String years, String type,
        @Schema(type = "string", example = "category1, category2, ...etc") Set<String> categories,
        Integer page, Integer size, String lang) {
}
