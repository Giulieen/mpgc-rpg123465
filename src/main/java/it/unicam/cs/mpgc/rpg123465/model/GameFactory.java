package it.unicam.cs.mpgc.rpg123465.model;

import it.unicam.cs.mpgc.rpg123465.model.floors.heights.HeightsFloors;
import it.unicam.cs.mpgc.rpg123465.model.floors.darkness.DarknessFloors;
import it.unicam.cs.mpgc.rpg123465.model.floors.rats.RatFloors;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.QuestionRepository;

import java.util.List;

/**
 * Crea una nuova partita di Tower of Self.
 *
 * Per aggiungere un piano basta creare il suo contenuto nel package
 * {@code floors} ed elencarlo qui, senza toccare il resto del gioco.
 */
public final class GameFactory {

    private static final String DEFAULT_PLAYER_NAME = "Viaggiatore";

    private GameFactory() {
        // Impedisce l'istanziazione.
    }

    /**
     * Crea una nuova partita.
     *
     * @param playerName nome scelto dal giocatore; se vuoto viene usato quello
     *                   predefinito
     * @param questions catalogo da cui i piani estraggono le proprie domande
     * @return motore di una nuova partita
     * @throws IllegalArgumentException se il catalogo è null
     */
    public static GameEngine createNewGame(String playerName, QuestionRepository questions) {
        if (questions == null) {
            throw new IllegalArgumentException("Il catalogo delle domande non può essere null.");
        }

        String name = (playerName == null || playerName.isBlank())
                ? DEFAULT_PLAYER_NAME
                : playerName.trim();

        Player player = new Player(name);

        return new GameEngine(player, createTower(questions));
    }

    /**
     * Costruisce la Torre.
     *
     * Al momento contiene tre piani:
     * I Topi, Il Buio e Le Altezze.
     *
     * @return la Torre da salire
     */
    private static Tower createTower(QuestionRepository questions) {
        Floor firstFloor = new Floor(1, "I Topi", RatFloors.fearEncounter(questions));

        Floor secondFloor = new Floor(2, "Il Buio", DarknessFloors.darkRoom());

        Floor thirdFloor = new Floor(3, "Le Altezze", HeightsFloors.altitudeCrossing());

        return new Tower(List.of(firstFloor, secondFloor, thirdFloor));
    }
}
