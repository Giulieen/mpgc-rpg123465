package it.unicam.cs.mpgc.rpg123465;

import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.engine.GameSession;

/**
 * Entry point dell'applicazione Tower of Self.
 */
public final class MainApp {

    private MainApp() {
    }

    public static void main(String[] args) {
        GameSession session = new GameSession(GameFactory.createNewGame());
        session.start();
    }
}