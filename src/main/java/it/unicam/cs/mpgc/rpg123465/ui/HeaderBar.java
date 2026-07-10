package it.unicam.cs.mpgc.rpg123465.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Barra di stato sempre visibile in cima alle schermate di gioco: nome del
 * giocatore, vita, lucidità e la prova in corso.
 * <p>
 * Le icone (Ikonli, pack Material Design) sono monocromatiche e tinte tutte
 * con lo stesso colore, così restano coerenti con il tema della Torre.
 */
public class HeaderBar {

    private static final Color ICON_COLOR = Color.web("#d8c07a");
    private static final int ICON_SIZE = 20;

    private final Label nameValue = new Label();
    private final Label vitaValue = new Label();
    private final Label lucidValue = new Label();
    private final Label stressValue = new Label();
    private final Label provaValue = new Label();

    /**
     * @param playerName nome del giocatore mostrato nella barra
     */
    public HeaderBar(String playerName) {
        nameValue.setText(playerName);
    }

    /**
     * @return la barra, da allineare in cima alla schermata
     */
    public Region createView() {
        HBox bar = new HBox(30,
                item("mdi2a-account", nameValue),
                item("mdi2h-heart", vitaValue),
                item("mdi2b-brain", lucidValue),
                item("mdi2l-lightning-bolt", stressValue),
                spacer(),
                item("mdi2b-book-open-variant", provaValue));

        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("header-bar");
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setMaxHeight(Region.USE_PREF_SIZE);
        return bar;
    }

    public void setVita(int current, int max) {
        vitaValue.setText(current + " / " + max);
    }

    public void setLucidita(int current, int max) {
        lucidValue.setText(current + " / " + max);
    }

    public void setStress(int current, int max) {
        stressValue.setText(current + " / " + max);
    }

    public void setProva(String text) {
        provaValue.setText(text);
    }

    private HBox item(String iconLiteral, Label value) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(ICON_SIZE);
        icon.setIconColor(ICON_COLOR);

        value.getStyleClass().add("header-value");

        HBox box = new HBox(8, icon, value);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
}
