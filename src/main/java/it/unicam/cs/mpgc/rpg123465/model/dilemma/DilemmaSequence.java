package it.unicam.cs.mpgc.rpg123465.model.dilemma;

import java.util.List;
import java.util.Objects;

/**
 * Mantiene l'avanzamento ordinato di una sequenza di dilemmi e impedisce che
 * lo stesso elemento venga risolto più volte.
 *
 * <p>
 * Non decide quando un dilemma vada mostrato: quello resta della scena che la
 * possiede. Risponde soltanto a quale sia il dilemma corrente, se ne resti
 * qualcuno e quanti ne siano già stati risolti.
 */
public final class DilemmaSequence {

    private final List<Dilemma> dilemmas;

    private int resolved;

    /**
     * @param dilemmas domande da porre, nell'ordine in cui vanno poste; una
     *                 lista vuota produce una sequenza già esaurita
     */
    public DilemmaSequence(List<Dilemma> dilemmas) {
        if (dilemmas == null) {
            throw new IllegalArgumentException("La lista dei dilemmi non può essere null.");
        }

        if (dilemmas.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("La sequenza non ammette dilemmi null.");
        }

        this.dilemmas = List.copyOf(dilemmas);
    }

    /** @return true se resta almeno un dilemma non risolto */
    public boolean hasNext() {
        return resolved < dilemmas.size();
    }

    /** @return quanti dilemmi sono già stati risolti */
    public int resolvedCount() {
        return resolved;
    }

    /**
     * @return il dilemma non ancora risolto
     * @throws IllegalStateException se la sequenza è esaurita
     */
    public Dilemma current() {
        if (!hasNext()) {
            throw new IllegalStateException("La sequenza dei dilemmi è esaurita.");
        }

        return dilemmas.get(resolved);
    }

    /**
     * Consuma un dilemma, se è ancora quello corrente.
     *
     * <p>
     * Il dilemma va passato per intero perché è ciò che rende l'operazione
     * ripetibile senza danno: una seconda chiamata lo trova già superato
     * dall'indice e risponde false. Chi registra la scelta lo fa solo quando
     * questo metodo risponde true, così una risposta non può essere
     * conteggiata due volte.
     *
     * @param dilemma dilemma a cui si sta rispondendo
     * @return true se il dilemma è stato consumato adesso
     */
    public boolean resolve(Dilemma dilemma) {
        if (!hasNext() || !current().equals(dilemma)) {

            return false;
        }

        resolved++;

        return true;
    }
}
