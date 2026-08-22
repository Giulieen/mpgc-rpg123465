package it.unicam.cs.mpgc.rpg123465.ui.support;

import javafx.scene.control.Label;

/**
 * Riga di riepilogo mostrata alla fine di una prova.
 *
 * <p>
 * I tre piani misurano cose diverse — un tempo, dei topi, degli errori — ma
 * devono chiuderle con lo stesso aspetto: da qui passano sia il formato del
 * tempo sia il separatore fra le voci.
 */
public final class TrialStats {

    private static final String SEPARATOR = "     ·     ";

    private TrialStats() {
        // Solo metodi statici.
    }

    /**
     * @param seconds durata in secondi
     * @return la durata come {@code mm:ss}
     */
    public static String time(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    /**
     * @param parts voci già formattate, ad esempio {@code "Tempo: 01:12"}
     * @return la riga di riepilogo pronta da inserire in un pannello
     */
    public static Label line(String... parts) {
        Label label = new Label(String.join(SEPARATOR, parts));

        label.getStyleClass().add("fear-effects");

        return label;
    }
}
