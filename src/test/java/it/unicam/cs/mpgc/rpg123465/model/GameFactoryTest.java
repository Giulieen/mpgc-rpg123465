package it.unicam.cs.mpgc.rpg123465.engine;

import it.unicam.cs.mpgc.rpg123465.domain.FloorAttempts;
import it.unicam.cs.mpgc.rpg123465.domain.FloorContent;
import it.unicam.cs.mpgc.rpg123465.floors.encounter.FearEncounter;
import it.unicam.cs.mpgc.rpg123465.testing.FakeQuestionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prove sulla fabbrica della partita, con un catalogo finto e deterministico. */
class GameFactoryTest {

    @Test
    void laTorreHaTrePiani() {
        GameEngine engine = GameFactory.createNewGame("Collaudo", new FakeQuestionRepository());

        assertEquals(3, engine.getTower().getTotalFloors());
    }

    @Test
    void iPianiSonoNumeratiInOrdineDaUno() {
        GameEngine engine = GameFactory.createNewGame("Collaudo", new FakeQuestionRepository());

        for (int i = 0; i < engine.getTower().getTotalFloors(); i++) {
            assertEquals(i + 1, engine.getTower().getFloor(i).getNumber());
        }
    }

    @Test
    void ogniPianoHaUnContenutoEUnTitolo() {
        GameEngine engine = GameFactory.createNewGame("Collaudo", new FakeQuestionRepository());

        for (int i = 0; i < engine.getTower().getTotalFloors(); i++) {
            assertNotNull(engine.getTower().getFloor(i).getContent());
            assertFalse(engine.getTower().getFloor(i).getContent().title().isBlank());
        }
    }

    @Test
    void laPartitaNasceAlPrimoPianoConITentativiInteri() {
        GameEngine engine = GameFactory.createNewGame("Collaudo", new FakeQuestionRepository());

        assertEquals(0, engine.getCurrentFloorIndex());
        assertFalse(engine.isGameCompleted());
        assertEquals(FloorAttempts.MAX, engine.getAttempts().getRemaining());
    }

    @Test
    void ilGiocatoreNasceSenzaAlcunaSceltaRegistrata() {
        GameEngine engine = GameFactory.createNewGame("Collaudo", new FakeQuestionRepository());

        assertEquals(0, engine.getPlayer().getMind().getTotalProfileChoices());
    }

    @Test
    void ilNomeVieneRipulitoDagliSpazi() {
        assertEquals("Giulia", GameFactory.createNewGame("  Giulia  ", new FakeQuestionRepository()).getPlayer().getName());
    }

    @Test
    void unNomeMancanteRicadeSulPredefinito() {
        assertEquals("Viaggiatore", GameFactory.createNewGame(null, new FakeQuestionRepository()).getPlayer().getName());
        assertEquals("Viaggiatore", GameFactory.createNewGame("", new FakeQuestionRepository()).getPlayer().getName());
        assertEquals("Viaggiatore", GameFactory.createNewGame("   ", new FakeQuestionRepository()).getPlayer().getName());
    }

    // --- catalogo iniettato ----------------------------------------------

    /**
     * La prova che le domande arrivano davvero dal catalogo ricevuto e non da
     * un accesso globale nascosto: il testo del Piano I porta il marcatore del
     * catalogo finto, che nel file spedito col gioco non compare.
     */
    @Test
    void ilPrimoPianoUsaIlCatalogoRicevuto() {
        GameEngine engine = GameFactory.createNewGame("Collaudo", new FakeQuestionRepository());

        FloorContent primoPiano = engine.getTower().getFloor(0).getContent();

        assertInstanceOf(FearEncounter.class, primoPiano);
        assertTrue(((FearEncounter) primoPiano).situation()
                .startsWith(FakeQuestionRepository.MARKER));
    }

    /** Il Piano I chiede la sua categoria, non una qualsiasi. */
    @Test
    void alCatalogoVieneChiestaLaCategoriaDeiTopi() {
        FakeQuestionRepository questions = new FakeQuestionRepository();

        GameFactory.createNewGame("Collaudo", questions);

        assertEquals(List.of("topi"), questions.requestedCategories());
    }

    @Test
    void laFabbricaRifiutaUnCatalogoMancante() {
        assertThrows(IllegalArgumentException.class,
                () -> GameFactory.createNewGame("Collaudo", null));
    }

    /**
     * Ogni partita costruisce la propria Torre: due motori non devono
     * condividere lo stato del giocatore.
     */
    @Test
    void duePartiteHannoStatoIndipendente() {
        GameEngine prima = GameFactory.createNewGame("Uno", new FakeQuestionRepository());
        GameEngine seconda = GameFactory.createNewGame("Due", new FakeQuestionRepository());

        prima.climb();
        prima.getAttempts().lose();

        assertEquals(0, seconda.getCurrentFloorIndex());
        assertEquals(FloorAttempts.MAX, seconda.getAttempts().getRemaining());
    }
}
