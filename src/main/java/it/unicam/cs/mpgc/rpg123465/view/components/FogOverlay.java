package it.unicam.cs.mpgc.rpg123465.view.components;

import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Velo di nebbia, basato sulla texture {@code fog.jpg}.
 *
 * <p>
 * La texture è fumo grigio su fondo nero: fondendola in
 * {@link BlendMode#SCREEN} il nero sparisce e resta il fumo.
 *
 * <p>
 * La nebbia era animata — derivava lentamente e l'opacità respirava — e per
 * farlo servivano nove copie a schermo intero per strato, due strati, un
 * ritaglio e due animazioni continue. Su uno schermo ad alta densità quel
 * conto si pagava a ogni fotogramma proprio nelle prime schermate, dove la
 * nebbia è più presente, e si vedeva. Ora il velo è fermo: due immagini
 * disegnate una volta sola, nessun lavoro ricorrente. L'atmosfera resta,
 * il movimento no — è uno scambio deliberato, perché la fluidità del gioco
 * vale più della deriva del fumo.
 */
public class FogOverlay {

    private static final Image FOG = loadFog();

    /**
     * Densità dei due veli sovrapposti. Sono le medie dei valori fra cui
     * l'opacità oscillava quando la nebbia respirava, così il velo fermo ha
     * la stessa consistenza che aveva in media prima.
     */
    private static final double FAR_DENSITY = 0.36;
    private static final double NEAR_DENSITY = 0.435;

    private final double intensity;

    public FogOverlay() {
        this(1.0);
    }

    /**
     * @param intensity quanto il velo è denso: 1 è la densità piena, valori
     *                  più bassi lo rendono più tenue
     * @throws IllegalArgumentException se il valore è negativo
     */
    public FogOverlay(double intensity) {
        if (intensity < 0) {
            throw new IllegalArgumentException("L'intensità della nebbia non può essere negativa.");
        }

        this.intensity = intensity;
    }

    /**
     * @return vista della nebbia
     */
    public Region createView() {
        StackPane fog = new StackPane();

        fog.setMouseTransparent(true);

        /*
         * Il secondo velo è specchiato: sovrapposto al primo senza girarlo
         * darebbe la stessa immagine raddoppiata, cioè fumo più scuro invece
         * che fumo più fitto.
         */
        fog.getChildren().addAll(
                fogLayer(FAR_DENSITY, false),
                fogLayer(NEAR_DENSITY, true)
        );

        return fog;
    }

    /**
     * Crea un velo di nebbia.
     *
     * @param density opacità del velo prima di applicare l'intensità
     * @param mirrored se ribaltare la texture, per non ripetere lo stesso
     *                 disegno sopra sé stesso
     * @return il velo, dimensionato da chi lo contiene
     */
    private Region fogLayer(double density, boolean mirrored) {
        ImageView tile = new ImageView(FOG);

        Region view = new StackPane(tile);

        tile.fitWidthProperty().bind(view.widthProperty());

        tile.fitHeightProperty().bind(view.heightProperty());

        if (mirrored) {
            tile.setScaleX(-1);
        }

        view.setBlendMode(BlendMode.SCREEN);

        view.setOpacity(density * intensity);

        /*
         * Preferenza a zero perché non chieda spazio, massimo illimitato
         * perché possa comunque riempire tutto quello che gli viene dato.
         */
        view.setMinSize(0, 0);
        view.setPrefSize(0, 0);
        view.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        return view;
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
