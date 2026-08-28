package it.unicam.cs.mpgc.rpg123465.audio;

/**
 * Un suono da riprodurre, con il proprio volume e un eventuale ritardo.
 * <p>
 * Serve a mescolare più effetti in una sola reazione: i file hanno volumi
 * registrati molto diversi fra loro, e sfalsarli nel tempo li rende una scena
 * invece che un rumore unico.
 *
 * @param resource     percorso nel classpath, es. {@code "/audio/squeak.wav"}
 * @param volume       volume da 0 a 1
 * @param delaySeconds attesa prima di riprodurlo, 0 per farlo partire subito
 */
public record SoundCue(String resource, double volume, double delaySeconds) {

    /** Un suono che parte immediatamente. */
    public static SoundCue now(String resource, double volume) {
        return new SoundCue(resource, volume, 0);
    }
}
