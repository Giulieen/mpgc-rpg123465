package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.model.FloorAttempts;
import it.unicam.cs.mpgc.rpg123465.model.floors.encounter.Direction;
import it.unicam.cs.mpgc.rpg123465.model.floors.encounter.GridPosition;
import it.unicam.cs.mpgc.rpg123465.model.floors.encounter.Rat;
import it.unicam.cs.mpgc.rpg123465.model.floors.encounter.RatMaze;
import it.unicam.cs.mpgc.rpg123465.testing.FakeQuestionRepository;
import it.unicam.cs.mpgc.rpg123465.testing.FakeSaveManager;
import it.unicam.cs.mpgc.rpg123465.testing.TestTowers;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RatMazeControllerTest {

    private final RatMaze maze = new RatMaze();

    private GameController game;
    private RatMazeController controller;

    @BeforeEach
    void setUp() {
        game = new GameController(TestTowers.engineWithFloors(3),
                new FakeSaveManager(), new FakeQuestionRepository());
        controller = new RatMazeController(maze, game, new Random(20260813L));
    }

    /**
     * Celle che un topo può occupare: l'unione dei sei tragitti tana-uscita.
     * Il giocatore fermo su una di queste cattura senza muoversi, il che
     * renderebbe non deterministiche le prove sulle fughe.
     */
    private Set<GridPosition> cellsOnRatPaths() {
        Set<GridPosition> cells = new HashSet<>();

        for (GridPosition den : maze.dens()) {
            for (GridPosition exit : maze.exits()) {
                cells.addAll(maze.findPath(den, exit));
            }
        }

        return cells;
    }

    /** Porta il giocatore dove nessun topo passera'. */
    private void parkPlayerOffAnyRatPath() {
        Set<GridPosition> busy = cellsOnRatPaths();

        for (int row = 0; row < maze.rows(); row++) {
            for (int column = 0; column < maze.columns(); column++) {
                GridPosition cell = new GridPosition(row, column);

                if (maze.isWalkable(cell) && !busy.contains(cell)) {
                    walkPlayerTo(cell);
                    return;
                }
            }
        }

        throw new IllegalStateException("ogni cella percorribile e' su un tragitto");
    }

    /** Porta il giocatore su una cella percorrendo il tragitto più breve. */
    private int walkPlayerTo(GridPosition target) {
        int captured = 0;

        /*
         * Se il giocatore e' gia' sulla cella non ci sarebbe alcun passo, e la
         * cattura scatta solo quando qualcuno si muove: un passo di lato
         * garantisce che l'arrivo sia un movimento vero.
         */
        if (controller.playerPosition().equals(target)) {
            captured += stepAside();
        }

        List<GridPosition> path = maze.findPath(controller.playerPosition(), target);

        for (int i = 1; i < path.size(); i++) {
            captured += controller.movePlayer(directionBetween(path.get(i - 1), path.get(i)));
        }

        return captured;
    }

    private int stepAside() {
        for (Direction direction : Direction.values()) {
            if (maze.isWalkable(controller.playerPosition().step(direction))) {
                return controller.movePlayer(direction);
            }
        }

        return 0;
    }

    private static Direction directionBetween(GridPosition from, GridPosition to) {
        if (to.row() < from.row()) {
            return Direction.UP;
        }
        if (to.row() > from.row()) {
            return Direction.DOWN;
        }
        if (to.column() < from.column()) {
            return Direction.LEFT;
        }
        return Direction.RIGHT;
    }

    /** Fa correre i topi finché la stanza non si svuota. */
    private RatMazeController.TickOutcome runUntilEmpty() {
        int captured = 0;
        int escaped = 0;

        for (int tick = 0; tick < 200 && !controller.rats().isEmpty(); tick++) {
            RatMazeController.TickOutcome outcome = controller.advanceRats();
            captured += outcome.captured();
            escaped += outcome.escaped();
        }

        return new RatMazeController.TickOutcome(captured, escaped);
    }

    // --- stato iniziale --------------------------------------------------

    @Test
    void laProvaIniziaVuotaEGiocabile() {
        assertEquals(RatMazeController.MazeState.PLAYING, controller.state());
        assertEquals(maze.playerStart(), controller.playerPosition());
        assertEquals(0, controller.capturedRats());
        assertTrue(controller.rats().isEmpty());
        assertEquals(FloorAttempts.MAX, controller.remainingAttempts());
    }

    // --- movimento -------------------------------------------------------

    @Test
    void unPassoVersoUnaCellaLiberaSpostaIlGiocatore() {
        GridPosition before = controller.playerPosition();

        controller.movePlayer(Direction.LEFT);

        assertEquals(before.step(Direction.LEFT), controller.playerPosition());
    }

    /**
     * Sbattere contro un muro non è un errore: è un passo che non avviene, e
     * non costa nulla al giocatore.
     */
    @Test
    void unPassoControUnMuroLasciaTuttoComEra() {
        walkPlayerTo(new GridPosition(1, 1));
        GridPosition corner = controller.playerPosition();

        controller.movePlayer(Direction.UP);
        controller.movePlayer(Direction.LEFT);

        assertEquals(corner, controller.playerPosition());
        assertEquals(FloorAttempts.MAX, controller.remainingAttempts());
        assertEquals(RatMazeController.MazeState.PLAYING, controller.state());
    }

    @Test
    void unaDirezioneNullaNonSpostaNulla() {
        GridPosition before = controller.playerPosition();

        assertEquals(0, controller.movePlayer(null));
        assertEquals(before, controller.playerPosition());
    }

    // --- comparsa dei topi -----------------------------------------------

    @Test
    void unTopoCompareInUnaTanaConUnPercorsoVersoUnUscita() {
        Rat rat = controller.spawnRat();

        assertNotNull(rat);
        assertTrue(maze.dens().contains(rat.position()));
        assertTrue(maze.exits().contains(rat.targetExit()));
        assertEquals(1, controller.rats().size());
    }

    @Test
    void nonComparePiuDiUnTopoOltreIlLimiteIniziale() {
        controller.spawnRat();
        controller.spawnRat();

        assertFalse(controller.canSpawn());
        assertNull(controller.spawnRat());
        assertEquals(2, controller.rats().size());
    }

    @Test
    void iTopiAvanzanoLungoIlProprioPercorso() {
        Rat rat = controller.spawnRat();
        GridPosition start = rat.position();

        controller.advanceRats();

        assertFalse(rat.position().equals(start));
        assertTrue(maze.isWalkable(rat.position()));
    }

    // --- cattura ---------------------------------------------------------

    @Test
    void raggiungereUnTopoLoCattura() {
        Rat rat = controller.spawnRat();

        int captured = walkPlayerTo(rat.position());

        assertEquals(1, captured);
        assertEquals(1, controller.capturedRats());
        assertTrue(controller.rats().isEmpty());
    }

    /**
     * La cattura deve valere una volta sola: fermarsi sulla cella dove il topo
     * è appena sparito non può incrementare di nuovo il contatore.
     */
    @Test
    void unaCatturaIncrementaIlContatoreUnaVoltaSola() {
        Rat rat = controller.spawnRat();
        walkPlayerTo(rat.position());

        controller.movePlayer(Direction.UP);
        controller.movePlayer(Direction.DOWN);
        controller.advanceRats();

        assertEquals(1, controller.capturedRats());
    }

    @Test
    void catturareNonCostaTentativi() {
        Rat rat = controller.spawnRat();

        walkPlayerTo(rat.position());

        assertEquals(FloorAttempts.MAX, controller.remainingAttempts());
    }

    // --- fuga ------------------------------------------------------------

    @Test
    void unTopoCheRaggiungeLUscitaCostaUnTentativo() {
        parkPlayerOffAnyRatPath();

        controller.spawnRat();

        RatMazeController.TickOutcome outcome = runUntilEmpty();

        assertEquals(1, outcome.escaped());
        assertEquals(0, outcome.captured());
        assertEquals(FloorAttempts.MAX - 1, controller.remainingAttempts());
        assertEquals(0, controller.capturedRats());
    }

    /**
     * Un topo produce un solo esito: continuare a far scorrere il tempo dopo
     * la sua fuga non può togliere un secondo tentativo.
     */
    @Test
    void unTopoNonPuoConsumareDueTentativi() {
        parkPlayerOffAnyRatPath();

        controller.spawnRat();
        runUntilEmpty();

        int afterEscape = controller.remainingAttempts();
        controller.advanceRats();
        controller.advanceRats();

        assertEquals(afterEscape, controller.remainingAttempts());
    }

    @Test
    void treFugheEsaurisconoLaProva() {
        parkPlayerOffAnyRatPath();

        for (int i = 0; i < FloorAttempts.MAX; i++) {
            controller.spawnRat();
            runUntilEmpty();
        }

        assertEquals(0, controller.remainingAttempts());
        assertEquals(RatMazeController.MazeState.FAILED, controller.state());
    }

    @Test
    void aProvaFallitaIlGiocatoreNonSiMuovePiu() {
        parkPlayerOffAnyRatPath();

        for (int i = 0; i < FloorAttempts.MAX; i++) {
            controller.spawnRat();
            runUntilEmpty();
        }

        GridPosition frozen = controller.playerPosition();
        controller.movePlayer(Direction.UP);

        assertEquals(frozen, controller.playerPosition());
        assertFalse(controller.canSpawn());
    }

    // --- vittoria --------------------------------------------------------

    @Test
    void catturareTuttiITopiRichiestiSuperaLaProva() {
        while (controller.state() == RatMazeController.MazeState.PLAYING
                && controller.capturedRats() < controller.targetRats()) {

            Rat rat = controller.spawnRat();

            if (rat == null) {
                break;
            }

            walkPlayerTo(rat.position());
        }

        assertEquals(controller.targetRats(), controller.capturedRats());
        assertEquals(RatMazeController.MazeState.COMPLETED, controller.state());
        assertTrue(controller.rats().isEmpty());
    }

    @Test
    void aProvaSuperataNonCompaionoAltriTopi() {
        while (controller.state() == RatMazeController.MazeState.PLAYING) {
            Rat rat = controller.spawnRat();
            if (rat == null) {
                break;
            }
            walkPlayerTo(rat.position());
        }

        assertFalse(controller.canSpawn());
        assertNull(controller.spawnRat());
    }

    // --- reset -----------------------------------------------------------

    @Test
    void ricominciareRiportaLaProvaAlleCondizioniDiPartenza() {
        controller.spawnRat();
        runUntilEmpty();
        walkPlayerTo(new GridPosition(1, 1));

        controller.reset();

        assertEquals(RatMazeController.MazeState.PLAYING, controller.state());
        assertEquals(maze.playerStart(), controller.playerPosition());
        assertEquals(0, controller.capturedRats());
        assertTrue(controller.rats().isEmpty());
        assertEquals(FloorAttempts.MAX, controller.remainingAttempts());
    }

    @Test
    void siPuoRicominciareAncheDopoAverFallito() {
        parkPlayerOffAnyRatPath();

        for (int i = 0; i < FloorAttempts.MAX; i++) {
            controller.spawnRat();
            runUntilEmpty();
        }

        controller.reset();

        assertEquals(RatMazeController.MazeState.PLAYING, controller.state());
        assertTrue(controller.canSpawn());
        assertNotNull(controller.spawnRat());
    }

    /**
     * Le risposte al dilemma sono state date prima della prova: ricominciarla
     * non deve toccare il profilo.
     */
    @Test
    void ricominciareNonAlteraIlProfilo() {
        game.getMind().registerTrait(it.unicam.cs.mpgc.rpg123465.model.ProfileTrait.CORAGGIO);

        controller.spawnRat();
        runUntilEmpty();
        controller.reset();

        assertEquals(1, game.getMind().getTotalProfileChoices());
    }

    // --- Rat -------------------------------------------------------------

    @Test
    void unTopoFermoInFondoAlPercorsoRisultaFuggito() {
        Rat rat = new Rat(List.of(new GridPosition(1, 1), new GridPosition(1, 2)));

        assertFalse(rat.hasEscaped());

        rat.advance();
        assertTrue(rat.hasEscaped());

        rat.advance();
        assertEquals(new GridPosition(1, 2), rat.position());
    }

    @Test
    void unTopoSenzaPercorsoNonPuoEsistere() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> new Rat(List.of()));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> new Rat(null));
    }
}
