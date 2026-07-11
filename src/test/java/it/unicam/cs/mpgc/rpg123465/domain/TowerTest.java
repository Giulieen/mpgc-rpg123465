package it.unicam.cs.mpgc.rpg123465.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test della struttura della Torre e dei suoi piani.
 */
class TowerTest {

    /** Un contenuto di piano qualunque, per provare la struttura. */
    private record ContenutoDiProva(String title) implements FloorContent {
    }

    private Floor nuovoPiano(int numero, String nome) {
        return new Floor(numero, nome, new ContenutoDiProva("Prova"));
    }

    @Test
    void laTorreRestituisceIlNumeroCorrettoDiPiani() {
        Tower tower = new Tower(List.of(nuovoPiano(1, "I Topi"), nuovoPiano(2, "Il Buio")));

        assertEquals(2, tower.getTotalFloors());
    }

    @Test
    void ilPianoRichiestoCorrispondeAllIndice() {
        Floor primo = nuovoPiano(1, "I Topi");
        Tower tower = new Tower(List.of(primo, nuovoPiano(2, "Il Buio")));

        assertEquals(primo, tower.getFloor(0));
    }

    @Test
    void unaTorreVuotaVieneRifiutata() {
        assertThrows(IllegalArgumentException.class, () -> new Tower(List.of()));
    }

    @Test
    void unIndiceNonValidoVieneRifiutato() {
        Tower tower = new Tower(List.of(nuovoPiano(1, "I Topi")));

        assertThrows(IllegalArgumentException.class, () -> tower.getFloor(5));
    }

    @Test
    void laListaDeiPianiNonEModificabile() {
        Tower tower = new Tower(List.of(nuovoPiano(1, "I Topi")));
        List<Floor> piani = tower.getFloors();

        assertThrows(UnsupportedOperationException.class,
                () -> piani.add(nuovoPiano(2, "Il Buio")));
    }

    @Test
    void unPianoCustodisceIlProprioContenuto() {
        FloorContent contenuto = new ContenutoDiProva("I Topi");
        Floor piano = new Floor(1, "I Topi", contenuto);

        assertEquals(1, piano.getNumber());
        assertEquals("I Topi", piano.getName());
        assertEquals(contenuto, piano.getContent());
    }

    @Test
    void unPianoMalFormatoVieneRifiutato() {
        FloorContent contenuto = new ContenutoDiProva("Prova");

        assertThrows(IllegalArgumentException.class, () -> new Floor(0, "I Topi", contenuto));
        assertThrows(IllegalArgumentException.class, () -> new Floor(1, " ", contenuto));
        assertThrows(IllegalArgumentException.class, () -> new Floor(1, "I Topi", null));
    }
}
