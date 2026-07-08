package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.Enemy;
import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.Inventory;
import it.unicam.cs.mpgc.rpg123465.domain.Item;
import it.unicam.cs.mpgc.rpg123465.domain.ItemType;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.domain.Stats;
import it.unicam.cs.mpgc.rpg123465.domain.Tower;
import it.unicam.cs.mpgc.rpg123465.events.CombatEvent;
import it.unicam.cs.mpgc.rpg123465.events.DialogueChoice;
import it.unicam.cs.mpgc.rpg123465.events.DialogueEvent;
import it.unicam.cs.mpgc.rpg123465.events.ItemEvent;

import java.util.List;

/**
 * Crea una nuova partita standard di Tower of Self.
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
        List<Floor> floors = List.of(
                new Floor(
                        1,
                        "Paura del Fallimento",
                        """
                        I gradini scricchiolano sotto i tuoi piedi.
                        Una voce sussurra: "Sei davvero sicuro di essere abbastanza?"

                        È la prima prova della Torre.
                        """,
                        new CombatEvent(
                                "Scontro con la Paura",
                                """
                                Dall'ombra prende forma una creatura con il tuo volto.
                                Ogni sua parola è un dubbio che hai già sentito dentro di te.
                                """,
                                new Enemy(
                                        "Paura del Fallimento",
                                        new Stats(35, 8, 2),
                                        "La manifestazione del timore di sbagliare."
                                )
                        )
                ),

                new Floor(
                        2,
                        "Rabbia",
                        """
                        L'aria diventa pesante.
                        Le pareti sembrano pulsare seguendo il ritmo del tuo cuore.

                        Qualcosa dentro di te vuole esplodere.
                        """,
                        new DialogueEvent(
                                "La Voce della Rabbia",
                                """
                                "Perché continuare a sopportare tutto questo?"

                                La voce aspetta una tua risposta.
                                """,
                                List.of(
                                        new DialogueChoice(
                                                "Lascia esplodere la rabbia",
                                                "Perdi il controllo. Ti senti più debole.",
                                                -10
                                        ),
                                        new DialogueChoice(
                                                "Respira e mantieni il controllo",
                                                "La rabbia si calma. Ritrovi lucidità e trovi un Momento di Calma.",
                                                5,
                                                new Item(
                                                        "Momento di Calma",
                                                        "Una pausa che ti ricorda come respirare.",
                                                        ItemType.HEALING,
                                                        15
                                                )
                                        )
                                )
                        )
                ),

                new Floor(
                        3,
                        "Ansia",
                        """
                        Le scale sembrano non finire mai.
                        Ogni passo accelera il respiro.

                        Ma qualcosa luccica sul pavimento.
                        """,
                        new ItemEvent(
                                "Respiro Profondo",
                                """
                                Trovi un piccolo simbolo inciso nella pietra.
                                Tenerlo tra le mani rallenta il battito del cuore.
                                """,
                                new Item(
                                        "Respiro Profondo",
                                        "Un ricordo di lucidità che calma il panico.",
                                        ItemType.HEALING,
                                        25
                                )
                        )
                ),

                new Floor(
                        4,
                        "Solitudine",
                        """
                        Il silenzio è assoluto.
                        Persino i tuoi passi sembrano svanire.

                        Per un attimo temi di essere completamente solo.
                        """,
                        new CombatEvent(
                                "Scontro con la Solitudine",
                                """
                                Una figura senza volto emerge lentamente.
                                Non parla. Ti osserva.
                                """,
                                new Enemy(
                                        "Solitudine",
                                        new Stats(45, 10, 3),
                                        "L'incarnazione dell'isolamento."
                                )
                        )
                ),

                new Floor(
                        5,
                        "Speranza",
                        """
                        Dopo tanta oscurità compare una piccola luce.

                        Non illumina la Torre.
                        Illumina te.
                        """,
                        new ItemEvent(
                                "Scintilla di Speranza",
                                """
                                Una fiamma azzurra resta sospesa davanti a te.
                                La raccogli senza paura.
                                """,
                                new Item(
                                        "Scintilla di Speranza",
                                        "Ricorda che anche il buio più profondo può finire.",
                                        ItemType.HEALING,
                                        40
                                )
                        )
                ),

                new Floor(
                        6,
                        "Alter Ego",
                        """
                        La cima della Torre è immobile.

                        Davanti a te c'è qualcuno.

                        Ha il tuo volto.
                        """,
                        new CombatEvent(
                                "Il confronto finale",
                                """
                                "Io sono tutto ciò che hai sempre cercato di nascondere."

                                Non puoi più fuggire.
                                """,
                                new Enemy(
                                        "Alter Ego",
                                        new Stats(70, 13, 5),
                                        "La somma di tutte le tue paure e delle parti che non hai mai accettato."
                                )
                        )
                )
        );

        return new Tower(floors);
    }
}