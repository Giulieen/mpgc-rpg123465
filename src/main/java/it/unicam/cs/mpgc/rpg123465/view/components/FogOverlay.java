package it.unicam.cs.mpgc.rpg123465.view.components;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.scene.effect.BlendMode;
import javafx.scene.CacheHint;
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
     * Copie affiancate della texture. Ne bastano tre: la fascia scorre di due
     * schermate e poi ricomincia, e con le copie dispari specchiate la
     * giuntura non si vede.
     */
    private static final int TILES = 3;

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
            throw new IllegalArgumentException("Il fattore di velocità non può essere negativo.");
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
                fogLayer(-9, 0.26, 0.46, 7.5),
                fogLayer(-16, 0.32, 0.55, 9.5)
        );

        return fog;
    }

    /**
     * Crea una banda di nebbia.
     *
     * <p>
     * La deriva è soltanto orizzontale, e non per scelta estetica: ogni copia
     * della texture è grande quanto la finestra, e farla scorrere anche in
     * verticale richiederebbe una griglia di nove copie invece di tre. Su uno
     * schermo ad alta densità quelle copie in più sono il costo maggiore delle
     * schermate iniziali, e il movimento verticale — lentissimo — non si
     * distingue da quello orizzontale.
     *
     * @param vx velocità orizzontale
     * @param minOpacity opacità minima
     * @param maxOpacity opacità massima
     * @param breathSeconds durata del ciclo di respirazione
     * @return layer animato
     */
    private Region fogLayer(
            double vx,
            double minOpacity,
            double maxOpacity,
            double breathSeconds
    ) {
        Pane track = new Pane();
        Pane view = new Pane(track);

        view.setBlendMode(BlendMode.SCREEN);

        /*
         * La fascia sporge di due schermate oltre il bordo, e un Pane calcola
         * la propria dimensione preferita dai figli: senza questo la nebbia
         * direbbe di volere il triplo dello spazio disponibile, e chi la
         * contiene si dimensionerebbe su quel numero. È decorazione stirata dal
         * genitore: la sua preferenza non deve pesare su nessuno.
         */
        neutralSize(track);
        neutralSize(view);

        for (int col = 0; col < TILES; col++) {
            ImageView tile = new ImageView(FOG);

            tile.fitWidthProperty().bind(view.widthProperty());

            tile.fitHeightProperty().bind(view.heightProperty());

            tile.layoutXProperty().bind(view.widthProperty().multiply(col));

            if (col % 2 == 1) {
                tile.setScaleX(-1);
            }

            track.getChildren().add(tile);
        }

        /*
         * La fascia viene ricomposta a ogni fotogramma sotto un clip e una
         * fusione: in cache viene disegnata una volta e poi soltanto traslata,
         * che è l'unica cosa che cambia davvero.
         */
        track.setCache(true);

        track.setCacheHint(CacheHint.SPEED);

        Rectangle clip = new Rectangle();

        clip.widthProperty().bind(view.widthProperty());

        clip.heightProperty().bind(view.heightProperty());

        view.setClip(clip);

        DriftTimer drift = new DriftTimer(view, track, vx * speedFactor);

        FadeTransition breath = createBreath(view, minOpacity, maxOpacity, breathSeconds);

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
     * Toglie un nodo dal calcolo delle dimensioni del genitore.
     *
     * Preferenza a zero perché non chieda spazio, massimo illimitato perché
     * possa comunque riempire tutto quello che gli viene dato.
     */
    private static void neutralSize(Region region) {
        region.setMinSize(0, 0);
        region.setPrefSize(0, 0);
        region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
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

        FadeTransition breath = new FadeTransition(Duration.seconds(seconds), layer);

        breath.setFromValue(maxOpacity);
        breath.setToValue(minOpacity);
        breath.setAutoReverse(true);
        breath.setCycleCount(Animation.INDEFINITE);

        return breath;
    }

    /**
     * Fa scorrere la fascia di nebbia, riportandola indietro di due schermate
     * quando è uscita di tanto: il ciclo è invisibile perché le copie dispari
     * sono specchiate.
     */
    private static final class DriftTimer
            extends AnimationTimer {

        private final Region view;
        private final Pane track;
        private final double vx;

        private long last;

        private DriftTimer(Region view, Pane track, double vx) {
            this.view = view;
            this.track = track;
            this.vx = vx;
        }

        private void startFresh() {
            last = 0;
            start();
        }

        @Override
        public void handle(long now) {
            double width = view.getWidth();

            if (width <= 0) {
                return;
            }

            if (last == 0) {
                last = now;
                return;
            }

            double elapsed = (now - last) / 1_000_000_000.0;

            last = now;

            /*
             * Pausa del thread grafico: riallineiamo il riferimento e saltiamo
             * il frame, così la nebbia riprende da dov'era invece di scattare
             * in avanti di tutta l'interruzione.
             */
            if (elapsed > SceneFx.MAX_FRAME_SECONDS) {
                return;
            }

            double period = 2 * width;

            double x =
                    track.getTranslateX()
                            + vx * elapsed;

            if (x <= -period) {
                x += period;
            } else if (x >= 0) {
                x -= period;
            }

            track.setTranslateX(x);
        }
    }

    /**
     * Carica una sola volta la texture della nebbia.
     */
    private static Image loadFog() {
        var stream =
                FogOverlay.class.getResourceAsStream("/images/bg/fog.jpg");

        if (stream == null) {
            throw new IllegalStateException(
                    "Risorsa della nebbia non trovata: "
                            + "/images/bg/fog.jpg"
            );
        }

        return new Image(stream);
    }
}
