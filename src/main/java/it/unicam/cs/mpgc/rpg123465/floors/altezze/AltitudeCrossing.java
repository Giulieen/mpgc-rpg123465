package it.unicam.cs.mpgc.rpg123465.floors.altezze;

import it.unicam.cs.mpgc.rpg123465.domain.FloorContent;

/**
 * La paura delle altezze: attraversare un abisso su tre ponti sospesi, tenendo
 * l'equilibrio contro le oscillazioni e cambiando ponte quando il percorso lo
 * impone, prima che il tempo scada.
 * <p>
 * Sotto la paura corre una metafora informatica: i tre ponti sono canali di
 * trasmissione con banda diversa, il tempo è la latenza massima, le frecce sono
 * i disturbi da correggere, il cambio di ponte è il rerouting, la caduta è la
 * perdita del pacchetto. Il contenuto porta i testi e la configurazione; le
 * regole del gioco vivono nella scena.
 *
 * @param title              titolo del piano
 * @param backgroundResource immagine dell'abisso e dei ponti
 * @param intro              breve spiegazione mostrata prima della traversata
 * @param victory            messaggio all'arrivo sull'altra sponda
 * @param gameOver           messaggio alla caduta nel vuoto
 * @param config             i parametri di gioco, bilanciabili
 */
public record AltitudeCrossing(
        String title,
        String backgroundResource,
        String intro,
        String victory,
        String gameOver,
        AltezzeConfig config) implements FloorContent {

    public AltitudeCrossing {
        if (isBlank(title) || isBlank(intro) || isBlank(victory) || isBlank(gameOver)) {
            throw new IllegalArgumentException("I testi della traversata sono obbligatori.");
        }
        if (config == null) {
            throw new IllegalArgumentException("La configurazione è obbligatoria.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
