package it.unicam.cs.mpgc.rpg123465.audio;

/**
 * Il servizio audio del gioco, visto da chi lo usa.
 *
 * <p>
 * L'interfaccia esiste perché il suono è infrastruttura: dipende dal toolkit
 * grafico e da un dispositivo di riproduzione. Le schermate chiedono di
 * sentire qualcosa senza sapere che dietro ci sia JavaFX, e una prova
 * automatica può sostituire l'implementazione con una che si limita ad
 * annotare cosa le è stato chiesto.
 *
 * <p>
 * La distinzione fra {@link #play} e {@link #loop} non è stilistica: gli
 * effetti che rispondono a un gesto del giocatore devono partire senza
 * ritardo percepibile, mentre un ambiente di sottofondo può permettersi di
 * essere aperto e decodificato con calma. Le due famiglie hanno quindi
 * bisogno di meccanismi diversi, ed è l'implementazione a sceglierli.
 */
public interface SoundPlayer {

    /**
     * Prepara in anticipo gli effetti indicati, così che la prima
     * riproduzione parta senza ritardo.
     *
     * @param resources percorsi nel classpath, ignorati se non esistono
     */
    void preload(String... resources);

    /**
     * Riproduce un effetto singolo.
     *
     * @param resource percorso nel classpath, oppure {@code null} per non
     *                 riprodurre nulla
     * @param volume volume da 0 a 1
     */
    void play(String resource, double volume);

    /**
     * Riproduce un effetto rispettandone volume ed eventuale ritardo.
     *
     * @param cue il suono da riprodurre, oppure {@code null}
     */
    void play(SoundCue cue);

    /**
     * Avvia un suono ripetuto all'infinito. Più chiamate si sovrappongono,
     * così si possono stratificare gli ambienti.
     *
     * @param resource percorso nel classpath
     * @param volume volume da 0 a 1
     */
    void loop(String resource, double volume);

    /**
     * Riproduce un effetto a intervalli casuali, finché non si cambia scena.
     *
     * @param resource percorso nel classpath
     * @param volume volume da 0 a 1
     * @param minSeconds attesa minima fra una riproduzione e l'altra
     * @param maxSeconds attesa massima
     */
    void occasional(String resource, double volume,
                    double minSeconds, double maxSeconds);

    /**
     * Zittisce tutto: ambienti, suoni sporadici ed effetti in corso. Da
     * chiamare quando si cambia scena.
     */
    void stopAll();
}
