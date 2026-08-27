package it.unicam.cs.mpgc.rpg123465.view.floors.darkness;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.model.floors.darkness.CombinationLockState;
import it.unicam.cs.mpgc.rpg123465.view.components.SceneFx;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * La vista ravvicinata della serratura: le quattro finestrelle con le cifre e i
 * pulsanti per comporle. Tiene lei stessa lo stato del lucchetto e, quando le
 * quattro cifre sono confermate, consegna il codice a chi la usa.
 */
public final class CombinationLockView {
    private static final double WIDTH = 560;
    private static final double[] COLUMNS = {0.352, 0.457, 0.562, 0.667};
    private static final Color AMBER = Color.web("#ffe6a3");
    private static final Color AMBER_DIM = Color.rgb(200, 170, 100, 0.75);

    private final CombinationLockState state;
    private final Consumer<String> onSubmit;
    private Label[] labels;
    private Rectangle highlight;
    private double height;

    /**
     * @param state stato della serratura (cifre e cifra attiva)
     * @param onSubmit riceve le quattro cifre quando la combinazione è confermata
     */
    public CombinationLockView(CombinationLockState state, Consumer<String> onSubmit) {
        this.state = state;
        this.onSubmit = onSubmit;
    }

    public StackPane createView() {
        Image image = SceneFx.image("/images/scenes/floor2-lock.jpg");

        ImageView lock = new ImageView(image);
        lock.setFitWidth(WIDTH);
        lock.setPreserveRatio(true);
        lock.setEffect(new ColorAdjust(0, -0.75, 0.18, 0.10));

        height = image != null && image.getWidth() > 0
                ? WIDTH * image.getHeight() / image.getWidth()
                : WIDTH * .66;

        Pane overlay = new Pane();
        overlay.setPrefSize(WIDTH, height);
        overlay.setMinSize(WIDTH, height);
        overlay.setMaxSize(WIDTH, height);
        overlay.setPickOnBounds(false);

        labels = new Label[state.size()];
        for (int i = 0; i < state.size(); i++) {
            Label label = new Label("0");
            label.setTextFill(AMBER_DIM);
            label.setOpacity(.85);
            label.getStyleClass().add("lock-digit");
            center(label, 48, 60, COLUMNS[i] * WIDTH, .470 * height);

            labels[i] = label;
            overlay.getChildren().add(label);
        }

        highlight = new Rectangle(52, 74, Color.TRANSPARENT);
        highlight.setArcWidth(8);
        highlight.setArcHeight(8);
        highlight.setStroke(AMBER);
        highlight.setStrokeWidth(2);
        overlay.getChildren().add(highlight);

        overlay.getChildren().addAll(
                button("mdi2a-arrow-left", 0, this::back),
                button("mdi2c-chevron-up", 1, this::up),
                button("mdi2c-chevron-down", 2, this::down),
                button("mdi2c-check", 3, this::confirm)
        );

        StackPane panel = new StackPane(lock, overlay);
        panel.setMaxSize(WIDTH, height);

        refresh();
        return panel;
    }

    // --- L'inserimento ------------------------------------------------------

    private void up() {
        state.increment();
        Sound.play("/audio/padlock-unlock.mp3", 0.25);
        refresh();
    }

    private void down() {
        state.decrement();
        Sound.play("/audio/padlock-unlock.mp3", 0.25);
        refresh();
    }

    private void back() {
        if (state.moveBack()) {
            refresh();
        }
    }

    private void confirm() {
        if (state.confirm()) {
            refresh();
        } else {
            onSubmit.accept(state.enteredCode());
        }
    }

    private void refresh() {
        for (int i = 0; i < labels.length; i++) {
            labels[i].setText(Integer.toString(state.digitAt(i)));
        }

        highlight.setLayoutX(COLUMNS[state.getActiveSlot()] * WIDTH - highlight.getWidth() / 2);
        highlight.setLayoutY(.470 * height - highlight.getHeight() / 2);
    }

    private Node button(String icon, int column, Runnable action) {
        FontIcon glyph = new FontIcon(icon);
        glyph.setIconSize(32);
        glyph.setIconColor(AMBER_DIM);

        StackPane button = new StackPane(glyph);
        center(button, 60, 54, COLUMNS[column] * WIDTH, .725 * height);

        button.setOnMouseClicked(e -> action.run());
        button.setOnMouseEntered(e -> glyph.setIconColor(AMBER));
        button.setOnMouseExited(e -> glyph.setIconColor(AMBER_DIM));

        return button;
    }

    /*
     * Le coordinate grafiche restano quattro numeri sciolti: sono la posizione
     * del centro e la dimensione del nodo, cioè lo stesso modo in cui JavaFX
     * stessa le tratta in setPrefSize e nei costruttori delle forme.
     */
    private void center(Node node, double w, double h, double x, double y) {
        node.setLayoutX(x - w / 2);
        node.setLayoutY(y - h / 2);

        if (node instanceof Region region) {
            region.setPrefSize(w, h);
            region.setMinSize(w, h);
            region.setMaxSize(w, h);
        }
    }
}
