package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.domain.Tower;
import it.unicam.cs.mpgc.rpg123465.floors.altezze.AltezzeFloors;
import it.unicam.cs.mpgc.rpg123465.floors.buio.BuioFloor;
import it.unicam.cs.mpgc.rpg123465.floors.encounter.EncounterFloors;

import java.util.List;

/**
 * Crea una nuova partita di Tower of Self.
 *
 * La Torre è composta dai piani delle paure: per aggiungerne uno basta creare
 * il suo contenuto nel package del piano (in {@code floors}) ed elencarlo qui,
 * senza toccare il resto del gioco.
 */
public final class GameFactory {

    private static final String DEFAULT_PLAYER_NAME = "Viaggiatore";

    private GameFactory() {
        // Impedisce l'istanziazione.
    }

    /**
     * Crea una nuova partita con il nome predefinito.
     *
     * @return motore di una nuova partita
     */
    public static GameEngine createNewGame() {
        return createNewGame(DEFAULT_PLAYER_NAME);
    }

    /**
     * Crea una nuova partita.
     *
     * @param playerName nome scelto dal giocatore; se vuoto viene usato quello
     *                   predefinito
     * @return motore di una nuova partita
     */
    public static GameEngine createNewGame(String playerName) {
        String name = (playerName == null || playerName.isBlank())
                ? DEFAULT_PLAYER_NAME
                : playerName.trim();

        Player player = new Player(name);

        return new GameEngine(
                player,
                createTower()
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
    private static Tower createTower() {
        Floor primoPiano = new Floor(
                1,
                "I Topi",
                EncounterFloors.topi()
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