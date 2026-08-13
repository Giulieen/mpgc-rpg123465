package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Ciò che il giocatore affronta salendo su un piano della Torre.
 * <p>
 * Il dominio non ha bisogno di sapere <em>come</em> una prova si superi: gli
 * basta sapere che un piano ha un contenuto e come si chiama. Le forme concrete
 * vivono nel package {@code floors} — un labirinto, una serratura a tempo, una
 * traversata — e se ne possono aggiungere di nuove senza toccare la struttura
 * della Torre.
 */
public interface FloorContent {

    /**
     * @return il titolo del contenuto, mostrato al giocatore
     */
    String title();
}
