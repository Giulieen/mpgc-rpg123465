package it.unicam.cs.mpgc.rpg123465.engine;

/**
 * Rappresenta lo stato corrente del flusso di gioco.
 * <p>
 * Lo stato guida ciò che il giocatore può fare in un dato momento e permette
 * alla vista di mostrare solo i comandi pertinenti.
 */
public enum GameState {

    /** Il giocatore esplora il piano e può eseguirne l'evento. */
    EXPLORING,

    /** È in corso un'interazione di dialogo. */
    DIALOGUE,

    /** È in corso un combattimento con un nemico. */
    COMBAT,

    /** È stato appena trovato un oggetto sul piano. */
    ITEM,

    /** La partita è stata completata con successo. */
    VICTORY,

    /** Il giocatore è stato sconfitto. */
    GAME_OVER
}
