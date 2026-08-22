package it.unicam.cs.mpgc.rpg123465.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;
import java.util.Properties;

/**
 * Record conservati in un unico file di properties.
 *
 * <p>
 * Un file di testo con una riga per prova basta e avanza: i record sono pochi,
 * piccoli e leggibili anche a mano. La scelta di tenerne uno solo, invece di un
 * file per piano, evita che ogni livello si scriva il proprio pezzo di
 * filesystem — che è esattamente come era nato il record delle Altezze.
 *
 * <p>
 * I record sono una comodità, non parte della partita: un errore di lettura o
 * scrittura non deve mai interrompere il gioco. In lettura si comporta come se
 * il record non esistesse, in scrittura rinuncia in silenzio.
 */
public final class FileRecordStore implements RecordStore {

    private static final String COMMENT = "Record personali di Tower of Self";

    private final Path path;

    /**
     * @param filePath percorso del file dei record
     */
    public FileRecordStore(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException(
                    "Il percorso del file dei record non può essere vuoto.");
        }

        this.path = Path.of(filePath);
    }

    @Override
    public OptionalInt best(String key) {
        requireKey(key);

        String value = load().getProperty(key);

        if (value == null) {
            return OptionalInt.empty();
        }

        try {
            return OptionalInt.of(Integer.parseInt(value.trim()));

        } catch (NumberFormatException e) {
            // Riga rovinata a mano: vale come record mai stabilito.
            return OptionalInt.empty();
        }
    }

    @Override
    public void save(String key, int value) {
        requireKey(key);

        Properties records = load();
        records.setProperty(key, Integer.toString(value));

        try {
            Path parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (OutputStream output = Files.newOutputStream(path)) {
                records.store(output, COMMENT);
            }

        } catch (IOException e) {
            // Il record è accessorio: un errore di I/O non ferma la partita.
        }
    }

    private Properties load() {
        Properties records = new Properties();

        if (!Files.exists(path)) {
            return records;
        }

        try (InputStream input = Files.newInputStream(path)) {
            records.load(input);

        } catch (IOException | IllegalArgumentException e) {
            // File illeggibile o rovinato: si riparte da nessun record.
        }

        return records;
    }

    private void requireKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("La chiave del record non può essere vuota.");
        }
    }
}
