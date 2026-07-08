package it.unicam.cs.mpgc.rpg123465.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test della classe {@link Stats}.
 */
class StatsTest {

    @Test
    void allaCreazioneLaVitaCorrenteEUgualeAllaVitaMassima() {
        Stats stats = new Stats(50, 10, 5);

        assertEquals(50, stats.getMaxHealth());
        assertEquals(50, stats.getCurrentHealth());
        assertTrue(stats.isAlive());
    }

    @Test
    void ilDannoRiduceLaVitaCorrente() {
        Stats stats = new Stats(50, 10, 5);

        stats.takeDamage(20);

        assertEquals(30, stats.getCurrentHealth());
        assertTrue(stats.isAlive());
    }

    @Test
    void laVitaNonScendeMaiSottoZero() {
        Stats stats = new Stats(30, 10, 5);

        stats.takeDamage(100);

        assertEquals(0, stats.getCurrentHealth());
        assertFalse(stats.isAlive());
    }

    @Test
    void laCuraNonSuperaLaVitaMassima() {
        Stats stats = new Stats(40, 10, 5);
        stats.takeDamage(30);

        stats.heal(100);

        assertEquals(40, stats.getCurrentHealth());
    }

    @Test
    void ilRipristinoRiportaLaVitaAlMassimo() {
        Stats stats = new Stats(40, 10, 5);
        stats.takeDamage(35);

        stats.restoreHealth();

        assertEquals(40, stats.getCurrentHealth());
    }

    @Test
    void impostareLaVitaCorrenteAggiornaIlValore() {
        Stats stats = new Stats(50, 10, 5);

        stats.setCurrentHealth(25);

        assertEquals(25, stats.getCurrentHealth());
    }

    @Test
    void impostareLaVitaCorrenteOltreIlMassimoVieneRifiutato() {
        Stats stats = new Stats(50, 10, 5);

        assertThrows(IllegalArgumentException.class, () -> stats.setCurrentHealth(60));
    }

    @Test
    void impostareLaVitaCorrenteNegativaVieneRifiutato() {
        Stats stats = new Stats(50, 10, 5);

        assertThrows(IllegalArgumentException.class, () -> stats.setCurrentHealth(-1));
    }

    @Test
    void laVitaMassimaNonPositivaVieneRifiutata() {
        assertThrows(IllegalArgumentException.class, () -> new Stats(0, 10, 5));
    }

    @Test
    void unDannoNegativoVieneRifiutato() {
        Stats stats = new Stats(40, 10, 5);

        assertThrows(IllegalArgumentException.class, () -> stats.takeDamage(-5));
    }
}
