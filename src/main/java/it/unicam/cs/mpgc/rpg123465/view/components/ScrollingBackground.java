package it.unicam.cs.mpgc.rpg123465.view.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Sfondo che riempie il contenitore.
 *
 * <p>
 * Prima l'immagine scorreva lentamente verso sinistra, e per farlo servivano
 * piu' copie affiancate grandi quanto la finestra, ricomposte a ogni
 * fotogramma sotto un ritaglio. Su uno schermo ad alta densita' quel conto si
 * pagava proprio nelle schermate d'apertura, dove lo sfondo e' l'elemento piu'
 * grande, e si vedeva. Ora l'immagine e' ferma: una sola copia, disegnata una
 * volta. L'atmosfera resta, lo scorrimento no — la fluidita' del gioco vale
 * piu' del movimento del bosco.
 */
public class ScrollingBackground {

    private final Image image;

    /**
     * @param resourcePath percorso dell'immagine nel classpath
     * @throws IllegalArgumentException se il percorso e' vuoto o la risorsa
     *                                  non esiste
     */
    public ScrollingBackground(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("Il percorso dell'immagine non può essere vuoto.");
        }

        var stream = getClass().getResourceAsStream(resourcePath);

        if (stream == null) {
            throw new IllegalArgumentException("Immagine non trovata: " + resourcePath);
        }

        this.image = new Image(stream);
    }

    /**
     * @return lo sfondo, dimensionato da chi lo contiene
     */
    public Region createView() {
        ImageView tile = new ImageView(image);

        Region view = new StackPane(tile);

        tile.fitWidthProperty().bind(view.widthProperty());

        tile.fitHeightProperty().bind(view.heightProperty());

        /*
         * Preferenza a zero perche' non chieda spazio, massimo illimitato
         * perche' possa comunque riempire tutto quello che gli viene dato.
         */
        view.setMinSize(0, 0);
        view.setPrefSize(0, 0);
        view.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        return view;
    }
}
