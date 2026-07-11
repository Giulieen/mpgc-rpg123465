package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.MindState;
import it.unicam.cs.mpgc.rpg123465.domain.Tower;
import it.unicam.cs.mpgc.rpg123465.fear.FearEncounter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    void unaNuovaPartitaIniziaConLaMenteIntatta() {
        MindState mente = GameFactory.createNewGame().getPlayer().getMind();

        assertEquals(MindState.MAX, mente.getLucidita());
        assertEquals(0, mente.getStress());
        assertEquals(0, mente.getTotalChoices());
    }

    @Test
    void unNomeVuotoRicadeSuQuelloPredefinito() {
        assertEquals("Viaggiatore", GameFactory.createNewGame(" ").getPlayer().getName());
        assertEquals("Viaggiatore", GameFactory.createNewGame(null).getPlayer().getName());
    }

    @Test
    void laTorreContieneSoloIlPrimoPiano() {
        Tower tower = GameFactory.createNewGame().getTower();

        assertEquals(1, tower.getTotalFloors());
        assertEquals("I Topi", tower.getFloor(0).getName());
    }

    @Test
    void ogniPianoCustodisceIlProprioContenuto() {
        Tower tower = GameFactory.createNewGame().getTower();

        for (int i = 0; i < tower.getTotalFloors(); i++) {
            assertNotNull(tower.getFloor(i).getContent());
        }

        // Il Piano I è un incontro con una paura.
        assertInstanceOf(FearEncounter.class, tower.getFloor(0).getContent());
    }
}
