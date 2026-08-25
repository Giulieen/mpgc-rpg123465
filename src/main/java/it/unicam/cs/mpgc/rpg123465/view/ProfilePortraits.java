package it.unicam.cs.mpgc.rpg123465.view;

import it.unicam.cs.mpgc.rpg123465.model.PlayerProfile;

/**
 * Associa a ogni profilo la figura che lo rappresenta.
 *
 * <p>
 * Il dominio dice chi è un profilo — il nome e la descrizione che lo
 * distinguono; con quale immagine mostrarlo è invece una decisione della
 * vista, e vive qui. Un'interfaccia diversa — web, mobile — può sceglierne
 * altre senza che {@link PlayerProfile} debba saperne nulla.
 *
 * <p>
 * Restituisce il percorso e non un'immagine già pronta: così la classe non
 * dipende da JavaFX e una prova può verificare che ogni profilo abbia
 * davvero la sua figura fra le risorse.
 */
public final class ProfilePortraits {

    private static final String FOLDER = "/images/profili/";

    private ProfilePortraits() {
        // Impedisce l'istanziazione.
    }

    /**
     * Indica dove si trova il ritratto di un profilo.
     *
     * @param profile profilo da illustrare
     * @return percorso della figura nel classpath
     * @throws IllegalArgumentException se il profilo è null
     */
    public static String resourceOf(PlayerProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Il profilo non può essere null.");
        }

        return FOLDER + switch (profile) {
            case CORAGGIOSO -> "coraggioso.png";
            case CURIOSO -> "curioso.png";
            case AVVENTURIERO -> "avventuriero.png";
            case ESPLORATORE -> "esploratore.png";
            case RISOLUTO -> "risoluto.png";
            case VISIONARIO -> "visionario.png";
            case IMPREVEDIBILE -> "imprevedibile.png";
        };
    }
}
