package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * Profilo verso cui il protagonista si sta dirigendo nella demo.
 *
 * Il risultato nasce dai tre tratti nascosti accumulati nei dilemmi:
 * Coraggio, Curiosità e Avventura.
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

    POLIEDRICO(
            "Il Poliedrico",
            "Le tue risposte non seguono una sola direzione. "
                    + "Coraggio, curiosità e desiderio d'avventura emergono in equilibrio, "
                    + "rendendo il tuo percorso ancora difficile da prevedere."
    );

    private final String nome;
    private final String descrizione;

    PlayerProfile(
            String nome,
            String descrizione
    ) {
        this.nome = nome;
        this.descrizione = descrizione;
    }

    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
