package it.unicam.cs.mpgc.rpg123465.model;

/**
 * I tentativi disponibili per superare il piano corrente.
 *
 * <p>
 * Ogni piano è una prova autonoma: si entra sempre con il massimo, un errore
 * grave ne consuma uno, e a zero la prova è fallita. Non esiste alcun modo di
 * recuperarne durante il piano, e nulla viene trascinato da un piano all'altro.
 *
 * <p>
 * Non sono serializzati: un salvataggio rappresenta l'ingresso di un piano, e
 * all'ingresso i tentativi sono per definizione al massimo. Ricalcolarli è più
 * semplice e più sicuro che conservarli.
 */
public class FloorAttempts {

    /**
     * Tentativi concessi all'ingresso di ogni piano.
     *
     * È l'unico punto in cui questo numero è definito.
     */
    public static final int MAX = 3;

    private int remaining = MAX;

    /**
     * @return tentativi ancora disponibili
     */
    public int getRemaining() {
        return remaining;
    }

    /**
     * @return tentativi concessi all'ingresso di un piano
     */
    public int getMax() {
        return MAX;
    }

    /**
     * @return {@code true} se la prova può ancora continuare
     */
    public boolean hasLeft() {
        return remaining > 0;
    }

    /**
     * Consuma un tentativo per un errore grave.
     *
     * Sotto zero non si scende: a zero la prova è già fallita.
     */
    public void lose() {
        if (remaining > 0) {
            remaining--;
        }
    }

    /**
     * Riporta i tentativi al massimo.
     *
     * Va chiamato entrando in un piano e ricominciandolo dopo un fallimento.
     */
    public void reset() {
        remaining = MAX;
    }
}
