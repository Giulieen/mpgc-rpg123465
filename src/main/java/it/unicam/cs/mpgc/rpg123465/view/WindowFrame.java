package it.unicam.cs.mpgc.rpg123465.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * La cornice della finestra, disegnata dal gioco invece che dal sistema.
 *
 * <p>
 * La barra bianca di Windows spezzava l'atmosfera già prima del menu, quindi la
 * finestra è senza decorazioni e i comandi vivono qui, nella stessa pietra e
 * nello stesso oro del resto. In cambio tocca a noi ciò che il sistema faceva
 * da sé: trascinare, ingrandire e ridimensionare dai bordi.
 *
 * <p>
 * Ospita anche i messaggi del gioco, che per lo stesso motivo non sono più
 * finestre di sistema.
 */
public final class WindowFrame {

    /** Quanto vicino a un bordo si deve stare per ridimensionare. */
    private static final double RESIZE_MARGIN = 6;

    private static final double MIN_WIDTH = 1000;
    private static final double MIN_HEIGHT = 760;

    private final Stage stage;

    private final StackPane contentLayer = new StackPane();

    /*
     * BorderPane e non VBox: il centro di un BorderPane riceve per definizione
     * tutto lo spazio che avanza sotto la barra. Con un VBox lo spazio dipende
     * dall'altezza preferita del figlio, e una schermata che ne chiede piu' del
     * dovuto si ritrova scentrata.
     */
    private final BorderPane root = new BorderPane();

    private FontIcon maximizeIcon;

    /** Scarto fra l'angolo della finestra e il punto afferrato col mouse. */
    private double grabX;
    private double grabY;

    /** Bordi afferrati per il ridimensionamento, decisi al momento del clic. */
    private boolean resizingLeft;
    private boolean resizingRight;
    private boolean resizingTop;
    private boolean resizingBottom;

    /** Geometria da ripristinare uscendo dall'ingrandimento. */
    private double restoreX;
    private double restoreY;
    private double restoreWidth;
    private double restoreHeight;

    public WindowFrame(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("La finestra non può essere null.");
        }

        this.stage = stage;

        root.getStyleClass().add("window-root");
        root.setTop(titleBar());
        root.setCenter(contentLayer);

