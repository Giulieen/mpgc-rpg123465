package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.Floor;

/**
 * Gestisce una sessione completa di gioco.
 */
public class GameSession {

    private final GameEngine gameEngine;

    public GameSession(GameEngine gameEngine) {
        if (gameEngine == null) {
            throw new IllegalArgumentException("Il motore di gioco non può essere null.");
        }

        this.gameEngine = gameEngine;
    }

    public void start() {
        printHeader();

        while (gameEngine.getPlayer().isAlive() && !gameEngine.isGameCompleted()) {
            playCurrentFloor();

            if (!gameEngine.getPlayer().isAlive() || gameEngine.isGameCompleted()) {
                break;
            }

            gameEngine.advanceFloor();
        }

        printFinalResult();
    }

    private void playCurrentFloor() {
        Floor floor = gameEngine.getCurrentFloor();

        System.out.println();
        System.out.println(floor);
        System.out.println(floor.getDescription());
        System.out.println("Evento: " + floor.getEvent().getTitle());
        System.out.println(floor.getEvent().getDescription());

        gameEngine.executeCurrentFloorEvent();
    }

    private void printHeader() {
        System.out.println("=================================");
        System.out.println("        Tower of Self");
        System.out.println("=================================");
        System.out.println("Giocatore: " + gameEngine.getPlayer().getName());
    }

    private void printFinalResult() {
        System.out.println();

        if (gameEngine.isGameCompleted()) {
            System.out.println("Hai raggiunto la cima della torre e affrontato il tuo Alter Ego.");
            System.out.println("Fine partita: vittoria.");
        } else {
            System.out.println("Il viaggio nella torre si interrompe.");
            System.out.println("Fine partita: sconfitta.");
        }
    }
}