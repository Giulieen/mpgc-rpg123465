package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.Tower;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test della factory {@link GameFactory}.
 */
class GameFactoryTest {

    @Test
    void laPartitaCreataHaGiocatoreETorreValidi() {
        GameEngine gameEngine = GameFactory.createNewGame();

        assertNotNull(gameEngine.getPlayer());
        assertNotNull(gameEngine.getTower());
        assertTrue(gameEngine.getPlayer().isAlive());
    }

    @Test
    void laTorreContieneSoloIlPrimoPiano() {
        Tower tower = GameFactory.createNewGame().getTower();

        assertEquals(1, tower.getTotalFloors());
        assertEquals("Paura del Fallimento", tower.getFloor(0).getName());
    }

    @Test
    void ogniPianoHaUnEventoAssociato() {
        Tower tower = GameFactory.createNewGame().getTower();

        for (int i = 0; i < tower.getTotalFloors(); i++) {
            assertNotNull(tower.getFloor(i).getEvent());
        }
    }
}
