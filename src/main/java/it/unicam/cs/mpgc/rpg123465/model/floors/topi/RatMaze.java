package it.unicam.cs.mpgc.rpg123465.model.floors.encounter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * La pianta della stanza dei Topi: muri, corridoi, tane, uscite e il punto da
 * cui parte il giocatore.
 *
 * <p>
 * La mappa è fissa e scritta come testo, così si legge e si modifica a occhio.
 * Non conosce JavaFX: sa dire quali celle si attraversano e trovare un percorso
 * fra due punti, e nient'altro.
 */
public final class RatMaze {

    private static final char WALL = '#';
    private static final char DEN = 'H';
    private static final char EXIT = 'E';
    private static final char START = 'P';

    /**
     * La pianta della stanza.
     *
     * <p>
     * Le tane stanno in basso, dietro i tratti di muro della penultima fila —
     * sono buchi nella parete, non porte. Le uscite sono i due angoli in alto:
     * ogni fuga costa quindi al topo tutta l'altezza della stanza. La pianta è
     * simmetrica rispetto all'asse verticale: le due tane laterali distano
     * otto passi dall'uscita più vicina, quella centrale dodici da entrambe.
     *
     * <p>
     * Le file 3 e 5 sono libere da parte a parte e le colonne 1, 5, 9 e 13
     * collegano l'alto al basso: il labirinto è un anello con tagli interni,
     * quindi ogni topo si può intercettare da più di un lato.
     */
    private static final String[] LAYOUT = {
            "###############",
            "#E...........E#",
            "#.###.....###.#",
            "#.............#",
            "#.###.###.###.#",
            "#......P......#",
            "#.###.....###.#",
            "#..H...H...H..#",
            "###############"
    };

    private final char[][] cells;
    private final List<GridPosition> dens = new ArrayList<>();
    private final List<GridPosition> exits = new ArrayList<>();
    private GridPosition playerStart;

    public RatMaze() {
        cells = new char[LAYOUT.length][];

        for (int row = 0; row < LAYOUT.length; row++) {
            cells[row] = LAYOUT[row].toCharArray();

            for (int column = 0; column < cells[row].length; column++) {
                GridPosition position = new GridPosition(row, column);

                switch (cells[row][column]) {
                    case DEN -> dens.add(position);
                    case EXIT -> exits.add(position);
                    case START -> playerStart = position;
                    default -> { }
                }
            }
        }
    }

    public int rows() {
        return cells.length;
    }

    public int columns() {
        return cells[0].length;
    }

    /**
     * @return le tane da cui compaiono i topi
     */
    public List<GridPosition> dens() {
        return List.copyOf(dens);
    }

    /**
     * @return le celle attraverso cui i topi possono sfuggire
     */
    public List<GridPosition> exits() {
        return List.copyOf(exits);
    }

    /**
     * @return la cella da cui parte il giocatore
     */
    public GridPosition playerStart() {
        return playerStart;
    }

    public boolean isWall(GridPosition position) {
        return !inside(position)
                || cells[position.row()][position.column()] == WALL;
    }

    public boolean isDen(GridPosition position) {
        return inside(position)
                && cells[position.row()][position.column()] == DEN;
    }

    public boolean isExit(GridPosition position) {
        return inside(position)
                && cells[position.row()][position.column()] == EXIT;
    }

    /**
     * @return {@code true} se la cella esiste e non è un muro
     */
    public boolean isWalkable(GridPosition position) {
        return inside(position)
                && cells[position.row()][position.column()] != WALL;
    }

    /**
     * Trova il percorso più breve fra due celle.
     *
     * <p>
     * Visita in ampiezza (BFS): la griglia non ha pesi, tutti i passi costano
     * uguale, e la prima volta che la visita raggiunge una cella lo ha fatto
     * per la via più corta. È l'algoritmo giusto per questa mappa — A* non
     * avrebbe niente da guadagnare su nove file per quindici colonne — e viene
     * eseguito una sola volta, alla nascita del topo, non a ogni frame.
     *
     * @param start cella di partenza
     * @param destination cella da raggiungere
     * @return il percorso, da {@code start} a {@code destination} inclusi,
     *         oppure una lista vuota se la destinazione non è raggiungibile
     */
    public List<GridPosition> findPath(GridPosition start, GridPosition destination) {
        if (!isWalkable(start) || !isWalkable(destination)) {
            return List.of();
        }

        if (start.equals(destination)) {
            return List.of(start);
        }

        Map<GridPosition, GridPosition> cameFrom = new HashMap<>();
        Deque<GridPosition> queue = new ArrayDeque<>();

        queue.add(start);
        cameFrom.put(start, start);

        while (!queue.isEmpty()) {
            GridPosition current = queue.poll();

            for (Direction direction : Direction.values()) {
                GridPosition next = current.step(direction);

                if (!isWalkable(next) || cameFrom.containsKey(next)) {
                    continue;
                }

                cameFrom.put(next, current);

                if (next.equals(destination)) {
                    return rebuild(cameFrom, start, destination);
                }

                queue.add(next);
            }
        }

        return List.of();
    }

    /**
     * Risale la catena dei predecessori e la rovescia.
     */
    private List<GridPosition> rebuild(
            Map<GridPosition, GridPosition> cameFrom,
            GridPosition start,
            GridPosition destination
    ) {
        List<GridPosition> path = new ArrayList<>();

        GridPosition current = destination;

        while (!current.equals(start)) {
            path.add(current);
            current = cameFrom.get(current);
        }

        path.add(start);

        java.util.Collections.reverse(path);

        return List.copyOf(path);
    }

    private boolean inside(GridPosition position) {
        return position.row() >= 0
                && position.row() < cells.length
                && position.column() >= 0
                && position.column() < cells[position.row()].length;
    }
}
