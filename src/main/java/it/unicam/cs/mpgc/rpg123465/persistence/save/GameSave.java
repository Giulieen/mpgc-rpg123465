package it.unicam.cs.mpgc.rpg123465.persistence.save;

import it.unicam.cs.mpgc.rpg123465.model.MindState;
import java.util.Objects;

import java.io.Serializable;

/**
 * Rappresenta i dati di una partita salvata.
 *
 * <p>
 * Un salvataggio fotografa l'<em>ingresso</em> di un piano, non un momento
 * qualsiasi: contiene quindi il piano raggiunto e le risposte date fino a lì.
 * I tentativi non ci sono perché all'ingresso di un piano sono per definizione
 * al massimo, e ricalcolarli è più sicuro che conservarli.
 */
public class GameSave implements Serializable {

    /*
     * Il formato è cambiato con la rimozione di Vita, Lucidità e Stress:
     * i salvataggi precedenti non sono compatibili.
     */
    private static final long serialVersionUID = 3L;

    private final String playerName;
    private final int currentFloor;
    private final boolean gameCompleted;

    /**
     * Le risposte ai dilemmi date fino all'ingresso di questo piano. Senza,
     * ricaricando si perderebbe il percorso da cui nasce il profilo finale.
     */
    private final MindState mindState;

    /**
     * Crea un nuovo salvataggio.
     *
     * @param playerName nome del giocatore
     * @param currentFloor piano corrente
     * @param gameCompleted indica se la partita è stata completata
     * @param mindState risposte registrate fino all'ingresso del piano
     */
    public GameSave(String playerName,
                    int currentFloor,
                    boolean gameCompleted,
                    MindState mindState) {

        this.playerName = playerName;
        this.currentFloor = currentFloor;
        this.gameCompleted = gameCompleted;
        this.mindState = mindState;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public boolean isGameCompleted() {
        return gameCompleted;
    }

    /**
     * @return le risposte registrate al momento del salvataggio
     */
    public MindState getMindState() {
        return mindState;
    }

    /**
     * Due salvataggi sono uguali quando descrivono la stessa partita nello
     * stesso punto: nome, piano, completamento e risposte registrate.
     *
     * @param obj oggetto da confrontare
     * @return true se i quattro campi coincidono
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        GameSave other = (GameSave) obj;

        return currentFloor == other.currentFloor
                && gameCompleted == other.gameCompleted
                && Objects.equals(playerName, other.playerName)
                && Objects.equals(mindState, other.mindState);
    }

    /**
     * Calcolato sugli stessi campi di {@link #equals(Object)}, come richiede
     * il contratto: due salvataggi uguali producono lo stesso valore.
     *
     * @return codice hash dei quattro campi
     */
    @Override
    public int hashCode() {
        return Objects.hash(playerName, currentFloor, gameCompleted, mindState);
    }

    /**
     * @return una descrizione sintetica del salvataggio, per la diagnostica
     */
    @Override
    public String toString() {
        return "GameSave[" + playerName
                + ", piano=" + currentFloor
                + ", completata=" + gameCompleted + "]";
    }
}
