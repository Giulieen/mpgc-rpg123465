package it.unicam.cs.mpgc.rpg123465.view.floors.heights;

import it.unicam.cs.mpgc.rpg123465.view.components.SceneFx;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * I tre fari dei ponti — sinistra, centro, destra — su un'unica riga in fondo
 * alla scena. Sono solo grafica: dentro il {@code Group} scalato dello sfondo il
 * picking dei nodi è inaffidabile, così il click non è gestito dai nodi ma dalla
 * scena per vicinanza (vedi {@link #at}).
 * <p>
 * Il componente sa mostrarli tutti accesi (scelta iniziale) oppure oscurare la
 * scena e far pulsare solo quello giusto (reroute), e dice a quale faro è vicino
 * un punto della finestra.
 */
final class BridgeBeacons {

    // Posizione dei fari in frazione della finestra: una sola riga in basso.
    private static final double[] BEACON_X = {0.27, 0.50, 0.73};
    private static final double BEACON_Y = 0.86;
    private static final double CLICK_RADIUS = 70;

    private final StackPane root;
    private final Rectangle dimOverlay;
    private final StackPane[] nodes = new StackPane[3];
    private final Circle[] lights = new Circle[3];
    private FadeTransition pulse;

    /**
     * @param root       la scena su cui posare i fari
     * @param dimOverlay l'oscuramento della scena, mostrato durante il reroute
     */
    BridgeBeacons(StackPane root, Rectangle dimOverlay) {
        this.root = root;
        this.dimOverlay = dimOverlay;
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = build(i);
        }
        root.getChildren().addAll(nodes[0], nodes[1], nodes[2]);
    }

    private StackPane build(int index) {
        Circle light = new Circle(20, Color.rgb(255, 232, 175, 0.92));
        light.setEffect(SceneFx.glow(34, 0.9));
        lights[index] = light;

        StackPane node = new StackPane(light);
        node.setPrefSize(86, 86);
        node.setMinSize(86, 86);
        node.setMaxSize(86, 86);
        node.setMouseTransparent(true);
        node.setVisible(false);
        placeInStack(node, BEACON_X[index], BEACON_Y);
        return node;
    }

    /** Posiziona un nodo per frazione della finestra, senza legare il layout (safe in StackPane). */
    private void placeInStack(Node node, double fracX, double fracY) {
        StackPane.setAlignment(node, Pos.CENTER);
        node.translateXProperty().bind(root.widthProperty().multiply(fracX - 0.5));
        node.translateYProperty().bind(root.heightProperty().multiply(fracY - 0.5));
    }

    /** A quale faro è vicino un punto della finestra, o -1 se nessuno. */
    int at(double x, double y) {
        double cy = root.getHeight() * BEACON_Y;
        for (int i = 0; i < BEACON_X.length; i++) {
            double cx = root.getWidth() * BEACON_X[i];
            if (Math.hypot(x - cx, y - cy) <= CLICK_RADIUS) {
                return i;
            }
        }
        return -1;
    }

    /** Ingrandisce il faro sotto il puntatore (o nessuno, con -1). */
    void hover(int index) {
        for (int i = 0; i < lights.length; i++) {
            double scale = i == index ? 1.35 : 1.0;
            lights[i].setScaleX(scale);
            lights[i].setScaleY(scale);
        }
    }

    /** Selezione iniziale: tutti e tre i fari accesi, in piena luce. */
    void showAll() {
        stopPulse();
        dimOverlay.setVisible(false);
        for (StackPane node : nodes) {
            node.setVisible(true);
            node.setOpacity(1);
        }
    }

    /**
     * Reroute: oscura la scena e lascia tutta la riga di partenza, ma solo il
     * faro giusto è acceso e pulsa; gli altri due restano fiochi.
     */
    void illuminate(int index) {
        stopPulse();
        dimOverlay.setVisible(true);
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].setVisible(true);
            nodes[i].setOpacity(i == index ? 1.0 : 0.28);
        }
        pulse = new FadeTransition(Duration.millis(650), nodes[index]);
        pulse.setFromValue(1);
        pulse.setToValue(0.4);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(FadeTransition.INDEFINITE);
        pulse.play();
    }

    void hide() {
        stopPulse();
        dimOverlay.setVisible(false);
        for (StackPane node : nodes) {
            node.setVisible(false);
        }
    }

    void stopPulse() {
        if (pulse != null) {
            pulse.stop();
            pulse = null;
        }
    }
}
