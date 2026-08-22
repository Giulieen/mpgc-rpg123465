package it.unicam.cs.mpgc.rpg123465.domain;

import java.io.Serializable;

/**
 * Memoria delle risposte ai dilemmi "Preferiresti".
 *
 * <p>
 * Registra, in modo invisibile al giocatore, quante volte ha scelto ciascuno
 * dei tre tratti — Coraggio, Curiosità e Avventura — e da quei conteggi ricava
 * il profilo verso cui si sta dirigendo.
 *
 * <p>
 * I tratti sono l'unica cosa che attraversa i piani. Le difficoltà della prova
 * corrente vivono altrove, nei tentativi, e non influiscono in alcun modo sul
 * profilo: sbagliare una combinazione o cadere da un ponte non dice nulla su
 * chi sta giocando.
 */
public class MindState implements Serializable {

    /*
     * Il formato è cambiato due volte: prima abbandonando Attitude, poi
     * togliendo Lucidità e Stress. I salvataggi precedenti non sono
     * compatibili.
     */
    private static final long serialVersionUID = 3L;

    private int coraggio;
    private int curiosita;
    private int avventura;

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
     * Determina il profilo verso cui il personaggio si sta dirigendo.
     *
     * Regole:
     * - se tutti e tre i valori differiscono al massimo di 1: Imprevedibile;
     * - se due tratti sono vicini (differenza massima 1) e il terzo è
     *   almeno 2 punti più basso: profilo combinato;
     * - altrimenti prevale il tratto con il punteggio maggiore.
     *
     * @return profilo corrente
     */
    public PlayerProfile profile() {
        if (getTotalProfileChoices() == 0) {
            return PlayerProfile.IMPREVEDIBILE;
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
            return PlayerProfile.IMPREVEDIBILE;
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
     * Crea una copia indipendente dei conteggi.
     *
     * Serve al checkpoint del piano: fotografa le risposte date fino
     * all'ingresso, così ricaricando non vengono contate due volte.
     */
    public MindState copy() {
        MindState copy =
                new MindState();

        copy.coraggio =
                this.coraggio;

        copy.curiosita =
                this.curiosita;

        copy.avventura =
                this.avventura;

        return copy;
    }

    /**
     * Ripristina questi conteggi da quelli salvati.
     *
     * @param salvato conteggi salvati
     */
    public void restoreFrom(
            MindState salvato
    ) {
        if (salvato == null) {
            return;
        }

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
}
