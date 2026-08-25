package it.unicam.cs.mpgc.rpg123465.model;

import it.unicam.cs.mpgc.rpg123465.model.FloorAttempts;
import it.unicam.cs.mpgc.rpg123465.model.Player;
import it.unicam.cs.mpgc.rpg123465.testing.TestTowers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineTest {

    @Test
    void unaPartitaNuovaIniziaDalPrimoPiano() {
        GameEngine engine = TestTowers.engineWithFloors(3);

        assertEquals(0, engine.getCurrentFloorIndex());
        assertEquals(1, engine.getCurrentFloor().getNumber());
        assertFalse(engine.isGameCompleted());
        assertFalse(engine.isOnLastFloor());
    }

    @Test
    void salireAvanzaDiUnPiano() {
        GameEngine engine = TestTowers.engineWithFloors(3);

        engine.climb();

        assertEquals(1, engine.getCurrentFloorIndex());
        assertFalse(engine.isGameCompleted());
    }

    @Test
    void salireDallUltimoPianoCompletaLaPartita() {
        GameEngine engine = TestTowers.engineWithFloors(3);

        engine.climb();
        engine.climb();
        assertTrue(engine.isOnLastFloor());

        engine.climb();

        assertTrue(engine.isGameCompleted());
        assertEquals(2, engine.getCurrentFloorIndex());
    }

    /**
     * Chiamare {@code climb()} a partita conclusa non deve far uscire l'indice
     * dalla Torre.
     */
    @Test
    void salireDopoIlCompletamentoNonCambiaNulla() {
        GameEngine engine = TestTowers.engineWithFloors(2);

        engine.climb();
        engine.climb();
        engine.climb();
        engine.climb();

        assertTrue(engine.isGameCompleted());
        assertEquals(1, engine.getCurrentFloorIndex());
    }

    // --- tentativi -------------------------------------------------------

    @Test
    void ogniPianoIniziaConITentativiAlMassimo() {
        GameEngine engine = TestTowers.engineWithFloors(3);

        assertEquals(FloorAttempts.MAX, engine.getAttempts().getRemaining());
    }

    /**
     * Ogni piano è una prova autonoma: quello che è costato il piano
     * precedente non si trascina dietro.
     */
    @Test
    void cambiandoPianoITentativiRipartonoDalMassimo() {
        GameEngine engine = TestTowers.engineWithFloors(3);

        engine.getAttempts().lose();
        engine.getAttempts().lose();
        assertEquals(1, engine.getAttempts().getRemaining());

        engine.climb();

        assertEquals(FloorAttempts.MAX, engine.getAttempts().getRemaining());
    }

    @Test
    void ripristinareUnaPartitaRiportaITentativiAlMassimo() {
        GameEngine engine = TestTowers.engineWithFloors(3);
        engine.getAttempts().lose();

        engine.restoreState(2, false);

        assertEquals(2, engine.getCurrentFloorIndex());
        assertEquals(FloorAttempts.MAX, engine.getAttempts().getRemaining());
    }

    // --- validazione -----------------------------------------------------

    @Test
    void ripristinareUnPianoInesistenteSollevaEccezione() {
        GameEngine engine = TestTowers.engineWithFloors(3);

        assertThrows(IllegalArgumentException.class, () -> engine.restoreState(3, false));
        assertThrows(IllegalArgumentException.class, () -> engine.restoreState(-1, false));
    }

    @Test
    void ripristinareUnaPartitaConclusaLaMantieneConclusa() {
        GameEngine engine = TestTowers.engineWithFloors(3);

        engine.restoreState(2, true);

        assertTrue(engine.isGameCompleted());
    }

    @Test
    void ilMotoreRifiutaGiocatoreOTorreMancanti() {
        assertThrows(IllegalArgumentException.class,
                () -> new GameEngine(null, TestTowers.withFloors(1)));

        assertThrows(IllegalArgumentException.class,
                () -> new GameEngine(new Player("Collaudo"), null));
    }
}
