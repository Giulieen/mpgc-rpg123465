package it.unicam.cs.mpgc.rpg123465.ui;

import javafx.scene.Parent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test della sequenza di scene che compone un piano.
 */
class SceneFlowTest {

    /**
     * Una scena finta: annota di essere stata mostrata e restituisce l'esito
     * deciso dal test. Non costruisce nulla di grafico, così la sequenza si può
     * verificare senza avviare JavaFX.
     */
    private static class ScenaFinta implements FloorScene {

        private final String nome;
        private final List<String> registro;
        private final SceneOutcome esito;

        ScenaFinta(String nome, List<String> registro, SceneOutcome esito) {
            this.nome = nome;
            this.registro = registro;
            this.esito = esito;
        }

        @Override
        public Parent createView(Consumer<SceneOutcome> onFinished) {
            registro.add(nome);
            onFinished.accept(esito);
            return null;
        }
    }

    /** Raccoglie l'ordine in cui le scene vengono mostrate. */
    private final List<String> registro = new ArrayList<>();

    private SceneFlow flusso(List<FloorScene> scene, boolean[] concluso) {
        return new SceneFlow(scene, vista -> { }, () -> concluso[0] = true);
    }

    @Test
    void leSceneVengonoAttraversateInOrdine() {
        boolean[] concluso = {false};
        SceneFlow flow = flusso(List.of(
                new ScenaFinta("arrivo", registro, SceneOutcome.AVANTI),
                new ScenaFinta("scelta", registro, SceneOutcome.AVANTI),
                new ScenaFinta("esito", registro, SceneOutcome.AVANTI)), concluso);

        flow.start();

        assertEquals(List.of("arrivo", "scelta", "esito"), registro);
    }

    @Test
    void superatalUltimaScenaIlPianoEConcluso() {
        boolean[] concluso = {false};
        SceneFlow flow = flusso(
                List.of(new ScenaFinta("unica", registro, SceneOutcome.AVANTI)), concluso);

        flow.start();

        assertTrue(concluso[0]);
    }

    @Test
    void ripetereRimostraLaStessaScenaSenzaConcludereIlPiano() {
        boolean[] concluso = {false};

        // La stanza buia: due volte la porta si apre sul vuoto e rimanda
        // indietro, alla terza si trova quella vera.
        FloorScene stanzaBuia = new FloorScene() {
            private int tentativi = 0;

            @Override
            public Parent createView(Consumer<SceneOutcome> onFinished) {
                registro.add("tentativo");
                tentativi++;
                onFinished.accept(tentativi < 3 ? SceneOutcome.RIPETI : SceneOutcome.AVANTI);
                return null;
            }
        };

        flusso(List.of(stanzaBuia), concluso).start();

        assertEquals(List.of("tentativo", "tentativo", "tentativo"), registro);
        assertTrue(concluso[0]);
    }

    @Test
    void ripetereNonFaAvanzareAllaScenaSuccessiva() {
        boolean[] concluso = {false};

        FloorScene rimbalza = new FloorScene() {
            private int volte = 0;

            @Override
            public Parent createView(Consumer<SceneOutcome> onFinished) {
                registro.add("porta");
                volte++;
                onFinished.accept(volte == 1 ? SceneOutcome.RIPETI : SceneOutcome.AVANTI);
                return null;
            }
        };

        flusso(List.of(rimbalza, new ScenaFinta("uscita", registro, SceneOutcome.AVANTI)),
                concluso).start();

        // La scena successiva arriva solo dopo che la prima ha lasciato passare.
        assertEquals(List.of("porta", "porta", "uscita"), registro);
    }

    @Test
    void unPianoSenzaSceneVieneRifiutato() {
        assertThrows(IllegalArgumentException.class,
                () -> new SceneFlow(List.of(), vista -> { }, () -> { }));
        assertThrows(IllegalArgumentException.class,
                () -> new SceneFlow(null, vista -> { }, () -> { }));
    }

    @Test
    void iCallbackMancantiVengonoRifiutati() {
        List<FloorScene> scene = List.of(
                new ScenaFinta("unica", registro, SceneOutcome.AVANTI));

        assertThrows(IllegalArgumentException.class,
                () -> new SceneFlow(scene, null, () -> { }));
        assertThrows(IllegalArgumentException.class,
                () -> new SceneFlow(scene, vista -> { }, null));
    }

    @Test
    void finoAllUltimaScenaIlPianoNonEConcluso() {
        boolean[] concluso = {false};
        List<String> ordine = new ArrayList<>();

        new SceneFlow(List.of(
                new FloorScene() {
                    @Override
                    public Parent createView(Consumer<SceneOutcome> onFinished) {
                        ordine.add("prima");
                        // Il piano non deve dirsi concluso a meta' strada.
                        assertFalse(concluso[0]);
                        onFinished.accept(SceneOutcome.AVANTI);
                        return null;
                    }
                },
                new ScenaFinta("seconda", ordine, SceneOutcome.AVANTI)),
                vista -> { }, () -> concluso[0] = true).start();

        assertEquals(List.of("prima", "seconda"), ordine);
        assertTrue(concluso[0]);
    }
}