        installResize();
    }

    /**
     * @return la radice da mettere nella scena
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * Sostituisce la schermata mostrata sotto la barra.
     *
     * @param content nuova schermata
     */
    public void setContent(Parent content) {
        /*
         * Nessuna schermata deve poter allargare la finestra: qui riceve lo
         * spazio che c'e', non quello che chiederebbe.
         */
        if (content instanceof Region region) {
            region.setMinSize(0, 0);
            region.setPrefSize(0, 0);
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }

        if (contentLayer.getChildren().isEmpty()) {
            contentLayer.getChildren().add(content);
            return;
        }

        contentLayer.getChildren().set(0, content);
    }

    // ---------------------------------------------------------------------
    // Barra
    // ---------------------------------------------------------------------

    private Region titleBar() {
        FontIcon mark = new FontIcon("mdi2c-castle");
        mark.setIconSize(16);
        mark.getStyleClass().add("window-mark");

        Label title = new Label("Tower of Self");
        title.getStyleClass().add("window-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        maximizeIcon = new FontIcon(maximizeLiteral());

        HBox bar = new HBox(
                10,
                mark,
                title,
                spacer,
                barButton(new FontIcon("mdi2w-window-minimize"),
                        () -> stage.setIconified(true), false),
                barButton(maximizeIcon, this::toggleMaximize, false),
                barButton(new FontIcon("mdi2w-window-close"), stage::close, true)
        );

        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 0, 0, 14));
        bar.getStyleClass().add("window-bar");
        bar.setMinHeight(34);
        bar.setPrefHeight(34);

        installDrag(bar);

        return bar;
    }

    private Button barButton(FontIcon icon, Runnable action, boolean danger) {
        icon.setIconSize(14);

        Button button = new Button();
        button.setGraphic(icon);
        button.setFocusTraversable(false);
        button.getStyleClass().add("window-button");

        if (danger) {
            button.getStyleClass().add("window-button-close");
        }

        button.setOnAction(event -> action.run());

        return button;
    }

    /**
     * Trascinamento della finestra dalla barra.
     *
     * Trascinando una finestra ingrandita la si rimette a misura, come fa il
     * sistema: altrimenti resterebbe incollata a schermo intero.
     */
    private void installDrag(Region bar) {
        bar.setOnMousePressed(event -> {
            grabX = event.getScreenX() - stage.getX();
            grabY = event.getScreenY() - stage.getY();
        });

        bar.setOnMouseDragged(event -> {
            if (stage.isMaximized()) {
                double ratio = grabX / stage.getWidth();

                restoreGeometry();
                grabX = ratio * stage.getWidth();
            }

            stage.setX(event.getScreenX() - grabX);
            stage.setY(event.getScreenY() - grabY);
        });

        bar.setOnMouseClicked(event -> { if (event.getClickCount() == 2) { toggleMaximize(); } });
    }

    private void toggleMaximize() {
        if (stage.isMaximized()) {
            restoreGeometry();
        } else {
            rememberGeometry();
            stage.setMaximized(true);
        }

        maximizeIcon.setIconLiteral(maximizeLiteral());
    }

    private void rememberGeometry() {
        restoreX = stage.getX();
        restoreY = stage.getY();
        restoreWidth = stage.getWidth();
        restoreHeight = stage.getHeight();
    }

    private void restoreGeometry() {
        stage.setMaximized(false);

        if (restoreWidth > 0) {
            stage.setX(restoreX);
            stage.setY(restoreY);
            stage.setWidth(restoreWidth);
            stage.setHeight(restoreHeight);
        }

        maximizeIcon.setIconLiteral(maximizeLiteral());
    }

    private String maximizeLiteral() {
        return stage.isMaximized()
                ? "mdi2w-window-restore"
                : "mdi2w-window-maximize";
    }

    // ---------------------------------------------------------------------
    // Ridimensionamento
    // ---------------------------------------------------------------------

    /**
     * Ridimensionamento dai bordi.
     *
     * Senza decorazioni il sistema non offre più le maniglie: le rifacciamo
     * guardando quanto il puntatore è vicino a ciascun lato. I bordi afferrati
     * si fissano al momento del clic, così durante il trascinamento la
     * direzione non cambia sotto le dita.
     */
    private void installResize() {
        root.setOnMouseMoved(event -> {
            if (stage.isMaximized()) {
                root.setCursor(Cursor.DEFAULT);
                return;
            }

            root.setCursor(cursorFor(
                    nearLeft(event), nearRight(event),
                    nearTop(event), nearBottom(event)
            ));
        });

        root.setOnMousePressed(event -> {
            resizingLeft = nearLeft(event);
            resizingRight = nearRight(event);
            resizingTop = nearTop(event);
            resizingBottom = nearBottom(event);
        });

        root.setOnMouseDragged(event -> {
            if (stage.isMaximized()) {
                return;
            }

            if (resizingRight) {
                stage.setWidth(Math.max(MIN_WIDTH, event.getScreenX() - stage.getX()));
            }

            if (resizingBottom) {
                stage.setHeight(Math.max(MIN_HEIGHT, event.getScreenY() - stage.getY()));
            }

            if (resizingLeft) {
                double right = stage.getX() + stage.getWidth();
                double width = Math.max(MIN_WIDTH, right - event.getScreenX());

                stage.setX(right - width);
                stage.setWidth(width);
            }

            if (resizingTop) {
                double bottom = stage.getY() + stage.getHeight();
                double height = Math.max(MIN_HEIGHT, bottom - event.getScreenY());

                stage.setY(bottom - height);
                stage.setHeight(height);
            }
        });

        root.setOnMouseReleased(event -> {
            resizingLeft = false;
            resizingRight = false;
            resizingTop = false;
            resizingBottom = false;
        });
    }

    private boolean nearLeft(MouseEvent event) {
        return event.getX() < RESIZE_MARGIN;
    }

    private boolean nearRight(MouseEvent event) {
        return event.getX() > root.getWidth() - RESIZE_MARGIN;
    }

    private boolean nearTop(MouseEvent event) {
        return event.getY() < RESIZE_MARGIN;
    }

    private boolean nearBottom(MouseEvent event) {
        return event.getY() > root.getHeight() - RESIZE_MARGIN;
    }

    private Cursor cursorFor(boolean left, boolean right, boolean top, boolean bottom) {
        if (top && left) {
            return Cursor.NW_RESIZE;
        }
        if (top && right) {
            return Cursor.NE_RESIZE;
        }
        if (bottom && left) {
            return Cursor.SW_RESIZE;
        }
        if (bottom && right) {
            return Cursor.SE_RESIZE;
        }
        if (left) {
            return Cursor.W_RESIZE;
        }
        if (right) {
            return Cursor.E_RESIZE;
        }
        if (top) {
            return Cursor.N_RESIZE;
        }
        if (bottom) {
            return Cursor.S_RESIZE;
        }

        return Cursor.DEFAULT;
    }

    // ---------------------------------------------------------------------
    // Messaggi
    // ---------------------------------------------------------------------

    /**
     * Mostra un messaggio del gioco, nello stile del gioco.
     *
     * <p>
     * Non blocca: la schermata sotto resta viva e il chiamante viene avvisato
     * alla chiusura. Chi deve tenere ferma una prova mentre il messaggio è a
     * schermo la ferma prima e la riprende in {@code onDismissed}.
     *
     * @param title titolo del messaggio
     * @param message testo del messaggio
     * @param buttonLabel cosa succede chiudendo, scritto sul pulsante
     * @param onDismissed eseguito quando il giocatore chiude, può essere null
     */
    public void showMessage(
            String title,
            String message,
            String buttonLabel,
            Runnable onDismissed
    ) {
        Label heading = new Label(title);
        heading.getStyleClass().add("dialog-title");

        Label body = new Label(message);
        body.setWrapText(true);
        body.setMaxWidth(560);
        body.getStyleClass().add("dialog-text");

        Button ok = new Button(buttonLabel);
        ok.getStyleClass().add("menu-button");

        VBox card = new VBox(20, heading, body, ok);
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        card.setPadding(new Insets(30, 40, 30, 40));
        card.getStyleClass().add("dialog-card");

        StackPane veil = new StackPane(card);
        veil.getStyleClass().add("dialog-veil");

        // Il velo assorbe i clic: sotto non si gioca finché il messaggio è lì.
        veil.setOnMouseClicked(event -> event.consume());

        ok.setOnAction(event -> {
            contentLayer.getChildren().remove(veil);

            if (onDismissed != null) {
                onDismissed.run();
            }
        });

        contentLayer.getChildren().add(veil);
        ok.requestFocus();
    }

    /**
     * Centra la finestra sullo schermo alla dimensione iniziale.
     */
    public void centreOnScreen(double width, double height) {
        var bounds = Screen.getPrimary().getVisualBounds();

        // Su uno schermo piccolo la misura iniziale non deve sbordare.
        double w = Math.min(width, bounds.getWidth());
        double h = Math.min(height, bounds.getHeight());

        stage.setWidth(w);
        stage.setHeight(h);
        stage.setX(bounds.getMinX() + (bounds.getWidth() - w) / 2);
        stage.setY(bounds.getMinY() + (bounds.getHeight() - h) / 2);
    }
}
