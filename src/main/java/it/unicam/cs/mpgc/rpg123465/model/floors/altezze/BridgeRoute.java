package it.unicam.cs.mpgc.rpg123465.model.floors.altezze;

import java.util.Random;

/**
 * Il cammino della traversata: su quale ponte si è, quanto se n'è percorso e
 * quando è ora di passare al successivo.
 *
 * <p>
 * Tiene le regole del percorso, non la sua messa in scena: quanto si avanza in
 * un fotogramma lo calcola la scena — dipende dalla velocità del ponte e dalla
 * spinta accumulata — e lo consegna qui, dove viene sommato e confrontato con
 * le soglie.
 */
public final class BridgeRoute {

    /** Percorso completato. */
    private static final double GOAL = 100;

    /** Nessun ponte scelto. */
    private static final int NONE = -1;

    private final AltezzeConfig config;
    private final Random rng;

    private int current = NONE;
    private int destination = NONE;

    private double progress;

    private int nextThreshold;
    private int routeStep;

    public BridgeRoute(AltezzeConfig config, Random rng) {
        if (config == null || rng == null) {
            throw new IllegalArgumentException(
                    "Configurazione e sorgente casuale sono obbligatorie."
            );
        }

        this.config = config;
        this.rng = rng;
    }

    /** @return il ponte su cui si sta camminando, oppure -1 se non è stato scelto */
    public int current() {
        return current;
    }

    /** @return true se un ponte è stato scelto e si può avanzare */
    public boolean hasBridge() {
        return current != NONE;
    }

    /** @return quanto della traversata è stato percorso, da 0 a 100 */
    public double progress() {
        return progress;
    }

    /** @return true se si è raggiunta l'altra sponda */
    public boolean isComplete() {
        return progress >= GOAL;
    }

    /**
     * Sceglie il ponte da cui partire.
     *
     * @param index posizione del ponte: 0 sinistra, 1 centro, 2 destra
     */
    public void select(int index) {
        current = index;
    }

    /**
     * Somma l'avanzamento di un fotogramma, senza superare la sponda.
     *
     * @param amount quanto si è avanzato, già calcolato dalla scena
     */
    public void advance(double amount) {
        progress = Math.min(GOAL, progress + amount);
    }

    /**
     * Consuma la prossima soglia di percorso, se è stata superata.
     *
     * <p>
     * Il metodo cambia stato: una soglia superata viene segnata come passata,
     * così il cambio di ponte scatta una volta sola anche se il controllo
     * avviene a ogni fotogramma.
     *
     * @return true se è il momento di cambiare ponte
     */
    public boolean consumeThreshold() {
        double[] thresholds = config.routeThresholds();

        if (nextThreshold < thresholds.length && progress >= thresholds[nextThreshold]) {

            nextThreshold++;
            return true;
        }

        return false;
    }

    /**
     * Decide verso quale ponte si passa.
     *
     * <p>
     * Se la configurazione detta un percorso fisso lo segue, altrimenti sceglie
     * uno dei due ponti diversi da quello attuale: l'offset va da 1 a 2 su tre
     * ponti, quindi non si resta mai dove si è.
     *
     * @return l'indice del ponte di destinazione
     */
    public int chooseDestination() {
        int[] manual = config.manualRoute();

        if (manual != null && routeStep < manual.length) {

            destination = manual[routeStep++];
            return destination;
        }

        routeStep++;

        int offset = rng.nextInt(2) + 1;

        destination = (current + offset) % 3;

        return destination;
    }

    /** @return il ponte verso cui si sta passando, oppure -1 */
    public int destination() {
        return destination;
    }

    /**
     * Serve alla vista per accendere il faro del ponte di arrivo.
     *
     * @param index posizione del ponte da verificare
     * @return true se è il ponte verso cui si sta passando
     */
    public boolean isDestination(int index) {
        return destination != NONE
                && index == destination;
    }

    /** Completa il passaggio: il ponte di destinazione diventa quello attuale. */
    public void jump() {
        current = destination;
        destination = NONE;
    }

    /** Riporta la traversata all'inizio, soglie e percorso compresi. */
    public void restart() {
        current = NONE;
        destination = NONE;

        progress = 0;
        nextThreshold = 0;
        routeStep = 0;
    }
}
