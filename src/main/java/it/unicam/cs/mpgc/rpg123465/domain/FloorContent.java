package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Ciò che il giocatore affronta salendo su un piano della Torre.
 * <p>
 * Il dominio non ha bisogno di sapere <em>come</em> una paura si affronti: gli
 * basta sapere che un piano ha un contenuto e come si chiama. Le forme concrete
 * vivono altrove (un incontro con una paura, e in futuro una stanza da
 * esplorare o una fuga a tempo), così se ne possono aggiungere di nuove senza
 * toccare la struttura della Torre.
 */
public interface FloorContent {

    /**
     * @return il titolo del contenuto, mostrato al giocatore
     */
    String title();
}
