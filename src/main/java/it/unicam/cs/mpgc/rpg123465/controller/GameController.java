package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.combat.CombatAction;
import it.unicam.cs.mpgc.rpg123465.combat.CombatEngine;
import it.unicam.cs.mpgc.rpg123465.combat.CombatResult;
import it.unicam.cs.mpgc.rpg123465.domain.Enemy;
import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.events.CombatEvent;
import it.unicam.cs.mpgc.rpg123465.events.EventResult;
import it.unicam.cs.mpgc.rpg123465.persistence.GameSave;
import it.unicam.cs.mpgc.rpg123465.persistence.SaveManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Fa da tramite tra l'interfaccia grafica (vista) e il modello di gioco.
 * <p>
 * Concentra la logica di gestione degli eventi, del combattimento e della
 * persistenza, restituendo alla vista soltanto messaggi e dati in sola lettura.
 * Non dipende da alcuna libreria grafica, così da poter essere riutilizzato
 * da viste diverse (desktop, web, ...) e testato senza avviare la GUI.
 */
public class GameController {

    private final CombatEngine combatEngine = new CombatEngine();
    private final SaveManager saveManager;

    private GameEngine gameEngine;
    private Enemy currentEnemy;
    private final Set<Integer> executedFloors = new HashSet<>();

    /**
     * Crea un nuovo controller.
     *
     * @param gameEngine motore di gioco iniziale
     * @param saveManager gestore della persistenza
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
    // Query di stato (sola lettura per la vista)
    // ---------------------------------------------------------------------

    public Floor getCurrentFloor() {
        return gameEngine.getCurrentFloor();
    }

    public int getPlayerCurrentHealth() {
        return player().getStats().getCurrentHealth();
    }

    public int getPlayerMaxHealth() {
        return player().getStats().getMaxHealth();
    }

    public boolean isPlayerAlive() {
        return player().isAlive();
    }

    public boolean isCombatActive() {
        return currentEnemy != null;
    }

    /**
     * Restituisce il nemico attualmente in combattimento.
     *
     * @return il nemico corrente, oppure {@code null} se nessun combattimento è attivo
     */
    public Enemy getCurrentEnemy() {
        return currentEnemy;
    }

    public boolean isGameCompleted() {
        return gameEngine.isGameCompleted();
    }

    public boolean isOnLastFloor() {
        return gameEngine.isOnLastFloor();
    }

    public boolean isCurrentEventExecuted() {
        return executedFloors.contains(gameEngine.getCurrentFloorIndex());
    }

    // ---------------------------------------------------------------------
    // Comandi (modificano lo stato e restituiscono un messaggio per la vista)
    // ---------------------------------------------------------------------

    /**
     * Esegue l'evento del piano corrente che non richiede interazione
     * (combattimento o oggetto).
     *
     * @return messaggio descrittivo dell'esito
     */
    public String executeStandardEvent() {
        Floor floor = gameEngine.getCurrentFloor();
        EventResult result = gameEngine.executeCurrentFloorEvent();

        if (floor.getEvent() instanceof CombatEvent combatEvent) {
            currentEnemy = combatEvent.getEnemy();
        }

        markCurrentEventExecuted();
        return "Risultato: " + result.getMessage();
    }

    /**
     * Applica la scelta selezionata in un evento di dialogo.
     *
     * @param choice scelta effettuata dal giocatore
     * @return messaggio descrittivo dell'esito
     */
    public String resolveDialogueChoice(String choice) {
        if (choice == null) {
            throw new IllegalArgumentException("La scelta non può essere null.");
        }

        String message;
        if (choice.toLowerCase().contains("forza")) {
            player().takeDamage(10);
            message = "Hai reagito con forza, ma la rabbia ti consuma. Perdi 10 punti vita.";
        } else {
            player().heal(10);
            message = "Respiri, osservi e ritrovi controllo. Recuperi 10 punti vita.";
        }

        markCurrentEventExecuted();
        return message;
    }

    /**
     * Esegue un turno di combattimento con l'azione scelta.
     *
     * @param action azione del giocatore
     * @return messaggio descrittivo dell'esito del turno
     */
    public String executeCombatAction(CombatAction action) {
        if (currentEnemy == null) {
            return "Nessun combattimento attivo.";
        }

        CombatResult result = combatEngine.executeTurn(player(), currentEnemy, action);

        if (result == null) {
            return "Il combattimento continua.";
        }

        String message = switch (result) {
            case VICTORY -> "Hai sconfitto " + currentEnemy.getName() + "!";
            case DEFEAT -> "Sei stato sconfitto...";
            case ESCAPE -> "Sei riuscito a fuggire.";
        };

        currentEnemy = null;
        return message;
    }

    /**
     * Avanza al piano successivo, se possibile, interrompendo l'eventuale combattimento.
     */
    public void advanceFloor() {
        if (!gameEngine.isGameCompleted()) {
            gameEngine.advanceFloor();
        }

        currentEnemy = null;
    }

    /**
     * Salva lo stato corrente della partita.
     *
     * @return messaggio descrittivo dell'esito
     */
    public String saveGame() {
        try {
            GameSave save = new GameSave(
                    player().getName(),
                    gameEngine.getCurrentFloorIndex(),
                    player().getStats().getCurrentHealth(),
                    gameEngine.isGameCompleted()
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

            gameEngine = GameFactory.createNewGame();
            gameEngine.restoreState(save.getCurrentFloor(), save.isGameCompleted());
            player().getStats().setCurrentHealth(save.getCurrentHealth());

            currentEnemy = null;
            executedFloors.clear();

            return "Partita caricata correttamente.";
        } catch (IOException | ClassNotFoundException e) {
            return "Errore durante il caricamento: " + e.getMessage();
        }
    }

    private void markCurrentEventExecuted() {
        executedFloors.add(gameEngine.getCurrentFloorIndex());
    }

    private Player player() {
        return gameEngine.getPlayer();
    }
}
