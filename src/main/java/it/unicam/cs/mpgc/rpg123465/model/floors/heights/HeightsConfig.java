package it.unicam.cs.mpgc.rpg123465.model.floors.heights;

import java.util.List;

/**
 * La configurazione del livello: tempo, punti equilibrio, soglie dei cambi di
 * ponte, difficoltà e i tre ponti. Tutto ciò che serve bilanciare sta qui.
 *
 * @param totalSeconds          durata totale della traversata
 * @param balancePoints         errori tollerati prima della caduta
 * @param routeThresholds       percentuali di progresso ai cambi di ponte (es. 20, 45, 70)
 * @param rerouteSeconds        secondi per raggiungere il ponte illuminato
 * @param endgameIntervalFactor previsto per stringere gli intervalli fra le
 *                              frecce a fine percorso, oggi non applicato
 * @param endgameResponseFactor previsto per accorciare il tempo di risposta a
 *                              fine percorso, oggi non applicato
 * @param bridges               i tre ponti: sinistra (0), centro (1), destra (2)
 * @param manualRoute           sequenza fissa di destinazioni per test, o {@code null}
 */
public record HeightsConfig(
        int totalSeconds,
        int balancePoints,
        double[] routeThresholds,
        double rerouteSeconds,
        double endgameIntervalFactor,
        double endgameResponseFactor,
        List<BridgeSpec> bridges,
        int[] manualRoute) {

    public HeightsConfig {
        if (totalSeconds <= 0 || balancePoints <= 0) {
            throw new IllegalArgumentException("Tempo e punti equilibrio devono essere positivi.");
        }
        if (bridges == null || bridges.size() != 3) {
            throw new IllegalArgumentException("Servono esattamente tre ponti.");
        }
        bridges = List.copyOf(bridges);
    }

    /**
     * @param index posizione del ponte: 0 sinistra, 1 centro, 2 destra
     * @return il ponte in quella posizione
     */
    public BridgeSpec bridge(int index) {
        return bridges.get(index);
    }

    /**
     * La configurazione predefinita, con i valori suggeriti per il
     * bilanciamento della prova.
     *
     * @return i parametri con cui il Piano III viene giocato
     */
    public static HeightsConfig standard() {
        /*
         * Novanta secondi di traversata. L'avanzamento dei tre ponti e' scalato
         * dello stesso fattore rispetto alla versione da 150 secondi: ciascuno
         * copre la stessa frazione del percorso nello stesso frazione di tempo,
         * quindi la difficolta' resta quella di prima e cambia solo la durata.
         */
        return new HeightsConfig(
                90,
                3,
                new double[] {20, 45, 72},
                7.0,
                1.0,
                1.0,
                List.of(
                        new BridgeSpec(BridgeType.FAST, "veloce",
                                "Stretto e rapido — reazione svelta richiesta, ma avanzi in fretta.",
                                650, 1000, 1.92),
                        new BridgeSpec(BridgeType.BALANCED, "intermedio",
                                "Equilibrato — ritmo e avanzamento medi.",
                                900, 1200, 1.25),
                        new BridgeSpec(BridgeType.SAFE, "sicuro",
                                "Largo e lento — reagisci con calma, ma il tempo scorre.",
                                1400, 1700, 0.83)),
                null);
    }
}
