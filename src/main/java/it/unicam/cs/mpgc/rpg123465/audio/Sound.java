package it.unicam.cs.mpgc.rpg123465.audio;

/**
 * Punto d'accesso comodo al servizio audio.
 *
 * <p>
 * Il suono è l'unica cosa che ogni schermata può voler produrre in qualsiasi
 * momento, anche da un gestore d'evento annidato: farlo arrivare fin lì
 * attraverso i costruttori significherebbe aggiungere un parametro a mezza
 * dozzina di classi che del suono non parlano. Questa facciata evita quel
 * costo senza rinunciare all'astrazione: la logica vera sta in
 * {@link JavaFxSoundPlayer}, dietro l'interfaccia {@link SoundPlayer}, e chi
 * ha bisogno di sostituirla — una prova automatica, o un domani un altro
 * toolkit — lavora sull'interfaccia e la installa con {@link #use(SoundPlayer)}.
 */
public final class Sound {

    private static SoundPlayer player = new JavaFxSoundPlayer();

    private Sound() {
        // Solo metodi statici.
    }

    /**
     * Sostituisce il servizio audio usato dal gioco.
     *
     * <p>
     * Serve alle prove automatiche, che non possono avviare il motore
     * multimediale di JavaFX e si limitano ad annotare cosa è stato chiesto.
     *
     * @param replacement il servizio da usare, non nullo
     * @throws IllegalArgumentException se il servizio è nullo
     */
    public static void use(SoundPlayer replacement) {
        if (replacement == null) {
            throw new IllegalArgumentException("Il servizio audio non può essere null.");
        }

        player = replacement;
    }

    /** @return il servizio audio attualmente in uso */
    public static SoundPlayer player() {
        return player;
    }

    /**
     * @param resources percorsi nel classpath degli effetti da preparare
     * @see SoundPlayer#preload(String...)
     */
    public static void preload(String... resources) {
        player.preload(resources);
    }

    /**
     * @param resource percorso nel classpath, es. {@code "/audio/ambience-forest.mp3"}
     * @param volume volume da 0 a 1
     * @see SoundPlayer#loop(String, double)
     */
    public static void loop(String resource, double volume) {
        player.loop(resource, volume);
    }

    /**
     * @param resource percorso nel classpath, oppure {@code null}
     * @param volume volume da 0 a 1
     * @see SoundPlayer#play(String, double)
     */
    public static void play(String resource, double volume) {
        player.play(resource, volume);
    }

    /**
     * @param cue il suono da riprodurre, oppure {@code null}
     * @see SoundPlayer#play(SoundCue)
     */
    public static void play(SoundCue cue) {
        player.play(cue);
    }

    /**
     * @param resource percorso nel classpath
     * @param volume volume da 0 a 1
     * @param minSeconds attesa minima fra una riproduzione e l'altra
     * @param maxSeconds attesa massima
     * @see SoundPlayer#occasional(String, double, double, double)
     */
    public static void occasional(String resource, double volume,
                                  double minSeconds, double maxSeconds) {
        player.occasional(resource, volume, minSeconds, maxSeconds);
    }

    /** @see SoundPlayer#stopAll() */
    public static void stopAll() {
        player.stopAll();
    }
}
