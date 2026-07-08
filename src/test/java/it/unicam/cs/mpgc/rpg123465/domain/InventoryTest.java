package it.unicam.cs.mpgc.rpg123465.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test della classe {@link Inventory}.
 */
class InventoryTest {

    private Item nuovoOggetto() {
        return new Item("Respiro Profondo", "Un ricordo di lucidità.", ItemType.SYMBOLIC);
    }

    @Test
    void unNuovoInventarioEVuoto() {
        Inventory inventory = new Inventory();

        assertTrue(inventory.isEmpty());
        assertEquals(0, inventory.size());
    }

    @Test
    void aggiungereUnOggettoAumentaLaDimensione() {
        Inventory inventory = new Inventory();

        inventory.addItem(nuovoOggetto());

        assertFalse(inventory.isEmpty());
        assertEquals(1, inventory.size());
    }

    @Test
    void rimuovereUnOggettoPresenteRestituisceVero() {
        Inventory inventory = new Inventory();
        Item item = nuovoOggetto();
        inventory.addItem(item);

        assertTrue(inventory.removeItem(item));
        assertTrue(inventory.isEmpty());
    }

    @Test
    void aggiungereUnOggettoNullVieneRifiutato() {
        Inventory inventory = new Inventory();

        assertThrows(IllegalArgumentException.class, () -> inventory.addItem(null));
    }

    @Test
    void laListaRestituitaNonEModificabile() {
        Inventory inventory = new Inventory();
        List<Item> items = inventory.getItems();

        assertThrows(UnsupportedOperationException.class, () -> items.add(nuovoOggetto()));
    }
}
