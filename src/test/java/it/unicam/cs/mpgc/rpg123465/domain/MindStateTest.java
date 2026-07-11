package it.unicam.cs.mpgc.rpg123465.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MindStateTest {

    @Test
    void partePienoDiLuciditaESenzaStress() {
        MindState mind = new MindState();

        assertEquals(MindState.MAX, mind.getLucidita());
        assertEquals(0, mind.getStress());
        assertEquals(0, mind.getTotalChoices());
    }

    @Test
    void applicareUnaReazioneSpostaGliStatiERegistraLAtteggiamento() {
        MindState mind = new MindState();

        mind.apply(-10, +20, Attitude.AGGREDISCI);

        assertEquals(90, mind.getLucidita());
        assertEquals(20, mind.getStress());
        assertEquals(1, mind.count(Attitude.AGGREDISCI));
        assertEquals(1, mind.getTotalChoices());
    }

    @Test
    void gliStatiRestanoNeiLimiti() {
        MindState mind = new MindState();

        mind.apply(+50, -50, Attitude.ACCOGLI);   // gia' al massimo / gia' a zero

        assertEquals(MindState.MAX, mind.getLucidita());
        assertEquals(0, mind.getStress());
    }

    @Test
    void entrareInUnaStanzaSommaLaTensioneAQuellaGiaPresente() {
        MindState mind = new MindState();

        mind.enterRoom(45);
        mind.enterRoom(30);

        assertEquals(75, mind.getStress());
    }

    @Test
    void leScaleRecuperanoSoloInParte() {
        MindState mind = new MindState();
        mind.apply(-45, +70, Attitude.AGGREDISCI);   // lucidita' 55, stress 70

        mind.rest();

        // +20 lucidita', -25 stress: si tira il fiato, non si guarisce.
        assertEquals(75, mind.getLucidita());
        assertEquals(45, mind.getStress());
    }

    @Test
    void ilRiposoNonSuperaIlMassimo() {
        MindState mind = new MindState();
        mind.apply(-5, 0, Attitude.RESISTI);   // lucidita' 95

        mind.rest();

        assertEquals(MindState.MAX, mind.getLucidita());
    }

    @Test
    void luciditaAZeroSignificaEssereSopraffatti() {
        MindState mind = new MindState();

        mind.apply(-100, 0, Attitude.FUGGI);

        assertTrue(mind.isSopraffatto());
    }

    // --- Alter ego ----------------------------------------------------------

    @Test
    void senzaSceltaLAlterEgoEIlRiflesso() {
        assertEquals(AlterEgo.RIFLESSO, new MindState().alterEgo());
    }

    @Test
    void chiAggredisceSempreIncontraLaBestia() {
        MindState mind = new MindState();
        ripeti(mind, Attitude.AGGREDISCI, 4);

        assertEquals(AlterEgo.BESTIA, mind.alterEgo());
    }

    @Test
    void chiFuggeSempreIncontraLOmbra() {
        MindState mind = new MindState();
        ripeti(mind, Attitude.FUGGI, 4);

        assertEquals(AlterEgo.OMBRA, mind.alterEgo());
    }

    @Test
    void chiResisteSempreIncontraIlCustode() {
        MindState mind = new MindState();
        ripeti(mind, Attitude.RESISTI, 4);

        assertEquals(AlterEgo.CUSTODE, mind.alterEgo());
    }

    @Test
    void chiAccoglieSempreIncontraIlMediatore() {
        MindState mind = new MindState();
        ripeti(mind, Attitude.ACCOGLI, 4);

        assertEquals(AlterEgo.MEDIATORE, mind.alterEgo());
    }

    @Test
    void unaSolaSceltaBastaGiaADeterminareLAlterEgo() {
        // Con un solo piano affrontato, la reazione che hai avuto e' l'unica
        // che ti descrive: non puo' risultare un percorso "equilibrato".
        MindState bestia = new MindState();
        bestia.apply(0, 0, Attitude.AGGREDISCI);
        assertEquals(AlterEgo.BESTIA, bestia.alterEgo());

        MindState mediatore = new MindState();
        mediatore.apply(0, 0, Attitude.ACCOGLI);
        assertEquals(AlterEgo.MEDIATORE, mediatore.alterEgo());
    }

    @Test
    void unPareggioNonFaPrevalereNessunaForma() {
        MindState mind = new MindState();
        ripeti(mind, Attitude.AGGREDISCI, 2);
        ripeti(mind, Attitude.FUGGI, 2);
        ripeti(mind, Attitude.RESISTI, 2);
        ripeti(mind, Attitude.ACCOGLI, 2);

        assertEquals(AlterEgo.RIFLESSO, mind.alterEgo());
    }

    @Test
    void unaInclinazioneTroppoDebolePortaAlRiflesso() {
        MindState mind = new MindState();
        ripeti(mind, Attitude.RESISTI, 2);
        ripeti(mind, Attitude.ACCOGLI, 1);
        ripeti(mind, Attitude.FUGGI, 1);
        ripeti(mind, Attitude.AGGREDISCI, 1);

        // 2 su 5 = 40%: piu' frequente, ma sotto la quota. Ti sei distribuito.
        assertEquals(AlterEgo.RIFLESSO, mind.alterEgo());
    }

    @Test
    void unaChiaraInclinazioneFaPrevalereLAtteggiamento() {
        MindState mind = new MindState();
        ripeti(mind, Attitude.RESISTI, 3);
        ripeti(mind, Attitude.ACCOGLI, 2);

        // 3 su 5 = 60%: hai davvero pendenza verso il resistere.
        assertEquals(AlterEgo.CUSTODE, mind.alterEgo());
    }

    // --- Tratti derivati -----------------------------------------------------

    @Test
    void iTrattiDerivanoDaiContatoriDelleScelte() {
        MindState mind = new MindState();
        ripeti(mind, Attitude.ACCOGLI, 3);      // empatia 3
        ripeti(mind, Attitude.RESISTI, 2);
        ripeti(mind, Attitude.AGGREDISCI, 1);   // impulsivita' 1
        ripeti(mind, Attitude.FUGGI, 1);

        assertEquals(3, mind.getEmpatia());
        assertEquals(1, mind.getImpulsivita());
        // coraggio = resisti + accogli - fuggi = 2 + 3 - 1
        assertEquals(4, mind.getCoraggio());
    }

    @Test
    void fuggireSempreNonRendeIlCoraggioNegativo() {
        MindState mind = new MindState();
        ripeti(mind, Attitude.FUGGI, 5);

        assertEquals(0, mind.getCoraggio());
    }

    // --- Persistenza ---------------------------------------------------------

    @Test
    void ripristinareRiportaStatiEScelteDelSalvataggio() {
        MindState salvato = new MindState();
        salvato.apply(-30, +40, Attitude.AGGREDISCI);
        ripeti(salvato, Attitude.AGGREDISCI, 3);

        MindState caricato = new MindState();
        caricato.restoreFrom(salvato);

        assertEquals(salvato.getLucidita(), caricato.getLucidita());
        assertEquals(salvato.getStress(), caricato.getStress());
        assertEquals(4, caricato.count(Attitude.AGGREDISCI));
        assertEquals(AlterEgo.BESTIA, caricato.alterEgo());
    }

    @Test
    void ripristinareDaUnSalvataggioVecchioNonRompeNulla() {
        MindState mind = new MindState();

        mind.restoreFrom(null);   // salvataggio di una versione precedente

        assertEquals(MindState.MAX, mind.getLucidita());
    }

    private void ripeti(MindState mind, Attitude attitude, int volte) {
        for (int i = 0; i < volte; i++) {
            mind.apply(0, 0, attitude);
        }
    }
}
