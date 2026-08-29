package it.unicam.cs.mpgc.rpg123465.view.components;

import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

/**
 * Sfondo che riempie il contenitore, fermo oppure in lento scorrimento
 * orizzontale.
 *
 * <p>
 * Le copie dell'immagine stanno su una striscia che si sposta tutta insieme:
 * per fotogramma cambia soltanto il {@code translateX} della striscia.
 *
 * <p>
 * Le copie sono due e la seconda è specchiata, così i bordi combaciano e la
 * giuntura non si vede. Percorsa una schermata la striscia torna al punto di
 * partenza e le due copie si scambiano il ribaltamento: a schermo resta lo
 * stesso disegno, quindi il ciclo riparte senza salti.
 *
 * <p>
 * La striscia non va messa in cache: sarebbe larga quanto tutte le copie
 * insieme, e su schermo ad alta densità supera il limite di texture delle
 * schede integrate.
 *
 * <p>
 * L'animazione parte solo quando la vista appartiene a una {@code Scene} e si
 * ferma da sola quando ne viene rimossa.
 */
public class ScrollingBackground {

    private final Image image;
    private final double pixelsPerSecond;

    /**
     * Sfondo fermo: una sola copia, disegnata una volta.
     *
     * @param resourcePath percorso dell'immagine nel classpath
     * @throws IllegalArgumentException se il percorso è vuoto o la risorsa
     *                                  non esiste
     */
    public ScrollingBackground(String resourcePath) {
        this(resourcePath, 0);
    }

    /**
     * Sfondo che scorre verso sinistra.
     *
     * @param resourcePath percorso dell'immagine nel classpath
     * @param pixelsPerSecond velocità di scorrimento; con zero lo sfondo resta
     *                        fermo
     * @throws IllegalArgumentException se il percorso è vuoto, la risorsa non
     *                                  esiste o la velocità è negativa
     */
    public ScrollingBackground(String resourcePath, double pixelsPerSecond) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("Il percorso dell'immagine non può essere vuoto.");
        }

        if (pixelsPerSecond < 0) {
            throw new IllegalArgumentException(
                    "La velocità di scorrimento non può essere negativa."
            );
        }

        var stream = getClass().getResourceAsStream(resourcePath);

        if (stream == null) {
            throw new IllegalArgumentException("Immagine non trovata: " + resourcePath);
        }

        this.image = new Image(stream);
        this.pixelsPerSecond = pixelsPerSecond;
    }

    /**
     * @return lo sfondo, dimensionato da chi lo contiene
     */
    public Region createView() {
        return pixelsPerSecond == 0
                ? stillView()
                : scrollingView();
    }

    /** Una sola copia e nessun timer: non c'è niente da far scorrere. */
    private Region stillView() {
        Pane view = fillingPane();

        ImageView tile = new ImageView(image);

        tile.fitWidthProperty().bind(view.widthProperty());

        tile.fitHeightProperty().bind(view.heightProperty());

        view.getChildren().add(tile);

        return view;
    }

    private Region scrollingView() {
        Pane view = fillingPane();

        ImageView[] tiles = {new ImageView(image), new ImageView(image)};

        Pane track = new Pane(tiles);

        /*
         * Striscia e copie fuori dal layout: le loro misure le decide il timer
         * quando cambia la finestra, e così lo spostamento della striscia non
         * fa ripassare il layout del contenitore a ogni fotogramma.
         */
        track.setManaged(false);

        for (ImageView tile : tiles) {
            tile.setManaged(false);
        }

        view.getChildren().add(track);

        // La copia di destra sporge oltre il bordo: il ritaglio la tiene dentro.
        Rectangle clip = new Rectangle();

        clip.widthProperty().bind(view.widthProperty());

        clip.heightProperty().bind(view.heightProperty());

        view.setClip(clip);

        ScrollingTimer timer = new ScrollingTimer(view, track, tiles, pixelsPerSecond);

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
     * Preferenza a zero perché non chieda spazio, massimo illimitato perché
     * possa comunque riempire tutto quello che gli viene dato.
     */
    private static Pane fillingPane() {
        Pane pane = new Pane();

        pane.setMinSize(0, 0);
        pane.setPrefSize(0, 0);
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        return pane;
    }

    /**
     * Timer dello scorrimento.
     *
     * <p>
     * Il timestamp viene azzerato a ogni riavvio per evitare che il tempo
     * trascorso mentre la schermata non era visibile provochi uno scatto.
     */
    private static final class ScrollingTimer extends AnimationTimer {

        private final Region view;
        private final Pane track;
        private final ImageView[] tiles;
        private final double pixelsPerSecond;

        private long last;

        /** Misure con cui le copie sono state disposte l'ultima volta. */
        private double tileWidth;
        private double tileHeight;

        /** Se la prima copia è quella ribaltata; si scambia a ogni giro. */
        private boolean mirrorFirst;

        private ScrollingTimer(Region view, Pane track, ImageView[] tiles, double pixelsPerSecond) {
            this.view = view;
            this.track = track;
            this.tiles = tiles;
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

            double height = view.getHeight();

            /*
             * Le copie si ridimensionano solo quando la finestra cambia: nei
             * fotogrammi normali questi due confronti sono tutto il lavoro che
             * si aggiunge allo spostamento.
             */
            if (width != tileWidth || height != tileHeight) {
                layoutTiles(width, height);
            }

            if (last == 0) {
                last = now;
                return;
            }

            double elapsed = (now - last) / 1_000_000_000.0;

            last = now;

            /*
             * Pausa del thread grafico: riallineiamo il riferimento e saltiamo
             * il frame, così lo sfondo riprende da dov'era invece di scattare
             * in avanti di tutta l'interruzione.
             */
            if (elapsed > SceneFx.MAX_FRAME_SECONDS) {
                return;
            }

            double x =
                    track.getTranslateX()
                            - pixelsPerSecond * elapsed;

            /*
             * Percorsa una schermata si torna al punto di partenza scambiando
             * il ribaltamento delle due copie: a schermo resta lo stesso
             * disegno, quindi il ritorno non si vede.
             */
            while (x <= -width) {
                x += width;

                swapMirror();
            }

            track.setTranslateX(x);
        }

        private void layoutTiles(double width, double height) {
            tileWidth = width;
            tileHeight = height;

            for (int i = 0; i < tiles.length; i++) {
                ImageView tile = tiles[i];

                tile.setFitWidth(width);

                tile.setFitHeight(height);

                tile.setLayoutX(i * width);
            }

            applyMirror();
        }

        private void swapMirror() {
            mirrorFirst = !mirrorFirst;

            applyMirror();
        }

        /** Una copia dritta e una ribaltata, in un ordine o nell'altro. */
        private void applyMirror() {
            tiles[0].setScaleX(mirrorFirst ? -1 : 1);

            tiles[1].setScaleX(mirrorFirst ? 1 : -1);
        }
    }
}
