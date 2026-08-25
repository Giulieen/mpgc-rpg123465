package it.unicam.cs.mpgc.rpg123465.view.floors.altezze;

import it.unicam.cs.mpgc.rpg123465.model.floors.altezze.ArrowDirection;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.function.Consumer;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * La barra di stato e i comandi del Piano III — Le Altezze: punteggio e record,
 * la serie (combo), i punti equilibrio, il timer, la freccia con l'anello di
 * reazione che si stringe, e il D-pad a quattro frecce.
 * <p>
 * Tiene i nodi e i loro aggiornamenti, così la scena resta la sola regia e non
 * si gonfia di costruzione grafica.
 */
final class AltezzeHud {

    private static final Color AMBER = Color.web("#ffe6a3");
    private static final Color AMBER_DIM = Color.rgb(200, 170, 100, 0.75);

    private final Label countdown = new Label();
    private final Label scoreLabel = new Label();
    private final Label recordLabel = new Label();
    private final Label channelLabel = new Label();
    private final Label comboLabel = new Label();
    private final Label rerouteLabel = new Label();
    private final Label hoverDesc = new Label();
    private final FontIcon arrowIcon = new FontIcon();
    private final Circle responseRing = new Circle(105);
    private final HBox balanceBox = new HBox(10);
    private final Circle[] balanceDots;
    private final GridPane dpad;
    private ScaleTransition ringAnim;

    /**
     * @param root          la scena su cui posare l'HUD
     * @param balancePoints quanti punti equilibrio mostrare
     * @param headerView    la barra di stato comune, da collocare in cima
     * @param onArrow       cosa fare quando si preme una freccia del D-pad
     */
    AltezzeHud(StackPane root, int balancePoints, Region headerView, Consumer<ArrowDirection> onArrow) {
        countdown.getStyleClass().add("dark-timer");
        StackPane.setAlignment(countdown, Pos.TOP_RIGHT);
        StackPane.setMargin(countdown, new Insets(72, 60, 0, 0));

        scoreLabel.getStyleClass().add("dark-timer");
        recordLabel.getStyleClass().add("fear-effects");
        channelLabel.getStyleClass().add("fear-effects");
        comboLabel.getStyleClass().add("fear-effects");

        balanceDots = new Circle[balancePoints];
        for (int i = 0; i < balanceDots.length; i++) {
            balanceDots[i] = new Circle(8, AMBER);
            balanceBox.getChildren().add(balanceDots[i]);
        }

        VBox topLeft = new VBox(6, scoreLabel, recordLabel, channelLabel, comboLabel, balanceBox);
        topLeft.setAlignment(Pos.TOP_LEFT);
        StackPane.setAlignment(topLeft, Pos.TOP_LEFT);
        StackPane.setMargin(topLeft, new Insets(70, 0, 0, 40));

        arrowIcon.setIconSize(150);
        arrowIcon.setIconColor(AMBER);
        arrowIcon.setVisible(false);
        arrowIcon.setMouseTransparent(true);
        arrowIcon.setTranslateY(-54);   // centro ottico: sopra il centro geometrico
        StackPane.setAlignment(arrowIcon, Pos.CENTER);

        responseRing.setFill(Color.TRANSPARENT);
        responseRing.setStroke(AMBER);
        responseRing.setStrokeWidth(4);
        responseRing.setOpacity(0.8);
        responseRing.setVisible(false);
        responseRing.setMouseTransparent(true);
        responseRing.setTranslateY(-54);
        StackPane.setAlignment(responseRing, Pos.CENTER);

        rerouteLabel.getStyleClass().add("fear-title");
        rerouteLabel.setVisible(false);
        rerouteLabel.setMouseTransparent(true);
        StackPane.setAlignment(rerouteLabel, Pos.TOP_CENTER);
        StackPane.setMargin(rerouteLabel, new Insets(120, 0, 0, 0));

        hoverDesc.getStyleClass().add("fear-text");
        hoverDesc.setWrapText(true);
        hoverDesc.setMaxWidth(760);
        hoverDesc.setMouseTransparent(true);
        StackPane.setAlignment(hoverDesc, Pos.BOTTOM_CENTER);
        StackPane.setMargin(hoverDesc, new Insets(0, 0, 22, 0));

        dpad = buildDpad(onArrow);
        dpad.setVisible(false);
        dpad.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(dpad, Pos.BOTTOM_CENTER);
        StackPane.setMargin(dpad, new Insets(0, 0, 50, 0));

        root.getChildren().addAll(topLeft, responseRing, arrowIcon, rerouteLabel, hoverDesc,
                dpad, headerView, countdown);
        StackPane.setAlignment(headerView, Pos.TOP_CENTER);
    }

