package it.unicam.cs.mpgc.rpg123465.floors.buio;

import it.unicam.cs.mpgc.rpg123465.domain.FloorContent;

/**
 * La paura del buio: una stanza chiusa da una porta con serratura a
 * combinazione, che si apre trovando le quattro cifre corrette prima
 * che il tempo scada.
 *
 * <p>
 * Le cifre sono scritte in binario sulle quattro pareti e si leggono
 * soltanto al buio. La serratura, invece, viene utilizzata avvicinandosi
 * alla porta.
 *
 * <p>
 * Il contenuto conserva soltanto i testi e i parametri del puzzle: le regole
 * della prova appartengono al controller del piano.
 *
 * @param title titolo del piano
 * @param backgroundResource immagine della stanza
 * @param intro spiegazione del puzzle
 * @param outro testo mostrato al completamento
 * @param wrongText testo mostrato con una combinazione errata
 * @param timeoutText testo mostrato allo scadere del tempo
 * @param combination combinazione corretta
 * @param seconds secondi disponibili
 */
public record DarkRoom(
        String title,
        String backgroundResource,
        String intro,
        String outro,
        String wrongText,
        String timeoutText,
        String combination,
        int seconds
) implements FloorContent {

    /**
     * Valida i dati usati per costruire il puzzle.
     */
    public DarkRoom {
        if (isBlank(title)
                || isBlank(backgroundResource)
                || isBlank(intro)
                || isBlank(outro)
                || isBlank(wrongText)
                || isBlank(timeoutText)) {

            throw new IllegalArgumentException(
                    "I testi e la risorsa della stanza sono obbligatori."
            );
        }

        if (combination == null
                || !combination.matches("\\d{4}")) {

            throw new IllegalArgumentException(
                    "La combinazione deve contenere quattro cifre."
            );
        }

        if (seconds <= 0) {
            throw new IllegalArgumentException(
                    "Il tempo deve essere positivo."
            );
        }
    }

    private static boolean isBlank(String value) {
        return value == null
                || value.isBlank();
    }
}