package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Il modo in cui il protagonista reagisce a una paura.
 * <p>
 * Non esistono reazioni sbagliate: esistono conseguenze. Chimeris conta quante
 * volte il giocatore si affida a ciascuna, e da quel conteggio si forma
 * l'{@link AlterEgo} che lo attende in cima alla Torre.
 */
public enum Attitude {

    /** Colpire ciò che si teme: la paura viene distrutta, non capita. */
    AGGREDISCI,

    /** Scacciare o evitare: sollievo immediato, ma la paura resta e ritorna. */
    FUGGI,

    /** Restare, con calma e ragione, senza cedere né colpire. */
    RESISTI,

    /** Avvicinarsi a ciò che si teme e riconoscerlo: la via più difficile. */
    ACCOGLI
}
