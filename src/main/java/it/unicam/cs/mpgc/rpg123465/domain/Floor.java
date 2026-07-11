package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Rappresenta un piano della Torre: una paura, e ciò che il giocatore vi
 * affronta.
 */
public class Floor {

    private final int number;
    private final String name;
    private final FloorContent content;

    /**
     * Crea un nuovo piano.
     *
     * @param number numero del piano, a partire da uno
     * @param name nome della paura custodita dal piano
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

    /**
     * Restituisce il numero del piano.
     *
     * @return numero del piano
     */
    public int getNumber() {
        return number;
    }

    /**
     * Restituisce il nome della paura custodita dal piano.
     *
     * @return nome del piano
     */
    public String getName() {
        return name;
    }

    /**
     * Restituisce ciò che il giocatore affronta su questo piano.
     *
     * @return contenuto del piano
     */
    public FloorContent getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Piano " + number + " - " + name;
    }
}
