package it.unicam.cs.mpgc.rpg123465.domain;

import java.io.Serializable;

/**
 * Stato interiore del protagonista.
 *
 * Mantiene Lucidità e Stress e registra, in modo invisibile al giocatore,
 * i tre tratti che emergono dalle risposte ai dilemmi "Preferiresti":
 * Coraggio, Curiosità e Avventura.
 */
public class MindState implements Serializable {

    /*
     * Il formato è cambiato rispetto al vecchio sistema basato su Attitude.
     * I vecchi salvataggi non sono quindi considerati compatibili.
     */
    private static final long serialVersionUID = 2L;

    public static final int MAX = 100;

    private static final int RIPOSO_LUCIDITA = 20;
    private static final int RIPOSO_STRESS = 25;

    private int lucidita = MAX;
    private int stress;

    private int coraggio;
    private int curiosita;
    private int avventura;

    public int getLucidita() {
        return lucidita;
    }

    public int getStress() {
        return stress;
    }

    public boolean isSopraffatto() {
        return lucidita <= 0;
    }

    /**
     * Aumenta lo Stress entrando in un nuovo ambiente.
     *
     * @param tensione tensione iniziale del piano
     */
    public void enterRoom(
            int tensione
    ) {
        stress = clamp(
                stress + tensione
        );
    }

    /**
     * Recupero applicato tra un piano e il successivo.
     */
    public void rest() {
        lucidita = clamp(
                lucidita + RIPOSO_LUCIDITA
        );

        stress = clamp(
                stress - RIPOSO_STRESS
        );
    }

    /**
     * Registra il tratto associato a una risposta.
     *
     * Il punteggio rimane nascosto nell'interfaccia.
     *
     * @param trait tratto associato alla risposta
     */
    public void registerTrait(
            ProfileTrait trait
    ) {
        if (trait == null) {
            throw new IllegalArgumentException(
                    "Il tratto non può essere null."
            );
        }

        switch (trait) {
            case CORAGGIO -> coraggio++;
            case CURIOSITA -> curiosita++;
            case AVVENTURA -> avventura++;
        }
    }

    public int getCoraggio() {
        return coraggio;
    }

    public int getCuriosita() {
        return curiosita;
    }

    public int getAvventura() {
        return avventura;
    }

    public int getTotalProfileChoices() {
        return coraggio
                + curiosita
                + avventura;
    }

    /**
     * Modifica solamente Lucidità e Stress.
     */
    public void harm(
            int lucidityDelta,
            int stressDelta
    ) {
        lucidita = clamp(
                lucidita + lucidityDelta
        );

        stress = clamp(
                stress + stressDelta
        );
    }

    /**
     * Ripristina direttamente Lucidità e Stress.
     */
    public void restoreStats(
            int lucidita,
            int stress
    ) {
        this.lucidita =
                clamp(lucidita);

        this.stress =
                clamp(stress);
    }

    /**
     * Determina il profilo verso cui il personaggio si sta dirigendo.
     *
     * Regole:
     * - se tutti e tre i valori differiscono al massimo di 1: Poliedrico;
     * - se due tratti sono vicini (differenza massima 1) e il terzo è
     *   almeno 2 punti più basso: profilo combinato;
     * - altrimenti prevale il tratto con il punteggio maggiore.
     *
     * @return profilo corrente
     */
    public PlayerProfile profile() {
        if (getTotalProfileChoices() == 0) {
            return PlayerProfile.POLIEDRICO;
        }

        int max =
                Math.max(
                        coraggio,
                        Math.max(
                                curiosita,
                                avventura
                        )
                );

        int min =
                Math.min(
                        coraggio,
                        Math.min(
                                curiosita,
                                avventura
                        )
                );

        if (max - min <= 1) {
            return PlayerProfile.POLIEDRICO;
        }

        if (Math.abs(coraggio - curiosita) <= 1
                && Math.min(coraggio, curiosita) - avventura >= 2) {

            return PlayerProfile.ESPLORATORE;
        }

        if (Math.abs(coraggio - avventura) <= 1
                && Math.min(coraggio, avventura) - curiosita >= 2) {

            return PlayerProfile.RISOLUTO;
        }

        if (Math.abs(curiosita - avventura) <= 1
                && Math.min(curiosita, avventura) - coraggio >= 2) {

            return PlayerProfile.VISIONARIO;
        }

        if (coraggio >= curiosita
                && coraggio >= avventura) {

            return PlayerProfile.CORAGGIOSO;
        }

        if (curiosita >= coraggio
                && curiosita >= avventura) {

            return PlayerProfile.CURIOSO;
        }

        return PlayerProfile.AVVENTURIERO;
    }

    /**
     * Crea una copia indipendente dello stato.
     *
     * Serve ai checkpoint dei piani.
     */
    public MindState copy() {
        MindState copy =
                new MindState();

        copy.lucidita =
                this.lucidita;

        copy.stress =
                this.stress;

        copy.coraggio =
                this.coraggio;

        copy.curiosita =
                this.curiosita;

        copy.avventura =
                this.avventura;

        return copy;
    }

    /**
     * Ripristina questo stato da quello salvato.
     *
     * @param salvato stato mentale salvato
     */
    public void restoreFrom(
            MindState salvato
    ) {
        if (salvato == null) {
            return;
        }

        this.lucidita =
                clamp(
                        salvato.lucidita
                );

        this.stress =
                clamp(
                        salvato.stress
                );

        this.coraggio =
                Math.max(
                        0,
                        salvato.coraggio
                );

        this.curiosita =
                Math.max(
                        0,
                        salvato.curiosita
                );

        this.avventura =
                Math.max(
                        0,
                        salvato.avventura
                );
    }

    private int clamp(
            int valore
    ) {
        return Math.clamp(
                valore,
                0,
                MAX
        );
    }
}
