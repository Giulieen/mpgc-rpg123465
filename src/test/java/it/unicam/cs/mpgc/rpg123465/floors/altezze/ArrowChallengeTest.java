package it.unicam.cs.mpgc.rpg123465.floors.altezze;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrowChallengeTest {

    private ArrowChallenge arrows;

    @BeforeEach
    void setUp() {
        arrows = new ArrowChallenge(new Random(7));
    }

    /** Come fa la scena: mostra una freccia e risponde con quella giusta. */
    private void indovina() {
        ArrowDirection shown = arrows.show();
        arrows.resolve(shown.opposite());
    }

    private void sbaglia() {
        ArrowDirection shown = arrows.show();
        arrows.resolve(shown);
    }

    // --- risposta giusta e sbagliata ---------------------------------------

    /** Si spinge nella direzione contraria alla freccia: è la regola del piano. */
    @Test
    void laRispostaGiustaEQuellaOppostaAllaFreccia() {
        ArrowDirection shown = arrows.show();

        assertTrue(arrows.resolve(shown.opposite()));
    }

    @Test
    void premereLaStessaDirezioneDellaFrecciaEUnErrore() {
        ArrowDirection shown = arrows.show();

        assertFalse(arrows.resolve(shown));
    }

    /** Il tempo scaduto arriva come risposta nulla e vale come errore. */
    @Test
    void ilTempoScadutoValeComeErrore() {
        arrows.show();

        assertFalse(arrows.resolve(null));
        assertEquals(1, arrows.misses());
    }

    // --- punteggio ---------------------------------------------------------

    /**
     * Il punteggio non è lineare: ogni freccia vale dieci punti più la lunghezza
     * della serie, quindi indovinare di fila rende molto più che alternare.
     */
    @Test
    void laSerieFaCrescereIlValoreDiOgniFreccia() {
        indovina();
        assertEquals(11, arrows.score());

        indovina();
        assertEquals(11 + 12, arrows.score());

        indovina();
        assertEquals(11 + 12 + 13, arrows.score());
    }

    @Test
    void unErroreAzzeraLaSerieMaNonIlPunteggio() {
        indovina();
        indovina();
        int prima = arrows.score();

        sbaglia();

        assertEquals(0, arrows.streak());
        assertEquals(prima, arrows.score());
    }

    @Test
    void dopoUnErroreLaSerieRicominciaDaUno() {
        indovina();
        indovina();
        sbaglia();

        int prima = arrows.score();
        indovina();

        assertEquals(1, arrows.streak());
        assertEquals(prima + 11, arrows.score());
    }

    @Test
    void indovinateESbagliateVengonoContateSeparatamente() {
        indovina();
        sbaglia();
        indovina();
        indovina();

        assertEquals(3, arrows.hits());
        assertEquals(1, arrows.misses());
    }

    // --- freccia in attesa -------------------------------------------------

    @Test
    void nessunaFrecciaAttendeFinchePrimaNonNeVieneMostrataUna() {
        assertFalse(arrows.isWaiting());

        arrows.show();

        assertTrue(arrows.isWaiting());
    }

    /**
     * Risolvere due volte la stessa freccia non deve valere due punteggi: è la
     * stessa garanzia che protegge i dilemmi dal doppio clic.
     */
    @Test
    void unaFrecciaGiaRisoltaNonPuoEssereRisoltaDiNuovo() {
        ArrowDirection shown = arrows.show();
        arrows.resolve(shown.opposite());

        int dopoLaPrima = arrows.score();

        assertFalse(arrows.resolve(shown.opposite()));
        assertEquals(dopoLaPrima, arrows.score());
        assertEquals(1, arrows.hits());
    }

    /** Scartare non è sbagliare: il cambio di ponte non costa un errore. */
    @Test
    void scartareUnaFrecciaNonContaComeErrore() {
        indovina();
        int prima = arrows.score();

        arrows.show();
        arrows.discard();

        assertFalse(arrows.isWaiting());
        assertEquals(0, arrows.misses());
        assertEquals(1, arrows.streak());
        assertEquals(prima, arrows.score());
    }

    // --- scelta delle direzioni --------------------------------------------

    /**
     * L'invariante che rende la sequenza credibile: la stessa direzione non
     * può uscire più di due volte di fila, altrimenti il giocatore legge la
     * ripetizione come un guasto del gioco.
     */
    @Test
    void laStessaDirezioneNonEsceMaiPiuDiDueVolteDiFila() {
        ArrowChallenge lunga = new ArrowChallenge(new Random(3));

        ArrowDirection precedente = null;
        int consecutive = 0;

        for (int i = 0; i < 3000; i++) {
            ArrowDirection shown = lunga.show();
            lunga.resolve(shown.opposite());

            consecutive = shown == precedente ? consecutive + 1 : 1;
            precedente = shown;

            assertTrue(consecutive <= 2,
                    "tre volte di fila la stessa direzione al giro " + i);
        }
    }

    @Test
    void tutteLeDirezioniPossonoUscire() {
        ArrowChallenge lunga = new ArrowChallenge(new Random(11));

        boolean[] viste = new boolean[ArrowDirection.values().length];

        for (int i = 0; i < 500; i++) {
            ArrowDirection shown = lunga.show();
            lunga.resolve(shown.opposite());
            viste[shown.ordinal()] = true;
        }

        for (int i = 0; i < viste.length; i++) {
            assertTrue(viste[i], ArrowDirection.values()[i] + " non esce mai");
        }
    }

    // --- ricominciare ------------------------------------------------------

    @Test
    void ricominciareAzzeraPunteggioSerieEContatori() {
        indovina();
        indovina();
        sbaglia();

        arrows.restart();

        assertEquals(0, arrows.score());
        assertEquals(0, arrows.streak());
        assertEquals(0, arrows.hits());
        assertEquals(0, arrows.misses());
        assertFalse(arrows.isWaiting());
    }

    @Test
    void laProvaRifiutaUnaSorgenteCasualeMancante() {
        assertThrows(IllegalArgumentException.class,
                () -> new ArrowChallenge(null));
    }
}
