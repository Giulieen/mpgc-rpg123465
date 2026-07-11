package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.FloorContent;
import it.unicam.cs.mpgc.rpg123465.domain.Inventory;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.domain.Stats;
import it.unicam.cs.mpgc.rpg123465.domain.Tower;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test del motore di gioco {@link GameEngine}: la salita della Torre.
 */
class GameEngineTest {

    /** Un contenuto di piano qualunque, per provare la salita. */
    private record ContenutoDiProva(String title) implements FloorContent {
    }

    private GameEngine gameEngine;

    @BeforeEach
    void setUp() {
        gameEngine = GameFactory.createNewGame();
    }

    /** Una torre di prova con il numero di piani voluto. */
    private GameEngine torreDi(int piani) {
        Player giocatore = new Player("Prova", new Stats(100, 10, 5), new Inventory());

        List<Floor> elenco = new java.util.ArrayList<>();
        for (int i = 1; i <= piani; i++) {
            elenco.add(new Floor(i, "Piano " + i, new ContenutoDiProva("Prova")));
        }

        return new GameEngine(giocatore, new Tower(elenco));
    }

    @Test
    void unaNuovaPartitaIniziaDalPrimoPiano() {
        assertEquals(0, gameEngine.getCurrentFloorIndex());
        assertFalse(gameEngine.isGameCompleted());
        assertTrue(gameEngine.getPlayer().isAlive());
    }

    @Test
    void salireUnPianoPortaAQuelloSuccessivo() {
        GameEngine torre = torreDi(3);

        torre.climb();

        assertEquals(1, torre.getCurrentFloorIndex());
        assertFalse(torre.isGameCompleted());
    }

    @Test
    void salireDallUltimoPianoConcludeLaTorre() {
        GameEngine torre = torreDi(2);

        torre.climb();   // dal primo al secondo, che è l'ultimo
        assertTrue(torre.isOnLastFloor());
        assertFalse(torre.isGameCompleted());

        torre.climb();   // affrontato anche l'ultimo: la Torre è finita

        assertTrue(torre.isGameCompleted());
    }

    @Test
    void unaTorreGiaConclusaNonSaleOltre() {
        GameEngine torre = torreDi(1);

        torre.climb();   // unico piano: la Torre è già conclusa
        assertTrue(torre.isGameCompleted());

        torre.climb();   // non deve rompere nulla, né avanzare

        assertTrue(torre.isGameCompleted());
        assertEquals(0, torre.getCurrentFloorIndex());
    }

    @Test
    void ilPianoCorrenteEQuelloDellIndice() {
        GameEngine torre = torreDi(2);

        assertEquals("Piano 1", torre.getCurrentFloor().getName());
        torre.climb();
        assertEquals("Piano 2", torre.getCurrentFloor().getName());
    }

    @Test
    void ripristinareUnoStatoValidoAggiornaIlPianoCorrente() {
        GameEngine torre = torreDi(3);

        torre.restoreState(2, false);

        assertEquals(2, torre.getCurrentFloorIndex());
        assertFalse(torre.isGameCompleted());
        assertTrue(torre.isOnLastFloor());
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
