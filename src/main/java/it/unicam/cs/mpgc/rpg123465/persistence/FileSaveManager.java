package it.unicam.cs.mpgc.rpg123465.persistence;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementazione di SaveManager basata su file binario.
 */
public class FileSaveManager implements SaveManager {

    private final String filePath;

    /**
     * Crea un gestore di salvataggio su file.
     *
     * @param filePath percorso del file di salvataggio
     */
    public FileSaveManager(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Il percorso del file non può essere vuoto.");
        }

        this.filePath = filePath;
    }

    @Override
    public void save(GameSave gameSave) throws IOException {
        if (gameSave == null) {
            throw new IllegalArgumentException("Il salvataggio non può essere null.");
        }

        Path path = Paths.get(filePath);
        Path parent = path.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (ObjectOutputStream outputStream =
                     new ObjectOutputStream(new FileOutputStream(filePath))) {
            outputStream.writeObject(gameSave);
        }
    }

    @Override
    public GameSave load() throws IOException, ClassNotFoundException {
        try (ObjectInputStream inputStream =
                     new ObjectInputStream(new FileInputStream(filePath))) {
            return (GameSave) inputStream.readObject();
        }
    }
}