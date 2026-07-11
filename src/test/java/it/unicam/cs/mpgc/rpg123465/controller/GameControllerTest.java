package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.domain.AlterEgo;
import it.unicam.cs.mpgc.rpg123465.domain.Attitude;
import it.unicam.cs.mpgc.rpg123465.domain.MindState;
import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.persistence.FileSaveManager;
import it.unicam.cs.mpgc.rpg123465.persistence.SaveManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test del controller {@link GameController}.
 */
class GameControllerTest {

    @TempDir
    Path cartellaTemporanea;

    private GameController controller;

    @BeforeEach
    void setUp() {
        SaveManager saveManager = new FileSaveManager(
                cartellaTemporanea.resolve("save.dat").toString());
        controller = new GameController(GameFactory.createNewGame("Giulia"), saveManager);
    }

    // --- Costruzione ---------------------------------------------------------

    @Test
    void costruttoreConParametriNullVieneRifiutato() {
        SaveManager saveManager = new FileSaveManager(
                cartellaTemporanea.resolve("altro.dat").toString());

        assertThrows(IllegalArgumentException.class,
                () -> new GameController(null, saveManager));
        assertThrows(IllegalArgumentException.class,
                () -> new GameController(GameFactory.createNewGame(), null));
    }

    // --- Salita della Torre --------------------------------------------------

    @Test
    void laPartitaIniziaSulPrimoPianoENonEConclusa() {
        assertEquals("I Topi", controller.getCurrentFloor().getName());
        assertNotNull(controller.getCurrentFloor().getContent());
        assertFalse(controller.isGameCompleted());
    }

    @Test
    void affrontatoLUnicoPianoLaTorreEConclusa() {
        controller.climbToNextFloor();

        assertTrue(controller.isGameCompleted());
    }

    @Test
    void salireLeScaleRecuperaSoloInParte() {
        MindState mente = controller.getMind();
        mente.apply(-45, +70, Attitude.AGGREDISCI);   // lucidita' 55, stress 70

        controller.climbToNextFloor();

        // Le scale danno respiro (+20 lucidita', -25 stress), non guarigione.
        assertEquals(75, mente.getLucidita());
        assertEquals(45, mente.getStress());
    }

    // --- Stato del giocatore -------------------------------------------------

    @Test
    void ilControllerEsponeIlNomeELaMenteDelGiocatore() {
        assertEquals("Giulia", controller.getPlayerName());
        assertEquals(MindState.MAX, controller.getMind().getLucidita());
        assertEquals(0, controller.getMind().getStress());
    }

    @Test
    void laMenteEsposaEQuellaDelGiocatore() {
        controller.getMind().apply(-10, +20, Attitude.AGGREDISCI);

        // La vista modifica lo stesso stato che il dominio conserva: e' cosi'
        // che il gioco ricorda il percorso da un piano all'altro.
        assertEquals(90, controller.getMind().getLucidita());
        assertEquals(20, controller.getMind().getStress());
        assertEquals(1, controller.getMind().count(Attitude.AGGREDISCI));
    }

    @Test
    void lAlterEgoNasceDalleScelteCompiute() {
        assertEquals(AlterEgo.RIFLESSO, controller.getAlterEgo());

        for (int i = 0; i < 4; i++) {
            controller.getMind().apply(0, 0, Attitude.FUGGI);
        }

        assertEquals(AlterEgo.OMBRA, controller.getAlterEgo());
    }

    // --- Persistenza ---------------------------------------------------------

    @Test
    void senzaSalvataggiNonCEPartitaDaCaricare() {
        assertFalse(controller.hasSavedGame());
    }

    @Test
    void salvareRendeLaPartitaCaricabile() {
        controller.saveGame();

        assertTrue(controller.hasSavedGame());
    }

    @Test
    void ilPercorsoInterioreSopravviveAlCicloDiSalvataggio() {
        controller.getMind().apply(-30, +45, Attitude.AGGREDISCI);
        controller.getMind().apply(0, 0, Attitude.AGGREDISCI);
        controller.getMind().apply(0, 0, Attitude.AGGREDISCI);
        controller.saveGame();

        controller.loadGame();

        assertEquals(70, controller.getMind().getLucidita());
        assertEquals(45, controller.getMind().getStress());
        assertEquals(3, controller.getMind().count(Attitude.AGGREDISCI));
        // Il percorso e' intatto: l'alter ego resta quello che era.
        assertEquals(AlterEgo.BESTIA, controller.getAlterEgo());
    }

    @Test
    void ilNomeDelGiocatoreSopravviveAlCicloDiSalvataggio() {
        controller.saveGame();

        controller.loadGame();

        assertEquals("Giulia", controller.getPlayerName());
    }

    @Test
    void caricareSenzaSalvataggioRestituisceUnErrore() {
        String esito = controller.loadGame();

        assertTrue(esito.startsWith("Errore durante il caricamento"));
    }
}
