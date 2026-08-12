package it.unicam.cs.mpgc.rpg123465.ui.support;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.InputStream;

/**
 * Utilità grafiche condivise dalle schermate della Torre: caricare immagini,
 * posizionare nodi per frazione, stendere veli e aloni, comporre testi.
 * <p>
 * Raccoglie in un solo posto i gesti che tutte le scene ripetono, così ogni
 * schermata resta concentrata su ciò che ha di proprio.
 */
public final class SceneFx {

    private SceneFx() {
        // Solo metodi statici.
    }

    /** Carica un'immagine dal classpath, o {@code null} se non esiste. */
    public static Image image(String resource) {
        if (resource == null) {
            return null;
        }
        InputStream stream = SceneFx.class.getResourceAsStream(resource);
        return stream != null ? new Image(stream) : null;
    }

    /** Un'immagine che riempie il contenitore, ritagliandola se serve. */
    public static ImageView cover(Region root, String resource) {
        ImageView view = new ImageView(image(resource));
        view.fitWidthProperty().bind(root.widthProperty());
        view.fitHeightProperty().bind(root.heightProperty());
        return view;
    }

    /**
     * L'immagine intera, scalata dentro il contenitore: con {@code scale} sotto 1
     * resta un bordo attorno e il soggetto si allontana.
     */
    public static ImageView contain(Region root, String resource, double scale) {
        ImageView view = new ImageView(image(resource));
        view.setPreserveRatio(true);
        view.fitWidthProperty().bind(root.widthProperty().multiply(scale));
        view.fitHeightProperty().bind(root.heightProperty().multiply(scale));
        return view;
    }

    /** Posiziona un nodo centrandolo su (fracX, fracY), in frazione del contenitore. */
    public static void place(Region root, Node node, double w, double h, double fracX, double fracY) {
        node.layoutXProperty().bind(root.widthProperty().multiply(fracX).subtract(w / 2.0));
        node.layoutYProperty().bind(root.heightProperty().multiply(fracY).subtract(h / 2.0));
    }

    /** Un velo nero, di opacità data, che copre l'intero contenitore. */
    public static Rectangle veil(Region root, double fillAlpha) {
        Rectangle rectangle = new Rectangle();
        rectangle.setFill(Color.rgb(0, 0, 0, fillAlpha));
        rectangle.widthProperty().bind(root.widthProperty());
        rectangle.heightProperty().bind(root.heightProperty());
        return rectangle;
    }

    /** Un alone caldo, per far risaltare un oggetto nel buio. */
    public static DropShadow glow(double radius, double opacity) {
        DropShadow shadow = new DropShadow(radius, Color.rgb(255, 214, 130, opacity));
        shadow.setSpread(0.2);
        return shadow;
    }

    /** Un paragrafo di testo nello stile delle paure. */
    public static Label paragraph(String content) {
        Label label = new Label(content);
        label.setWrapText(true);
        label.setMaxWidth(720);
        label.getStyleClass().add("fear-text");
        return label;
    }

    /**
     * Mostra un messaggio al centro della scena e lo fa svanire da solo: una
     * sola indicazione, che non resta a ingombrare.
     */
    public static void flashMessage(StackPane root, String text) {
        Label message = new Label(text);
        message.setWrapText(true);
        message.setMaxWidth(640);
        message.getStyleClass().add("fear-text");
        StackPane.setAlignment(message, Pos.CENTER);
        root.getChildren().add(message);

        FadeTransition fade = new FadeTransition(Duration.seconds(1.6), message);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setDelay(Duration.seconds(2.6));
        fade.setOnFinished(event -> root.getChildren().remove(message));
        fade.play();
    }

    /**
     * Un intervallo fra due momenti: cala un velo, mostra un testo (e una nota
     * facoltativa più tenue), attende, poi prosegue.
     *
     * @param note riga aggiuntiva sotto il messaggio, oppure {@code null}
     * @param seconds quanto resta a schermo prima di proseguire
     * @param onDone cosa fare allo scadere
     */
    public static void interlude(StackPane root, String message, String note,
                                 double seconds, Runnable onDone) {
        root.getChildren().add(veil(root, 0.82));

        VBox panel = new VBox(18);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(760);
        panel.getStyleClass().add("fear-panel");
        panel.getChildren().add(paragraph(message));

        if (note != null) {
            Label secondary = new Label(note);
            secondary.setWrapText(true);
            secondary.setMaxWidth(680);
            secondary.getStyleClass().add("fear-memory");
            panel.getChildren().add(secondary);
        }

        root.getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.CENTER);

        PauseTransition wait = new PauseTransition(Duration.seconds(seconds));
        wait.setOnFinished(event -> onDone.run());
        wait.play();
    }
}
