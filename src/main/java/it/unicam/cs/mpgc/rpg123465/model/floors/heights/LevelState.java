package it.unicam.cs.mpgc.rpg123465.model.floors.heights;

/**
 * Gli stati della traversata. Ogni stato accetta solo gli input che gli
 * competono: il clic sul ponte durante la scelta iniziale, le frecce durante
 * l'attraversamento, il clic sul ponte illuminato durante un cambio.
 */
public enum LevelState {
    INTRO,
    BRIDGE_SELECTION,
    PLAYING,
    CHANGING_BRIDGE,
    VICTORY,
    GAME_OVER
}
