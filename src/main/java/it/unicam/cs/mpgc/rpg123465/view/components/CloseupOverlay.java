package it.unicam.cs.mpgc.rpg123465.view.components;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Un primo piano su un oggetto: lo sfondo si oscura, l'oggetto compare in
 * dissolvenza al centro, e da un pulsante "Indietro" (o toccando il buio
 * attorno) ci si allontana di nuovo.
 * <p>
 * Non sa cosa mostra: gli si passa un nodo qualsiasi. Serve ovunque il
 * giocatore debba avvicinarsi a qualcosa per usarlo.
 */
public final class CloseupOverlay {

    /**
     * Distanza dall'alto del comando per allontanarsi.
     * <p>
     * Le schermate di gioco tengono in cima una barra di stato che resta sopra
     * il primo piano: il comando parte sotto quella fascia, altrimenti finirebbe
     * coperto e non più cliccabile.
     */
    private static final double BACK_TOP_INSET = 76;

    private final StackPane root;
    private final Runnable onClose;
    private StackPane view;

    /**
     * @param root il contenitore della scena, su cui aprire il primo piano
     * @param onClose azione da eseguire quando ci si allontana
     */
    public CloseupOverlay(StackPane root, Runnable onClose) {
        this.root = root;
        this.onClose = onClose;
    }

    public boolean isOpen() {
        return view != null;
    }

    /** Avvicina la vista all'oggetto indicato. */
    public void open(Node content) {
        if (view != null) {
            return;
        }
        Rectangle backdrop = SceneFx.veil(root, 0.86);
        backdrop.setCursor(Cursor.HAND);
        backdrop.setOnMouseClicked(event -> close());

        Label back = new Label("◀  Indietro");
        back.getStyleClass().add("fear-memory");
        back.setCursor(Cursor.HAND);
        back.setOnMouseClicked(event -> close());
        StackPane.setAlignment(back, Pos.TOP_LEFT);
        StackPane.setMargin(back, new Insets(BACK_TOP_INSET, 0, 0, 34));

        view = new StackPane(backdrop, content, back);
        view.setOpacity(0);
        root.getChildren().add(view);

        FadeTransition fade = new FadeTransition(Duration.millis(260), view);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /** Si allontana, rimuovendo il primo piano. */
    public void close() {
        if (view == null) {
            return;
        }
        root.getChildren().remove(view);
        view = null;
        onClose.run();
    }
}
