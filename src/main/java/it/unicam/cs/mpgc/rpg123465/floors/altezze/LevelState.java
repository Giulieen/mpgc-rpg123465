package it.unicam.cs.mpgc.rpg123465.floors.altezze;

/**
 * Gli stati del livello. Ogni stato accetta solo gli input che gli competono:
 * la selezione del ponte, la risposta alle frecce, il cambio di ponte col
 * movimento del mouse, l'esito.
 */
public enum LevelState {
    INTRO,
    BRIDGE_SELECTION,
    PLAYING,
    ROUTE_CHANGE_WARNING,
    CHANGING_BRIDGE,
    PAUSED,
    VICTORY,
    GAME_OVER
}
