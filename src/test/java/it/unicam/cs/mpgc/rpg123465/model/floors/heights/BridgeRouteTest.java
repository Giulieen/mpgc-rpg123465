package it.unicam.cs.mpgc.rpg123465.model.floors.heights;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BridgeRouteTest {

    private BridgeRoute route;

    @BeforeEach
    void setUp() {
        route = new BridgeRoute(HeightsConfig.standard(), new Random(5));
    }

    /** Percorso fisso, per prove ripetibili sulla scelta della destinazione. */
    private static BridgeRoute conPercorso(int... tappe) {
        HeightsConfig base = HeightsConfig.standard();

        return new BridgeRoute(
                new HeightsConfig(
                        base.totalSeconds(),
                        base.balancePoints(),
                        base.routeThresholds(),
                        7.0, 1.0, 1.0,
                        List.of(base.bridge(0), base.bridge(1), base.bridge(2)),
                        tappe),
                new Random(1));
    }

    // --- avanzamento -------------------------------------------------------

    @Test
    void laTraversataIniziaSenzaPonteEACapoDelPercorso() {
        assertFalse(route.hasBridge());
        assertEquals(-1, route.current());
        assertEquals(0, route.progress());
        assertFalse(route.isComplete());
    }

    @Test
    void sceglierePonteRendePossibileAvanzare() {
        route.select(1);

        assertTrue(route.hasBridge());
        assertEquals(1, route.current());
    }

    @Test
    void avanzareSommaIlProgresso() {
        route.select(0);

        route.advance(12.5);
        route.advance(7.5);

        assertEquals(20, route.progress());
    }

    /**
     * Il progresso non supera mai la sponda: senza il limite lo zoom del
     * fondale, che è proporzionale al progresso, continuerebbe a crescere.
     */
    @Test
    void ilProgressoNonSuperaMaiLaSponda() {
        route.select(0);

        route.advance(80);
        route.advance(80);

        assertEquals(100, route.progress());
        assertTrue(route.isComplete());
    }

    @Test
    void laTraversataSiCompletaSoloAllaSponda() {
        route.select(0);

        route.advance(99.9);
        assertFalse(route.isComplete());

        route.advance(0.1);
        assertTrue(route.isComplete());
    }

    // --- soglie ------------------------------------------------------------

    /**
     * Le quattro soglie del piano sono 15, 35, 55 e 78: sotto la prima non si
     * cambia ponte, superata la prima si cambia una volta sola.
     */
    @Test
    void sottoLaPrimaSogliaNonSiCambiaPonte() {
        route.select(0);
        route.advance(19);

        assertFalse(route.consumeThreshold());
    }

    @Test
    void unaSogliaSuperataFaScattareIlCambioUnaVoltaSola() {
        route.select(0);
        route.advance(20);

        assertTrue(route.consumeThreshold());
        assertFalse(route.consumeThreshold());
        assertFalse(route.consumeThreshold());
    }

    @Test
    void leTreSoglieScattanoNellOrdine() {
        route.select(0);

        int cambi = 0;

        for (int i = 0; i < 100; i++) {
            route.advance(1);

            while (route.consumeThreshold()) {
                cambi++;
            }
        }

        assertEquals(3, cambi);
    }

    /**
     * Un avanzamento che scavalca due soglie insieme non ne perde nessuna:
     * vengono consumate una per chiamata.
     */
    @Test
    void unSaltoCheScavalcaDueSoglieLeConsumaEntrambe() {
        route.select(0);
        route.advance(50);

        assertTrue(route.consumeThreshold());
        assertTrue(route.consumeThreshold());
        assertFalse(route.consumeThreshold());
    }

    @Test
    void oltreLUltimaSogliaNonSiCambiaPiuPonte() {
        route.select(0);
        route.advance(100);

        for (int i = 0; i < 3; i++) {
            assertTrue(route.consumeThreshold());
        }

        assertFalse(route.consumeThreshold());
    }

    // --- destinazione ------------------------------------------------------

    /**
     * La regola che rende sensato il cambio: non si può essere mandati sul
     * ponte su cui si sta già camminando.
     */
    @Test
    void laDestinazioneNonEMaiIlPonteAttuale() {
        for (int seme = 0; seme < 3; seme++) {
            BridgeRoute r = new BridgeRoute(HeightsConfig.standard(), new Random(seme));
            r.select(seme);

            for (int i = 0; i < 200; i++) {
                int partenza = r.current();
                int scelto = r.chooseDestination();

                assertNotEquals(partenza, scelto);
                assertTrue(scelto >= 0 && scelto < 3);

                r.jump();
            }
        }
    }

    @Test
    void unPercorsoFissoVieneSeguitoNellOrdine() {
        BridgeRoute fisso = conPercorso(2, 0, 1);
        fisso.select(0);

        assertEquals(2, fisso.chooseDestination());
        fisso.jump();

        assertEquals(0, fisso.chooseDestination());
        fisso.jump();

        assertEquals(1, fisso.chooseDestination());
    }

    @Test
    void esauritoIlPercorsoFissoSiTornaAllaSceltaCasuale() {
        BridgeRoute fisso = conPercorso(2);
        fisso.select(0);

        assertEquals(2, fisso.chooseDestination());
        fisso.jump();

        int dopo = fisso.chooseDestination();

        assertNotEquals(2, dopo);
        assertTrue(dopo >= 0 && dopo < 3);
    }

    @Test
    void saltareRendeAttualeIlPonteDiDestinazione() {
        route.select(0);

        int scelto = route.chooseDestination();

        assertTrue(route.isDestination(scelto));

        route.jump();

        assertEquals(scelto, route.current());
        assertEquals(-1, route.destination());
        assertFalse(route.isDestination(scelto));
    }

    /** Finché non si sceglie, nessun ponte è la destinazione. */
    @Test
    void senzaUnCambioInCorsoNessunPonteEDestinazione() {
        route.select(1);

        for (int i = 0; i < 3; i++) {
            assertFalse(route.isDestination(i));
        }
    }

    // --- ricominciare ------------------------------------------------------

    /**
     * Dopo una caduta la traversata riparte da zero: se le soglie non tornassero
     * indietro, il percorso si rifarebbe senza più cambi di ponte.
     */
    @Test
    void ricominciareRiportaProgressoSoglieEPonteAllInizio() {
        route.select(0);
        route.advance(60);
        route.consumeThreshold();
        route.consumeThreshold();
        route.chooseDestination();

        route.restart();

        assertEquals(0, route.progress());
        assertFalse(route.hasBridge());
        assertEquals(-1, route.destination());

        route.select(0);
        route.advance(20);

        assertTrue(route.consumeThreshold());
    }

    @Test
    void ilPercorsoRifiutaArgomentiMancanti() {
        assertThrows(IllegalArgumentException.class, () -> new BridgeRoute(null, new Random()));

        assertThrows(IllegalArgumentException.class,
                () -> new BridgeRoute(HeightsConfig.standard(), null));
    }
}
