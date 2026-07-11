package it.unicam.cs.mpgc.rpg123465.domain;

/**
 * La forma che Chimeris assume all'ultimo piano della Torre.
 * <p>
 * Non è scelta né casuale: nasce dall'atteggiamento con cui il giocatore ha
 * affrontato le paure dei piani precedenti. Non descrive il giocatore, ma il
 * <em>percorso compiuto dal personaggio</em>.
 */
public enum AlterEgo {

    /** Chi ha colpito ogni paura. */
    BESTIA("La Bestia",
            "Hai distrutto ogni cosa che ti spaventava, senza mai chiederti perché ti spaventasse."),

    /** Chi ha evitato ogni paura. */
    OMBRA("L'Ombra",
            "Non hai mai affrontato nulla: sei fuggito. E ciò che fuggi ti segue."),

    /** Chi è rimasto lucido davanti a ogni paura. */
    CUSTODE("Il Custode",
            "Sei rimasto. Non hai vinto la paura: hai imparato a starle accanto senza cedere."),

    /** Chi ha accolto le paure invece di combatterle. */
    MEDIATORE("Il Mediatore",
            "Hai teso la mano a ciò che ti terrorizzava. Nulla di ciò che si comprende resta un mostro."),

    /** Chi non si è mai affidato a un solo modo di reagire. */
    RIFLESSO("Il Riflesso",
            "Non ti sei mai fissato in un'unica forma. Per questo, in cima, non trovi un mostro: trovi te stesso.");

    private final String nome;
    private final String descrizione;

    AlterEgo(String nome, String descrizione) {
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
