package it.unicam.cs.mpgc.rpg123465.persistence.save;

import it.unicam.cs.mpgc.rpg123465.model.MindState;
import it.unicam.cs.mpgc.rpg123465.model.ProfileTrait;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSaveManagerTest {

    @TempDir
    Path temp;

    private static MindState mindWith(int coraggio, int curiosita, int avventura) {
        MindState mind = new MindState();

        for (int i = 0; i < coraggio; i++) {
            mind.registerTrait(ProfileTrait.CORAGGIO);
        }
        for (int i = 0; i < curiosita; i++) {
            mind.registerTrait(ProfileTrait.CURIOSITA);
        }
        for (int i = 0; i < avventura; i++) {
            mind.registerTrait(ProfileTrait.AVVENTURA);
        }

        return mind;
    }

    @Test
    void primaDelPrimoSalvataggioNonEsisteNulla() {
        SaveManager manager = new FileSaveManager(temp.resolve("save.dat").toString());

        assertFalse(manager.exists());
    }

    @Test
    void dopoIlSalvataggioIlFileEsiste() throws Exception {
        SaveManager manager = new FileSaveManager(temp.resolve("save.dat").toString());

        manager.save(new GameSave("Giulia", 0, false, new MindState()));

        assertTrue(manager.exists());
    }

    @Test
    void ilSalvataggioCreaLeCartelleMancanti() throws Exception {
        Path nested = temp.resolve("uno").resolve("due").resolve("save.dat");
        SaveManager manager = new FileSaveManager(nested.toString());

        manager.save(new GameSave("Giulia", 0, false, new MindState()));

        assertTrue(Files.exists(nested));
    }

    @Test
    void ilSalvataggioSopravviveAlGiroCompleto() throws Exception {
        SaveManager manager = new FileSaveManager(temp.resolve("save.dat").toString());

        manager.save(new GameSave("Giulia", 2, true, mindWith(3, 1, 4)));
        GameSave loaded = manager.load();

        assertEquals("Giulia", loaded.getPlayerName());
        assertEquals(2, loaded.getCurrentFloor());
        assertTrue(loaded.isGameCompleted());
        assertEquals(3, loaded.getMindState().getCoraggio());
        assertEquals(1, loaded.getMindState().getCuriosita());
        assertEquals(4, loaded.getMindState().getAvventura());
    }

    @Test
    void unNuovoSalvataggioSostituisceIlPrecedente() throws Exception {
        SaveManager manager = new FileSaveManager(temp.resolve("save.dat").toString());

        manager.save(new GameSave("Primo", 0, false, new MindState()));
        manager.save(new GameSave("Secondo", 1, false, new MindState()));

        assertEquals("Secondo", manager.load().getPlayerName());
        assertEquals(1, manager.load().getCurrentFloor());
    }

    @Test
    void caricareUnFileInesistenteSollevaUnErroreDiIo() {
        SaveManager manager = new FileSaveManager(temp.resolve("assente.dat").toString());

        assertThrows(IOException.class, manager::load);
    }

    @Test
    void unFileCorrottoSollevaUnErroreDiIo() throws Exception {
        Path path = temp.resolve("corrotto.dat");
        Files.writeString(path, "non sono un flusso di oggetti", StandardCharsets.UTF_8);

        SaveManager manager = new FileSaveManager(path.toString());

        assertThrows(IOException.class, manager::load);
    }

    /**
     * Un file che si deserializza ma contiene altro non deve far uscire una
     * {@code ClassCastException}: è un errore di caricamento come gli altri.
     */
    @Test
    void unOggettoDiTipoSbagliatoDiventaUnErroreDiIo() throws Exception {
        Path path = temp.resolve("estraneo.dat");

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            out.writeObject("non sono un salvataggio");
        }

        SaveManager manager = new FileSaveManager(path.toString());

        IOException error = assertThrows(IOException.class, manager::load);
        assertFalse(error.getMessage().isBlank());
    }

    @Test
    void salvareNullNonEAmmesso() {
        SaveManager manager = new FileSaveManager(temp.resolve("save.dat").toString());

        assertThrows(IllegalArgumentException.class, () -> manager.save(null));
    }

    @Test
    void unPercorsoVuotoNonEAmmesso() {
        assertThrows(IllegalArgumentException.class, () -> new FileSaveManager(""));
        assertThrows(IllegalArgumentException.class, () -> new FileSaveManager(null));
    }
}
