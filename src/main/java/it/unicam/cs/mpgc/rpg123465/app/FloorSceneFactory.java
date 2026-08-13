package it.unicam.cs.mpgc.rpg123465.app;

import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.domain.FloorContent;
import it.unicam.cs.mpgc.rpg123465.floors.altezze.AltitudeCrossing;
import it.unicam.cs.mpgc.rpg123465.floors.altezze.AltezzeScene;
import it.unicam.cs.mpgc.rpg123465.floors.buio.DarkRoom;
import it.unicam.cs.mpgc.rpg123465.floors.buio.DarkRoomScene;
import it.unicam.cs.mpgc.rpg123465.floors.encounter.FearEncounter;
import it.unicam.cs.mpgc.rpg123465.floors.encounter.FearEncounterScreen;
import it.unicam.cs.mpgc.rpg123465.floors.encounter.RatMazeScene;
import it.unicam.cs.mpgc.rpg123465.persistence.RecordStore;
import it.unicam.cs.mpgc.rpg123465.ui.FloorScene;

import java.util.List;
import java.util.function.Consumer;

/**
 * Sceglie le scene con cui mostrare un piano a partire dal suo contenuto.
 */
public final class FloorSceneFactory {

    private final GameController controller;
    private final RecordStore records;
    private final Consumer<Runnable> onSave;
    private final Runnable onExit;

    public FloorSceneFactory(
            GameController controller,
            RecordStore records,
            Consumer<Runnable> onSave,
            Runnable onExit
    ) {
        if (controller == null || records == null) {
            throw new IllegalArgumentException(
                    "Il controller e l'archivio dei record non possono essere null."
            );
        }

        this.controller = controller;
        this.records = records;
        this.onSave = onSave;
        this.onExit = onExit;
    }

    public List<FloorScene> scenesFor(FloorContent content) {
        if (content instanceof FearEncounter encounter) {
            /*
             * Il Piano I e' fatto di due momenti: prima il dilemma, che vale
             * una sola risposta, poi la prova, che si puo' ripetere. Tenerli
             * come due scene lascia a SceneFlow il compito di incatenarli.
             */
            return List.of(
                    new FearEncounterScreen(
                            encounter,
                            controller,
                            onSave,
                            onExit
                    ),
                    new RatMazeScene(
                            encounter,
                            controller,
                            onSave,
                            onExit
                    )
            );
        }

        if (content instanceof DarkRoom room) {
            return List.of(
                    new DarkRoomScene(
                            room,
                            controller,
                            records,
                            onSave,
                            onExit
                    )
            );
        }

        if (content instanceof AltitudeCrossing crossing) {
            return List.of(
                    new AltezzeScene(
                            crossing,
                            controller,
                            records,
                            onSave,
                            onExit
                    )
            );
        }

        throw new IllegalStateException(
                "Nessuna scena sa mostrare questo contenuto: "
                        + content.getClass()
        );
    }
}
