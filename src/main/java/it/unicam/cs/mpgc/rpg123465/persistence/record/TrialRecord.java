package it.unicam.cs.mpgc.rpg123465.persistence.record;

import java.util.OptionalInt;

/**
 * Il record di una singola prova, con il verso del confronto.
 *
 * <p>
 * {@link RecordStore} conserva numeri senza sapere quale sia il migliore: per
 * un tempo vince il più basso, per un punteggio il più alto. Questa classe
 * lega una chiave al suo verso, così ogni piano dichiara la regola una volta
 * sola invece di riscrivere il confronto a ogni esito.
 */
public final class TrialRecord {

    private final RecordStore store;
    private final String key;
    private final boolean lowerWins;

    private TrialRecord(RecordStore store, String key, boolean lowerWins) {
        if (store == null || key == null || key.isBlank()) {

            throw new IllegalArgumentException("Archivio e chiave del record sono obbligatori.");
        }

        this.store = store;
        this.key = key;
        this.lowerWins = lowerWins;
    }

    /** Record in cui vince il valore più basso, come un tempo di fuga. */
    public static TrialRecord lowerIsBetter(RecordStore store, String key) {
        return new TrialRecord(store, key, true);
    }

    /** Record in cui vince il valore più alto, come un punteggio. */
    public static TrialRecord higherIsBetter(RecordStore store, String key) {
        return new TrialRecord(store, key, false);
    }

    /** @return il record attuale, vuoto se la prova non è mai stata superata */
    public OptionalInt best() {
        return store.best(key);
    }

    /**
     * Registra un risultato, conservandolo solo se batte il precedente.
     *
     * @param value risultato appena ottenuto
     * @return il record da mostrare al giocatore dopo questa prova
     */
    public int submit(int value) {
        OptionalInt previous = best();

        if (previous.isEmpty() || beats(value, previous.getAsInt())) {

            store.save(key, value);
            return value;
        }

        return previous.getAsInt();
    }

    private boolean beats(int value, int previous) {
        return lowerWins
                ? value < previous
                : value > previous;
    }
}
