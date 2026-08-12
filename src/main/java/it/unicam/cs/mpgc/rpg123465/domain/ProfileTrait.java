package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Tratti che emergono dalle risposte ai dilemmi "Preferiresti".
 *
 * Il giocatore non vede i punti assegnati alle singole risposte:
 * vengono utilizzati internamente per determinare il profilo verso
 * cui sta andando.
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