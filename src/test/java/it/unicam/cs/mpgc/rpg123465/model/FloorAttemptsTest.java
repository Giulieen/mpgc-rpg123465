package it.unicam.cs.mpgc.rpg123465.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FloorAttemptsTest {

    @Test
    void siEntraInUnPianoConTuttiITentativi() {
        FloorAttempts attempts = new FloorAttempts();

        assertEquals(FloorAttempts.MAX, attempts.getRemaining());
        assertEquals(FloorAttempts.MAX, attempts.getMax());
        assertTrue(attempts.hasLeft());
    }

    @Test
    void perdereUnTentativoNeToglieEsattamenteUno() {
        FloorAttempts attempts = new FloorAttempts();

        attempts.lose();

        assertEquals(FloorAttempts.MAX - 1, attempts.getRemaining());
        assertTrue(attempts.hasLeft());
    }

    @Test
    void esaurireITentativiChiudeLaProva() {
        FloorAttempts attempts = new FloorAttempts();

        for (int i = 0; i < FloorAttempts.MAX; i++) {
            attempts.lose();
        }

        assertEquals(0, attempts.getRemaining());
        assertFalse(attempts.hasLeft());
    }

    /**
     * Un piano può segnalare più errori dello stesso tentativo: il conteggio
     * si ferma a zero invece di andare in negativo.
     */
    @Test
    void ilConteggioNonScendeSottoZero() {
        FloorAttempts attempts = new FloorAttempts();

        for (int i = 0; i < FloorAttempts.MAX + 5; i++) {
            attempts.lose();
        }

        assertEquals(0, attempts.getRemaining());
        assertFalse(attempts.hasLeft());
    }

    @Test
    void ilResetRiportaITentativiAlMassimo() {
        FloorAttempts attempts = new FloorAttempts();
        attempts.lose();
        attempts.lose();

        attempts.reset();

        assertEquals(FloorAttempts.MAX, attempts.getRemaining());
        assertTrue(attempts.hasLeft());
    }

    @Test
    void ilResetFunzionaAncheDopoAverliEsauriti() {
        FloorAttempts attempts = new FloorAttempts();

        for (int i = 0; i < FloorAttempts.MAX; i++) {
            attempts.lose();
        }
        attempts.reset();

        assertEquals(FloorAttempts.MAX, attempts.getRemaining());
        assertTrue(attempts.hasLeft());
    }

    @Test
    void ilResetSuTentativiIntattiNonCambiaNulla() {
        FloorAttempts attempts = new FloorAttempts();

        attempts.reset();

        assertEquals(FloorAttempts.MAX, attempts.getRemaining());
    }

    @Test
    void ogniPianoConcedeTreTentativi() {
        assertEquals(3, FloorAttempts.MAX);
    }
}
