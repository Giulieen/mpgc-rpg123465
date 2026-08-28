package it.unicam.cs.mpgc.rpg123465.view.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Prove sul contratto dello sfondo che non richiedono il toolkit JavaFX.
 *
 * <p>
 * Costruire la vista caricherebbe un'immagine, e caricare un'immagine avvia
 * JavaFX, che in una prova automatica non c'è. I controlli sugli argomenti
 * però vengono prima del caricamento, apposta: un percorso vuoto o una
 * velocità negativa sono errori di chi chiama, e devono farsi sentire subito
 * invece di trasformarsi in uno sfondo fermo o che scorre all'indietro.
 */
class ScrollingBackgroundTest {

    @Test
    void unPercorsoVuotoNonEAmmesso() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ScrollingBackground("   ", 35)
        );
    }

    @Test
    void unaVelocitaNegativaNonEAmmessa() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ScrollingBackground("/images/bg/forest.jpg", -1)
        );
    }

    @Test
    void unImmagineInesistenteVieneSegnalataSubito() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ScrollingBackground("/images/bg/non-esiste.jpg", 35)
        );
    }
}
