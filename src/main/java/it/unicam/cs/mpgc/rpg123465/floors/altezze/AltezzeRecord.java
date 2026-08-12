package it.unicam.cs.mpgc.rpg123465.floors.altezze;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gestisce il miglior punteggio ottenuto nel Piano III.
 *
 * Questa implementazione è volutamente semplice e temporanea:
 * il record viene salvato in un piccolo file di testo.
 */
final class AltezzeRecord {

    private static final Path RECORD_PATH =
            Path.of("saves", "altezze-record.txt");

    private AltezzeRecord() {
        // Classe di utilità.
    }

    /**
     * Carica il record salvato.
     *
     * @return record precedente oppure 0 se non esiste o non è leggibile
     */
    static int load() {
        if (!Files.exists(RECORD_PATH)) {
            return 0;
        }

        try {
            String content = Files.readString(RECORD_PATH).trim();

            if (content.isEmpty()) {
                return 0;
            }

            int value = Integer.parseInt(content);

            return Math.max(0, value);

        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Salva il record.
     *
     * @param value punteggio migliore ottenuto
     */
    static void save(int value) {
        if (value < 0) {
            return;
        }

        try {
            Path parent = RECORD_PATH.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(
                    RECORD_PATH,
                    Integer.toString(value)
            );

        } catch (IOException e) {
            // Il record è una funzionalità accessoria:
            // un errore di I/O non deve interrompere la partita.
        }
    }
}