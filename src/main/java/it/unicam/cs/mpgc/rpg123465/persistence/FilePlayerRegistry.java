package it.unicam.cs.mpgc.rpg123465.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Nomi già usati, conservati in un file di testo con un nome per riga.
 *
 * <p>
 * Il confronto ignora maiuscole e spazi ai bordi: "Giulia" e " giulia " sono la
 * stessa persona per chiunque tranne che per {@code equals}. Il file conserva
 * però il nome come è stato scritto, perché è quello che il giocatore rivedrà.
 *
 * <p>
 * Un errore di lettura o scrittura non ferma la partita: nel peggiore dei casi
 * un nome viene riproposto, il che è meno grave che non poter giocare.
 */
public final class FilePlayerRegistry implements PlayerRegistry {

    private final Path path;

    /**
     * @param filePath percorso del file dei nomi
     */
    public FilePlayerRegistry(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Il percorso del registro non può essere vuoto.");
        }

        this.path = Path.of(filePath);
    }

    @Override
    public boolean isTaken(String name) {
        String key = key(name);

        if (key.isEmpty()) {
            return false;
        }

        return load().stream()
                .map(this::key)
                .anyMatch(key::equals);
    }

    @Override
    public void register(String name) {
        String trimmed = name == null ? "" : name.trim();

        if (trimmed.isEmpty() || isTaken(trimmed)) {
            return;
        }

        try {
            Path parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(
                    path,
                    trimmed + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (IOException exception) {
            // Il registro è una comodità: un errore di I/O non ferma il gioco.
        }
    }

    private Set<String> load() {
        if (!Files.exists(path)) {
            return Set.of();
        }

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

            Set<String> names = new LinkedHashSet<>();

            for (String line : lines) {
                if (!line.isBlank()) {
                    names.add(line.trim());
                }
            }

            return names;

        } catch (IOException exception) {
            return Set.of();
        }
    }

    private String key(String name) {
        return name == null
                ? ""
                : name.trim().toLowerCase(Locale.ROOT);
    }
}
