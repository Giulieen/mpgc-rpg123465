package it.unicam.cs.mpgc.rpg123465.model.floors.altezze;

import it.unicam.cs.mpgc.rpg123465.model.FloorContent;

/**
 * La traversata dell'abisso: tre ponti sospesi, l'equilibrio da tenere contro
 * le oscillazioni, e un cambio di ponte quando il percorso cede, il tutto prima
 * che il tempo scada.
 * <p>
 * Il contenuto porta i testi e la configurazione; le regole vivono nella scena
 * e le conseguenze nel controller.
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
