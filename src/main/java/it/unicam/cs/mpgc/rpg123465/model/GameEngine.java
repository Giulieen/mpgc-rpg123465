package it.unicam.cs.mpgc.rpg123465.model;

/**
 * Coordina la salita della Torre: a che piano si trova il giocatore e quando il
 * cammino è concluso.
 * <p>
 * Non sa nulla di come le prove vengano superate: quella è materia del
 * contenuto di ciascun piano. Qui si tiene soltanto il filo del percorso, e i
 * tentativi che restano su quello corrente.
 */
public class GameEngine {

    private final Player player;
    private final Tower tower;

    /**
     * Tentativi per la prova del piano corrente.
     *
     * Stanno qui e non nelle scene perché ogni piano è una prova autonoma:
     * il motore sa quando si cambia piano ed è quindi l'unico punto che può
     * garantire che si ricominci sempre dal massimo.
     */
    private final FloorAttempts attempts = new FloorAttempts();

    private int currentFloorIndex;
    private boolean gameCompleted;

    /**
     * Crea un nuovo motore di gioco.
     *
     * @param player giocatore
     * @param tower torre da salire
     * @throws IllegalArgumentException se un parametro è null
     */
    public GameEngine(Player player, Tower tower) {
        if (player == null) {
            throw new IllegalArgumentException("Il giocatore non può essere null.");
        }
        if (tower == null) {
            throw new IllegalArgumentException("La torre non può essere null.");
        }

        this.player = player;
        this.tower = tower;
        this.currentFloorIndex = 0;
        this.gameCompleted = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Tower getTower() {
        return tower;
    }

    /** @return indice del piano corrente, a partire da zero */
    public int getCurrentFloorIndex() {
        return currentFloorIndex;
    }

    public Floor getCurrentFloor() {
        return tower.getFloor(currentFloorIndex);
    }

    /** @return {@code true} se non ci sono altri piani sopra */
    public boolean isOnLastFloor() {
        return currentFloorIndex == tower.getTotalFloors() - 1;
    }

    /** @return {@code true} se la Torre è stata salita fino in cima */
    public boolean isGameCompleted() {
        return gameCompleted;
    }

    public FloorAttempts getAttempts() {
        return attempts;
    }

    /**
     * Sale al piano successivo, una volta affrontato quello corrente. Se il
     * piano era l'ultimo, la Torre è conclusa.
     *
     * Il nuovo piano è una prova nuova: i tentativi ripartono dal massimo.
     */
    public void climb() {
        if (gameCompleted) {
            return;
        }

        if (isOnLastFloor()) {
            gameCompleted = true;
        } else {
            currentFloorIndex++;
        }

        attempts.reset();
    }

    /**
     * Ripristina il punto della salita raggiunto in una partita salvata.
     *
     * Un salvataggio rappresenta l'ingresso di un piano, quindi i tentativi
     * ripartono dal massimo: non serve conservarli nel file.
     *
     * @param currentFloorIndex indice del piano corrente
     * @param gameCompleted indica se la partita era già conclusa
     * @throws IllegalArgumentException se l'indice del piano non è valido
     */
    public void restoreState(int currentFloorIndex, boolean gameCompleted) {
        if (currentFloorIndex < 0 || currentFloorIndex >= tower.getTotalFloors()) {
            throw new IllegalArgumentException("Indice del piano non valido.");
        }

        this.currentFloorIndex = currentFloorIndex;
        this.gameCompleted = gameCompleted;

        attempts.reset();
    }
}
