package it.unicam.cs.mpgc.rpg123465.ui.support;

import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

/**
 * Sfondo che scorre di continuo in orizzontale.
 *
 * Per evitare qualsiasi giuntura visibile l'immagine viene affiancata a copie
 * specchiate: il bordo destro di una combacia sempre con il bordo specchiato
 * della successiva.
 *
 * L'animazione viene avviata soltanto quando la vista appartiene a una Scene
 * e viene fermata automaticamente quando la vista viene rimossa.
 */
public class ScrollingBackground {

    private static final int TILES = 4;

    private final Image image;
    private final double pixelsPerSecond;

    /**
     * @param resourcePath percorso dell'immagine nel classpath
     * @param pixelsPerSecond velocità di scorrimento verso sinistra
     */
    public ScrollingBackground(
            String resourcePath,
            double pixelsPerSecond
    ) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException(
                    "Il percorso dell'immagine non può essere vuoto."
            );
        }

        if (pixelsPerSecond < 0) {
            throw new IllegalArgumentException(
                    "La velocità di scorrimento non può essere negativa."
            );
        }

        var stream = getClass().getResourceAsStream(resourcePath);

        if (stream == null) {
            throw new IllegalArgumentException(
                    "Risorsa grafica non trovata: " + resourcePath
            );
        }

        this.image = new Image(stream);
        this.pixelsPerSecond = pixelsPerSecond;
    }

    /**
     * Crea la vista dello sfondo.
     *
     * L'AnimationTimer segue automaticamente il ciclo di vita della Scene:
     * parte quando il nodo viene mostrato e si ferma quando viene rimosso.
     *
     * @return vista animata
     */
    public Region createView() {
        Pane track = new Pane();
        Pane view = new Pane(track);

        for (int i = 0; i < TILES; i++) {
            ImageView tile = new ImageView(image);

            tile.fitWidthProperty().bind(
                    view.widthProperty()
            );

            tile.fitHeightProperty().bind(
                    view.heightProperty()
            );

            tile.layoutXProperty().bind(
                    view.widthProperty().multiply(i)
            );

            if (i % 2 == 1) {
                // La copia specchiata rende invisibile la giuntura.
                tile.setScaleX(-1);
            }

            track.getChildren().add(tile);
        }

        Rectangle clip = new Rectangle();

        clip.widthProperty().bind(
                view.widthProperty()
        );

        clip.heightProperty().bind(
                view.heightProperty()
        );

        view.setClip(clip);

        ScrollingTimer timer = new ScrollingTimer(
                view,
                track,
                pixelsPerSecond
        );

        /*
         * Il timer esiste soltanto finché questa vista è effettivamente
         * collegata a una Scene.
         */
        view.sceneProperty().addListener(
                (observable, oldScene, newScene) -> {
                    if (newScene == null) {
                        timer.stop();
                    } else {
                        timer.startFresh();
                    }
                }
        );

        return view;
    }

    /**
     * Timer dello scorrimento.
     *
     * Il timestamp viene azzerato a ogni riavvio per evitare che il tempo
     * trascorso mentre la schermata non era visibile provochi uno scatto.
     */
    private static final class ScrollingTimer extends AnimationTimer {

        private final Region view;
        private final Pane track;
        private final double pixelsPerSecond;

        private long last;

        private ScrollingTimer(
                Region view,
                Pane track,
                double pixelsPerSecond
        ) {
            this.view = view;
            this.track = track;
            this.pixelsPerSecond = pixelsPerSecond;
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

            double elapsed =
                    (now - last) / 1_000_000_000.0;

            last = now;

            /*
             * Pausa del thread grafico: riallineiamo il riferimento e saltiamo
             * il frame, così lo sfondo riprende da dov'era invece di scattare
             * in avanti di tutta l'interruzione.
             */
            if (elapsed > SceneFx.MAX_FRAME_SECONDS) {
                return;
            }

            /*
             * Il motivo si ripete ogni due immagini:
             * una normale e una specchiata.
             */
            double period = 2 * width;

            double x =
                    track.getTranslateX()
                            - pixelsPerSecond * elapsed;

            if (x <= -period) {
                x += period;
            }

            track.setTranslateX(x);
        }
    }
}