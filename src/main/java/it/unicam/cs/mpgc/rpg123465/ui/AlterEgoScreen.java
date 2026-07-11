package it.unicam.cs.mpgc.rpg123465.ui;

import it.unicam.cs.mpgc.rpg123465.audio.Sound;
import it.unicam.cs.mpgc.rpg123465.domain.AlterEgo;
import it.unicam.cs.mpgc.rpg123465.domain.MindState;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * La rivelazione finale: la forma che Chimeris ha assunto in cima alla Torre.
 * <p>
 * Non è scelta né casuale, ed è importante che il giocatore lo capisca: non
 * descrive lui, ma il percorso che ha compiuto. Per questo, accanto al nome
 * dell'alter ego, la schermata mostra i tratti da cui è nato.
 */
public class AlterEgoScreen {

    private final MindState mind;
    private final AlterEgo alterEgo;
    private final Runnable onBackToMenu;
    private final StackPane root = new StackPane();

    /**
     * Crea la schermata finale.
     *
     * @param mind stato interiore da cui è nato l'alter ego
     * @param onBackToMenu azione da eseguire per tornare al menu
     * @throws IllegalArgumentException se un parametro è null
     */
    public AlterEgoScreen(MindState mind, Runnable onBackToMenu) {
        if (mind == null || onBackToMenu == null) {
            throw new IllegalArgumentException("Gli argomenti non possono essere null.");
        }

        this.mind = mind;
        this.alterEgo = mind.alterEgo();
        this.onBackToMenu = onBackToMenu;
    }

    /**
     * @return radice della schermata finale
     */
    public Parent createView() {
        root.getStyleClass().add("fear-root");

        Sound.stopAll();
        Sound.loop("/audio/ambience-topi.mp3", 0.25);

        Region background = new ScrollingBackground("/images/bg/forest.jpg", 12).createView();
        Region fog = new FogOverlay(0.4).createView();

        root.getChildren().setAll(background, darkOverlay(), fog, content());
        return root;
    }

    private VBox content() {
        Label nome = new Label(alterEgo.getNome());
        nome.getStyleClass().add("fear-title");

        Label descrizione = paragraph(alterEgo.getDescrizione());

        Label percorso = new Label(riepilogoDelPercorso());
        percorso.getStyleClass().add("fear-effects");

        Label monito = new Label(
                "Questa forma non dice chi sei: dice come hai attraversato la Torre.");
        monito.setWrapText(true);
        monito.setMaxWidth(680);
        monito.getStyleClass().add("fear-memory");

        Button menu = new Button("Torna al menu");
        menu.getStyleClass().add("menu-button");
        menu.setOnAction(event -> {
            Sound.stopAll();
            onBackToMenu.run();
        });

        VBox box = new VBox(24, nome, descrizione, percorso, monito, menu);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(760);
        box.getStyleClass().add("fear-panel");
        return box;
    }

    /** I tratti che le scelte hanno lasciato: è da qui che nasce l'alter ego. */
    private String riepilogoDelPercorso() {
        return "Coraggio " + mind.getCoraggio()
                + "     ·     Empatia " + mind.getEmpatia()
                + "     ·     Impulsività " + mind.getImpulsivita()
                + "     ·     Lucidità " + mind.getLucidita() + " / " + MindState.MAX;
    }

    private Label paragraph(String content) {
        Label label = new Label(content);
        label.setWrapText(true);
        label.setMaxWidth(720);
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
