package it.unicam.cs.mpgc.rpg123465.ui;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Nebbia animata riutilizzabile, basata sulla texture {@code fog.png}.
 * <p>
 * La texture è fumo grigio su fondo nero: fondendola in {@link BlendMode#SCREEN}
 * il nero sparisce e resta solo il fumo, che schiarisce la scena sottostante
 * come vera nebbia. Due bande derivano molto lente in diagonale — verso
 * sinistra e verso l'alto, così sembra salire dal terreno — e "respirano"
 * (l'opacità oscilla) per un effetto vivo e inquietante. La ripetizione a
 * specchio su entrambi gli assi rende lo scorrimento privo di giunture.
 * È trasparente ai clic, così si può sovrapporre a qualunque scena.
 */
public class FogOverlay {

    private static final Image FOG =
            new Image(FogOverlay.class.getResourceAsStream("/images/bg/fog.jpg"));

    /** Moltiplicatore di velocità: 1 = normale, valori più bassi = più lenta. */
    private final double speedFactor;

    public FogOverlay() {
        this(1.0);
    }

    /**
     * @param speedFactor moltiplicatore della velocità (1 = normale,
     *                    es. 0.4 = molto più lenta)
     */
    public FogOverlay(double speedFactor) {
        this.speedFactor = speedFactor;
    }

    /**
     * @return una vista che riempie lo spazio disponibile e vi fa scorrere la
     *         nebbia.
     */
    public Region createView() {
        StackPane fog = new StackPane();
        fog.setMouseTransparent(true);
        fog.getChildren().addAll(
                fogLayer(-9, -5, 0.26, 0.46, 7.5),    // lontana: lenta, sale piano
                fogLayer(-16, -9, 0.32, 0.55, 9.5));  // vicina: più marcata
        return fog;
    }

    /**
     * Crea una banda di nebbia che deriva a velocità {@code (vx, vy)} px/s
     * (valori negativi = verso sinistra / verso l'alto).
     */
    private Region fogLayer(double vx, double vy, double minOpacity,
                            double maxOpacity, double breathSeconds) {
        Pane track = new Pane();
        Pane view = new Pane(track);
        view.setBlendMode(BlendMode.SCREEN);   // il nero della texture sparisce

        // Griglia 3x3 di copie specchiate per parità: nessuna giuntura, né in
        // orizzontale né in verticale.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                ImageView tile = new ImageView(FOG);
                tile.fitWidthProperty().bind(view.widthProperty());
                tile.fitHeightProperty().bind(view.heightProperty());
                tile.layoutXProperty().bind(view.widthProperty().multiply(col));
                tile.layoutYProperty().bind(view.heightProperty().multiply(row));
                if (col % 2 == 1) {
                    tile.setScaleX(-1);
                }
                if (row % 2 == 1) {
                    tile.setScaleY(-1);
                }
                track.getChildren().add(tile);
            }
        }

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        view.setClip(clip);

        drift(view, track, vx * speedFactor, vy * speedFactor);
        breathe(view, minOpacity, maxOpacity, breathSeconds);
        return view;
    }

    private void drift(Region view, Pane track, double vx, double vy) {
        new AnimationTimer() {
            private long last = 0;

            @Override
            public void handle(long now) {
                double width = view.getWidth();
                double height = view.getHeight();
                if (width <= 0 || height <= 0) {
                    return;
                }
                if (last == 0) {
                    last = now;
                    return;
                }

                double elapsed = (now - last) / 1_000_000_000.0;
                last = now;

                double periodX = 2 * width;
                double periodY = 2 * height;

                double x = track.getTranslateX() + vx * elapsed;
                double y = track.getTranslateY() + vy * elapsed;

                if (x <= -periodX) {
                    x += periodX;
                } else if (x >= 0) {
                    x -= periodX;
                }
                if (y <= -periodY) {
                    y += periodY;
                } else if (y >= 0) {
                    y -= periodY;
                }

                track.setTranslateX(x);
                track.setTranslateY(y);
            }
        }.start();
    }

    /** L'opacità oscilla piano fra i due valori: la nebbia sembra respirare. */
    private void breathe(Region layer, double minOpacity, double maxOpacity, double seconds) {
        layer.setOpacity(maxOpacity);

        FadeTransition breath = new FadeTransition(Duration.seconds(seconds), layer);
        breath.setFromValue(maxOpacity);
        breath.setToValue(minOpacity);
        breath.setAutoReverse(true);
        breath.setCycleCount(Animation.INDEFINITE);
        breath.play();
    }
}
