package it.unicam.cs.mpgc.rpg123465.floors.encounter;

/**
 * Le quattro direzioni di spostamento nel labirinto.
 *
 * Non esistono diagonali: ogni passo cambia una sola coordinata.
 */
public enum Direction {

    SU(-1, 0),
    GIU(1, 0),
    SINISTRA(0, -1),
    DESTRA(0, 1);

    private final int rowDelta;
    private final int columnDelta;

    Direction(int rowDelta, int columnDelta) {
        this.rowDelta = rowDelta;
        this.columnDelta = columnDelta;
    }

    public int rowDelta() {
        return rowDelta;
    }

    public int columnDelta() {
        return columnDelta;
    }
}
