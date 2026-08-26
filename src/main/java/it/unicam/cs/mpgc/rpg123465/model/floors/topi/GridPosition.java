package it.unicam.cs.mpgc.rpg123465.model.floors.topi;

/**
 * Una cella del labirinto, indicata per riga e colonna.
 *
 * @param row riga, da zero in alto
 * @param column colonna, da zero a sinistra
 */
public record GridPosition(int row, int column) {

    /**
     * @param direction direzione dello spostamento
     * @return la cella adiacente nella direzione indicata
     */
    public GridPosition step(Direction direction) {
        return new GridPosition(row + direction.rowDelta(), column + direction.columnDelta());
    }
}
