package it.unicam.cs.mpgc.rpg123465.persistence.record;

import java.util.OptionalInt;

/**
 * Archivio dei record personali delle prove.
 *
 * <p>
 * È separato da {@link it.unicam.cs.mpgc.rpg123465.persistence.save.SaveManager SaveManager} perché risponde a una domanda diversa. Un
 * salvataggio è <em>una partita</em> e viene sostituito iniziandone un'altra;
 * un record è il migliore risultato mai ottenuto su questa installazione e deve
 * sopravvivere a "Nuova partita".
 *
 * <p>
 * L'archivio non sa cosa renda un risultato migliore di un altro: per il tempo
 * di una fuga vince il valore più basso, per un punteggio il più alto. Il
 * confronto appartiene al piano, che è l'unico a conoscere il significato del
 * numero; qui si conserva soltanto.
 */
public interface RecordStore {

    /**
     * Legge il record associato a una chiave.
     *
     * @param key identificatore della prova, ad esempio {@code "buio.tempo"}
     * @return il valore memorizzato, oppure vuoto se non esiste
     */
    OptionalInt best(String key);

    /**
     * Memorizza un valore, sostituendo quello precedente.
     *
     * Va chiamato solo dopo aver stabilito che il nuovo risultato è migliore.
     *
     * @param key identificatore della prova
     * @param value valore da conservare
     */
    void save(String key, int value);
}
