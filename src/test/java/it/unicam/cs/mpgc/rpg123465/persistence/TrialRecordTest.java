package it.unicam.cs.mpgc.rpg123465.persistence;

import it.unicam.cs.mpgc.rpg123465.testing.FakeRecordStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrialRecordTest {

    private FakeRecordStore store;

    @BeforeEach
    void setUp() {
        store = new FakeRecordStore();
    }

    // --- primo risultato -------------------------------------------------

    @Test
    void senzaRecordPrecedenteNonCeNulla() {
        assertFalse(TrialRecord.lowerIsBetter(store, "buio.tempo").best().isPresent());
    }

    @Test
    void ilPrimoRisultatoDiventaSempreIlRecord() {
        TrialRecord record = TrialRecord.lowerIsBetter(store, "buio.tempo");

        assertEquals(75, record.submit(75));
        assertEquals(75, record.best().getAsInt());
    }

    // --- tempo: vince il piu' basso --------------------------------------

    @Test
    void unTempoMigliorePrendeIlPostoDelPrecedente() {
        TrialRecord record = TrialRecord.lowerIsBetter(store, "buio.tempo");

        record.submit(75);

        assertEquals(48, record.submit(48));
        assertEquals(48, record.best().getAsInt());
    }

    /**
     * Un tempo peggiore non deve toccare l'archivio: restituisce il record da
     * mostrare, che resta quello vecchio.
     */
    @Test
    void unTempoPeggioreLasciaIlRecordDovEra() {
        TrialRecord record = TrialRecord.lowerIsBetter(store, "buio.tempo");

        record.submit(48);
        int writesDopoIlPrimo = store.writes();

        assertEquals(48, record.submit(92));
        assertEquals(48, record.best().getAsInt());
        assertEquals(writesDopoIlPrimo, store.writes());
    }

    @Test
    void unTempoUgualeNonVieneRiscritto() {
        TrialRecord record = TrialRecord.lowerIsBetter(store, "buio.tempo");

        record.submit(48);
        int writes = store.writes();

        assertEquals(48, record.submit(48));
        assertEquals(writes, store.writes());
    }

    // --- punteggio: vince il piu' alto -----------------------------------

    @Test
    void unPunteggioMigliorePrendeIlPostoDelPrecedente() {
        TrialRecord record = TrialRecord.higherIsBetter(store, "altezze.punteggio");

        record.submit(120);

        assertEquals(340, record.submit(340));
        assertEquals(340, record.best().getAsInt());
    }

    @Test
    void unPunteggioPeggioreLasciaIlRecordDovEra() {
        TrialRecord record = TrialRecord.higherIsBetter(store, "altezze.punteggio");

        record.submit(340);

        assertEquals(340, record.submit(120));
        assertEquals(340, record.best().getAsInt());
    }

    /**
     * È l'errore che questa classe esiste per impedire: lo stesso numero vale
     * come record in un piano e non nell'altro, e il verso lo dichiara il piano
     * una volta sola.
     */
    @Test
    void ilVersoDelConfrontoDistingueITuePiani() {
        TrialRecord tempo = TrialRecord.lowerIsBetter(store, "buio.tempo");
        TrialRecord punteggio = TrialRecord.higherIsBetter(store, "altezze.punteggio");

        tempo.submit(100);
        punteggio.submit(100);

        assertEquals(50, tempo.submit(50));
        assertEquals(100, punteggio.submit(50));
    }

    // --- chiavi separate --------------------------------------------------

    @Test
    void iPianiNonSiSovrascrivonoIlRecord() {
        TrialRecord buio = TrialRecord.lowerIsBetter(store, "buio.tempo");
        TrialRecord topi = TrialRecord.lowerIsBetter(store, "topi.tempo");

        buio.submit(60);
        topi.submit(31);

        assertEquals(60, buio.best().getAsInt());
        assertEquals(31, topi.best().getAsInt());
    }

    // --- costruzione ------------------------------------------------------

    @Test
    void ilRecordRifiutaArchivioOChiaveMancanti() {
        assertThrows(IllegalArgumentException.class,
                () -> TrialRecord.lowerIsBetter(null, "buio.tempo"));

        assertThrows(IllegalArgumentException.class,
                () -> TrialRecord.higherIsBetter(store, null));

        assertThrows(IllegalArgumentException.class,
                () -> TrialRecord.higherIsBetter(store, "   "));
    }

    @Test
    void ilRecordSopravviveAUnaNuovaLetturaDelloStessoArchivio() {
        TrialRecord.lowerIsBetter(store, "topi.tempo").submit(44);

        assertTrue(TrialRecord.lowerIsBetter(store, "topi.tempo")
                .best().isPresent());
        assertEquals(44, TrialRecord.lowerIsBetter(store, "topi.tempo")
                .best().getAsInt());
    }
}
