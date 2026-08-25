package it.unicam.cs.mpgc.rpg123465.model.floors.encounter;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RatMazeTest {

    private final RatMaze maze = new RatMaze();

    private static void assertContiguous(List<GridPosition> path) {
        for (int i = 1; i < path.size(); i++) {
            GridPosition previous = path.get(i - 1);
            GridPosition current = path.get(i);

            int distance = Math.abs(previous.row() - current.row())
                    + Math.abs(previous.column() - current.column());

            assertEquals(1, distance, "passo non adiacente fra " + previous + " e " + current);
        }
    }

    // --- pianta ----------------------------------------------------------

    @Test
    void laPiantaHaTreTaneEDueUscite() {
        assertEquals(3, maze.dens().size());
        assertEquals(2, maze.exits().size());
    }

    @Test
    void ilBordoEInteramenteMuro() {
        for (int column = 0; column < maze.columns(); column++) {
            assertTrue(maze.isWall(new GridPosition(0, column)));
            assertTrue(maze.isWall(new GridPosition(maze.rows() - 1, column)));
        }

        for (int row = 0; row < maze.rows(); row++) {
            assertTrue(maze.isWall(new GridPosition(row, 0)));
            assertTrue(maze.isWall(new GridPosition(row, maze.columns() - 1)));
        }
    }

    @Test
    void taneUsciteEPartenzaSonoCellePercorribili() {
        assertTrue(maze.isWalkable(maze.playerStart()));
        maze.dens().forEach(den -> assertTrue(maze.isWalkable(den)));
        maze.exits().forEach(exit -> assertTrue(maze.isWalkable(exit)));
    }

    @Test
    void unaCellaFuoriDallaGrigliaContaComeMuro() {
        assertTrue(maze.isWall(new GridPosition(-1, 0)));
        assertTrue(maze.isWall(new GridPosition(0, -1)));
        assertTrue(maze.isWall(new GridPosition(maze.rows(), 0)));
        assertFalse(maze.isWalkable(new GridPosition(maze.rows(), 0)));
    }

    // --- BFS -------------------------------------------------------------

    @Test
    void ilPercorsoParteDallOrigineEFinisceNellaDestinazione() {
        GridPosition start = maze.dens().get(0);
        GridPosition end = maze.exits().get(0);

        List<GridPosition> path = maze.findPath(start, end);

        assertFalse(path.isEmpty());
        assertEquals(start, path.get(0));
        assertEquals(end, path.get(path.size() - 1));
    }

    @Test
    void ilPercorsoProcedeUnaCellaAllaVolta() {
        assertContiguous(maze.findPath(maze.dens().get(1), maze.exits().get(1)));
    }

    @Test
    void ilPercorsoNonAttraversaMuri() {
        for (GridPosition den : maze.dens()) {
            for (GridPosition exit : maze.exits()) {
                for (GridPosition step : maze.findPath(den, exit)) {
                    assertFalse(maze.isWall(step), "il percorso passa dal muro " + step);
                }
            }
        }
    }

    /**
     * Il motivo per cui il livello usa una visita in ampiezza invece di un
     * algoritmo pesato: su una griglia senza costi il primo percorso trovato è
     * già il più corto. Se qualcuno lo sostituisse con una ricerca qualsiasi,
     * questa verifica cadrebbe.
     */
    @Test
    void ilPercorsoTrovatoEIlPiuCortoPossibile() {
        GridPosition start = maze.playerStart();
        GridPosition end = maze.exits().get(0);

        int found = maze.findPath(start, end).size() - 1;
        int shortest = shortestByFlood(start, end);

        assertEquals(shortest, found);
    }

    /** Distanza minima calcolata in modo indipendente dal codice di produzione. */
    private int shortestByFlood(GridPosition start, GridPosition end) {
        int[][] distance = new int[maze.rows()][maze.columns()];
        for (int[] row : distance) {
            java.util.Arrays.fill(row, Integer.MAX_VALUE);
        }
        distance[start.row()][start.column()] = 0;

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int r = 0; r < maze.rows(); r++) {
                for (int c = 0; c < maze.columns(); c++) {
                    if (distance[r][c] == Integer.MAX_VALUE) {
                        continue;
                    }
                    for (Direction direction : Direction.values()) {
                        GridPosition next = new GridPosition(r, c).step(direction);
                        if (!maze.isWalkable(next)) {
                            continue;
                        }
                        if (distance[next.row()][next.column()] > distance[r][c] + 1) {
                            distance[next.row()][next.column()] = distance[r][c] + 1;
                            changed = true;
                        }
                    }
                }
            }
        }

        return distance[end.row()][end.column()];
    }

    @Test
    void unPercorsoVersoSeStessiContieneSoloQuellaCella() {
        GridPosition start = maze.playerStart();

        assertEquals(List.of(start), maze.findPath(start, start));
    }

    @Test
    void nonEsistePercorsoDaOVersoUnMuro() {
        GridPosition wall = new GridPosition(0, 0);

        assertTrue(maze.findPath(wall, maze.playerStart()).isEmpty());
        assertTrue(maze.findPath(maze.playerStart(), wall).isEmpty());
    }

    // --- connettivita' ---------------------------------------------------

    /**
     * Ogni topo deve poter uscire da qualunque tana verso qualunque uscita,
     * altrimenti lo spawn produrrebbe topi bloccati.
     */
    @Test
    void ogniTanaRaggiungeOgniUscita() {
        for (GridPosition den : maze.dens()) {
            for (GridPosition exit : maze.exits()) {
                assertFalse(maze.findPath(den, exit).isEmpty(),
                        "nessun percorso da " + den + " a " + exit);
            }
        }
    }

    @Test
    void ilGiocatoreRaggiungeOgniCellaPercorribile() {
        for (int row = 0; row < maze.rows(); row++) {
            for (int column = 0; column < maze.columns(); column++) {
                GridPosition cell = new GridPosition(row, column);

                if (maze.isWalkable(cell)) {
                    assertFalse(maze.findPath(maze.playerStart(), cell).isEmpty(),
                            "cella irraggiungibile: " + cell);
                }
            }
        }
    }

    // --- GridPosition ----------------------------------------------------

    @Test
    void ogniDirezioneSpostaDiUnaSolaCoordinata() {
        GridPosition origin = new GridPosition(4, 4);

        assertEquals(new GridPosition(3, 4), origin.step(Direction.UP));
        assertEquals(new GridPosition(5, 4), origin.step(Direction.DOWN));
        assertEquals(new GridPosition(4, 3), origin.step(Direction.LEFT));
        assertEquals(new GridPosition(4, 5), origin.step(Direction.RIGHT));
    }
}
