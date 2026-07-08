package it.unicam.cs.mpgc.rpg123465.combat;

import it.unicam.cs.mpgc.rpg123465.domain.Enemy;
import it.unicam.cs.mpgc.rpg123465.domain.Player;

/**
 * Gestisce la logica del combattimento tra giocatore e nemico.
 */
public class CombatEngine {

    /**
     * Esegue un turno di combattimento.
     *
     * @param player giocatore
     * @param enemy nemico
     * @param action azione scelta dal giocatore
     * @return risultato del combattimento, oppure {@code null} se continua
     */
    public CombatResult executeTurn(Player player, Enemy enemy, CombatAction action) {

        if (player == null || enemy == null || action == null) {
            throw new IllegalArgumentException("I parametri non possono essere null.");
        }

        return switch (action) {
            case ATTACK -> resolveAttack(player, enemy);
            // L'oggetto viene usato dal controller: qui il turno prosegue con la reazione del nemico.
            case USE_ITEM -> resolveEnemyReaction(player, enemy);
            case ESCAPE -> CombatResult.ESCAPE;
        };
    }

    private CombatResult resolveAttack(Player player, Enemy enemy) {
        int playerDamage = Math.max(
                1,
                player.getStats().getAttack() - enemy.getStats().getDefense()
        );

        enemy.takeDamage(playerDamage);

        if (!enemy.isAlive()) {
            return CombatResult.VICTORY;
        }

        return resolveEnemyReaction(player, enemy);
    }

    private CombatResult resolveEnemyReaction(Player player, Enemy enemy) {
        int enemyDamage = Math.max(
                1,
                enemy.getStats().getAttack() - player.getStats().getDefense()
        );

        player.takeDamage(enemyDamage);

        if (!player.isAlive()) {
            return CombatResult.DEFEAT;
        }

        return null;
    }
}