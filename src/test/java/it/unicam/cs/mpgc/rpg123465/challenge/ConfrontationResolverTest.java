package it.unicam.cs.mpgc.rpg123465.challenge;

import it.unicam.cs.mpgc.rpg123465.domain.Enemy;
import it.unicam.cs.mpgc.rpg123465.domain.Stats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test di {@link ConfrontationResolver}.
 */
class ConfrontationResolverTest {

    private final ConfrontationResolver resolver = new ConfrontationResolver();

    private ChallengeQuestion domanda() {
        return new ChallengeQuestion(
                "E se fallissi davanti a tutti?",
                List.of("Potrei imparare dal fallimento.", "Meglio non provarci."),
                0,
                "La Paura vacilla.",
                "La Paura si nutre della tua risposta."
        );
    }

    private Enemy nuovoNemico() {
        return new Enemy("Paura del Fallimento", new Stats(35, 8, 2), "La paura di sbagliare.");
    }

    @Test
    void laRispostaCostruttivaIndebolisceIlNemico() {
        Enemy enemy = nuovoNemico();

        String message = resolver.resolve(domanda(), 0, enemy);

        assertEquals(30, enemy.getStats().getCurrentHealth());
        assertTrue(message.contains("vacilla"));
    }

    @Test
    void laRispostaNegativaCuraIlNemico() {
        Enemy enemy = nuovoNemico();
        enemy.takeDamage(20);

        String message = resolver.resolve(domanda(), 1, enemy);

        assertEquals(18, enemy.getStats().getCurrentHealth());
        assertTrue(message.contains("sorride"));
    }

    @Test
    void ilConfrontoNonUccideMaiIlNemico() {
        Enemy enemy = nuovoNemico();

        resolver.resolve(domanda(), 0, enemy);

        assertTrue(enemy.isAlive());
    }

    @Test
    void parametriNullVengonoRifiutati() {
        Enemy enemy = nuovoNemico();

        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(null, 0, enemy));
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(domanda(), 0, null));
    }
}
