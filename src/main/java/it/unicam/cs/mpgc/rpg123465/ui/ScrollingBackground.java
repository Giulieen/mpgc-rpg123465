package it.unicam.cs.mpgc.rpg123465.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

/**
 * Sfondo che scorre di continuo in orizzontale.
 * <p>
 * Per evitare qualsiasi giuntura visibile l'immagine viene affiancata a copie
 * specchiate: il bordo destro di una combacia sempre con il bordo (specchiato)
 * della successiva, così il riavvolgimento del ciclo è impercettibile anche con
 * immagini non ripetibili.
 */
public class ScrollingBackground {

    private static final int TILES = 4;

    private final Image image;
    private final double pixelsPerSecond;

    /**
     * @param resourcePath    percorso dell'immagine nel classpath (es.
     *                        {@code "/images/bg/forest.jpg"})
     * @param pixelsPerSecond velocità di scorrimento verso sinistra
     */
    public ScrollingBackground(String resourcePath, double pixelsPerSecond) {
        this.image = new Image(getClass().getResourceAsStream(resourcePath));
        this.pixelsPerSecond = pixelsPerSecond;
    }

    /**
     * @return una vista che riempie lo spazio disponibile e vi fa scorrere
     *         l'immagine all'infinito.
     */
    public Region createView() {
        Pane track = new Pane();
        Pane view = new Pane(track);

        for (int i = 0; i < TILES; i++) {
            ImageView tile = new ImageView(image);
            tile.fitWidthProperty().bind(view.widthProperty());
            tile.fitHeightProperty().bind(view.heightProperty());
            tile.layoutXProperty().bind(view.widthProperty().multiply(i));
            if (i % 2 == 1) {
                tile.setScaleX(-1);   // copia specchiata: la giuntura sparisce
            }
            track.getChildren().add(tile);
        }

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        view.setClip(clip);

        startScrolling(view, track);
        return view;
    }

    private void startScrolling(Region view, Pane track) {
        new AnimationTimer() {
            private long last = 0;

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

                // Il motivo si ripete ogni due immagini (una dritta + una
                // specchiata): riavvolgo di quel passo per non avere scatti.
                double period = 2 * width;
                double x = track.getTranslateX() - pixelsPerSecond * elapsed;
                if (x <= -period) {
                    x += period;
                }
                track.setTranslateX(x);
            }
        }.start();
    }
}
