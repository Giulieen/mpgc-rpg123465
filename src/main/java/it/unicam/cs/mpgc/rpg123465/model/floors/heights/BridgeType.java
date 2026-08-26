package it.unicam.cs.mpgc.rpg123465.model.floors.heights;

/**
 * Identifica le tre tipologie di ponte disponibili nel Piano III.
 *
 * Ogni tipo rappresenta un diverso compromesso fra velocità,
 * tempo di risposta e sicurezza.
 */
public enum BridgeType {

    /**
     * Ponte stretto e rapido.
     */
    FAST,

    /**
     * Ponte con caratteristiche intermedie.
     */
    BALANCED,

    /**
     * Ponte più lento ma con tempi di risposta più permissivi.
     */
    SAFE
}
