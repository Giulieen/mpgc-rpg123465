package it.unicam.cs.mpgc.rpg123465.persistence;

/**
 * Elenco dei nomi già usati su questa installazione.
 *
 * <p>
 * Serve a una cosa sola: impedire che due partite si presentino con lo stesso
 * nome. Vive accanto ai record e non dentro il salvataggio, perché deve
 * sopravvivere a "Nuova partita" — un nome usato resta usato anche quando la
 * partita che lo portava non c'è più.
 */
public interface PlayerRegistry {

    /**
     * @param name nome scelto dal giocatore
     * @return {@code true} se qualcuno lo ha già usato
     */
    boolean isTaken(String name);

    /**
     * Annota un nome come usato.
     *
     * @param name nome da riservare
     */
    void register(String name);
}
