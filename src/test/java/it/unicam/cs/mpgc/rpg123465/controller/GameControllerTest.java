package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.domain.FloorAttempts;
import it.unicam.cs.mpgc.rpg123465.domain.MindState;
import it.unicam.cs.mpgc.rpg123465.domain.PlayerProfile;
import it.unicam.cs.mpgc.rpg123465.domain.ProfileTrait;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import it.unicam.cs.mpgc.rpg123465.persistence.GameSave;
import it.unicam.cs.mpgc.rpg123465.testing.FakeSaveManager;
import it.unicam.cs.mpgc.rpg123465.testing.TestTowers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameControllerTest {

    private FakeSaveManager saves;
    private GameEngine engine;
    private GameController controller;

    @BeforeEach
    void setUp() {
        saves = new FakeSaveManager();
        engine = TestTowers.engineWithFloors(3);
        controller = new GameController(engine, saves);
    }

    private void answer(ProfileTrait trait) {
        controller.getMind().registerTrait(trait);
    }

    // --- checkpoint ------------------------------------------------------

    /**
     * È il contratto centrale del salvataggio: il file rappresenta l'ingresso
     * del piano, non il momento in cui si preme Salva.
     */
    @Test
    void salvareDopoUnCheckpointScriveLoStatoDIngresso() {
        controller.beginFloorCheckpoint();
        answer(ProfileTrait.CORAGGIO);
        answer(ProfileTrait.CURIOSITA);

        assertTrue(controller.saveGame().success());

        assertEquals(0, saves.stored().getMindState().getTotalProfileChoices());
        assertEquals(2, controller.getMind().getTotalProfileChoices());
    }

    @Test
    void salvareSenzaCheckpointScriveLoStatoCorrente() {
        answer(ProfileTrait.AVVENTURA);

        assertTrue(controller.saveGame().success());

        assertEquals(1, saves.stored().getMindState().getAvventura());
    }

    @Test
    void ilCheckpointRestaValidoPerPiuSalvataggiSuccessivi() {
        controller.beginFloorCheckpoint();
        answer(ProfileTrait.CORAGGIO);

        controller.saveGame();
        answer(ProfileTrait.CORAGGIO);
        controller.saveGame();

        assertEquals(2, saves.saveCount());
        assertEquals(0, saves.stored().getMindState().getTotalProfileChoices());
    }

    /**
     * Salire azzera il checkpoint: il piano successivo deve fissare il proprio,
     * altrimenti si tornerebbe indietro di un piano ricaricando.
     */
    @Test
    void salireDiPianoAzzeraIlCheckpointPrecedente() {
        controller.beginFloorCheckpoint();
        answer(ProfileTrait.CORAGGIO);

        controller.climbToNextFloor();
        controller.saveGame();

        assertEquals(1, saves.stored().getCurrentFloor());
        assertEquals(1, saves.stored().getMindState().getTotalProfileChoices());
    }

    @Test
    void ilCheckpointRegistraIlPianoCorrente() {
        controller.climbToNextFloor();
        controller.beginFloorCheckpoint();
        controller.saveGame();

        assertEquals(1, saves.stored().getCurrentFloor());
    }

    // --- stato della partita ---------------------------------------------

    @Test
    void salireAggiornaIlPianoCorrente() {
        assertEquals(1, controller.getCurrentFloor().getNumber());

        controller.climbToNextFloor();

        assertEquals(2, controller.getCurrentFloor().getNumber());
        assertFalse(controller.isGameCompleted());
    }

    @Test
    void salireDallUltimoPianoCompletaLaPartita() {
        controller.climbToNextFloor();
        controller.climbToNextFloor();
        controller.climbToNextFloor();

        assertTrue(controller.isGameCompleted());
    }

    @Test
    void ilProfiloSeguesceLeScelteRegistrate() {
        answer(ProfileTrait.CORAGGIO);
        answer(ProfileTrait.CORAGGIO);
        answer(ProfileTrait.CORAGGIO);
        answer(ProfileTrait.CORAGGIO);

        assertEquals(PlayerProfile.CORAGGIOSO, controller.getPlayerProfile());
    }

    // --- tentativi -------------------------------------------------------

    @Test
    void laProvaSiPerdeSoloQuandoITentativiFiniscono() {
        assertFalse(controller.isDefeated());

        controller.loseAttempt();
        controller.loseAttempt();
        assertFalse(controller.isDefeated());
        assertTrue(controller.hasAttemptsLeft());

        controller.loseAttempt();

        assertTrue(controller.isDefeated());
        assertFalse(controller.hasAttemptsLeft());
        assertEquals(0, controller.getRemainingAttempts());
    }

    @Test
    void ricominciareLaProvaRestituisceTuttiITentativi() {
        controller.loseAttempt();
        controller.loseAttempt();
        controller.loseAttempt();

        controller.resetAttempts();

        assertEquals(FloorAttempts.MAX, controller.getRemainingAttempts());
        assertFalse(controller.isDefeated());
    }

    @Test
    void ilMassimoDeiTentativiEQuelloDelDominio() {
        assertEquals(FloorAttempts.MAX, controller.getMaxAttempts());
    }

    // --- caricamento -----------------------------------------------------

    @Test
    void senzaFileNonRisultaAlcunSalvataggio() {
        assertFalse(controller.hasSavedGame());
    }

    @Test
    void dopoUnSalvataggioIlFileRisultaPresente() {
        controller.saveGame();

        assertTrue(controller.hasSavedGame());
    }

    @Test
    void caricareRipristinaPianoETratti() {
        MindState salvato = new MindState();
        salvato.registerTrait(ProfileTrait.CURIOSITA);
        salvato.registerTrait(ProfileTrait.CURIOSITA);
        salvato.registerTrait(ProfileTrait.AVVENTURA);

        saves.preload(new GameSave("Ripreso", 1, false, salvato));

        assertTrue(controller.loadGame().success());

        assertEquals(1, controller.getCurrentFloor().getNumber() - 1);
        assertEquals("Ripreso", controller.getPlayerName());
        assertEquals(2, controller.getMind().getCuriosita());
        assertEquals(1, controller.getMind().getAvventura());
    }

    /**
     * Un salvataggio fotografa l'ingresso di un piano, dove i tentativi sono
     * per definizione interi: non vengono serializzati e vanno ricalcolati.
     */
    @Test
    void caricareRiportaITentativiAlMassimo() {
        saves.preload(new GameSave("Ripreso", 0, false, new MindState()));
        controller.loseAttempt();
        controller.loseAttempt();

        controller.loadGame();

        assertEquals(FloorAttempts.MAX, controller.getRemainingAttempts());
    }

    @Test
    void caricareUnaPartitaConclusaLaMantieneConclusa() {
        saves.preload(new GameSave("Ripreso", 2, true, new MindState()));

        controller.loadGame();

        assertTrue(controller.isGameCompleted());
    }

    @Test
    void dopoIlCaricamentoIlProfiloRiflettePureLeScelteSalvate() {
        MindState salvato = new MindState();
        for (int i = 0; i < 5; i++) {
            salvato.registerTrait(ProfileTrait.AVVENTURA);
        }

        saves.preload(new GameSave("Ripreso", 0, false, salvato));
        controller.loadGame();

        assertEquals(PlayerProfile.AVVENTURIERO, controller.getPlayerProfile());
    }

    // --- errori ----------------------------------------------------------

    @Test
    void unErroreDiScritturaDiventaUnEsitoNegativoConDettaglio() {
        saves.failOnSave(new IOException("disco pieno"));

        OperationResult result = controller.saveGame();

        assertFalse(result.success());
        assertEquals("disco pieno", result.detail());
    }

    @Test
    void unErroreDiLetturaDiventaUnEsitoNegativoSenzaPropagarsi() {
        saves.failOnLoad(new IOException("file illeggibile"));

        OperationResult result = controller.loadGame();

        assertFalse(result.success());
        assertNotNull(result.detail());
    }

    /**
     * Un salvataggio con un piano che la Torre non ha deve essere respinto come
     * esito negativo, non far uscire un'eccezione verso l'interfaccia.
     */
    @Test
    void unPianoNonValidoNelFileNonFaUscireEccezioni() {
        saves.preload(new GameSave("Rotto", 99, false, new MindState()));

        OperationResult result = controller.loadGame();

        assertFalse(result.success());
    }

    @Test
    void ilFallimentoDiUnCaricamentoLasciaLaPartitaGiocabile() {
        saves.failOnLoad(new IOException("guasto"));

        controller.loadGame();

        assertEquals(1, controller.getCurrentFloor().getNumber());
        assertEquals(FloorAttempts.MAX, controller.getRemainingAttempts());
    }

    @Test
    void ilControllerRifiutaMotoreOSalvataggioMancanti() {
        assertThrows(IllegalArgumentException.class,
                () -> new GameController(null, saves));

        assertThrows(IllegalArgumentException.class,
                () -> new GameController(engine, null));
    }
}
