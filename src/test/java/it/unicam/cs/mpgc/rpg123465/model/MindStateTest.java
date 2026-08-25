package it.unicam.cs.mpgc.rpg123465.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MindStateTest {

    private static MindState with(int coraggio, int curiosita, int avventura) {
        MindState mind = new MindState();

        for (int i = 0; i < coraggio; i++) {
            mind.registerTrait(ProfileTrait.CORAGGIO);
        }
        for (int i = 0; i < curiosita; i++) {
            mind.registerTrait(ProfileTrait.CURIOSITA);
        }
        for (int i = 0; i < avventura; i++) {
            mind.registerTrait(ProfileTrait.AVVENTURA);
        }

        return mind;
    }

    @Test
    void unNuovoStatoNonHaAlcunaSceltaRegistrata() {
        MindState mind = new MindState();

        assertEquals(0, mind.getTotalProfileChoices());
        assertEquals(0, mind.getCoraggio());
        assertEquals(0, mind.getCuriosita());
        assertEquals(0, mind.getAvventura());
    }

    @Test
    void registrareUnTrattoIncrementaSoloQuelTratto() {
        MindState mind = new MindState();

        mind.registerTrait(ProfileTrait.CORAGGIO);

        assertEquals(1, mind.getCoraggio());
        assertEquals(0, mind.getCuriosita());
        assertEquals(0, mind.getAvventura());
        assertEquals(1, mind.getTotalProfileChoices());
    }

    @Test
    void ogniTrattoHaIlProprioContatoreIndipendente() {
        MindState mind = with(2, 3, 4);

        assertEquals(2, mind.getCoraggio());
        assertEquals(3, mind.getCuriosita());
        assertEquals(4, mind.getAvventura());
        assertEquals(9, mind.getTotalProfileChoices());
    }

    @Test
    void registrareUnTrattoNullSollevaEccezione() {
        MindState mind = new MindState();

        assertThrows(IllegalArgumentException.class, () -> mind.registerTrait(null));
        assertEquals(0, mind.getTotalProfileChoices());
    }

    // --- profile() -------------------------------------------------------

    @Test
    void senzaRisposteIlProfiloNonPuoEssereDeterminato() {
        assertEquals(PlayerProfile.IMPREVEDIBILE, new MindState().profile());
    }

    @Test
    void coraggioDominanteDaIlCoraggioso() {
        assertEquals(PlayerProfile.CORAGGIOSO, with(5, 1, 1).profile());
    }

    @Test
    void curiositaDominanteDaIlCurioso() {
        assertEquals(PlayerProfile.CURIOSO, with(1, 5, 1).profile());
    }

    @Test
    void avventuraDominanteDaLAvventuriero() {
        assertEquals(PlayerProfile.AVVENTURIERO, with(1, 1, 5).profile());
    }

    @Test
    void coraggioECuriositaVicineDannoLEsploratore() {
        assertEquals(PlayerProfile.ESPLORATORE, with(4, 3, 1).profile());
    }

    @Test
    void coraggioEAvventuraVicineDannoIlRisoluto() {
        assertEquals(PlayerProfile.RISOLUTO, with(4, 1, 3).profile());
    }

    @Test
    void curiositaEAvventuraVicineDannoIlVisionario() {
        assertEquals(PlayerProfile.VISIONARIO, with(1, 4, 3).profile());
    }

    @Test
    void trattiRavvicinatiDannoLImprevedibile() {
        assertEquals(PlayerProfile.IMPREVEDIBILE, with(3, 3, 2).profile());
    }

    /**
     * Un pareggio esatto fra due tratti dominanti deve risolversi nel profilo
     * combinato, non nel tratto che capita prima nell'ordine di valutazione.
     */
    @Test
    void unPareggioFraDueTrattiDaIlProfiloCombinato() {
        assertEquals(PlayerProfile.ESPLORATORE, with(4, 4, 0).profile());
        assertEquals(PlayerProfile.RISOLUTO, with(4, 0, 4).profile());
        assertEquals(PlayerProfile.VISIONARIO, with(0, 4, 4).profile());
    }

    /**
     * L'algoritmo deve restare totale: nessuna combinazione può restare senza
     * risposta, qualunque sia il numero di scelte registrate.
     */
    @Test
    void ogniCombinazioneDiOttoRisposteProduceUnProfilo() {
        for (int coraggio = 0; coraggio <= 8; coraggio++) {
            for (int curiosita = 0; curiosita <= 8 - coraggio; curiosita++) {
                int avventura = 8 - coraggio - curiosita;

                assertEquals(8, with(coraggio, curiosita, avventura).getTotalProfileChoices());
                org.junit.jupiter.api.Assertions.assertNotNull(
                        with(coraggio, curiosita, avventura).profile(),
                        "profilo mancante per " + coraggio + "/" + curiosita + "/" + avventura);
            }
        }
    }

    @Test
    void ilProfiloEStabileFraChiamateSuccessive() {
        MindState mind = with(4, 2, 1);

        assertEquals(mind.profile(), mind.profile());
    }

    // --- copy() e restoreFrom() ------------------------------------------

    @Test
    void laCopiaHaGliStessiConteggiMaEUnOggettoDiverso() {
        MindState originale = with(2, 1, 3);
        MindState copia = originale.copy();

        assertNotSame(originale, copia);
        assertEquals(2, copia.getCoraggio());
        assertEquals(1, copia.getCuriosita());
        assertEquals(3, copia.getAvventura());
    }

    /**
     * Il checkpoint dei piani poggia su questa indipendenza: modificare lo
     * stato vivo non deve toccare la fotografia presa all'ingresso.
     */
    @Test
    void modificareLOriginaleNonAlteraLaCopia() {
        MindState originale = with(1, 1, 1);
        MindState copia = originale.copy();

        originale.registerTrait(ProfileTrait.CORAGGIO);

        assertEquals(2, originale.getCoraggio());
        assertEquals(1, copia.getCoraggio());
    }

    @Test
    void modificareLaCopiaNonAlteraLOriginale() {
        MindState originale = with(1, 1, 1);
        MindState copia = originale.copy();

        copia.registerTrait(ProfileTrait.AVVENTURA);

        assertEquals(1, originale.getAvventura());
        assertEquals(2, copia.getAvventura());
    }

    @Test
    void ripristinareSostituisceIConteggiCorrenti() {
        MindState corrente = with(5, 5, 5);
        MindState salvato = with(1, 2, 3);

        corrente.restoreFrom(salvato);

        assertEquals(1, corrente.getCoraggio());
        assertEquals(2, corrente.getCuriosita());
        assertEquals(3, corrente.getAvventura());
    }

    @Test
    void ripristinareDaNullLasciaLoStatoInvariato() {
        MindState mind = with(2, 2, 2);

        mind.restoreFrom(null);

        assertEquals(6, mind.getTotalProfileChoices());
    }

    @Test
    void ilRipristinoNonCollegaIDueStati() {
        MindState corrente = new MindState();
        MindState salvato = with(1, 1, 1);

        corrente.restoreFrom(salvato);
        corrente.registerTrait(ProfileTrait.CORAGGIO);

        assertEquals(1, salvato.getCoraggio());
        assertEquals(2, corrente.getCoraggio());
    }
}
