package it.unicam.cs.mpgc.rpg123465.app;

import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.domain.FloorContent;
import it.unicam.cs.mpgc.rpg123465.floors.altezze.AltitudeCrossing;
import it.unicam.cs.mpgc.rpg123465.floors.altezze.AltezzeScene;
import it.unicam.cs.mpgc.rpg123465.floors.buio.DarkRoom;
import it.unicam.cs.mpgc.rpg123465.floors.buio.DarkRoomScene;
import it.unicam.cs.mpgc.rpg123465.floors.encounter.FearEncounter;
import it.unicam.cs.mpgc.rpg123465.floors.encounter.FearEncounterScreen;
import it.unicam.cs.mpgc.rpg123465.ui.FloorScene;

import java.util.List;

/**
 * Sceglie le scene con cui mostrare un piano a partire dal suo contenuto.
 */
public final class FloorSceneFactory {

    private final GameController controller;
    private final Runnable onSave;
    private final Runnable onExit;

    public FloorSceneFactory(
            GameController controller,
            Runnable onSave,
            Runnable onExit
    ) {
        if (controller == null) {
            throw new IllegalArgumentException(
                    "Il controller non può essere null."
            );
        }

        this.controller = controller;
        this.onSave = onSave;
        this.onExit = onExit;
    }

    public List<FloorScene> scenesFor(FloorContent content) {
        if (content instanceof FearEncounter encounter) {
            return List.of(
                    new FearEncounterScreen(
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
