package org.cyberrealm.tech.muvio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record MovieVibeRequestDto(@NotNull @NotBlank String vibe, String years, String type,
                                  Set<String> categories) {
}
