package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.domain.PlayerProfile;
import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.MindState;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.persistence.GameSave;
import it.unicam.cs.mpgc.rpg123465.persistence.SaveManager;

import java.io.IOException;

/**
 * Fa da tramite tra interfaccia grafica e modello di gioco.
 */
public class GameController {

    private final SaveManager saveManager;
    private GameEngine gameEngine;

    /**
     * Snapshot dello stato prima di entrare nel piano corrente.
     *
     * Salvare durante un minigioco significa salvare questo checkpoint:
     * ricaricando si riparte dall'inizio dello stesso piano, senza duplicare
     * Stress d'ingresso o scelte narrative.
     */
    private GameSave floorCheckpoint;

    public GameController(GameEngine gameEngine, SaveManager saveManager) {
        if (gameEngine == null) {
            throw new IllegalArgumentException(
                    "Il motore di gioco non può essere null."
            );
        }

        if (saveManager == null) {
            throw new IllegalArgumentException(
                    "Il gestore di salvataggio non può essere null."
            );
        }

        this.gameEngine = gameEngine;
        this.saveManager = saveManager;
    }

    public Floor getCurrentFloor() {
        return gameEngine.getCurrentFloor();
    }

    public boolean isGameCompleted() {
        return gameEngine.isGameCompleted();
    }

    public void climbToNextFloor() {
        player().getMind().rest();
        gameEngine.climb();
        floorCheckpoint = null;
    }

    public String getPlayerName() {
        return player().getName();
    }

    public MindState getMind() {
        return player().getMind();
    }

    public PlayerProfile getPlayerProfile() {
       return player().getMind().profile();
    }

    public int getPlayerCurrentHealth() {
        return player().getStats().getCurrentHealth();
    }

    public int getPlayerMaxHealth() {
        return player().getStats().getMaxHealth();
    }

    public void woundPlayer(int damage) {
        player().takeDamage(damage);
    }

    public void setPlayerHealth(int value) {
        player().getStats().setCurrentHealth(value);
    }

    public boolean isDefeated() {
        return !player().isAlive()
                || player().getMind().isSopraffatto();
    }

    public boolean hasSavedGame() {
        return saveManager.exists();
    }

    /**
     * Fissa il punto di ripresa del piano corrente.
     *
     * Va chiamato prima di creare la scena del piano, quindi prima che
     * enterRoom() o una scelta possano modificare il giocatore.
     */
    public void beginFloorCheckpoint() {
        floorCheckpoint = snapshotCurrentState();
    }

    /**
     * Salva il checkpoint del piano corrente.
     *
     * Se non è ancora stato creato un checkpoint, salva lo stato corrente.
     */
    public OperationResult saveGame() {
        try {
            GameSave save = floorCheckpoint != null
                    ? floorCheckpoint
                    : snapshotCurrentState();

            saveManager.save(save);
            return OperationResult.ok();

        } catch (IOException e) {
            return OperationResult.failure(e.getMessage());
        }
    }

    /**
     * Carica l'ultima partita salvata.
     */
    public OperationResult loadGame() {
        try {
            GameSave save = saveManager.load();

            GameEngine loadedEngine =
                    GameFactory.createNewGame(save.getPlayerName());

            loadedEngine.restoreState(
                    save.getCurrentFloor(),
                    save.isGameCompleted()
            );

            loadedEngine.getPlayer()
                    .getStats()
                    .setCurrentHealth(save.getCurrentHealth());

            loadedEngine.getPlayer()
                    .getMind()
                    .restoreFrom(save.getMindState());

            gameEngine = loadedEngine;

            /*
             * Il file rappresenta già l'ingresso del piano.
             * Manteniamo una copia indipendente per eventuali nuovi salvataggi.
             */
            floorCheckpoint = snapshotCurrentState();

            return OperationResult.ok();

        } catch (IOException
                 | ClassNotFoundException
                 | IllegalArgumentException e) {

            return OperationResult.failure(e.getMessage());
        }
    }

    private GameSave snapshotCurrentState() {
        return new GameSave(
                player().getName(),
                gameEngine.getCurrentFloorIndex(),
                player().getStats().getCurrentHealth(),
                gameEngine.isGameCompleted(),
                player().getMind().copy()
        );
    }

    private Player player() {
        return gameEngine.getPlayer();
    }
}
