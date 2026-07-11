package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.domain.AlterEgo;
import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.MindState;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.persistence.GameSave;
import it.unicam.cs.mpgc.rpg123465.persistence.SaveManager;

import java.io.IOException;

/**
 * Fa da tramite tra l'interfaccia grafica e il modello di gioco.
 * <p>
 * Espone alla vista la salita della Torre, lo stato interiore del giocatore e
 * la persistenza, senza dipendere da alcuna libreria grafica: può così essere
 * riutilizzato da viste diverse e testato senza avviare la GUI.
 */
public class GameController {

    private final SaveManager saveManager;

    private GameEngine gameEngine;

    /**
     * Crea un nuovo controller.
     *
     * @param gameEngine motore di gioco iniziale
     * @param saveManager gestore della persistenza
     * @throws IllegalArgumentException se un parametro è null
     */
    public GameController(GameEngine gameEngine, SaveManager saveManager) {
        if (gameEngine == null) {
            throw new IllegalArgumentException("Il motore di gioco non può essere null.");
        }
        if (saveManager == null) {
            throw new IllegalArgumentException("Il gestore di salvataggio non può essere null.");
        }

        this.gameEngine = gameEngine;
        this.saveManager = saveManager;
    }

    // ---------------------------------------------------------------------
    // Salita della Torre
    // ---------------------------------------------------------------------

    /**
     * Restituisce il piano su cui si trova il giocatore.
     *
     * @return piano corrente
     */
    public Floor getCurrentFloor() {
        return gameEngine.getCurrentFloor();
    }

    /**
     * Verifica se la Torre è stata salita fino in cima.
     *
     * @return {@code true} se la partita è conclusa
     */
    public boolean isGameCompleted() {
        return gameEngine.isGameCompleted();
    }

    /**
     * Sale le scale verso il piano successivo, una volta affrontato quello
     * corrente.
     * <p>
     * Le scale sono anche il respiro fra due paure: si recupera parte della
     * Lucidità e si scarica parte dello Stress. Le conseguenze delle scelte
     * pesano ancora, semplicemente non condannano.
     */
    public void climbToNextFloor() {
        player().getMind().rest();
        gameEngine.climb();
    }

    // ---------------------------------------------------------------------
    // Stato del giocatore (sola lettura per la vista)
    // ---------------------------------------------------------------------

    /**
     * Restituisce il nome del giocatore.
     *
     * @return nome del giocatore
     */
    public String getPlayerName() {
        return player().getName();
    }

    /**
     * Restituisce lo stato interiore del giocatore: Lucidità, Stress e la
     * memoria delle reazioni alle paure.
     * <p>
     * È condiviso: le schermate lo leggono e lo modificano, così il gioco
     * ricorda il percorso da un piano all'altro.
     *
     * @return stato interiore del giocatore
     */
    public MindState getMind() {
        return player().getMind();
    }

    /**
     * Restituisce la forma che Chimeris ha assunto, costruita dalle scelte
     * compiute finora.
     *
     * @return alter ego del giocatore
     */
    public AlterEgo getAlterEgo() {
        return player().getMind().alterEgo();
    }

    /**
     * Restituisce i punti vita correnti del giocatore.
     *
     * @return punti vita correnti
     */
    public int getPlayerCurrentHealth() {
        return player().getStats().getCurrentHealth();
    }

    /**
     * Restituisce i punti vita massimi del giocatore.
     *
     * @return punti vita massimi
     */
    public int getPlayerMaxHealth() {
        return player().getStats().getMaxHealth();
    }

    // ---------------------------------------------------------------------
    // Persistenza
    // ---------------------------------------------------------------------

    /**
     * Indica se esiste una partita salvata da poter caricare.
     *
     * @return {@code true} se un salvataggio è disponibile
     */
    public boolean hasSavedGame() {
        return saveManager.exists();
    }

    /**
     * Salva lo stato corrente della partita, percorso interiore compreso.
     *
     * @return messaggio descrittivo dell'esito
     */
    public String saveGame() {
        try {
            GameSave save = new GameSave(
                    player().getName(),
                    gameEngine.getCurrentFloorIndex(),
                    player().getStats().getCurrentHealth(),
                    gameEngine.isGameCompleted(),
                    player().getMind()
            );

            saveManager.save(save);
            return "Partita salvata correttamente.";
        } catch (IOException e) {
            return "Errore durante il salvataggio: " + e.getMessage();
        }
    }

    /**
     * Carica l'ultima partita salvata, sostituendo lo stato corrente.
     *
     * @return messaggio descrittivo dell'esito
     */
    public String loadGame() {
        try {
            GameSave save = saveManager.load();

            gameEngine = GameFactory.createNewGame(save.getPlayerName());
            gameEngine.restoreState(save.getCurrentFloor(), save.isGameCompleted());
            player().getStats().setCurrentHealth(save.getCurrentHealth());

            // Ripristina il percorso interiore: senza, l'alter ego finale non
            // rifletterebbe le scelte fatte prima del salvataggio.
            player().getMind().restoreFrom(save.getMindState());

            return "Partita caricata correttamente.";
        } catch (IOException | ClassNotFoundException e) {
            return "Errore durante il caricamento: " + e.getMessage();
        }
    }

    private Player player() {
        return gameEngine.getPlayer();
    }
}
