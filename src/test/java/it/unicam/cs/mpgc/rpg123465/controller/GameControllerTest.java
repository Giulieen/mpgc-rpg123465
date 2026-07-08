package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.combat.CombatAction;
import it.unicam.cs.mpgc.rpg123465.domain.Item;
import it.unicam.cs.mpgc.rpg123465.domain.ItemType;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.persistence.FileSaveManager;
import it.unicam.cs.mpgc.rpg123465.persistence.SaveManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        controller = new GameController(GameFactory.createNewGame(), saveManager);
    }

    @Test
    void eseguireIlPrimoEventoAvviaIlCombattimento() {
        assertFalse(controller.isCombatActive());

        controller.executeStandardEvent();

        assertTrue(controller.isCombatActive());
        assertTrue(controller.isCurrentEventExecuted());
    }

    @Test
    void unTurnoNonRisolutivoLasciaIlCombattimentoAttivo() {
        controller.executeStandardEvent();

        String message = controller.executeCombatAction(CombatAction.ATTACK);

        assertEquals("Il combattimento continua.", message);
        assertTrue(controller.isCombatActive());
    }

    @Test
    void unAzioneDiCombattimentoSenzaNemicoVieneSegnalata() {
        String message = controller.executeCombatAction(CombatAction.ATTACK);

        assertEquals("Nessun combattimento attivo.", message);
    }

    @Test
    void usareUnOggettoSenzaAverneVieneSegnalato() {
        controller.executeStandardEvent();

        String message = controller.executeCombatAction(CombatAction.USE_ITEM);

        assertEquals("Non hai oggetti curativi da usare.", message);
        assertTrue(controller.isCombatActive());
    }

    @Test
    void usareUnOggettoCurativoLoConsuma() {
        GameEngine engine = GameFactory.createNewGame();
        engine.getPlayer().addItem(
                new Item("Pozione di Lucidità", "Ripristina la calma.", ItemType.HEALING, 30));
        SaveManager saveManager = new FileSaveManager(
                cartellaTemporanea.resolve("uso.dat").toString());
        GameController controller = new GameController(engine, saveManager);
        controller.executeStandardEvent();

        String message = controller.executeCombatAction(CombatAction.USE_ITEM);

        assertTrue(message.startsWith("Usi Pozione di Lucidità"));
        assertFalse(engine.getPlayer().hasHealingItem());
        assertTrue(controller.isCombatActive());
    }

    @Test
    void laFugaTerminaIlCombattimento() {
        controller.executeStandardEvent();

        controller.executeCombatAction(CombatAction.ESCAPE);

        assertFalse(controller.isCombatActive());
    }

    @Test
    void laSceltaDiForzaRiduceLaVitaDelGiocatore() {
        int vitaIniziale = controller.getPlayerCurrentHealth();

        controller.resolveDialogueChoice("Reagisci con forza");

        assertEquals(vitaIniziale - 10, controller.getPlayerCurrentHealth());
        assertTrue(controller.isCurrentEventExecuted());
    }

    @Test
    void avanzareDiPianoAzzeraIlCombattimento() {
        controller.executeStandardEvent();
        assertTrue(controller.isCombatActive());

        controller.advanceFloor();

        assertFalse(controller.isCombatActive());
        assertEquals(2, controller.getCurrentFloor().getNumber());
    }

    @Test
    void salvareECaricareRipristinaIlPianoCorrente() {
        controller.advanceFloor();
        controller.advanceFloor();
        assertEquals("Partita salvata correttamente.", controller.saveGame());

        GameController altro = new GameController(
                GameFactory.createNewGame(),
                new FileSaveManager(cartellaTemporanea.resolve("save.dat").toString()));

        assertEquals("Partita caricata correttamente.", altro.loadGame());
        assertEquals(controller.getCurrentFloor().getNumber(), altro.getCurrentFloor().getNumber());
    }

    @Test
    void costruttoreConParametriNullVieneRifiutato() {
        SaveManager saveManager = new FileSaveManager(
                cartellaTemporanea.resolve("altro.dat").toString());

        assertThrows(IllegalArgumentException.class,
                () -> new GameController(null, saveManager));
        assertThrows(IllegalArgumentException.class,
                () -> new GameController(GameFactory.createNewGame(), null));
    }
}
