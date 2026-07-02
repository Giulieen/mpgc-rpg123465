package it.unicam.cs.mpgc.rpg123465.combat;

/**
 * Rappresenta le azioni che il giocatore può eseguire durante un combattimento.
 */
public enum CombatAction {

    /**
     * Attacca il nemico.
     */
    ATTACK,

    /**
     * Utilizza un oggetto dell'inventario.
     */
    USE_ITEM,

    /**
     * Tenta di fuggire dal combattimento.
     */
    ESCAPE
}