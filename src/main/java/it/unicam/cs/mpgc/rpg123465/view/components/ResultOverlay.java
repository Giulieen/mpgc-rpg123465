package it.unicam.cs.mpgc.rpg123465.view.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Supplier;

/**
 * Il cartello che chiude una prova: titolo, esito, riepilogo e un pulsante.
 *
 * <p>
 * Piano I e Piano III lo mostrano negli stessi momenti — prova superata,
 * prova fallita, caduta — e tenerlo in un posto solo evita che si allontanino
 * nell'aspetto a ogni ritocco.
 */
public final class ResultOverlay {

    /** Larghezza massima del cartello. */
    private static final double CARD_WIDTH = 760;

    /** Quanto scurisce la scena sottostante. */
    private static final double VEIL = 0.82;

    private final StackPane root;
    private final Supplier<Node> keepOnTop;

    private StackPane current;

    /**
     * @param root contenitore della scena
     * @param keepOnTop nodo da tenere sopra il cartello — di solito la barra
     *                  di gioco — letto al momento in cui il cartello appare,
     *                  perché la scena lo costruisce dopo di questo
     */
    public ResultOverlay(StackPane root, Supplier<Node> keepOnTop) {
        if (root == null || keepOnTop == null) {
            throw new IllegalArgumentException(
                    "Contenitore e nodo in primo piano sono obbligatori."
            );
        }

        this.root = root;
        this.keepOnTop = keepOnTop;
    }

    /** @return true se un cartello è a schermo */
    public boolean isOpen() {
        return current != null;
    }

    /**
     * Mostra il cartello, sostituendo quello eventualmente già a schermo.
     *
     * @param title titolo dell'esito, oppure null se l'esito parla da sé
     * @param message testo dell'esito
     * @param stats riga di riepilogo, oppure null se l'esito non ne ha una
     * @param buttonText testo del pulsante
     * @param action operazione eseguita alla pressione
     */
    public void show(
            String title,
            String message,
            Label stats,
            String buttonText,
            Runnable action
    ) {
        show(title, message, stats, buttonText, action, VEIL);
    }

    /**
     * Come {@link #show(String, String, Label, String, Runnable)}, ma con il
     * velo regolabile: una presentazione può volerlo più leggero, per
     * lasciar intravedere la scena a cui il giocatore sta per entrare.
     *
     * @param title titolo dell'esito, oppure null se l'esito parla da sé
     * @param message testo dell'esito
     * @param stats riga di riepilogo, oppure null se l'esito non ne ha una
     * @param buttonText testo del pulsante
     * @param action operazione eseguita alla pressione
     * @param veilOpacity quanto il velo copre la scena, da 0 a 1
     */
    public void show(
            String title,
            String message,
            Label stats,
            String buttonText,
            Runnable action,
            double veilOpacity
    ) {
        if (message == null || buttonText == null || action == null) {

            throw new IllegalArgumentException("Messaggio, pulsante e azione sono obbligatori.");
        }

        hide();

        Button button = new Button(buttonText);
        button.getStyleClass().add("menu-button");
        button.setOnAction(event -> action.run());

        VBox card = new VBox(22);

        if (title != null) {
            Label heading = new Label(title);
            heading.getStyleClass().add("fear-title");
            card.getChildren().add(heading);
        }

        card.getChildren().add(SceneFx.paragraph(message));

        if (stats != null) {
            card.getChildren().add(stats);
        }

        card.getChildren().add(button);

        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(CARD_WIDTH);
        card.getStyleClass().add("fear-panel");

        current = new StackPane(SceneFx.veil(root, veilOpacity), card);
        StackPane.setAlignment(card, Pos.CENTER);

        root.getChildren().add(current);

        Node sopra = keepOnTop.get();

        if (sopra != null) {
            sopra.toFront();
        }
    }

    /** Toglie il cartello, se c'è. */
    public void hide() {
        if (current != null) {
            root.getChildren().remove(current);
            current = null;
        }
    }
}
