package it.unicam.cs.mpgc.rpg123465.fear;

import it.unicam.cs.mpgc.rpg123465.domain.Attitude;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test delle reazioni a una paura e del prezzo che comportano.
 */
class FearChoiceTest {

    private FearChoice reazione(int danno) {
        return new FearChoice("Gesto", "Conseguenza.", danno, 0, 0,
                Attitude.RESISTI, List.of(), false);
    }

    @Test
    void unaReazioneCheFerisceLoDichiara() {
        assertTrue(reazione(8).hurts());
        assertEquals(8, reazione(8).damage());
    }

    @Test
    void unaReazioneCheNonFerisceNonLasciaSegniSulCorpo() {
        assertFalse(reazione(0).hurts());
    }

    @Test
    void unDannoNegativoVieneRifiutato() {
        // Una reazione può ferire, non curare: il segno del danno sarebbe
        // ambiguo e trasformerebbe una ferita in una guarigione.
        assertThrows(IllegalArgumentException.class, () -> reazione(-5));
    }

    @Test
    void iTrePrezziRestanoDistinti() {
        FearChoice scelta = new FearChoice("Fuggi", "Ti azzanna.", 8, -2, -12,
                Attitude.FUGGI, List.of(), false);

        // Il corpo, la mente e la tensione del momento si pagano a parte.
        assertEquals(8, scelta.damage());
        assertEquals(-2, scelta.lucidityDelta());
        assertEquals(-12, scelta.stressDelta());
    }
}
