package it.unicam.cs.mpgc.rpg123465.floors.altezze;

/**
 * La direzione in cui il personaggio sta perdendo l'equilibrio: un disturbo
 * nella trasmissione, da correggere cliccando dalla parte opposta.
 */
public enum ArrowDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    /** La freccia da premere per contrastare questo sbilanciamento. */
    public ArrowDirection opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}
