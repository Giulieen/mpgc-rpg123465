package it.unicam.cs.mpgc.rpg123465.floors.encounter;

import it.unicam.cs.mpgc.rpg123465.controller.GameController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Le regole della prova dei Topi, senza alcuna dipendenza da JavaFX.
 *
 * <p>
 * Tiene la posizione del giocatore, i topi in circolazione, le catture e lo
 * stato della prova. Il tempo non è affar suo: è la scena a decidere ogni
 * quanto chiamare {@link #advanceRats()} e {@link #spawnRat()}, così tutte le
 * regole restano verificabili senza far girare un'interfaccia.
 */
public final class RatMazeController {

    /** Topi da catturare per superare la prova. */
    public static final int TARGET_RATS = 10;

    /**
     * Quanti topi possono essere in circolazione insieme.
     *
     * Si comincia con uno solo e si sale con le catture: la difficoltà cresce
     * con quanto il giocatore ha già dimostrato di saper fare.
     */
    private static final int SECOND_RAT_AFTER = 3;
    private static final int THIRD_RAT_AFTER = 7;

    /** Lo stato della prova. */
    public enum MazeState {
        PLAYING,
        FAILED,
        COMPLETED
    }

    /**
     * Cosa è successo durante un passo dei topi.
     *
     * @param captured topi finiti addosso al giocatore
     * @param escaped topi arrivati a un'uscita
     */
    public record TickOutcome(
            int captured,
            int escaped
    ) { }

    private final RatMaze maze;
    private final GameController game;
    private final Random rng;

    private final List<Rat> rats = new ArrayList<>();

    private GridPosition player;
    private int capturedRats;
    private MazeState state = MazeState.PLAYING;

    public RatMazeController(
            RatMaze maze,
            GameController game
    ) {
        this(maze, game, new Random());
    }

    /**
     * @param rng sorgente casuale, iniettabile per rendere deterministici i test
     */
    public RatMazeController(
            RatMaze maze,
            GameController game,
            Random rng
    ) {
        if (maze == null || game == null || rng == null) {
            throw new IllegalArgumentException(
                    "Gli argomenti non possono essere null."
            );
        }

        this.maze = maze;
        this.game = game;
        this.rng = rng;
        this.player = maze.playerStart();
    }

    public MazeState state() {
        return state;
    }

    public GridPosition playerPosition() {
        return player;
    }

    public List<Rat> rats() {
        return List.copyOf(rats);
    }

    public int capturedRats() {
        return capturedRats;
    }

    public int targetRats() {
        return TARGET_RATS;
    }

    public int remainingAttempts() {
        return game.getRemainingAttempts();
    }

    public int maxAttempts() {
        return game.getMaxAttempts();
    }

    /**
     * Sposta il giocatore di una cella, se non c'è un muro.
     *
     * <p>
     * Sbattere contro un muro non costa nulla: è semplicemente un passo che
     * non avviene.
     *
     * @param direction direzione richiesta
     * @return quanti topi sono stati catturati entrando nella nuova cella
     */
    public int movePlayer(
            Direction direction
    ) {
        if (state != MazeState.PLAYING || direction == null) {
            return 0;
        }

        GridPosition next = player.step(direction);

        if (maze.isWall(next)) {
            return 0;
        }

        player = next;

        return collectCaptures();
    }

    /**
     * Fa avanzare tutti i topi di una cella.
     *
     * <p>
     * Prima si controllano le fughe, poi le catture: un topo arrivato
     * sull'uscita è già uscito, anche se il giocatore si trovava lì. Ogni topo
     * produce quindi un solo esito, e nessuno può costare due volte.
     *
     * @return catture e fughe di questo passo
     */
    public TickOutcome advanceRats() {
        if (state != MazeState.PLAYING) {
            return new TickOutcome(0, 0);
        }

        int escaped = 0;

        for (Rat rat : rats) {
            rat.advance();
        }

        Iterator<Rat> iterator = rats.iterator();

        while (iterator.hasNext()) {
            if (iterator.next().hasEscaped()) {
                iterator.remove();
                escaped++;
            }
        }

        for (int i = 0; i < escaped; i++) {
            game.loseAttempt();
        }

        if (!game.hasAttemptsLeft()) {
            state = MazeState.FAILED;
            rats.clear();

            return new TickOutcome(0, escaped);
        }

        return new TickOutcome(collectCaptures(), escaped);
    }

    /**
     * @return {@code true} se c'è spazio per un altro topo in circolazione
     */
    public boolean canSpawn() {
        return state == MazeState.PLAYING
                && rats.size() < maxConcurrentRats()
                && capturedRats + rats.size() < TARGET_RATS;
    }

    /**
     * Fa comparire un topo in una tana a caso, diretto a un'uscita a caso.
     *
     * <p>
     * L'uscita viene sorteggiata prima di calcolare il percorso: due topi nati
     * nella stessa tana possono così prendere strade opposte, senza che serva
     * alcuna logica di inseguimento.
     *
     * @return il topo comparso, oppure {@code null} se non c'era spazio
     */
    public Rat spawnRat() {
        if (!canSpawn()) {
            return null;
        }

        List<GridPosition> dens = maze.dens();
        List<GridPosition> exits = maze.exits();

        GridPosition den = dens.get(rng.nextInt(dens.size()));
        GridPosition exit = exits.get(rng.nextInt(exits.size()));

        List<GridPosition> path = maze.findPath(den, exit);

        if (path.isEmpty()) {
            return null;
        }

        Rat rat = new Rat(path);
        rats.add(rat);

        return rat;
    }

    /**
     * Riporta la prova alle condizioni di partenza dopo un fallimento.
     *
     * <p>
     * I tentativi tornano al massimo, la stanza si svuota e il conteggio
     * riparte da zero. La risposta al dilemma resta registrata: è stata data
     * prima della prova e non viene richiesta di nuovo.
     */
    public void reset() {
        game.resetAttempts();

        rats.clear();
        capturedRats = 0;
        player = maze.playerStart();
        state = MazeState.PLAYING;
    }

    /**
     * Toglie di mezzo i topi finiti nella cella del giocatore.
     *
     * @return quanti ne sono stati catturati
     */
    private int collectCaptures() {
        int captured = 0;

        Iterator<Rat> iterator = rats.iterator();

        while (iterator.hasNext()) {
            if (iterator.next().position().equals(player)) {
                iterator.remove();
                captured++;
            }
        }

        capturedRats += captured;

        if (capturedRats >= TARGET_RATS) {
            state = MazeState.COMPLETED;
            rats.clear();
        }

        return captured;
    }

    private int maxConcurrentRats() {
        if (capturedRats >= THIRD_RAT_AFTER) {
            return 3;
        }

        if (capturedRats >= SECOND_RAT_AFTER) {
            return 2;
        }

        return 1;
    }
}
