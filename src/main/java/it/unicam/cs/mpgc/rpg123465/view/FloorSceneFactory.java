package it.unicam.cs.mpgc.rpg123465.view;

import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.model.FloorContent;
import it.unicam.cs.mpgc.rpg123465.model.floors.altezze.AltitudeCrossing;
import it.unicam.cs.mpgc.rpg123465.model.floors.buio.DarkRoom;
import it.unicam.cs.mpgc.rpg123465.model.floors.topi.FearEncounter;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.QuestionRepository;
import it.unicam.cs.mpgc.rpg123465.persistence.record.RecordStore;
import it.unicam.cs.mpgc.rpg123465.view.FloorScene;
import it.unicam.cs.mpgc.rpg123465.view.floors.altezze.AltezzeScene;
import it.unicam.cs.mpgc.rpg123465.view.floors.buio.DarkRoomScene;
import it.unicam.cs.mpgc.rpg123465.view.floors.topi.FearEncounterScreen;
import it.unicam.cs.mpgc.rpg123465.view.floors.topi.RatMazeScene;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Sceglie le scene con cui mostrare un piano a partire dal suo contenuto.
 *
 * <p>
 * L'abbinamento contenuto/scene è un elenco, non una catena di controlli:
 * aggiungere un piano significa scrivere una riga in {@link #registerFloors()},
 * mentre {@link #scenesFor(FloorContent)} resta com'è. I contenuti sono record,
 * quindi final: cercarli per classe esatta copre tutti i casi possibili.
 */
public final class FloorSceneFactory {

    private final GameController controller;
    private final QuestionRepository questions;
    private final RecordStore records;
    private final Consumer<Runnable> onSave;
    private final Runnable onExit;

    /**
     * Le scene di ogni tipo di contenuto, nell'ordine in cui sono registrate.
     */
    private final Map<Class<? extends FloorContent>, Function<FloorContent, List<FloorScene>>>
            builders = new LinkedHashMap<>();

    public FloorSceneFactory(
            GameController controller,
            QuestionRepository questions,
            RecordStore records,
            Consumer<Runnable> onSave,
            Runnable onExit
    ) {
        if (controller == null || questions == null || records == null) {
            throw new IllegalArgumentException(
                    "Controller, catalogo e archivio dei record non possono essere null."
            );
        }

        this.controller = controller;
        this.questions = questions;
        this.records = records;
        this.onSave = onSave;
        this.onExit = onExit;

        registerFloors();
    }

    /**
     * Dichiara con quali scene si mostra ciascun contenuto.
     *
     * È l'unico punto da toccare per aggiungere un piano.
     */
    private void registerFloors() {
        /*
         * Il Piano I e' fatto di due momenti: prima il dilemma, che vale una
         * sola risposta, poi la prova, che si puo' ripetere. Tenerli come due
         * scene lascia a SceneFlow il compito di incatenarli.
         */
        register(FearEncounter.class, encounter -> List.of(
                new FearEncounterScreen(encounter, controller, onSave, onExit),
                new RatMazeScene(encounter, controller, records, onSave, onExit)
        ));

        register(DarkRoom.class, room -> List.of(
                new DarkRoomScene(room, controller, questions, records, onSave, onExit)
        ));

        register(AltitudeCrossing.class, crossing -> List.of(
                new AltezzeScene(crossing, controller, questions, records, onSave, onExit)
        ));
    }

    /**
     * Associa a un tipo di contenuto le scene che lo mostrano.
     *
     * <p>
     * Il tipo compare sia come chiave sia nella firma della funzione: il cast
     * resta confinato qui ed è garantito dalla chiave con cui si è registrato.
     *
     * @param type tipo di contenuto
     * @param builder costruisce le scene per quel contenuto
     * @param <T> tipo di contenuto trattato dal costruttore
     */
    private <T extends FloorContent> void register(
            Class<T> type,
            Function<T, List<FloorScene>> builder
    ) {
        builders.put(type, content -> builder.apply(type.cast(content)));
    }

    /**
     * Costruisce le scene di un piano.
     *
     * @param content contenuto del piano
     * @return le scene da mostrare, nell'ordine
     * @throws IllegalArgumentException se il contenuto è null
     * @throws IllegalStateException se nessuna scena sa mostrare quel contenuto
     */
    public List<FloorScene> scenesFor(FloorContent content) {
        if (content == null) {
            throw new IllegalArgumentException("Il contenuto del piano non può essere null.");
        }

        Function<FloorContent, List<FloorScene>> builder = builders.get(content.getClass());

        if (builder == null) {
            throw new IllegalStateException(
                    "Nessuna scena sa mostrare questo contenuto: " + content.getClass()
            );
        }

        return builder.apply(content);
    }
}
