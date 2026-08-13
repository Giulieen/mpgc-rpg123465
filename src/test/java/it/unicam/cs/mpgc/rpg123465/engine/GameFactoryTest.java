package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.FloorAttempts;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Prove sulla fabbrica della partita. A differenza degli altri test del motore
 * queste toccano il catalogo reale delle domande, perché la Torre viene
 * costruita con i contenuti veri dei tre piani.
 */
class GameFactoryTest {

    @Test
    void laTorreHaTrePiani() {
        GameEngine engine = GameFactory.createNewGame("Collaudo");

        assertEquals(3, engine.getTower().getTotalFloors());
    }

    @Test
    void iPianiSonoNumeratiInOrdineDaUno() {
        GameEngine engine = GameFactory.createNewGame("Collaudo");

        for (int i = 0; i < engine.getTower().getTotalFloors(); i++) {
            assertEquals(i + 1, engine.getTower().getFloor(i).getNumber());
        }
    }

    @Test
    void ogniPianoHaUnContenutoEUnTitolo() {
        GameEngine engine = GameFactory.createNewGame("Collaudo");

        for (int i = 0; i < engine.getTower().getTotalFloors(); i++) {
            assertNotNull(engine.getTower().getFloor(i).getContent());
            assertFalse(engine.getTower().getFloor(i).getContent().title().isBlank());
        }
    }

    @Test
    void laPartitaNasceAlPrimoPianoConITentativiInteri() {
        GameEngine engine = GameFactory.createNewGame("Collaudo");

        assertEquals(0, engine.getCurrentFloorIndex());
        assertFalse(engine.isGameCompleted());
        assertEquals(FloorAttempts.MAX, engine.getAttempts().getRemaining());
    }

    @Test
    void ilGiocatoreNasceSenzaAlcunaSceltaRegistrata() {
        GameEngine engine = GameFactory.createNewGame("Collaudo");

        assertEquals(0, engine.getPlayer().getMind().getTotalProfileChoices());
    }

    @Test
    void ilNomeVieneRipulitoDagliSpazi() {
        assertEquals("Giulia", GameFactory.createNewGame("  Giulia  ").getPlayer().getName());
    }

    @Test
    void unNomeMancanteRicadeSulPredefinito() {
        assertEquals("Viaggiatore", GameFactory.createNewGame(null).getPlayer().getName());
        assertEquals("Viaggiatore", GameFactory.createNewGame("").getPlayer().getName());
        assertEquals("Viaggiatore", GameFactory.createNewGame("   ").getPlayer().getName());
    }

    /**
     * Ogni partita costruisce la propria Torre: due motori non devono
     * condividere lo stato del giocatore.
     */
    @Test
    void duePartiteHannoStatoIndipendente() {
        GameEngine prima = GameFactory.createNewGame("Uno");
        GameEngine seconda = GameFactory.createNewGame("Due");

        prima.climb();
        prima.getAttempts().lose();

        assertEquals(0, seconda.getCurrentFloorIndex());
        assertEquals(FloorAttempts.MAX, seconda.getAttempts().getRemaining());
    }
}
