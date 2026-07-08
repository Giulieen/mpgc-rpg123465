package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.Enemy;
import it.unicam.cs.mpgc.rpg123465.domain.Floor;
import it.unicam.cs.mpgc.rpg123465.domain.Inventory;
import it.unicam.cs.mpgc.rpg123465.domain.Item;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.domain.Stats;
import it.unicam.cs.mpgc.rpg123465.domain.Tower;
import it.unicam.cs.mpgc.rpg123465.events.CombatEvent;
import it.unicam.cs.mpgc.rpg123465.events.DialogueEvent;
import it.unicam.cs.mpgc.rpg123465.events.ItemEvent;
import it.unicam.cs.mpgc.rpg123465.domain.ItemType;
import java.util.List;

/**
 * Crea una nuova partita standard di Tower of Self.
 */
public final class GameFactory {

    private GameFactory() {
        // Impedisce l'istanziazione.
    }

    /**
     * Crea una nuova partita completa.
     *
     * @return motore di gioco inizializzato
     */
    public static GameEngine createNewGame() {
        Player player = new Player(
                "Viaggiatore",
                new Stats(100, 14, 5),
                new Inventory()
        );

        Tower tower = createTower();

        return new GameEngine(player, tower);
    }

    private static Tower createTower() {
        List<Floor> floors = List.of(
                new Floor(
                        1,
                        "Paura del Fallimento",
                        "Un piano avvolto da voci che mettono in dubbio ogni scelta.",
                        new CombatEvent(
                                "Scontro con la Paura",
                                "Una figura incerta emerge dall'ombra e ti accusa di non essere abbastanza.",
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
                        "Le pareti tremano al ritmo di emozioni represse.",
                        new DialogueEvent(
                                "La voce della Rabbia",
                                "Una voce ti chiede se vuoi reagire con forza o cercare controllo.",
                                List.of("Reagisci con forza", "Respira e osserva")
                        )
                ),
                new Floor(
                        3,
                        "Ansia",
                        "Il tempo sembra accelerare e ogni passo pesa piu del precedente.",
                        new ItemEvent(
                                "Respiro Profondo",
                                "Trovi un piccolo simbolo di calma interiore.",
                                new Item(
                                    "Respiro Profondo",
                                    "Un ricordo di lucidità che aiuta a non cedere al panico.",
                                    ItemType.HEALING,
                                    25
                            )
                        )
                ),
                new Floor(
                        4,
                        "Solitudine",
                        "Un corridoio silenzioso riflette la sensazione di essere invisibile.",
                        new CombatEvent(
                                "Scontro con la Solitudine",
                                "Una presenza vuota tenta di convincerti che nessuno ti ascoltera.",
                                new Enemy(
                                        "Solitudine",
                                        new Stats(45, 10, 3),
                                        "La sensazione di isolamento resa forma."
                                )
                        )
                ),
                new Floor(
                        5,
                        "Speranza",
                        "Una luce fragile attraversa le crepe della torre.",
                        new ItemEvent(
                                "Scintilla di Speranza",
                                "Raccogli una piccola luce calda.",
                               new Item(
                                    "Scintilla di Speranza",
                                    "Una risorsa interiore che ricorda al giocatore perché continuare.",
                                    ItemType.HEALING,
                                    40
                            )
                        )
                ),
                new Floor(
                        6,
                        "Alter Ego",
                        "La cima della torre riflette tutto cio che hai evitato.",
                        new CombatEvent(
                                "Confronto finale",
                                "Il tuo Alter Ego ti attende in silenzio.",
                                new Enemy(
                                        "Alter Ego",
                                        new Stats(70, 13, 5),
                                        "La somma dei dubbi, delle paure e delle parti non accettate."
                                )
                        )
                )
        );

        return new Tower(floors);
    }
}