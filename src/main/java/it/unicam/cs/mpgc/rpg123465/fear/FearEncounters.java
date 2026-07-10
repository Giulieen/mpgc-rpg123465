package it.unicam.cs.mpgc.rpg123465.fear;

import it.unicam.cs.mpgc.rpg123465.audio.SoundCue;

import java.util.List;

/**
 * Raccolta degli incontri con le paure della Torre.
 * <p>
 * Ogni metodo costruisce un {@link FearEncounter} con le reazioni specifiche
 * di quella paura: le azioni nascono dalla sua natura, non da un elenco fisso.
 */
public final class FearEncounters {

    private FearEncounters() {
        // Solo metodi statici.
    }

    /** Piano I — I Topi. */
    public static FearEncounter topi() {
        return new FearEncounter(
                "Piano I — I Topi",
                "/images/scenes/floor1-topi.jpg",
                "L'aria sa di muffa e di cera consumata. Poi, nel buio, decine "
                        + "di occhi si accendono. I topi sciamano tra le gambe dei "
                        + "tavoli; uno ti corre sul piede. La stanza intera sembra "
                        + "muoversi.",
                45,
                List.of(
                        // La violenza non risolve, e Chimeris se ne nutre: scatta la
                        // trappola, e subito dopo lui ride.
                        new FearChoice(
                                "Metti trappole e veleno in ogni angolo",
                                "Ne cadono a decine... ma da ogni crepa ne escono altri. "
                                        + "La stanza non si svuota mai.",
                                -10, +20, Attitude.COLPISCI,
                                List.of(
                                        SoundCue.now("/audio/mousetrap-snap.mp3", 0.60),
                                        new SoundCue("/audio/chimeris-laugh.mp3", 0.65, 0.7)),
                                false),
                        // Scacciare dà sollievo immediato: il prezzo lo riscuote Chimeris,
                        // dopo. La fiammata, lo sciame che fugge, poi il grattare nel buio.
                        new FearChoice(
                                "Accendi la torcia e falli fuggire",
                                "Si disperdono negli angoli. Ma appena la luce cala, li "
                                        + "senti ancora: grattano nel buio.",
                                -2, -12, Attitude.CONTIENI,
                                List.of(
                                        SoundCue.now("/audio/torch-whoosh.mp3", 0.70),
                                        // Lo sciame è registrato piano: lo alzo quasi al massimo.
                                        new SoundCue("/audio/rats-many.mp3", 0.95, 0.25),
                                        new SoundCue("/audio/scratching.mp3", 0.55, 1.8)),
                                false),
                        // La tua calma non piace alla Torre: è lei a tremare, non tu.
                        new FearChoice(
                                "Resta fermo al buio e respira",
                                "Il ribrezzo ti sale in gola. Non ti muovi. A poco a poco "
                                        + "i topi ti girano intorno senza toccarti, e si placano.",
                                0, -18, Attitude.TOLLERI,
                                List.of(SoundCue.now("/audio/rumble.mp3", 0.75)),
                                true),
                        // Accogliere ti lascia più forte di come sei entrato.
                        new FearChoice(
                                "Lascia una briciola, ne accudisci uno",
                                "Un topolino si ferma, annusa, prende il pane dalla tua "
                                        + "mano. Per un istante non fa più paura: è solo "
                                        + "piccolo, e affamato.",
                                +5, -30, Attitude.ACCOGLI,
                                List.of(SoundCue.now("/audio/squeak.mp3", 0.70)),
                                false)));
    }
}
