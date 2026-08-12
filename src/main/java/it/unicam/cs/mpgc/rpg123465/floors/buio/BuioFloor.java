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
                        + "Il lucchetto scatta a vuoto e il buio sembra "
                        + "stringersi attorno a te. La porta resta chiusa. "
                        + "Raccogli ciò che ricordi e prova ancora.",

                "Il tempo si esaurisce. "
                        + "La serratura si blocca con uno scatto secco "
                        + "e la stanza torna immobile. "
                        + "Il buio non ti concede altro tempo: "
                        + "dovrai ricominciare.",

                "3524",
                60,
                40
        );
    }
}