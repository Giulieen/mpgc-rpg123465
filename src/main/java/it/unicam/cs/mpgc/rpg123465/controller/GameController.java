package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.challenge.Challenge;
import it.unicam.cs.mpgc.rpg123465.challenge.ChallengeManager;
import it.unicam.cs.mpgc.rpg123465.challenge.ChallengeQuestion;
import it.unicam.cs.mpgc.rpg123465.challenge.ConfrontationResolver;
import it.unicam.cs.mpgc.rpg123465.combat.CombatAction;
import it.unicam.cs.mpgc.rpg123465.combat.CombatEngine;
import it.unicam.cs.mpgc.rpg123465.combat.CombatResult;
import it.unicam.cs.mpgc.rpg123465.domain.Enemy;
import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.Item;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.engine.GameEngine;
import it.unicam.cs.mpgc.rpg123465.engine.GameFactory;
import it.unicam.cs.mpgc.rpg123465.engine.GameState;
import it.unicam.cs.mpgc.rpg123465.events.CombatEvent;
import it.unicam.cs.mpgc.rpg123465.events.DialogueChoice;
import it.unicam.cs.mpgc.rpg123465.events.EventResult;
import it.unicam.cs.mpgc.rpg123465.events.ItemEvent;
import it.unicam.cs.mpgc.rpg123465.persistence.GameSave;
import it.unicam.cs.mpgc.rpg123465.persistence.SaveManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
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
    private final ChallengeManager challengeManager = new ChallengeManager();
    private final ConfrontationResolver confrontationResolver = new ConfrontationResolver();
    private final SaveManager saveManager;

    private GameEngine gameEngine;
    private Enemy currentEnemy;
    private ChallengeQuestion currentQuestion;
    private GameState state = GameState.EXPLORING;
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

    /**
     * Restituisce il nome del giocatore.
     *
     * @return nome del giocatore
     */
    public String getPlayerName() {
        return player().getName();
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

    /**
     * Restituisce lo stato corrente del flusso di gioco.
     *
     * @return stato corrente
     */
    public GameState getState() {
        return state;
    }

    /**
     * Indica se esiste una partita salvata da poter caricare.
     *
     * @return {@code true} se un salvataggio è disponibile
     */
    public boolean hasSavedGame() {
        return saveManager.exists();
    }

    /**
     * Restituisce gli oggetti attualmente posseduti dal giocatore.
     *
     * @return lista non modificabile degli oggetti nell'inventario
     */
    public List<Item> getInventoryItems() {
        return player().getInventory().getItems();
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
            state = GameState.COMBAT;
        } else if (floor.getEvent() instanceof ItemEvent) {
            state = GameState.ITEM;
        } else {
            state = GameState.EXPLORING;
        }

        markCurrentEventExecuted();
        refreshTerminalState();
        return "Risultato: " + result.getMessage();
    }

    /**
     * Segnala l'inizio di un'interazione di dialogo.
     */
    public void beginDialogue() {
        state = GameState.DIALOGUE;
    }

    /**
     * Annulla l'interazione di dialogo in corso, tornando all'esplorazione.
     */
    public void abortDialogue() {
        state = GameState.EXPLORING;
    }

    /**
     * Applica la scelta selezionata in un evento di dialogo.
     *
     * @param choice scelta effettuata dal giocatore
     * @return messaggio descrittivo dell'esito
     */
    public String resolveDialogueChoice(DialogueChoice choice) {
        if (choice == null) {
            throw new IllegalArgumentException("La scelta non può essere null.");
        }

        String message = choice.applyTo(player());
        markCurrentEventExecuted();
        state = GameState.EXPLORING;
        refreshTerminalState();
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

        if (action == CombatAction.USE_ITEM) {
            return useHealingItem();
        }

        return resolveCombatOutcome(combatEngine.executeTurn(player(), currentEnemy, action));
    }

    /**
     * Indica se sul piano corrente, durante il combattimento, è disponibile
     * un confronto interiore con il nemico.
     *
     * @return {@code true} se il confronto è possibile
     */
    public boolean isConfrontationAvailable() {
        return currentEnemy != null
                && challengeManager.getChallengeForFloor(currentFloorNumber()).isPresent();
    }

    /**
     * Avvia un confronto proponendo una domanda del piano corrente.
     *
     * @return domanda da mostrare al giocatore
     * @throws IllegalStateException se non è disponibile alcun confronto
     */
    public ChallengeQuestion startConfrontation() {
        Challenge challenge = challengeManager.getChallengeForFloor(currentFloorNumber())
                .orElseThrow(() -> new IllegalStateException("Nessun confronto disponibile."));

        currentQuestion = challenge.nextQuestion();
        return currentQuestion;
    }

    /**
     * Risolve il confronto in corso applicando l'effetto della risposta scelta.
     *
     * @param answerIndex indice della risposta scelta
     * @return messaggio descrittivo dell'esito
     * @throws IllegalStateException se non c'è un confronto in corso
     */
    public String resolveConfrontation(int answerIndex) {
        if (currentQuestion == null) {
            throw new IllegalStateException("Nessun confronto in corso.");
        }
        if (currentEnemy == null) {
            return "Nessun combattimento attivo.";
        }

        String message = confrontationResolver.resolve(currentQuestion, answerIndex, currentEnemy);
        currentQuestion = null;
        return message;
    }

    private String useHealingItem() {
        if (!player().hasHealingItem()) {
            return "Non hai oggetti curativi da usare.";
        }

        String enemyName = currentEnemy.getName();

        int healthBeforeUse = player().getStats().getCurrentHealth();
        Item used = player().useHealingItem();
        int recovered = player().getStats().getCurrentHealth() - healthBeforeUse;

        int healthBeforeReaction = player().getStats().getCurrentHealth();
        CombatResult reaction = combatEngine.executeTurn(player(), currentEnemy, CombatAction.USE_ITEM);
        int damageTaken = healthBeforeReaction - player().getStats().getCurrentHealth();

        String message = "Usi " + used.getName() + ": recuperi " + recovered
                + " di lucidità. " + enemyName + " contrattacca: perdi "
                + damageTaken + " di lucidità.";

        if (reaction == CombatResult.DEFEAT) {
            currentEnemy = null;
            refreshTerminalState();
            return message + " Sei stato sconfitto...";
        }

        return message;
    }

    private String resolveCombatOutcome(CombatResult result) {
        if (result == null) {
            return "Il combattimento continua.";
        }

        currentEnemy = null;

        return switch (result) {
            case VICTORY -> {
                // La vittoria completa la partita solo se era l'ultima prova.
                state = gameEngine.isGameCompleted() ? GameState.VICTORY : GameState.EXPLORING;
                yield "La Paura si dissolve.";
            }
            case DEFEAT -> {
                state = GameState.GAME_OVER;
                yield "Sei stato sconfitto...";
            }
            case ESCAPE -> {
                state = GameState.EXPLORING;
                yield "Sei riuscito a fuggire.";
            }
        };
    }

    /**
     * Avanza al piano successivo, se possibile, interrompendo l'eventuale combattimento.
     */
    public void advanceFloor() {
        if (!gameEngine.isGameCompleted()) {
            gameEngine.advanceFloor();
        }

        currentEnemy = null;
        state = GameState.EXPLORING;
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

            gameEngine = GameFactory.createNewGame(save.getPlayerName());
            gameEngine.restoreState(save.getCurrentFloor(), save.isGameCompleted());
            player().getStats().setCurrentHealth(save.getCurrentHealth());

            currentEnemy = null;
            executedFloors.clear();
            state = GameState.EXPLORING;
            refreshTerminalState();

            return "Partita caricata correttamente.";
        } catch (IOException | ClassNotFoundException e) {
            return "Errore durante il caricamento: " + e.getMessage();
        }
    }

    /**
     * Forza gli stati terminali (sconfitta o vittoria) quando le relative
     * condizioni sono soddisfatte, così da centralizzarne la logica.
     */
    private void refreshTerminalState() {
        if (!player().isAlive()) {
            state = GameState.GAME_OVER;
        } else if (gameEngine.isGameCompleted() && currentEnemy == null) {
            state = GameState.VICTORY;
        }
    }

    private void markCurrentEventExecuted() {
        executedFloors.add(gameEngine.getCurrentFloorIndex());
    }

    private int currentFloorNumber() {
        return gameEngine.getCurrentFloor().getNumber();
    }

    private Player player() {
        return gameEngine.getPlayer();
    }
}
