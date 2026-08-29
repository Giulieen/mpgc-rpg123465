package it.unicam.cs.mpgc.rpg123465.view.components;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;

/**
 * Tiene agganciata la tastiera alla {@link Scene} corrente.
 *
 * <p>
 * Una prova che si comanda da tastiera ha un problema di ciclo di vita: la
 * scena a cui agganciarsi non esiste ancora quando la vista viene costruita, e
 * può cambiare mentre il gioco è in corso. Chi ascolta deve quindi spostare il
 * proprio filtro da una scena all'altra, e ricordarsi di toglierlo quando il
 * piano finisce — altrimenti il gestore resta appeso a una scena che non si
 * vede più e continua a ricevere i tasti.
 */
public final class KeyboardBinding {

    private final EventHandler<KeyEvent> handler;

    private Scene bound;

    /**
     * @param handler chi riceve i tasti premuti, non nullo
     * @throws IllegalArgumentException se il gestore è nullo
     */
    public KeyboardBinding(EventHandler<KeyEvent> handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Il gestore dei tasti non può essere null.");
        }

        this.handler = handler;
    }

    /**
     * Sposta l'ascolto sulla scena indicata, lasciando quella precedente.
     *
     * @param scene la nuova scena, oppure {@code null} per smettere di
     *              ascoltare senza agganciarsi ad altro
     */
    public void bindTo(Scene scene) {
        release();

        bound = scene;

        if (scene != null) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, handler);
        }
    }

    /**
     * Smette di ascoltare. Chiamarlo più volte non fa danno: è pensato per il
     * riordino di fine piano, che può passare da qui anche se la scena non è
     * mai stata agganciata.
     */
    public void release() {
        if (bound != null) {
            bound.removeEventFilter(KeyEvent.KEY_PRESSED, handler);

            bound = null;
        }
    }
}
