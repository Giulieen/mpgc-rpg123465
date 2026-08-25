package it.unicam.cs.mpgc.rpg123465.model.floors.buio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombinationLockStateTest {

    private final CombinationLockState lock = new CombinationLockState(4);

    private void enter(String code) {
        for (int i = 0; i < code.length(); i++) {
            int digit = code.charAt(i) - '0';

            for (int step = 0; step < digit; step++) {
                lock.increment();
            }

            lock.confirm();
        }
    }

    @Test
    void unaSerraturaNuovaMostraTuttiZeri() {
        assertEquals("0000", lock.enteredCode());
        assertEquals(0, lock.getActiveSlot());
        assertEquals(4, lock.size());
    }

    @Test
    void incrementareCambiaSoloLaCifraAttiva() {
        lock.increment();
        lock.increment();

        assertEquals("2000", lock.enteredCode());
        assertEquals(2, lock.digitAt(0));
        assertEquals(0, lock.digitAt(1));
    }

    @Test
    void leCifreScorronoInCerchio() {
        for (int i = 0; i < 10; i++) {
            lock.increment();
        }

        assertEquals(0, lock.digitAt(0));

        lock.decrement();

        assertEquals(9, lock.digitAt(0));
    }

    @Test
    void confermareSpostaSullaCifraSuccessiva() {
        assertTrue(lock.confirm());

        assertEquals(1, lock.getActiveSlot());
    }

    /**
     * L'ultima cifra non ha un successivo: confermarla segnala che il codice è
     * completo, ed è ciò che la scena usa per far scattare la verifica.
     */
    @Test
    void confermareLUltimaCifraSegnalaIlCodiceCompleto() {
        lock.confirm();
        lock.confirm();
        lock.confirm();

        assertFalse(lock.confirm());
        assertEquals(3, lock.getActiveSlot());
    }

    @Test
    void tornareIndietroDallaPrimaCifraNonEPossibile() {
        assertFalse(lock.moveBack());
        assertEquals(0, lock.getActiveSlot());
    }

    @Test
    void tornareIndietroRiportaSullaCifraPrecedente() {
        lock.confirm();
        lock.confirm();

        assertTrue(lock.moveBack());
        assertEquals(1, lock.getActiveSlot());
    }

    @Test
    void ilCodiceInseritoRispecchiaLeCifreScelte() {
        enter("3524");

        assertEquals("3524", lock.enteredCode());
    }

    @Test
    void ilCodiceHaSempreLaLunghezzaDellaSerratura() {
        lock.increment();

        assertEquals(4, lock.enteredCode().length());
    }

    @Test
    void ilResetRiportaCifreEPosizioneAllInizio() {
        enter("3524");

        lock.reset();

        assertEquals("0000", lock.enteredCode());
        assertEquals(0, lock.getActiveSlot());
    }

    /**
     * Dopo un tentativo fallito la scena ricostruisce il tentativo: la
     * serratura deve poter ripartire pulita e accettare un nuovo codice.
     */
    @Test
    void dopoUnResetSiPuoInserireUnNuovoCodice() {
        enter("1111");
        lock.reset();

        enter("9876");

        assertEquals("9876", lock.enteredCode());
    }

    @Test
    void unaSerraturaSenzaCifreNonHaSenso() {
        assertThrows(IllegalArgumentException.class, () -> new CombinationLockState(0));
        assertThrows(IllegalArgumentException.class, () -> new CombinationLockState(-1));
    }
}
