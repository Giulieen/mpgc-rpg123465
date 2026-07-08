package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.Enemy;
import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.Inventory;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.domain.Stats;
import it.unicam.cs.mpgc.rpg123465.domain.Tower;
import it.unicam.cs.mpgc.rpg123465.events.CombatEvent;

import java.util.List;

/**
 * Crea una nuova partita standard di Tower of Self.
 * <p>
 * Al momento la torre è composta dal solo Piano 1 (Paura del Fallimento):
 * i piani successivi sono stati temporaneamente rimossi per rifinire il
 * primo. Per reintrodurli è sufficiente aggiungere altri {@link Floor} in
 * {@link #createTower()}.
 */
public final class GameFactory {

    private static final String DEFAULT_PLAYER_NAME = "Viaggiatore";

    private GameFactory() {
        // Impedisce l'istanziazione.
    }

    public static GameEngine createNewGame() {
        return createNewGame(DEFAULT_PLAYER_NAME);
    }

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

    private static Tower createTower() {
        Floor firstFloor = new Floor(
                1,
                "Paura del Fallimento",
                """
                I gradini scricchiolano sotto i tuoi piedi.
                Una voce sussurra: "Sei davvero sicuro di essere abbastanza?"

                È la prima, e per ora unica, prova della Torre.
                """,
                new CombatEvent(
                        "Scontro con la Paura",
                        """
                        Dall'ombra prende forma una creatura con il tuo volto.
                        Ogni sua parola è un dubbio che hai già sentito dentro di te.
                        Puoi affrontarla combattendo oppure confrontarti con le sue parole.
                        """,
                        new Enemy(
                                "Paura del Fallimento",
                                new Stats(30, 8, 3),
                                "La manifestazione del timore di sbagliare."
                        )
                )
        );

        return new Tower(List.of(firstFloor));
    }
}