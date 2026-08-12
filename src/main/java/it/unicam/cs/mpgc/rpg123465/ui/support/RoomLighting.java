package it.unicam.cs.mpgc.rpg123465.ui.support;

import javafx.scene.Cursor;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.BooleanSupplier;

/**
 * La luce di una stanza al buio: il velo d'ombra su cui la torcia apre il suo
 * alone, la luce calda della lampada quando si accende, e l'interruttore per
 * passare dall'una all'altra.
 * <p>
 * Non conosce ciò che la stanza nasconde (le pareti, i numeri): si limita a
 * dire quand'è accesa e a lasciarsi guidare. Chi la usa collega la reazione
 * al cambio di luce con {@link #setOnChange(Runnable)} e recupera il velo con
 * {@link #darkness()} per farci passare la torcia.
 */
public final class RoomLighting {

    private static final Color AMBER = Color.web("#ffe6a3");
    private static final Color DIM = Color.rgb(120, 100, 55);

    private final ColorAdjust brightness = new ColorAdjust();
    private final Rectangle darkness;
    private final Rectangle lampGlow;
    private final FontIcon lightSwitch;

    private boolean lit;
    private Runnable onChange = () -> { };
    private BooleanSupplier canToggle = () -> true;

    /**
     * @param root il contenitore della stanza, a cui velo e alone si adattano
     */
    public RoomLighting(Region root) {
        darkness = SceneFx.veil(root, 0.985);

        lampGlow = new Rectangle();
        lampGlow.widthProperty().bind(root.widthProperty());
        lampGlow.heightProperty().bind(root.heightProperty());
        // Un alone caldo dall'alto: rischiara il centro e lascia gli angoli in
        // ombra, molto meglio di uno schiarimento uniforme (che darebbe grigio).
        lampGlow.setFill(new RadialGradient(
                0, 0, 0.5, 0.16, 0.85, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(255, 226, 172, 0.42)),
                new Stop(0.45, Color.rgb(255, 210, 150, 0.16)),
                new Stop(1, Color.TRANSPARENT)));
        lampGlow.setMouseTransparent(true);

        lightSwitch = new FontIcon("mdi2l-lightbulb-outline");
        lightSwitch.setIconSize(44);
        lightSwitch.setCursor(Cursor.HAND);
        lightSwitch.setEffect(SceneFx.glow(12, 0.30));   // un fioco riflesso, per trovarlo al buio
        lightSwitch.setOnMouseClicked(event -> {
            if (canToggle.getAsBoolean()) {
                toggle();
            }
        });

        setLit(false);
    }

    /** Applica la luce all'immagine di sfondo indicata. */
    public void attach(ImageView background) {
        background.setEffect(brightness);
    }

    /** Il velo d'ombra, su cui la torcia disegna il suo alone. */
    public Rectangle darkness() {
        return darkness;
    }

    /** L'alone caldo della lampada, da porre sopra lo sfondo. */
    public Rectangle lampGlow() {
        return lampGlow;
    }

    /** L'interruttore, da collocare nella stanza. */
    public FontIcon switchIcon() {
        return lightSwitch;
    }

    public boolean isLit() {
        return lit;
    }

    /** Reazione al cambio di luce (per esempio nascondere ciò che il buio mostra). */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    /** Condizione che deve valere perché l'interruttore risponda. */
    public void setToggleGuard(BooleanSupplier canToggle) {
        this.canToggle = canToggle;
    }

    /**
     * Porta la luce nello stato dato senza scatenare la reazione: serve a
     * rimettere la stanza al buio all'inizio di ogni tentativo.
     */
    public void setLit(boolean lit) {
        this.lit = lit;
        if (lit) {
            brightness.setBrightness(0.12);
            brightness.setContrast(0.10);
            darkness.setVisible(false);
            lampGlow.setVisible(true);
            lightSwitch.setIconLiteral("mdi2l-lightbulb-on");
            lightSwitch.setIconColor(AMBER);
        } else {
            brightness.setBrightness(0);
            brightness.setContrast(0);
            darkness.setVisible(true);
            darkness.setFill(Color.rgb(0, 0, 0, 0.985));
            lampGlow.setVisible(false);
            lightSwitch.setIconLiteral("mdi2l-lightbulb-outline");
            lightSwitch.setIconColor(DIM);
        }
    }

    private void toggle() {
        setLit(!lit);
        onChange.run();
    }
}
