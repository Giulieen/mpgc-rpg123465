package it.unicam.cs.mpgc.rpg123465.persistence;

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
        GameSave originale = new GameSave("Viaggiatore", 3, 42, false);

        saveManager.save(originale);
        GameSave caricato = saveManager.load();

        assertEquals(originale.getPlayerName(), caricato.getPlayerName());
        assertEquals(originale.getCurrentFloor(), caricato.getCurrentFloor());
        assertEquals(originale.getCurrentHealth(), caricato.getCurrentHealth());
        assertEquals(originale.isGameCompleted(), caricato.isGameCompleted());
    }

    @Test
    void ilSalvataggioCreaLaCartellaMancante(@TempDir Path cartellaTemporanea) throws IOException {
        Path file = cartellaTemporanea.resolve("sotto/cartella/save.dat");
        SaveManager saveManager = new FileSaveManager(file.toString());

        saveManager.save(new GameSave("Viaggiatore", 1, 100, false));

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