    /** L'etichetta del conto alla rovescia, da affidare al clock. */
    Label countdown() {
        return countdown;
    }

    void setStats(int score, int best, int streak) {
        scoreLabel.setText("Punti  " + score);
        recordLabel.setText("Record  " + best);
        comboLabel.setText(streak > 0 ? "Serie  " + streak : "Serie  —");
        comboLabel.setTextFill(streak >= 10 ? AMBER : AMBER_DIM);
    }

    void setBalance(int balance) {
        for (int i = 0; i < balanceDots.length; i++) {
            balanceDots[i].setFill(i < balance ? AMBER : Color.rgb(255, 255, 255, 0.12));
        }
    }

    void setChannel(String name) {
        channelLabel.setText(name == null ? "" : "Canale: " + name);
    }

    void setHover(String text) {
        hoverDesc.setText(text);
    }

    void showReroute(String text) {
        rerouteLabel.setText(text);
        rerouteLabel.setVisible(true);
    }

    void hideReroute() {
        rerouteLabel.setVisible(false);
    }

    void showDpad(boolean visible) {
        dpad.setVisible(visible);
    }

    /** Mostra la freccia da contrastare e avvia l'anello che si stringe nel tempo di risposta. */
    void showArrow(ArrowDirection direction, double responseMs) {
        arrowIcon.setIconLiteral(literal(direction));
        arrowIcon.setVisible(true);
        if (ringAnim != null) {
            ringAnim.stop();
        }
        responseRing.setVisible(true);
        ringAnim = new ScaleTransition(Duration.millis(responseMs), responseRing);
        ringAnim.setFromX(1.6);
        ringAnim.setFromY(1.6);
        ringAnim.setToX(0.78);
        ringAnim.setToY(0.78);
        ringAnim.play();
    }

    void hideArrow() {
        arrowIcon.setVisible(false);
        responseRing.setVisible(false);
        if (ringAnim != null) {
            ringAnim.stop();
            ringAnim = null;
        }
    }

    // --- Il D-pad -----------------------------------------------------------

    private GridPane buildDpad(Consumer<ArrowDirection> onArrow) {
        GridPane pad = new GridPane();
        pad.setHgap(6);
        pad.setVgap(6);
        pad.setAlignment(Pos.CENTER);
        pad.add(padButton(ArrowDirection.UP, onArrow), 1, 0);
        pad.add(padButton(ArrowDirection.LEFT, onArrow), 0, 1);
        pad.add(padButton(ArrowDirection.RIGHT, onArrow), 2, 1);
        pad.add(padButton(ArrowDirection.DOWN, onArrow), 1, 2);
        return pad;
    }

    private Node padButton(ArrowDirection direction, Consumer<ArrowDirection> onArrow) {
        FontIcon glyph = new FontIcon(literal(direction));
        glyph.setIconSize(34);
        glyph.setIconColor(AMBER_DIM);

        StackPane button = new StackPane(glyph);
        button.setPrefSize(66, 66);
        button.setMinSize(66, 66);
        button.setMaxSize(66, 66);
        button.setStyle("-fx-background-color: rgba(16,14,12,0.72); -fx-background-radius: 10;"
                + "-fx-border-color: rgba(150,128,82,0.5); -fx-border-radius: 10; -fx-cursor: hand;");
        button.setOnMouseEntered(event -> glyph.setIconColor(AMBER));
        button.setOnMouseExited(event -> glyph.setIconColor(AMBER_DIM));
        button.setOnMouseClicked(event -> onArrow.accept(direction));
        return button;
    }

    private String literal(ArrowDirection direction) {
        return switch (direction) {
            case UP -> "mdi2a-arrow-up-bold";
            case DOWN -> "mdi2a-arrow-down-bold";
            case LEFT -> "mdi2a-arrow-left-bold";
            case RIGHT -> "mdi2a-arrow-right-bold";
        };
    }
}
