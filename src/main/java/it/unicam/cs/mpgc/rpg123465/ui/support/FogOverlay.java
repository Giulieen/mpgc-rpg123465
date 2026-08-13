package it.unicam.cs.mpgc.rpg123465.ui.support;

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
 * Nebbia animata riutilizzabile, basata sulla texture {@code fog.jpg}.
 *
 * La texture è fumo grigio su fondo nero: fondendola in
 * {@link BlendMode#SCREEN} il nero sparisce e resta il fumo.
 *
 * Le animazioni seguono automaticamente il ciclo di vita della Scene:
 * vengono avviate quando la nebbia viene mostrata e fermate quando viene
 * rimossa, evitando timer e transizioni rimasti attivi in background.
 */
public class FogOverlay {

    private static final Image FOG = loadFog();

    /**
     * Moltiplicatore di velocità:
     * 1 = normale, valori più bassi = più lenta.
     */
    private final double speedFactor;

    public FogOverlay() {
        this(1.0);
    }

    /**
     * @param speedFactor moltiplicatore della velocità
     */
    public FogOverlay(double speedFactor) {
        if (speedFactor < 0) {
            throw new IllegalArgumentException(
                    "Il fattore di velocità non può essere negativo."
            );
        }

        this.speedFactor = speedFactor;
    }

    /**
     * @return vista della nebbia
     */
    public Region createView() {
        StackPane fog = new StackPane();

        fog.setMouseTransparent(true);

        fog.getChildren().addAll(
                fogLayer(
                        -9,
                        -5,
                        0.26,
                        0.46,
                        7.5
                ),
                fogLayer(
                        -16,
                        -9,
                        0.32,
                        0.55,
                        9.5
                )
        );

        return fog;
    }

    /**
     * Crea una banda di nebbia.
     *
     * @param vx velocità orizzontale
     * @param vy velocità verticale
     * @param minOpacity opacità minima
     * @param maxOpacity opacità massima
     * @param breathSeconds durata del ciclo di respirazione
     * @return layer animato
     */
    private Region fogLayer(
            double vx,
            double vy,
            double minOpacity,
            double maxOpacity,
            double breathSeconds
    ) {
        Pane track = new Pane();
        Pane view = new Pane(track);

        view.setBlendMode(BlendMode.SCREEN);

        /*
         * Griglia 3x3 di copie specchiate:
         * nessuna giuntura visibile durante lo spostamento.
         */
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                ImageView tile = new ImageView(FOG);

                tile.fitWidthProperty().bind(
                        view.widthProperty()
                );

                tile.fitHeightProperty().bind(
                        view.heightProperty()
                );

                tile.layoutXProperty().bind(
                        view.widthProperty().multiply(col)
                );

                tile.layoutYProperty().bind(
                        view.heightProperty().multiply(row)
                );

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

        clip.widthProperty().bind(
                view.widthProperty()
        );

        clip.heightProperty().bind(
                view.heightProperty()
        );

        view.setClip(clip);

        DriftTimer drift = new DriftTimer(
                view,
                track,
                vx * speedFactor,
                vy * speedFactor
        );

        FadeTransition breath = createBreath(
                view,
                minOpacity,
                maxOpacity,
                breathSeconds
        );

        /*
         * Le animazioni sono attive soltanto quando questo layer appartiene
         * effettivamente a una Scene JavaFX.
         */
        view.sceneProperty().addListener(
                (observable, oldScene, newScene) -> {
                    if (newScene == null) {
                        drift.stop();
                        breath.stop();
                    } else {
                        drift.startFresh();
                        breath.playFromStart();
                    }
                }
        );

        return view;
    }

    /**
     * Crea l'animazione che fa oscillare lentamente l'opacità.
     */
    private FadeTransition createBreath(
            Region layer,
            double minOpacity,
            double maxOpacity,
            double seconds
    ) {
        layer.setOpacity(maxOpacity);

        FadeTransition breath =
                new FadeTransition(
                        Duration.seconds(seconds),
                        layer
                );

        breath.setFromValue(maxOpacity);
        breath.setToValue(minOpacity);
        breath.setAutoReverse(true);
        breath.setCycleCount(Animation.INDEFINITE);

        return breath;
    }

    /**
     * Timer che sposta continuamente una banda di nebbia.
     */
    private static final class DriftTimer
            extends AnimationTimer {

        private final Region view;
        private final Pane track;
        private final double vx;
        private final double vy;

        private long last;

        private DriftTimer(
                Region view,
                Pane track,
                double vx,
                double vy
        ) {
            this.view = view;
            this.track = track;
            this.vx = vx;
            this.vy = vy;
        }

        private void startFresh() {
            last = 0;
            start();
        }

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

            double elapsed =
                    (now - last) / 1_000_000_000.0;

            last = now;

            /*
             * Pausa del thread grafico: riallineiamo il riferimento e saltiamo
             * il frame, così la nebbia riprende da dov'era invece di scattare
             * in avanti di tutta l'interruzione.
             */
            if (elapsed > SceneFx.MAX_FRAME_SECONDS) {
                return;
            }

            double periodX = 2 * width;
            double periodY = 2 * height;

            double x =
                    track.getTranslateX()
                            + vx * elapsed;

            double y =
                    track.getTranslateY()
                            + vy * elapsed;

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
    }

    /**
     * Carica una sola volta la texture della nebbia.
     */
    private static Image loadFog() {
        var stream =
                FogOverlay.class.getResourceAsStream(
                        "/images/bg/fog.jpg"
                );

        if (stream == null) {
            throw new IllegalStateException(
                    "Risorsa della nebbia non trovata: "
                            + "/images/bg/fog.jpg"
            );
        }

        return new Image(stream);
    }
}