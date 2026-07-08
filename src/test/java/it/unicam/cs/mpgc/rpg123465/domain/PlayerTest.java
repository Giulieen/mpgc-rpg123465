package it.unicam.cs.mpgc.rpg123465.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test del giocatore {@link Player}, in particolare dell'uso degli oggetti curativi.
 */
class PlayerTest {

    private Player nuovoGiocatore() {
        return new Player("Viaggiatore", new Stats(100, 14, 5), new Inventory());
    }

    private Item oggettoCurativo(int potenza) {
        return new Item("Pozione di Lucidità", "Ripristina la calma interiore.",
                ItemType.HEALING, potenza);
    }

    @Test
    void usareUnOggettoCurativoRipristinaLaVitaELoRimuove() {
        Player player = nuovoGiocatore();
        player.addItem(oggettoCurativo(30));
        player.takeDamage(40);

        Item usato = player.useHealingItem();

        assertEquals(90, player.getStats().getCurrentHealth());
        assertTrue(player.getInventory().isEmpty());
        assertEquals("Pozione di Lucidità", usato.getName());
    }

    @Test
    void laCuraNonSuperaLaVitaMassima() {
        Player player = nuovoGiocatore();
        player.addItem(oggettoCurativo(50));
        player.takeDamage(10);

        player.useHealingItem();

        assertEquals(100, player.getStats().getCurrentHealth());
    }

    @Test
    void senzaOggettiCurativiNonSiPuoCurare() {
        Player player = nuovoGiocatore();
        player.addItem(new Item("Ricordo", "Un simbolo.", ItemType.SYMBOLIC));

        assertFalse(player.hasHealingItem());
        assertNull(player.useHealingItem());
        assertEquals(1, player.getInventory().size());
    }

    @Test
    void haUnOggettoCurativoSoloSePresenteNellInventario() {
        Player player = nuovoGiocatore();
        assertFalse(player.hasHealingItem());

        Item pozione = oggettoCurativo(20);
        player.addItem(pozione);

        assertTrue(player.hasHealingItem());
        assertSame(pozione, player.useHealingItem());
    }
}
