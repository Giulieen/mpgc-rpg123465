package it.unicam.cs.mpgc.rpg123465.fear;

import java.util.List;

/**
 * Un incontro con una paura: la scena, la situazione narrata e le reazioni
 * possibili. È il "combattimento" del piano, declinato secondo la paura.
 *
 * @param title              titolo dell'incontro
 * @param backgroundResource percorso dell'immagine di sfondo nel classpath
 * @param situation          descrizione della situazione
 * @param initialStress      tensione già presente all'ingresso nella scena:
 *                           gestirla bene la fa scendere, reagire male la alza
 * @param choices            reazioni disponibili
 */
public record FearEncounter(
        String title,
        String backgroundResource,
        String situation,
        int initialStress,
        List<FearChoice> choices) {
}
