package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.domain.Tower;
import it.unicam.cs.mpgc.rpg123465.floors.altezze.AltezzeFloors;
import it.unicam.cs.mpgc.rpg123465.floors.buio.BuioFloor;
import it.unicam.cs.mpgc.rpg123465.floors.encounter.EncounterFloors;
import it.unicam.cs.mpgc.rpg123465.questions.QuestionRepository;

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
    public static GameEngine createNewGame(
            String playerName,
            QuestionRepository questions
    ) {
        if (questions == null) {
            throw new IllegalArgumentException(
                    "Il catalogo delle domande non può essere null."
            );
        }

        String name = (playerName == null || playerName.isBlank())
                ? DEFAULT_PLAYER_NAME
                : playerName.trim();

        Player player = new Player(name);

        return new GameEngine(
                player,
                createTower(questions)
        );
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
        Floor primoPiano = new Floor(
                1,
                "I Topi",
                EncounterFloors.topi(questions)
        );

        Floor secondoPiano = new Floor(
                2,
                "Il Buio",
                BuioFloor.buio()
        );

        Floor terzoPiano = new Floor(
                3,
                "Le Altezze",
                AltezzeFloors.altezze()
        );

        return new Tower(
                List.of(
                        primoPiano,
                        secondoPiano,
                        terzoPiano
                )
        );
    }
}