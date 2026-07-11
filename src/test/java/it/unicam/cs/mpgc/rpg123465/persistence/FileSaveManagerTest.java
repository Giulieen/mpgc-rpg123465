package it.unicam.cs.mpgc.rpg123465.persistence;

import it.unicam.cs.mpgc.rpg123465.domain.AlterEgo;
import it.unicam.cs.mpgc.rpg123465.domain.Attitude;
import it.unicam.cs.mpgc.rpg123465.domain.MindState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test dell'implementazione {@link FileSaveManager} basata su serializzazione.
 */
class FileSaveManagerTest {

    @Test
    void unSalvataggioRilettoMantieneGliStessiDati(@TempDir Path cartellaTemporanea) throws IOException, ClassNotFoundException {
        Path file = cartellaTemporanea.resolve("save.dat");
        SaveManager saveManager = new FileSaveManager(file.toString());
        GameSave originale = new GameSave("Viaggiatore", 3, 42, false, new MindState());

        saveManager.save(originale);
        GameSave caricato = saveManager.load();

        assertEquals(originale.getPlayerName(), caricato.getPlayerName());
        assertEquals(originale.getCurrentFloor(), caricato.getCurrentFloor());
        assertEquals(originale.getCurrentHealth(), caricato.getCurrentHealth());
        assertEquals(originale.isGameCompleted(), caricato.isGameCompleted());
    }

    @Test
    void ilPercorsoInterioreSopravviveAlSalvataggio(@TempDir Path cartellaTemporanea)
            throws IOException, ClassNotFoundException {
        Path file = cartellaTemporanea.resolve("save.dat");
        SaveManager saveManager = new FileSaveManager(file.toString());

        MindState mente = new MindState();
        mente.apply(-30, +45, Attitude.AGGREDISCI);
        mente.apply(0, 0, Attitude.AGGREDISCI);
        mente.apply(0, 0, Attitude.AGGREDISCI);

        saveManager.save(new GameSave("Viaggiatore", 1, 100, false, mente));
        MindState riletta = saveManager.load().getMindState();

        assertEquals(70, riletta.getLucidita());
        assertEquals(45, riletta.getStress());
        assertEquals(3, riletta.count(Attitude.AGGREDISCI));
        // Il percorso e' intatto: l'alter ego finale resta quello.
        assertEquals(AlterEgo.BESTIA, riletta.alterEgo());
    }

    @Test
    void ilSalvataggioCreaLaCartellaMancante(@TempDir Path cartellaTemporanea) throws IOException {
        Path file = cartellaTemporanea.resolve("sotto/cartella/save.dat");
        SaveManager saveManager = new FileSaveManager(file.toString());

        saveManager.save(new GameSave("Viaggiatore", 1, 100, false, new MindState()));

        assertTrue(file.toFile().exists());
    }

    @Test
    void salvareUnValoreNullVieneRifiutato(@TempDir Path cartellaTemporanea) {
        Path file = cartellaTemporanea.resolve("save.dat");
        SaveManager saveManager = new FileSaveManager(file.toString());

        assertThrows(IllegalArgumentException.class, () -> saveManager.save(null));
    }

    @Test
    void caricareDaUnFileInesistenteSollevaEccezione(@TempDir Path cartellaTemporanea) {
        Path file = cartellaTemporanea.resolve("inesistente.dat");
        SaveManager saveManager = new FileSaveManager(file.toString());

        assertThrows(IOException.class, saveManager::load);
    }

    @Test
    void unPercorsoVuotoVieneRifiutato() {
        assertThrows(IllegalArgumentException.class, () -> new FileSaveManager(" "));
    }
}
