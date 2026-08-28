package it.unicam.cs.mpgc.rpg123465.view.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Prove sull'aggancio della tastiera che non richiedono una {@code Scene}
 * viva.
 *
 * <p>
 * Costruire una {@code Scene} avvierebbe il toolkit JavaFX, che in una prova
 * automatica non c'è: qui si verifica il contratto che si può verificare senza
 * di esso — il rifiuto di un gestore nullo e il fatto che rilasciare senza mai
 * essersi agganciati non sia un errore, perché il riordino di fine piano passa
 * da lì anche quando la prova non è mai partita.
 */
class KeyboardBindingTest {

    @Test
    void unGestoreNulloNonEAmmesso() {
        assertThrows(IllegalArgumentException.class, () -> new KeyboardBinding(null));
    }

    @Test
    void rilasciareSenzaEssersiAgganciatiNonFaDanno() {
        KeyboardBinding binding = new KeyboardBinding(event -> { });

        binding.release();
        binding.release();
    }

    @Test
    void agganciarsiANullEquivaleARilasciare() {
        KeyboardBinding binding = new KeyboardBinding(event -> { });

        binding.bindTo(null);
        binding.release();
    }
}
