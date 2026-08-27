package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.model.FloorAttempts;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.Dilemma;
import it.unicam.cs.mpgc.rpg123465.model.dilemma.DilemmaSequence;
import it.unicam.cs.mpgc.rpg123465.model.floors.darkness.DarkRoom;
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
 * Prove sul consumo dei dilemmi del Piano II senza JavaFX: riproducono ciò che
 * fa la callback della scena, cioè risolvere il dilemma corrente e registrare
 * il tratto solo se la sequenza lo ha davvero consumato.
 */
class DarkRoomDilemmaFlowTest {

    private GameController game;
    private DarkRoomController controller;
    private DilemmaSequence dilemmas;

    private static DarkRoom room() {
        return new DarkRoom(
                "Piano II — Il Buio",
                "/images/scenes/floor2-darkness.jpg",
                "intro",
                "outro",
                "errore",
                "tempo scaduto",
                "3524",
                60);
    }

    @BeforeEach
    void setUp() {
        game = new GameController(TestTowers.engineWithFloors(3),
                new FakeSaveManager(), new FakeQuestionRepository());

        controller = new DarkRoomController(room(), game);

        dilemmas = new DilemmaSequence(new FakeQuestionRepository().randomQuestions("buio", 3));
    }

    /** Come la scena: risolve, e registra solo se il consumo è avvenuto. */
    private void answer(Dilemma dilemma) {
        if (dilemmas.resolve(dilemma)) {
            controller.registerChoice(dilemma.first().trait());
        }
    }

    @Test
    void ilPianoPoneTreDilemmiNellOrdineDelCatalogo() {
        List<Integer> posti = new ArrayList<>();

        while (dilemmas.hasNext()) {
            Dilemma current = dilemmas.current();
            posti.add(current.id());
            answer(current);
        }

        assertEquals(List.of(1, 2, 3), posti);
        assertEquals(3, game.getMind().getTotalProfileChoices());
    }

    /**
     * Un doppio clic sulla stessa risposta non deve valere due scelte: è il
     * caso che la sequenza esiste per impedire.
     */
    @Test
    void rispondereDueVolteAlloStessoDilemmaRegistraUnaSolaScelta() {
        Dilemma first = dilemmas.current();

        answer(first);
        answer(first);

        assertEquals(1, game.getMind().getTotalProfileChoices());
        assertEquals(1, dilemmas.resolvedCount());
        assertTrue(dilemmas.hasNext());
    }

    /**
     * Un tentativo fallito fa ricominciare il puzzle ma non le domande: la
     * sequenza sopravvive al retry, come prima dell'estrazione.
     */
    @Test
    void unRetryNonRiproponeUnDilemmaGiaRisolto() {
        Dilemma first = dilemmas.current();
        answer(first);

        controller.registerFailedAttempt();

        assertEquals(FloorAttempts.MAX - 1, controller.remainingAttempts());
        assertEquals(1, dilemmas.resolvedCount());
        assertFalse(dilemmas.current().equals(first));
        assertEquals(1, game.getMind().getTotalProfileChoices());
    }

    /**
     * Nemmeno ricominciare la prova a tentativi esauriti rimette in gioco le
     * domande già risposte.
     */
    @Test
    void ricominciareLaProvaNonRiproponeIDilemmiGiaRisolti() {
        answer(dilemmas.current());
        answer(dilemmas.current());

        for (int i = 0; i < FloorAttempts.MAX; i++) {
            controller.registerFailedAttempt();
        }

        controller.restartTrial();

        assertEquals(FloorAttempts.MAX, controller.remainingAttempts());
        assertEquals(2, dilemmas.resolvedCount());
        assertEquals(2, game.getMind().getTotalProfileChoices());
    }

    /**
     * Le due domande a tempo si riconoscono da quante ne sono già state
     * risolte: è il criterio con cui la scena decide i 40 e i 20 secondi.
     */
    @Test
    void ilConteggioDeiRisoltiIdentificaLaDomandaDaMostrare() {
        assertEquals(0, dilemmas.resolvedCount());

        answer(dilemmas.current());
        assertEquals(1, dilemmas.resolvedCount());

        answer(dilemmas.current());
        assertEquals(2, dilemmas.resolvedCount());

        answer(dilemmas.current());
        assertFalse(dilemmas.hasNext());
    }
}
