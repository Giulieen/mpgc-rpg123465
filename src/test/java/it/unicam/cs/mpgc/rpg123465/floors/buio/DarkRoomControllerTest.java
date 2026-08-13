package it.unicam.cs.mpgc.rpg123465.floors.buio;

import it.unicam.cs.mpgc.rpg123465.controller.GameController;
import it.unicam.cs.mpgc.rpg123465.domain.FloorAttempts;
import it.unicam.cs.mpgc.rpg123465.domain.ProfileTrait;
import it.unicam.cs.mpgc.rpg123465.testing.FakeSaveManager;
import it.unicam.cs.mpgc.rpg123465.testing.TestTowers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DarkRoomControllerTest {

    private static final String CODICE = "3524";

    private GameController game;
    private DarkRoomController controller;

    private static DarkRoom room() {
        return new DarkRoom(
                "Piano II — Il Buio",
                "/images/scenes/floor2-buio.jpg",
                "intro",
                "outro",
                "errore",
                "tempo scaduto",
                CODICE,
                60);
    }

    @BeforeEach
    void setUp() {
        game = new GameController(TestTowers.engineWithFloors(3), new FakeSaveManager());
        controller = new DarkRoomController(room(), game);
    }

    // --- combinazione ----------------------------------------------------

    @Test
    void laCombinazioneGiustaApreLaPorta() {
        assertTrue(controller.opens(CODICE));
    }

    @Test
    void unaCombinazioneSbagliataNonApre() {
        assertFalse(controller.opens("0000"));
        assertFalse(controller.opens("3525"));
        assertFalse(controller.opens(""));
    }

    /**
     * La verifica non consuma nulla di per sé: è la scena a decidere che un
     * codice errato costa un tentativo.
     */
    @Test
    void verificareLaCombinazioneNonConsumaTentativi() {
        controller.opens("0000");
        controller.opens("1111");

        assertEquals(FloorAttempts.MAX, controller.remainingAttempts());
    }

    // --- tentativi -------------------------------------------------------

    @Test
    void laProvaIniziaConTuttiITentativi() {
        assertEquals(FloorAttempts.MAX, controller.remainingAttempts());
        assertEquals(FloorAttempts.MAX, controller.maxAttempts());
        assertTrue(controller.canRetry());
    }

    @Test
    void unErroreConsumaEsattamenteUnTentativo() {
        controller.registerFailedAttempt();

        assertEquals(FloorAttempts.MAX - 1, controller.remainingAttempts());
        assertTrue(controller.canRetry());
    }

    @Test
    void esauritiITentativiNonSiPuoPiuRiprovare() {
        for (int i = 0; i < FloorAttempts.MAX; i++) {
            controller.registerFailedAttempt();
        }

        assertEquals(0, controller.remainingAttempts());
        assertFalse(controller.canRetry());
    }

    @Test
    void ricominciareLaProvaRestituisceTuttiITentativi() {
        for (int i = 0; i < FloorAttempts.MAX; i++) {
            controller.registerFailedAttempt();
        }

        controller.restartTrial();

        assertEquals(FloorAttempts.MAX, controller.remainingAttempts());
        assertTrue(controller.canRetry());
    }

    /**
     * Il controller del piano non tiene un contatore proprio: legge e scrive
     * quello della partita, così la barra e la prova non possono divergere.
     */
    @Test
    void iTentativiSonoQuelliDellaPartita() {
        controller.registerFailedAttempt();

        assertEquals(game.getRemainingAttempts(), controller.remainingAttempts());

        game.loseAttempt();

        assertEquals(FloorAttempts.MAX - 2, controller.remainingAttempts());
    }

    // --- profilo ---------------------------------------------------------

    @Test
    void rispondereAUnDilemmaRegistraIlTratto() {
        controller.registerChoice(ProfileTrait.CURIOSITA);

        assertEquals(1, game.getMind().getCuriosita());
        assertEquals(1, game.getMind().getTotalProfileChoices());
    }

    /**
     * Errori, timeout e ricominciamenti non devono toccare il profilo: la
     * difficoltà della prova non dice nulla su chi sta giocando.
     */
    @Test
    void perdereTentativiERicominciareNonAlteraIlProfilo() {
        controller.registerChoice(ProfileTrait.CORAGGIO);
        controller.registerChoice(ProfileTrait.AVVENTURA);

        for (int i = 0; i < FloorAttempts.MAX; i++) {
            controller.registerFailedAttempt();
        }
        controller.restartTrial();

        assertEquals(2, game.getMind().getTotalProfileChoices());
        assertEquals(1, game.getMind().getCoraggio());
        assertEquals(1, game.getMind().getAvventura());
    }

    @Test
    void registrareUnTrattoNullSollevaEccezione() {
        assertThrows(IllegalArgumentException.class, () -> controller.registerChoice(null));
    }

    // --- costruzione -----------------------------------------------------

    @Test
    void ilControllerRifiutaArgomentiMancanti() {
        assertThrows(IllegalArgumentException.class,
                () -> new DarkRoomController(null, game));

        assertThrows(IllegalArgumentException.class,
                () -> new DarkRoomController(room(), null));
    }

    @Test
    void laStanzaRifiutaUnaCombinazioneNonDiQuattroCifre() {
        assertThrows(IllegalArgumentException.class,
                () -> new DarkRoom("t", "/i.jpg", "i", "o", "w", "x", "35", 60));

        assertThrows(IllegalArgumentException.class,
                () -> new DarkRoom("t", "/i.jpg", "i", "o", "w", "x", "abcd", 60));
    }

    @Test
    void laStanzaRifiutaUnTempoNonPositivo() {
        assertThrows(IllegalArgumentException.class,
                () -> new DarkRoom("t", "/i.jpg", "i", "o", "w", "x", CODICE, 0));
    }
}
