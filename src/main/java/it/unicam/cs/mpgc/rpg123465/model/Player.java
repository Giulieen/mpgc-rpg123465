package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Il protagonista controllato dal giocatore.
 * <p>
 * Porta con sé una cosa sola che attraversa la Torre: la memoria delle proprie
 * risposte, nel {@link MindState}, da cui nasce il profilo finale. Quanto gli
 * resta per superare la prova corrente non è suo ma del piano, e vive nei
 * tentativi.
 */
public class Player extends GameCharacter {

    /** Ciò che le sue scelte lo stanno facendo diventare. */
    private final MindState mind = new MindState();

    /**
     * Crea un nuovo giocatore.
     *
     * @param name nome del giocatore
     * @throws IllegalArgumentException se il nome non è valido
     */
    public Player(String name) {
        super(name);
    }

    /**
     * Restituisce la memoria delle risposte date ai dilemmi.
     *
     * @return conteggi dei tratti accumulati dal giocatore
     */
    public MindState getMind() {
        return mind;
    }
}
