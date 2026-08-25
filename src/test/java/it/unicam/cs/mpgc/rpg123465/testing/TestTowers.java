package it.unicam.cs.mpgc.rpg123465.testing;

import it.unicam.cs.mpgc.rpg123465.model.Floor;
import it.unicam.cs.mpgc.rpg123465.model.FloorContent;
import it.unicam.cs.mpgc.rpg123465.model.GameEngine;
import it.unicam.cs.mpgc.rpg123465.model.Player;
import it.unicam.cs.mpgc.rpg123465.model.Tower;

import java.util.ArrayList;
import java.util.List;

/**
 * Torri costruite a mano per i test.
 *
 * <p>
 * Le prove non usano {@code GameFactory} perché quella legge il catalogo delle
 * domande dal classpath: un contenuto finto tiene i test del motore e dei
 * controller indipendenti dai dati di gioco.
 */
public final class TestTowers {

    /** Contenuto minimo di un piano: solo il titolo, come chiede il dominio. */
    public record FakeContent(String title) implements FloorContent { }

    private TestTowers() {
    }

    public static Tower withFloors(int count) {
        List<Floor> floors = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            floors.add(new Floor(i, "Piano " + i, new FakeContent("Prova " + i)));
        }

        return new Tower(floors);
    }

    public static GameEngine engineWithFloors(int count) {
        return new GameEngine(new Player("Collaudo"), withFloors(count));
    }
}
