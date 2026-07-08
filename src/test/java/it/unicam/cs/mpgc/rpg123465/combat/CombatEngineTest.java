package it.unicam.cs.mpgc.rpg123465.combat;

import it.unicam.cs.mpgc.rpg123465.domain.Enemy;
import it.unicam.cs.mpgc.rpg123465.domain.Inventory;
import it.unicam.cs.mpgc.rpg123465.domain.Player;
import it.unicam.cs.mpgc.rpg123465.domain.Stats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test della logica di combattimento di {@link CombatEngine}.
 */
class CombatEngineTest {

    private final CombatEngine combatEngine = new CombatEngine();

    private Player nuovoGiocatore(Stats stats) {
        return new Player("Viaggiatore", stats, new Inventory());
    }

    private Enemy nuovoNemico(Stats stats) {
        return new Enemy("Ombra", stats, "Una presenza oscura.");
    }

    @Test
    void unAttaccoLetaleRestituisceVittoria() {
        Player player = nuovoGiocatore(new Stats(100, 14, 5));
        Enemy enemy = nuovoNemico(new Stats(1, 0, 0));

        CombatResult result = combatEngine.executeTurn(player, enemy, CombatAction.ATTACK);

        assertEquals(CombatResult.VICTORY, result);
        assertFalse(enemy.isAlive());
    }

    @Test
    void seIlNemicoSopravviveEUccideIlGiocatoreESconfitta() {
        Player player = nuovoGiocatore(new Stats(1, 1, 0));
        Enemy enemy = nuovoNemico(new Stats(100, 50, 0));

        CombatResult result = combatEngine.executeTurn(player, enemy, CombatAction.ATTACK);

        assertEquals(CombatResult.DEFEAT, result);
        assertFalse(player.isAlive());
        assertTrue(enemy.isAlive());
    }

    @Test
    void seEntrambiSopravvivonoIlCombattimentoContinua() {
        Player player = nuovoGiocatore(new Stats(100, 14, 5));
        Enemy enemy = nuovoNemico(new Stats(100, 10, 3));

        CombatResult result = combatEngine.executeTurn(player, enemy, CombatAction.ATTACK);

        assertNull(result);
        assertTrue(player.isAlive());
        assertTrue(enemy.isAlive());
    }

    @Test
    void unAttaccoInfliggeSempreAlmenoUnDanno() {
        Player player = nuovoGiocatore(new Stats(100, 1, 100));
        Enemy enemy = nuovoNemico(new Stats(100, 1, 100));
        int vitaInizialeNemico = enemy.getStats().getCurrentHealth();

        combatEngine.executeTurn(player, enemy, CombatAction.ATTACK);

        assertEquals(vitaInizialeNemico - 1, enemy.getStats().getCurrentHealth());
    }

    @Test
    void laFugaRestituisceEscape() {
        Player player = nuovoGiocatore(new Stats(100, 14, 5));
        Enemy enemy = nuovoNemico(new Stats(100, 10, 3));

        CombatResult result = combatEngine.executeTurn(player, enemy, CombatAction.ESCAPE);

        assertEquals(CombatResult.ESCAPE, result);
    }

    @Test
    void parametriNullVengonoRifiutati() {
        Player player = nuovoGiocatore(new Stats(100, 14, 5));
        Enemy enemy = nuovoNemico(new Stats(100, 10, 3));

        assertThrows(IllegalArgumentException.class,
                () -> combatEngine.executeTurn(null, enemy, CombatAction.ATTACK));
        assertThrows(IllegalArgumentException.class,
                () -> combatEngine.executeTurn(player, null, CombatAction.ATTACK));
        assertThrows(IllegalArgumentException.class,
                () -> combatEngine.executeTurn(player, enemy, null));
    }
}
