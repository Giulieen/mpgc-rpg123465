package it.unicam.cs.mpgc.rpg123465.model;

/**
 * Profilo verso cui il protagonista si sta dirigendo nella demo.
 *
 * Il risultato nasce dai tre tratti nascosti accumulati nei dilemmi:
 * Coraggio, Curiosità e Avventura.
 *
 * <p>
 * Un profilo è fatto di ciò che dice del giocatore, non di come lo si mostra:
 * la figura che lo illustra è una scelta della vista e vive nel package
 * {@code view}, così una futura interfaccia può illustrarlo a modo suo.
 */
public enum PlayerProfile {

    CORAGGIOSO(
            "Il Coraggioso",
            "Davanti all'incertezza tendi a scegliere ciò che richiede presenza e decisione. "
                    + "La paura non scompare, ma raramente ti impedisce di avanzare."
    ),

    CURIOSO(
            "Il Curioso",
            "Ciò che non conosci ti attira. Preferisci osservare, capire e scoprire, "
                    + "anche quando sapere significa avvicinarsi a qualcosa di inquietante."
    ),

    AVVENTURIERO(
            "L'Avventuriero",
            "Il cambiamento ti richiama più della sicurezza. Le tue scelte cercano esperienza, "
                    + "movimento e possibilità nuove, anche quando comportano un rischio."
    ),

    ESPLORATORE(
            "L'Esploratore",
            "Coraggio e curiosità procedono insieme. Non ti basta superare ciò che trovi davanti: "
                    + "vuoi anche capire cosa si nasconde oltre."
    ),

    RISOLUTO(
            "Il Risoluto",
            "Coraggio e desiderio d'avventura guidano le tue decisioni. "
                    + "Quando devi scegliere, preferisci una direzione netta all'immobilità."
    ),

    VISIONARIO(
            "Il Visionario",
            "Curiosità e avventura si alimentano a vicenda. "
                    + "Cerchi possibilità che ancora non conosci e sei disposto a cambiare prospettiva per raggiungerle."
    ),

    IMPREVEDIBILE(
            "L'Imprevedibile",
            "Le tue risposte non seguono una sola direzione. "
                    + "Coraggio, curiosità e desiderio d'avventura emergono in equilibrio, "
                    + "rendendo il tuo percorso ancora difficile da prevedere."
    );

    private final String name;
    private final String description;

    PlayerProfile(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /** @return il nome del profilo, come compare al giocatore */
    public String getName() {
        return name;
    }

    /** @return il testo che descrive il profilo al giocatore */
    public String getDescription() {
        return description;
    }
}
