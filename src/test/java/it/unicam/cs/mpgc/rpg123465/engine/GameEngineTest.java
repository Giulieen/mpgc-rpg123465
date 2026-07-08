package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.events.EventResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test del motore di gioco {@link GameEngine}.
 */
class GameEngineTest {

    private GameEngine gameEngine;

    @BeforeEach
    void setUp() {
        gameEngine = GameFactory.createNewGame();
    }

    @Test
    void unaNuovaPartitaIniziaDalPrimoPiano() {
        assertEquals(0, gameEngine.getCurrentFloorIndex());
        assertFalse(gameEngine.isGameCompleted());
        assertTrue(gameEngine.getPlayer().isAlive());
    }

    @Test
    void laTorreContieneUnSoloPiano() {
        assertEquals(1, gameEngine.getTower().getTotalFloors());
        assertTrue(gameEngine.isOnLastFloor());
    }

    @Test
    void eseguireUnEventoRestituisceUnRisultato() {
        EventResult result = gameEngine.executeCurrentFloorEvent();

        assertNotNull(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void raggiungereLUltimoPianoImpedisceUlterioriAvanzamenti() {
        while (!gameEngine.isOnLastFloor()) {
            gameEngine.advanceFloor();
        }

        assertTrue(gameEngine.isOnLastFloor());
        assertThrows(IllegalStateException.class, () -> gameEngine.advanceFloor());
    }

    @Test
    void completareLUltimoPianoConclusioneLaPartita() {
        while (!gameEngine.isOnLastFloor()) {
            gameEngine.advanceFloor();
        }

        gameEngine.executeCurrentFloorEvent();

        assertTrue(gameEngine.isGameCompleted());
    }

    @Test
    void ripristinareUnoStatoValidoAggiornaIlPianoCorrente() {
        gameEngine.restoreState(0, false);

        assertEquals(0, gameEngine.getCurrentFloorIndex());
        assertFalse(gameEngine.isGameCompleted());
    }

    @Test
    void ripristinareUnIndiceNonValidoVieneRifiutato() {
        assertThrows(IllegalArgumentException.class, () -> gameEngine.restoreState(-1, false));
        assertThrows(IllegalArgumentException.class, () -> gameEngine.restoreState(999, false));
    }

    @Test
    void costruttoreConParametriNullVieneRifiutato() {
        assertThrows(IllegalArgumentException.class,
                () -> new GameEngine(null, gameEngine.getTower()));
        assertThrows(IllegalArgumentException.class,
                () -> new GameEngine(gameEngine.getPlayer(), null));
    }
}
