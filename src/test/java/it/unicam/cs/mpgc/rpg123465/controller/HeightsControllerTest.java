package it.unicam.cs.mpgc.rpg123465.controller;

import it.unicam.cs.mpgc.rpg123465.model.FloorAttempts;
import it.unicam.cs.mpgc.rpg123465.model.ProfileTrait;
import it.unicam.cs.mpgc.rpg123465.model.floors.heights.HeightsConfig;
import it.unicam.cs.mpgc.rpg123465.model.floors.heights.AltitudeCrossing;
import it.unicam.cs.mpgc.rpg123465.testing.FakeQuestionRepository;
import it.unicam.cs.mpgc.rpg123465.testing.FakeSaveManager;
import it.unicam.cs.mpgc.rpg123465.testing.TestTowers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeightsControllerTest {

    private GameController game;
    private HeightsController controller;

    private static AltitudeCrossing crossing() {
        return new AltitudeCrossing(
                "Piano III — Le Altezze",
                "/images/scenes/floor3-altezze.png",
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
    }

    @Test
    void laTraversataIniziaConTuttiITentativi() {
        assertEquals(FloorAttempts.MAX, controller.remainingAttempts());
        assertEquals(FloorAttempts.MAX, controller.maxAttempts());
        assertTrue(controller.canRetry());
    }

    @Test
    void unaCadutaConsumaEsattamenteUnTentativo() {
        controller.registerFall();

        assertEquals(FloorAttempts.MAX - 1, controller.remainingAttempts());
        assertTrue(controller.canRetry());
    }

    @Test
    void treCaduteEsaurisconoLaProva() {
        controller.registerFall();
        controller.registerFall();
        assertTrue(controller.canRetry());

        controller.registerFall();

        assertEquals(0, controller.remainingAttempts());
        assertFalse(controller.canRetry());
    }

    /**
     * A tentativi esauriti una caduta ulteriore non deve portare il conteggio
     * in negativo: la prova è già finita.
     */
    @Test
    void cadereAncoraATentativiEsauritiNonScendeSottoZero() {
        for (int i = 0; i < FloorAttempts.MAX + 2; i++) {
            controller.registerFall();
        }

        assertEquals(0, controller.remainingAttempts());
    }

    @Test
    void ricominciareLaProvaRestituisceTuttiITentativi() {
        controller.registerFall();
        controller.registerFall();
        controller.registerFall();

        controller.restartTrial();

        assertEquals(FloorAttempts.MAX, controller.remainingAttempts());
        assertTrue(controller.canRetry());
    }

    @Test
    void iTentativiSonoQuelliDellaPartita() {
        controller.registerFall();

        assertEquals(game.getRemainingAttempts(), controller.remainingAttempts());
        assertTrue(game.hasAttemptsLeft());
    }

    @Test
    void aTentativiEsauritiLaPartitaRisultaSconfitta() {
        for (int i = 0; i < FloorAttempts.MAX; i++) {
            controller.registerFall();
        }

        assertTrue(game.isDefeated());
    }

    // --- profilo ---------------------------------------------------------

    @Test
    void rispondereAUnDilemmaRegistraIlTratto() {
        controller.registerChoice(ProfileTrait.AVVENTURA);

        assertEquals(1, game.getMind().getAvventura());
    }

    /**
     * Le cadute non dicono nulla su chi sta giocando: solo le risposte
     * contribuiscono al profilo.
     */
    @Test
    void cadereNonAlteraIlProfilo() {
        controller.registerChoice(ProfileTrait.CORAGGIO);

        controller.registerFall();
        controller.registerFall();
        controller.restartTrial();

        assertEquals(1, game.getMind().getTotalProfileChoices());
        assertEquals(1, game.getMind().getCoraggio());
    }

    @Test
    void registrareUnTrattoNullSollevaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> controller.registerChoice(null));
    }

    @Test
    void ilControllerRifiutaArgomentiMancanti() {
        assertThrows(IllegalArgumentException.class, () -> new HeightsController(null, game));

        assertThrows(IllegalArgumentException.class, () -> new HeightsController(crossing(), null));
    }

    // --- configurazione --------------------------------------------------

    @Test
    void laConfigurazioneStandardHaTrePontiEQuattroSoglie() {
        HeightsConfig config = HeightsConfig.standard();

        assertEquals(3, config.bridges().size());
        assertEquals(4, config.routeThresholds().length);
        assertEquals(3, config.balancePoints());
        assertTrue(config.totalSeconds() > 0);
    }

    @Test
    void laConfigurazioneRifiutaUnNumeroDiPontiDiverboDaTre() {
        assertThrows(IllegalArgumentException.class, () -> new HeightsConfig(
                150, 3, new double[] {50}, 7.0, 1.0, 1.0,
                java.util.List.of(HeightsConfig.standard().bridge(0)), null));
    }
}
