package it.unicam.cs.mpgc.rpg123465.view;

import it.unicam.cs.mpgc.rpg123465.model.PlayerProfile;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica l'abbinamento fra i profili e le loro figure.
 *
 * Un file rinominato fra le risorse non rompe la compilazione: si vedrebbe
 * solo a schermo, alla fine di una partita intera. Queste prove lo fanno
 * emergere subito.
 */
class ProfilePortraitsTest {

    @Test
    @DisplayName("ogni profilo ha la sua figura fra le risorse")
    void everyProfileHasItsPortrait() throws Exception {
        for (PlayerProfile profile : PlayerProfile.values()) {
            String resource = ProfilePortraits.resourceOf(profile);

            try (InputStream stream = ProfilePortraits.class.getResourceAsStream(resource)) {
                assertNotNull(stream, "figura mancante per " + profile.name() + ": " + resource);
            }
        }
    }

    @Test
    @DisplayName("due profili non condividono la stessa figura")
    void portraitsAreDistinct() {
        Set<String> resources = new HashSet<>();

        for (PlayerProfile profile : PlayerProfile.values()) {
            assertTrue(resources.add(ProfilePortraits.resourceOf(profile)),
                    "figura ripetuta in " + profile.name());
        }
    }

    @Test
    @DisplayName("un profilo nullo non ha figura")
    void nullProfileIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> ProfilePortraits.resourceOf(null));
    }
}
