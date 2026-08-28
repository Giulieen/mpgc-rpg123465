package it.unicam.cs.mpgc.rpg123465.audio;

import it.unicam.cs.mpgc.rpg123465.testing.FakeSoundPlayer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica che la facciata {@link Sound} deleghi al servizio installato e che
 * il servizio sia davvero sostituibile.
 *
 * <p>
 * Nessuna di queste prove avvia il motore multimediale di JavaFX: è proprio il
 * motivo per cui l'astrazione esiste.
 */
class SoundTest {

    private FakeSoundPlayer fake;
    private SoundPlayer originale;

    @BeforeEach
    void installaIlServizioFinto() {
        originale = Sound.player();
        fake = new FakeSoundPlayer();
        Sound.use(fake);
    }

    @AfterEach
    void ripristinaIlServizioVero() {
        Sound.use(originale);
    }

    @Test
    void ilServizioInstallatoEQuelloRestituito() {
        assertSame(fake, Sound.player());
    }

    @Test
    void unServizioNullNonEAmmesso() {
        assertThrows(IllegalArgumentException.class, () -> Sound.use(null));
    }

    @Test
    void riprodurreUnEffettoArrivaAlServizio() {
        Sound.play("/audio/squeak.wav", 0.5);

        assertEquals(java.util.List.of("/audio/squeak.wav"), fake.played());
    }

    @Test
    void unEffettoNulloNonProduceNulla() {
        Sound.play((String) null, 0.5);

        assertTrue(fake.played().isEmpty());
    }

    @Test
    void ilCueViaggiaColProprioPercorso() {
        Sound.play(new SoundCue("/audio/scream.wav", 0.8, 0));

        assertEquals(java.util.List.of("/audio/scream.wav"), fake.played());
    }

    @Test
    void gliAmbientiSonoDistintiDagliEffetti() {
        Sound.loop("/audio/ambience-dark.mp3", 0.25);

        assertEquals(java.util.List.of("/audio/ambience-dark.mp3"), fake.looped());
        assertTrue(fake.played().isEmpty());
    }

    @Test
    void ilPrecaricamentoRicevaTuttiIPercorsi() {
        Sound.preload("/audio/arrow-tap.wav", "/audio/fall.wav");

        assertEquals(
                java.util.List.of("/audio/arrow-tap.wav", "/audio/fall.wav"),
                fake.preloaded()
        );
    }

    @Test
    void zittireTuttoArrivaAlServizio() {
        Sound.stopAll();

        assertEquals(1, fake.stopAllCount());
    }
}
