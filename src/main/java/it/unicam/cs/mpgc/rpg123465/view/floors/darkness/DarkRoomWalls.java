package it.unicam.cs.mpgc.rpg123465.view.floors.darkness;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

/** Disegna le pareti e rende visibile, al passaggio della torcia, la cifra attiva. */
public final class DarkRoomWalls {
    private static final double TORCH_RADIUS = .15;
    private static final double REVEAL_PX = 95;
    private static final double MAX_REVEAL = .55;
    private static final Color NUMBER_COLOR = Color.web("#5c6a74");
    private static final Color NUMBER_GLOW = Color.rgb(90, 120, 150, .45);
    private static final double[] X = {.50, .87, .50, .13};
    private static final double[] Y = {.13, .45, .86, .45};

    private final StackPane root;
    private final Node[] numbers = new Node[4];

    public DarkRoomWalls(StackPane root, String combination) {
        this.root = root;
        for (int i = 0; i < numbers.length; i++) {
            Label value = new Label(binary(combination.charAt(i)));
            value.setTextFill(NUMBER_COLOR); value.getStyleClass().add("wall-binary");
            value.setEffect(new DropShadow(8, NUMBER_GLOW)); value.setOpacity(0); value.setAlignment(Pos.CENTER);
            value.setPrefWidth(170); value.setMinWidth(170);
            value.layoutXProperty().bind(root.widthProperty().multiply(X[i]).subtract(85));
            value.layoutYProperty().bind(root.heightProperty().multiply(Y[i]).subtract(35));
            numbers[i] = value;
        }
    }

    public Pane createView() {
        Pane pane = new Pane(numbers);
        pane.setPickOnBounds(false);
        pane.prefWidthProperty().bind(root.widthProperty());
        pane.prefHeightProperty().bind(root.heightProperty());
        return pane;
    }

    public void updateTorch(Rectangle darkness, double mouseX, double mouseY, int activeSlot) {
        double width = root.getWidth(), height = root.getHeight();
        if (width <= 0 || height <= 0) return;
        darkness.setFill(new RadialGradient(0, 0, mouseX / width, mouseY / height, TORCH_RADIUS, true,
                CycleMethod.NO_CYCLE, new Stop(0, Color.TRANSPARENT),
                new Stop(.55, Color.rgb(0, 0, 0, .4)), new Stop(1, Color.rgb(0, 0, 0, .985))));
        for (int i = 0; i < numbers.length; i++) {
            double distance = Math.hypot(mouseX - width * X[i], mouseY - height * Y[i]);
            numbers[i].setOpacity(i == activeSlot ? clamp((REVEAL_PX - distance) / REVEAL_PX) * MAX_REVEAL : 0);
        }
    }

    public void hideNumbers() { for (Node number : numbers) number.setOpacity(0); }
    private static String binary(char digit) { String bits = Integer.toBinaryString(digit - '0'); return "0".repeat(Math.max(0, 4 - bits.length())) + bits; }
    private static double clamp(double value) { return Math.max(0, Math.min(1, value)); }
}
