package it.unicam.cs.mpgc.rpg123465.floors.buio;

/**
 * Contenuto predefinito del Piano II — Il Buio.
 */
public final class BuioFloor {

    private BuioFloor() {
        // Solo metodi statici.
    }

    /**
     * @return contenuto del Piano II
     */
    public static DarkRoom buio() {
        return new DarkRoom(
                "Piano II — Il Buio",

                "/images/scenes/floor2-buio.jpg",

                "Controlla le pareti. "
                        + "Trova i numeri e componi il codice "
                        + "per aprire la serratura.",

                "L'ultima cifra scatta al suo posto. "
                        + "La serratura cede e la porta si apre. "
                        + "Il buio non è scomparso: hai semplicemente "
                        + "imparato ad attraversarlo.",

                "Le cifre non coincidono. "
                        + "Il buio non nasconde niente che tu non possa rileggere.",

                "Il tempo si esaurisce. "
                        + "La serratura si blocca, ma le pareti restano dove sono.",

                "3524",
                60,
                40
        );
    }
}