package it.unicam.cs.mpgc.rpg123465.ui;

import it.unicam.cs.mpgc.rpg123465.domain.PlayerProfile;
import it.unicam.cs.mpgc.rpg123465.ui.support.FogOverlay;
import it.unicam.cs.mpgc.rpg123465.ui.support.ScrollingBackground;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * Schermata che spiega la prova prima di salire il primo piano.
 *
 * Mostra i sette profili raggiungibili, così il giocatore sa fin dall'inizio
 * che le risposte contano e verso cosa lo stanno portando.
 */
public final class ProfileGalleryScreen {

    /** Riquadro della miniatura: la figura ci sta dentro, comunque sia fatta. */
    private static final double THUMB_WIDTH = 104;
    private static final double THUMB_HEIGHT = 148;

    /**
     * Larghezza di ogni casella della fila.
     *
     * Fissa e non dedotta dal contenuto: senza, ogni casella si allargherebbe
     * sulla propria etichetta e i profili dal nome lungo finirebbero più
     * vicini ai vicini di quelli dal nome corto.
     */
    private static final double TILE_WIDTH = 118;

    /** Spazio fra una casella e l'altra. */
    private static final double THUMB_GAP = 20;

    private final Runnable onEnter;

    private final StackPane root = new StackPane();

    public ProfileGalleryScreen(Runnable onEnter) {
        if (onEnter == null) {
            throw new IllegalArgumentException("Il callback di ingresso non può essere null.");
        }

        this.onEnter = onEnter;
    }

    /**
     * @return radice della schermata
     */
    public Parent createView() {
        root.getStyleClass().add("fear-root");

        Region background =
                new ScrollingBackground("/images/bg/forest.jpg", 30).createView();

        Region fog = new FogOverlay(0.35).createView();

        root.getChildren().setAll(background, darkOverlay(), fog, content());

        return root;
    }

    private VBox content() {
        Label title = new Label("PRIMA DI SALIRE");

        title.getStyleClass().add("fear-title");

        Label what =
                paragraph(
                        "Ogni piano della Torre ha una prova da superare "
                                + "e domande a cui rispondere."
                );

        Label why = paragraph("Le prove misurano le tue abilità; " + "le domande misurano te.");

        Label hint =
                paragraph(
                        "Uno di questi profili emergerà dalle tue risposte. "
                                + "Nessuno è migliore degli altri."
                );

        hint.getStyleClass().add("fear-memory");

        Button enter = new Button("Sali il primo piano");

        enter.getStyleClass().add("menu-button");

        enter.setOnAction(event -> onEnter.run());

        VBox box = new VBox(16, title, what, why, gallery(), hint, enter);

        box.setAlignment(Pos.CENTER);

        box.setMaxWidth(960);

        box.getStyleClass().add("fear-panel");

        return box;
    }

    /**
     * I sette profili su una riga sola.
     *
     * Va bene una HBox e non una TilePane: quest'ultima decide da sé quante
     * colonne stanno nello spazio disponibile e, ripiegando su tre, alzava la
     * galleria al punto da far troncare il testo qui sopra.
     */
    private HBox gallery() {
        HBox tiles = new HBox(THUMB_GAP);

        tiles.setAlignment(Pos.BOTTOM_CENTER);

        tiles.setMaxWidth(Region.USE_PREF_SIZE);

        for (PlayerProfile profile : PlayerProfile.values()) {
            tiles.getChildren().add(tile(profile));
        }

        return tiles;
    }

    private VBox tile(PlayerProfile profile) {
        ImageView portrait =
                new ImageView(new Image(getClass().getResourceAsStream(profile.getRitratto())));

        portrait.setPreserveRatio(true);
        portrait.setFitWidth(THUMB_WIDTH);
        portrait.setFitHeight(THUMB_HEIGHT);
        portrait.setSmooth(true);

        /*
         * Il riquadro ha altezza fissa e la figura vi si appoggia in basso:
         * così le sette etichette restano su una riga sola, invece di seguire
         * ognuna l'altezza del proprio disegno.
         */
        StackPane frame = new StackPane(portrait);

        frame.setMinHeight(THUMB_HEIGHT);
        frame.setPrefHeight(THUMB_HEIGHT);
        frame.setMaxHeight(THUMB_HEIGHT);

        StackPane.setAlignment(portrait, Pos.BOTTOM_CENTER);

        /*
         * Il nome mostrato è quello del profilo, non quello che il personaggio
         * ha nel pacchetto grafico: quei nomi non appartengono a questa storia
         * e prometterebbero incontri che non ci sono.
         */
        Label name = new Label(profile.getNome());

        name.getStyleClass().add("fear-effects");

        VBox tile = new VBox(6, frame, name);

        tile.setAlignment(Pos.BOTTOM_CENTER);

        tile.setMinWidth(TILE_WIDTH);
        tile.setPrefWidth(TILE_WIDTH);
        tile.setMaxWidth(TILE_WIDTH);

        return tile;
    }

    private Label paragraph(String content) {
        Label label = new Label(content);

        label.setWrapText(true);

        label.setMaxWidth(760);

        /*
         * Senza questo il VBox, quando lo spazio stringe, riduce l'etichetta
         * alla sua altezza minima — una riga — e il resto della frase sparisce
         * dietro i puntini di sospensione invece di andare a capo.
         */
        label.setMinHeight(Region.USE_PREF_SIZE);

        label.getStyleClass().add("fear-text");

        return label;
    }

    private Node darkOverlay() {
        Rectangle rectangle = new Rectangle();

        rectangle.getStyleClass().add("fear-dark");

        rectangle.widthProperty().bind(root.widthProperty());

        rectangle.heightProperty().bind(root.heightProperty());

        return rectangle;
    }
}
