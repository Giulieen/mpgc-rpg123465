package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.model.FloorAttempts;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.Dilemma;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.DilemmaSequence;
import it.unicam.cs.mpgc.rpg123465.model.floors.heights.HeightsConfig;
import it.unicam.cs.mpgc.rpg123465.model.floors.heights.AltitudeCrossing;
import it.unicam.cs.mpgc.rpg123465.testing.FakeQuestionRepository;
import it.unicam.cs.mpgc.rpg123465.testing.FakeSaveManager;
import it.unicam.cs.mpgc.rpg123465.testing.TestTowers;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prove sul consumo dei dilemmi del Piano III senza JavaFX: riproducono ciò
 * che fa la callback del cambio ponte.
 */
class HeightsDilemmaFlowTest {

    private GameController game;
    private HeightsController controller;
    private DilemmaSequence dilemmas;

    private static AltitudeCrossing crossing() {
        return new AltitudeCrossing(
                "Piano III — Le Altezze",
                "/images/scenes/floor3-heights.png",
                "intro",
                "vittoria",
                "caduta",
                HeightsConfig.standard());
    }

    @BeforeEach
    void setUp() {
        game = new GameController(TestTowers.engineWithFloors(3),
                new FakeSaveManager(), new FakeQuestionRepository());

        controller = new HeightsController(crossing(), game);

        dilemmas = new DilemmaSequence(new FakeQuestionRepository().randomQuestions("altezze", 4));
    }

    /** Come la scena: risolve, e registra solo se il consumo è avvenuto. */
    private void answer(Dilemma dilemma) {
        if (dilemmas.resolve(dilemma)) {
            controller.registerChoice(dilemma.second().trait());
        }
    }

    @Test
    void ilPianoPoneQuattroDilemmiNellOrdineDelCatalogo() {
        List<Integer> posti = new ArrayList<>();

        while (dilemmas.hasNext()) {
            Dilemma current = dilemmas.current();
            posti.add(current.id());
            answer(current);
        }

        assertEquals(List.of(1, 2, 3, 4), posti);
        assertEquals(4, game.getMind().getTotalProfileChoices());
    }

    /**
     * Il piano ha quattro soglie: una volta esaurite le domande, le soglie
     * successive devono passare oltre senza riproporne una già risposta.
     */
    @Test
    void unaSogliaOltreLUltimaDomandaNonRiusaUnDilemma() {
        while (dilemmas.hasNext()) {
            answer(dilemmas.current());
        }

        assertFalse(dilemmas.hasNext());
        assertEquals(4, game.getMind().getTotalProfileChoices());
    }

    @Test
    void rispondereDueVolteAlloStessoDilemmaRegistraUnaSolaScelta() {
        Dilemma first = dilemmas.current();

        answer(first);
        answer(first);

        assertEquals(1, game.getMind().getTotalProfileChoices());
        assertEquals(1, dilemmas.resolvedCount());
    }

    /**
     * Una caduta azzera il percorso ma non le domande: dopo il retry la
     * sequenza riprende da dove era, senza riconteggiare i tratti.
     */
    @Test
    void unaCadutaNonRiproponeUnDilemmaGiaRisolto() {
        Dilemma first = dilemmas.current();
        answer(first);

        controller.registerFall();

        assertEquals(FloorAttempts.MAX - 1, controller.remainingAttempts());
        assertEquals(1, dilemmas.resolvedCount());
        assertFalse(dilemmas.current().equals(first));
        assertEquals(1, game.getMind().getTotalProfileChoices());
    }

    @Test
    void ricominciareLaProvaNonRiproponeIDilemmiGiaRisolti() {
        answer(dilemmas.current());
        answer(dilemmas.current());
        answer(dilemmas.current());

        for (int i = 0; i < FloorAttempts.MAX; i++) {
            controller.registerFall();
        }

        controller.restartTrial();

        assertEquals(FloorAttempts.MAX, controller.remainingAttempts());
        assertEquals(3, dilemmas.resolvedCount());
        assertTrue(dilemmas.hasNext());
        assertEquals(3, game.getMind().getTotalProfileChoices());
    }
}
