package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Il protagonista controllato dal giocatore.
 * <p>
 * Porta con sé due cose che la Torre logora in modi diversi: il corpo, nelle
 * {@link Stats} ereditate da {@link GameCharacter}, e la mente, nel
 * {@link MindState}. È quest'ultimo a ricordare il percorso da un piano
 * all'altro, e a decidere quale alter ego attenderà in cima.
 */
public class Player extends GameCharacter {

    /** Ciò che la Torre gli fa, e ciò che le sue scelte lo stanno facendo diventare. */
    private final MindState mind = new MindState();

    /**
     * Crea un nuovo giocatore.
     *
     * @param name nome del giocatore
     * @param stats statistiche del giocatore
     * @throws IllegalArgumentException se un parametro non è valido
     */
    public Player(String name, Stats stats) {
        super(name, stats);
    }

    /**
     * Restituisce lo stato interiore del giocatore: Lucidità, Stress e la
     * memoria di come ha reagito alle paure.
     *
     * @return stato interiore del giocatore
     */
    public MindState getMind() {
        return mind;
    }
}
