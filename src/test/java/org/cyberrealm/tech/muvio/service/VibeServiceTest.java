package org.cyberrealm.tech.muvio.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.cyberrealm.tech.muvio.model.GenreEntity;
import org.cyberrealm.tech.muvio.model.Vibe;
import org.cyberrealm.tech.muvio.service.impl.VibeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class VibeServiceTest {
    private static final Set<String> RATINGS = Set.of("G", "PG", "U");
    private static final Set<GenreEntity> GENRES = Set.of(GenreEntity.COMEDY, GenreEntity.FAMILY);
    private static final VibeServiceImpl vibeService = new VibeServiceImpl();

    @Test
    @DisplayName("Verify getVibes() method works")
    public void getVibes_ValidResponse_ReturnSetVibes() {
        assertThat(vibeService.getVibes(RATINGS, GENRES)).containsExactlyInAnyOrder(
                Vibe.MAKE_ME_CHILL, Vibe.MAKE_ME_FEEL_GOOD);
    }
}
