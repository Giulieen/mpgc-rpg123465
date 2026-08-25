package it.unicam.cs.mpgc.rpg123465.model;

/**
 * Tratti che emergono dalle risposte ai dilemmi "Preferiresti".
 *
 * Il giocatore non vede i punti assegnati alle singole risposte:
 * vengono utilizzati internamente per determinare il profilo verso
 * cui sta andando.
 *
 * <p>
 * I tre nomi restano in italiano perché sono il vocabolario del gioco e non
 * identificatori qualunque: con questi compaiono nei profili che ne derivano e
 * nel catalogo {@code questions.json}, che associa ogni risposta al proprio
 * tratto. Ovunque altro gli identificatori sono in inglese e i commenti in
 * italiano.
 */
public enum ProfileTrait {

    /**
     * Tendenza ad affrontare situazioni difficili o spiacevoli.
     */
    CORAGGIO,

    /**
     * Tendenza a esplorare, conoscere e scoprire.
     */
    CURIOSITA,

    /**
     * Tendenza a scegliere esperienza, rischio e cambiamento.
     */
    AVVENTURA
}
