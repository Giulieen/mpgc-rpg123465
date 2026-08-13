package it.unicam.cs.mpgc.rpg123465.floors.altezze;

/**
 * Gli stati della traversata. Ogni stato accetta solo gli input che gli
 * competono: il clic sul ponte durante la scelta iniziale, le frecce durante
 * l'attraversamento, il clic sul ponte illuminato durante un cambio.
 *
 * <p>
 * {@link #ROUTE_CHANGE_WARNING} e {@link #PAUSED} non sono usati: restano da
 * una versione in cui il cambio di ponte era preceduto da un avviso e la
 * traversata si poteva mettere in pausa.
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
