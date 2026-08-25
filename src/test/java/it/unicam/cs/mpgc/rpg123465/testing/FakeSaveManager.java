package it.unicam.cs.mpgc.rpg123465.testing;

import it.unicam.cs.mpgc.rpg123465.persistence.save.GameSave;
import it.unicam.cs.mpgc.rpg123465.persistence.save.SaveManager;

import java.io.IOException;

/**
 * Salvataggio in memoria, per verificare <em>cosa</em> viene scritto senza
 * toccare il disco.
 *
 * <p>
 * Può essere istruito a fallire in scrittura o in lettura, così i percorsi
 * d'errore di {@code GameController} si possono provare senza rendere
 * illeggibile un file vero.
 */
public final class FakeSaveManager implements SaveManager {

    private GameSave stored;
    private IOException saveFailure;
    private IOException loadFailure;

    private int saveCount;

    /** Precarica un salvataggio, come se esistesse già su disco. */
    public void preload(GameSave save) {
        this.stored = save;
    }

    public GameSave stored() {
        return stored;
    }

    public int saveCount() {
        return saveCount;
    }

    public void failOnSave(IOException failure) {
        this.saveFailure = failure;
    }

    public void failOnLoad(IOException failure) {
        this.loadFailure = failure;
    }

    @Override
    public void save(GameSave gameSave) throws IOException {
        if (saveFailure != null) {
            throw saveFailure;
        }

        stored = gameSave;
        saveCount++;
    }

    @Override
    public GameSave load() throws IOException {
        if (loadFailure != null) {
            throw loadFailure;
        }

        if (stored == null) {
            throw new IOException("Nessun salvataggio in memoria.");
        }

        return stored;
    }

    @Override
    public boolean exists() {
        return stored != null;
    }
}
