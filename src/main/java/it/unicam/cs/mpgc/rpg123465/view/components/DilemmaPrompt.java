package it.unicam.cs.mpgc.rpg123465.ui.support;

import it.unicam.cs.mpgc.rpg123465.domain.ProfileTrait;
import it.unicam.cs.mpgc.rpg123465.questions.Dilemma;
import it.unicam.cs.mpgc.rpg123465.questions.DilemmaOption;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Overlay riutilizzabile per i dilemmi "Preferiresti".
 *
 * Ogni risposta è associata internamente a un {@link ProfileTrait},
 * che non viene mostrato al giocatore.
 */
public final class DilemmaPrompt {

    /**
     * Una delle due risposte del dilemma.
     *
     * @param label testo del pulsante
     * @param trait tratto nascosto associato alla risposta
     */
    public record Option(String label, ProfileTrait trait) {
        public Option {
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("Il testo della risposta non può essere vuoto.");
            }

            if (trait == null) {
                throw new IllegalArgumentException("Il tratto non può essere null.");
            }
        }
    }

    private DilemmaPrompt() {
        // Solo metodi statici.
    }

    /**
     * Traduce le due risposte di un dilemma nelle opzioni dell'overlay.
     *
     * @param dilemma dilemma da mostrare
     * @return le due risposte, nell'ordine del dilemma
     */
    public static List<Option> optionsOf(Dilemma dilemma) {
        if (dilemma == null) {
            throw new IllegalArgumentException("Il dilemma non può essere null.");
        }

        return List.of(toOption(dilemma.first()), toOption(dilemma.second()));
    }

    private static Option toOption(DilemmaOption option) {
        return new Option(option.text(), option.trait());
    }

    /**
     * Mostra un dilemma con esattamente due risposte.
     *
     * @param root contenitore della scena
     * @param question domanda "Preferiresti"
     * @param options le due risposte
     * @param onChosen callback della risposta scelta
     * @param keepOnTop nodo da mantenere sopra l'overlay, ad esempio HeaderBar
     */
    public static void show(
            StackPane root,
            String question,
            List<Option> options,
            Consumer<Option> onChosen,
            Node keepOnTop
    ) {
        if (root == null
                || question == null
                || question.isBlank()
                || options == null
                || options.size() != 2
                || onChosen == null) {

            throw new IllegalArgumentException("Il dilemma deve avere una domanda e due risposte.");
        }

        StackPane overlay = new StackPane(SceneFx.veil(root, 0.65));

        Label prompt = SceneFx.paragraph(question);

        prompt.getStyleClass().add("fear-title");

        VBox buttons = new VBox(12);

        buttons.setAlignment(Pos.CENTER);

        for (Option option : options) {
            Button button = new Button(option.label());

            button.getStyleClass().add("menu-button");

            button.setMaxWidth(Double.MAX_VALUE);

            button.setOnAction(event -> {
                root.getChildren().remove(overlay);

                onChosen.accept(option);

                keepOnTop(root, keepOnTop);
            });

            buttons.getChildren().add(button);
        }

        VBox panel = new VBox(26, prompt, buttons);

        panel.setAlignment(Pos.CENTER);

        panel.setMaxWidth(760);

        panel.getStyleClass().add("fear-panel");

        overlay.getChildren().add(panel);

        StackPane.setAlignment(panel, Pos.CENTER);

        root.getChildren().add(overlay);

        keepOnTop(root, keepOnTop);
    }

    private static void keepOnTop(StackPane root, Node node) {
        if (node == null) {
            return;
        }

        if (!root.getChildren().contains(node)) {
            root.getChildren().add(node);
        }

        node.toFront();

        StackPane.setAlignment(node, Pos.TOP_CENTER);
    }
}
