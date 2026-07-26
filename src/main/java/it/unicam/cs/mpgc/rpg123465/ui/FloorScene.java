package it.unicam.cs.mpgc.rpg123465.ui;

import javafx.scene.Parent;

import java.util.function.Consumer;

/**
 * Un momento di un piano della Torre: l'arrivo in una stanza, una scelta da
 * compiere, una prova da superare.
 * <p>
 * Un piano non è una schermata sola ma una sequenza di scene: così può
 * alternare narrazione, scelte e azione senza che serva una classe nuova ogni
 * volta. Ogni scena costruisce la propria vista e, quando ha finito, dichiara
 * con un {@link SceneOutcome} come si prosegue.
 */
public interface FloorScene {

    /**
     * Costruisce la vista della scena.
     *
     * @param onFinished da invocare quando la scena è conclusa, indicando come
     *                   proseguire
     * @return radice della scena
     */
    Parent createView(Consumer<SceneOutcome> onFinished);
}
