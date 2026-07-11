package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.Inventory;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.domain.Stats;
import it.unicam.cs.mpgc.rpg123465.domain.Tower;
import it.unicam.cs.mpgc.rpg123465.fear.FearEncounters;

import java.util.List;

/**
 * Crea una nuova partita di Tower of Self.
 * <p>
 * La Torre è composta dai piani delle paure: per aggiungerne uno basta scrivere
 * il suo incontro in {@link FearEncounters} ed elencarlo qui, senza toccare il
 * resto del gioco.
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

        Player player = new Player(
                name,
                new Stats(100, 14, 5),
                new Inventory()
        );

        return new GameEngine(player, createTower());
    }

    /**
     * Costruisce la Torre.
     * <p>
     * Al momento contiene il solo Piano I. I piani successivi (il buio, le
     * altezze, la solitudine, il fallimento e il confronto con l'alter ego)
     * andranno aggiunti qui, in ordine di salita.
     *
     * @return la Torre da salire
     */
    private static Tower createTower() {
        Floor primoPiano = new Floor(1, "I Topi", FearEncounters.topi());

        return new Tower(List.of(primoPiano));
    }
}
