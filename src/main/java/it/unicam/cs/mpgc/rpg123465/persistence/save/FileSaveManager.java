package it.unicam.cs.mpgc.rpg123465.persistence.save;

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
        /*
         * Il file è dichiarato come risorsa a sé: se la costruzione di
         * ObjectInputStream fallisce — ed è quello che succede con un file
         * corrotto, che è proprio il caso da gestire — un flusso annidato
         * resterebbe aperto e il file bloccato.
         */
        try (FileInputStream file = new FileInputStream(filePath);
             ObjectInputStream inputStream = new ObjectInputStream(file)) {

            Object loaded = inputStream.readObject();

            /*
             * Un cast diretto solleverebbe ClassCastException, che è una
             * RuntimeException e risalirebbe fino al gestore d'evento JavaFX
             * senza che nessuno la traduca in un messaggio. Un file che si
             * deserializza ma contiene altro è un errore di caricamento come
             * gli altri, e va segnalato allo stesso modo.
             */
            if (!(loaded instanceof GameSave save)) {
                throw new IOException(
                        "Il file di salvataggio non contiene dati validi per il gioco.");
            }

            return save;
        }
    }

    @Override
    public boolean exists() {
        return Files.exists(Paths.get(filePath));
    }
}
