package it.unicam.cs.mpgc.rpg123465.domain;

import it.unicam.cs.mpgc.rpg123465.events.DialogueChoice;
import it.unicam.cs.mpgc.rpg123465.events.DialogueEvent;
import it.unicam.cs.mpgc.rpg123465.events.FloorEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test della classe {@link Tower}.
 */
class TowerTest {

    private Floor nuovoPiano(int numero) {
        FloorEvent event = new DialogueEvent(
                "Bivio",
                "Una voce ti pone una domanda.",
                List.of(
                        new DialogueChoice("Sì", "Hai accettato.", 0),
                        new DialogueChoice("No", "Hai rifiutato.", 0)
                )
        );
        return new Floor(numero, "Piano " + numero, "Descrizione del piano.", event);
    }

    @Test
    void laTorreRestituisceIlNumeroCorrettoDiPiani() {
        Tower tower = new Tower(List.of(nuovoPiano(1), nuovoPiano(2)));

        assertEquals(2, tower.getTotalFloors());
    }

    @Test
    void ilPianoRichiestoCorrispondeAllIndice() {
        Floor primo = nuovoPiano(1);
        Tower tower = new Tower(List.of(primo, nuovoPiano(2)));

        assertEquals(primo, tower.getFloor(0));
    }

    @Test
    void unaTorreVuotaVieneRifiutata() {
        assertThrows(IllegalArgumentException.class, () -> new Tower(List.of()));
    }

    @Test
    void unIndiceNonValidoVieneRifiutato() {
        Tower tower = new Tower(List.of(nuovoPiano(1)));

        assertThrows(IllegalArgumentException.class, () -> tower.getFloor(5));
    }

    @Test
    void laListaDeiPianiNonEModificabile() {
        Tower tower = new Tower(List.of(nuovoPiano(1)));
        List<Floor> piani = tower.getFloors();

        assertThrows(UnsupportedOperationException.class, () -> piani.add(nuovoPiano(2)));
    }
}
