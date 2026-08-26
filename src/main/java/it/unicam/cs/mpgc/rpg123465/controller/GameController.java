package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.model.Floor;
import it.unicam.cs.mpgc.rpg123465.model.GameEngine;
import it.unicam.cs.mpgc.rpg123465.model.GameFactory;
import it.unicam.cs.mpgc.rpg123465.model.MindState;
import it.unicam.cs.mpgc.rpg123465.model.Player;
import it.unicam.cs.mpgc.rpg123465.model.PlayerProfile;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.QuestionRepository;
import it.unicam.cs.mpgc.rpg123465.persistence.save.GameSave;
import it.unicam.cs.mpgc.rpg123465.persistence.save.SaveManager;

import java.io.IOException;

/**
 * Fa da tramite tra interfaccia grafica e modello di gioco.
 */
public class GameController {

    private final SaveManager saveManager;

    /**
     * Catalogo con cui ricostruire la Torre caricando una partita: il motore
     * viene rifatto da capo, e i piani ne estraggono di nuovo le domande.
     */
    private final QuestionRepository questions;

    private GameEngine gameEngine;

    /**
     * Snapshot dello stato prima di entrare nel piano corrente.
     *
     * Salvare durante un minigioco significa salvare questo checkpoint:
     * ricaricando si riparte dall'inizio dello stesso piano, senza contare
     * due volte le risposte già date su quel piano.
     */
    private GameSave floorCheckpoint;

    public GameController(
            GameEngine gameEngine,
            SaveManager saveManager,
            QuestionRepository questions
    ) {
        if (gameEngine == null) {
            throw new IllegalArgumentException("Il motore di gioco non può essere null.");
        }

        if (saveManager == null) {
            throw new IllegalArgumentException("Il gestore di salvataggio non può essere null.");
        }

        if (questions == null) {
            throw new IllegalArgumentException("Il catalogo delle domande non può essere null.");
        }

        this.gameEngine = gameEngine;
        this.saveManager = saveManager;
        this.questions = questions;
    }

    public Floor getCurrentFloor() {
        return gameEngine.getCurrentFloor();
    }

    public boolean isGameCompleted() {
        return gameEngine.isGameCompleted();
    }

    public void climbToNextFloor() {
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

    /**
     * @return {@code true} se la prova corrente è fallita per tentativi esauriti
     */
    public boolean isDefeated() {
        return !hasAttemptsLeft();
    }

    /**
     * @return tentativi ancora disponibili sul piano corrente
     */
    public int getRemainingAttempts() {
        return gameEngine.getAttempts().getRemaining();
    }

    /**
     * @return tentativi concessi all'ingresso di ogni piano
     */
    public int getMaxAttempts() {
        return gameEngine.getAttempts().getMax();
    }

    /**
     * @return {@code true} se la prova corrente può continuare
     */
    public boolean hasAttemptsLeft() {
        return gameEngine.getAttempts().hasLeft();
    }

    /**
     * Consuma un tentativo per un errore grave del piano.
     */
    public void loseAttempt() {
        gameEngine.getAttempts().lose();
    }

    /**
     * Riporta i tentativi al massimo per ricominciare la prova.
     */
    public void resetAttempts() {
        gameEngine.getAttempts().reset();
    }

    public boolean hasSavedGame() {
        return saveManager.exists();
    }

    /**
     * Fissa il punto di ripresa del piano corrente.
     *
     * Va chiamato prima di creare la scena del piano, quindi prima che una
     * risposta a un dilemma possa entrare nel conteggio dei tratti.
     */
    public void beginFloorCheckpoint() {
        floorCheckpoint = snapshotCurrentState();
    }

    /**
     * Salva il checkpoint del piano corrente.
     *
     * Se non è ancora stato creato un checkpoint, salva lo stato corrente.
     *
     * @return l'esito della scrittura, con il dettaglio in caso di errore
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
     *
     * @return l'esito della lettura, con il dettaglio in caso di errore
     */
    public OperationResult loadGame() {
        try {
            GameSave save = saveManager.load();

            GameEngine loadedEngine =
                    GameFactory.createNewGame(save.getPlayerName(), questions);

            loadedEngine.restoreState(save.getCurrentFloor(), save.isGameCompleted());

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

        } catch (IOException | ClassNotFoundException | IllegalArgumentException e) {

            return OperationResult.failure(e.getMessage());
        }
    }

    private GameSave snapshotCurrentState() {
        return new GameSave(
                player().getName(),
                gameEngine.getCurrentFloorIndex(),
                gameEngine.isGameCompleted(),
                player().getMind().copy()
        );
    }

    private Player player() {
        return gameEngine.getPlayer();
    }
}
