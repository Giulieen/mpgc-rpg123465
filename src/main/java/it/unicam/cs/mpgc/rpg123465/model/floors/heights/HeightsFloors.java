package it.unicam.cs.mpgc.rpg123465.model.floors.heights;

/**
 * Contenuto del Piano III — Le Altezze.
 */
public final class HeightsFloors {

    private HeightsFloors() {
        // Solo metodi statici.
    }

    /**
     * @return contenuto del Piano III
     */
    public static AltitudeCrossing altitudeCrossing() {
        return new AltitudeCrossing(
                "Piano III — Le Altezze",

                "/images/scenes/floor3-altezze.png",

                "Scegli uno dei tre ponti.\n\n"
                        + "Durante l'attraversata compariranno delle frecce: "
                        + "premi ogni volta la freccia opposta per recuperare "
                        + "l'equilibrio.\n\n"
                        + "Se un ponte si illumina, cliccalo rapidamente. "
                        + "Tre errori e precipiti.",

                "Raggiungi l'altra sponda. "
                        + "Per un momento il vuoto resta alle tue spalle. "
                        + "La porta davanti a te si apre.",

                "L'equilibrio ti abbandona. "
                        + "Il ponte scompare sotto i tuoi piedi "
                        + "e il vuoto ti trascina con sé.",

                HeightsConfig.standard()
        );
    }
}
