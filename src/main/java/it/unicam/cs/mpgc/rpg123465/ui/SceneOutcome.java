package it.unicam.cs.mpgc.rpg123465.ui;

/**
 * Come prosegue il cammino una volta conclusa una scena.
 */
public enum SceneOutcome {

    /**
     * Si passa alla scena successiva; se era l'ultima, il piano è superato.
     */
    AVANTI,

    /**
     * Si ricomincia la stessa scena.
     * <p>
     * È il caso delle scorciatoie che non portano da nessuna parte: la porta
     * che si apre sul vuoto rimette il giocatore dov'era, con qualcosa in meno.
     */
    RIPETI
}
