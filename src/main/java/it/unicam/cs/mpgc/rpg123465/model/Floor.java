package it.unicam.cs.mpgc.rpg123465.model;

/**
 * Rappresenta un piano della Torre: un luogo, e la prova che vi si affronta.
 */
public class Floor {

    private final int number;
    private final String name;
    private final FloorContent content;

    /**
     * Crea un nuovo piano.
     *
     * @param number numero del piano, a partire da uno
     * @param name nome del piano, come compare al giocatore
     * @param content ciò che il giocatore vi affronta
     * @throws IllegalArgumentException se un parametro non è valido
     */
    public Floor(int number, String name, FloorContent content) {
        if (number <= 0) {
            throw new IllegalArgumentException("Il numero del piano deve essere positivo.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Il nome del piano non può essere vuoto.");
        }
        if (content == null) {
            throw new IllegalArgumentException("Il contenuto del piano non può essere null.");
        }

        this.number = number;
        this.name = name;
        this.content = content;
    }

    /** @return numero del piano, a partire da uno */
    public int getNumber() {
        return number;
    }

    /**
     * @return nome del piano, come compare al giocatore
     */
    public String getName() {
        return name;
    }

    /** @return la prova che si affronta su questo piano */
    public FloorContent getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Piano " + number + " - " + name;
    }
}
