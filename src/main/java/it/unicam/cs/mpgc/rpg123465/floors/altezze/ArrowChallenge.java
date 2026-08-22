package it.unicam.cs.mpgc.rpg123465.floors.altezze;

import java.util.Random;

/**
 * La prova delle frecce del Piano III: sceglie la direzione da mostrare,
 * giudica la risposta e tiene il punteggio della traversata in corso.
 *
 * <p>
 * Non conosce né il tempo né lo schermo: quando mostrare una freccia, per
 * quanto lasciarla e cosa disegnare restano della scena. Qui vivono soltanto
 * le regole — quale direzione può uscire, quale risposta è giusta e quanto
 * vale — così si possono provare senza avviare l'interfaccia.
 */
public final class ArrowChallenge {

    /**
     * Quante volte di fila può ripetersi la stessa direzione.
     *
     * Senza questo limite la casualità produce sequenze che sembrano guaste:
     * cinque frecce identiche di seguito il giocatore le legge come un errore
     * del gioco, non come sfortuna.
     */
    private static final int MAX_SAME_DIRECTION = 2;

    /** Punti di una freccia indovinata, prima del bonus di serie. */
    private static final int HIT_POINTS = 10;

    private final Random rng;

    private ArrowDirection last;
    private int sameCount;

    private ArrowDirection current;

    private int streak;
    private int score;
    private int hits;
    private int misses;

    public ArrowChallenge(Random rng) {
        if (rng == null) {
            throw new IllegalArgumentException("La sorgente casuale non può essere null.");
        }

        this.rng = rng;
    }

    /**
     * Sceglie la prossima direzione e la rende quella in attesa di risposta.
     *
     * @return la direzione da mostrare al giocatore
     */
    public ArrowDirection show() {
        ArrowDirection[] all = ArrowDirection.values();

        ArrowDirection chosen;

        do {
            chosen = all[rng.nextInt(all.length)];

        } while (chosen == last && sameCount >= MAX_SAME_DIRECTION);

        if (chosen == last) {
            sameCount++;
        } else {
            last = chosen;
            sameCount = 1;
        }

        current = chosen;

        return chosen;
    }

    /** @return true se una freccia è a schermo e attende una risposta */
    public boolean isWaiting() {
        return current != null;
    }

    /**
     * Chiude la freccia in corso e aggiorna il punteggio.
     *
     * <p>
     * Il tasto giusto è quello <em>opposto</em> alla freccia mostrata: si
     * spinge nella direzione contraria per non perdere l'equilibrio.
     *
     * @param pressed direzione premuta, oppure null se il tempo è scaduto
     * @return true se la risposta era giusta
     */
    public boolean resolve(ArrowDirection pressed) {
        if (current == null) {
            return false;
        }

        boolean correct =
                pressed != null
                        && pressed == current.opposite();

        current = null;

        if (correct) {
            hits++;
            streak++;
            score += HIT_POINTS + streak;
        } else {
            misses++;
            streak = 0;
        }

        return correct;
    }

    /**
     * Toglie di mezzo la freccia in corso senza giudicarla.
     *
     * Serve quando la prova viene interrotta da qualcosa che non dipende dal
     * giocatore — il cambio di ponte — dove segnare un errore sarebbe ingiusto.
     */
    public void discard() {
        current = null;
    }

    /** Ricomincia da capo la traversata, azzerando anche la serie. */
    public void restart() {
        current = null;
        last = null;

        sameCount = 0;
        streak = 0;
        score = 0;
        hits = 0;
        misses = 0;
    }

    public int score() {
        return score;
    }

    public int streak() {
        return streak;
    }

    public int hits() {
        return hits;
    }

    public int misses() {
        return misses;
    }
}
