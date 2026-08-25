package it.unicam.cs.mpgc.rpg123465.questions;

/**
 * Segnala che il catalogo delle domande non è utilizzabile.
 *
 * <p>
 * Copre i guasti dei <em>dati</em>: file assente, JSON non valido, catalogo
 * vuoto, categoria mancante, domande insufficienti per un piano. Gli errori di
 * programmazione — una categoria nulla, un numero di domande non positivo —
 * restano {@link IllegalArgumentException}, perché non si correggono
 * modificando il catalogo.
 *
 * <p>
 * È volutamente non controllata: il catalogo viene letto anche da
 * {@code createView()}, che implementa un'interfaccia e non può dichiarare
 * eccezioni. Chi avvia una partita la intercetta e la traduce in un messaggio
 * comprensibile, invece di lasciarla finire nella console.
 */
public class QuestionCatalogException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message descrizione del problema, rivolta a chi cura il catalogo
     */
    public QuestionCatalogException(String message) {
        super(message);
    }

    /**
     * @param message descrizione del problema
     * @param cause errore originale, tipicamente di lettura o di sintassi
     */
    public QuestionCatalogException(String message, Throwable cause) {
        super(message, cause);
    }
}
