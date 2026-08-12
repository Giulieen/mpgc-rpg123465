package it.unicam.cs.mpgc.rpg123465.floors.altezze;

/**
 * I parametri di un ponte: il ritmo delle frecce, quanto tempo si ha per
 * rispondere e quanto velocemente si avanza. Sono i valori da bilanciare.
 *
 * @param type             tipo di ponte / canale
 * @param name             nome breve mostrato al giocatore, es. "veloce"
 * @param blurb            descrizione di velocità, stabilità e difficoltà
 * @param arrowIntervalMs  millisecondi fra una freccia e la successiva
 * @param responseMs       millisecondi massimi per rispondere a una freccia
 * @param advancePerSecond punti percentuali di avanzamento al secondo
 */
public record BridgeSpec(
        BridgeType type,
        String name,
        String blurb,
        double arrowIntervalMs,
        double responseMs,
        double advancePerSecond) {

    public BridgeSpec {
        if (name == null || name.isBlank() || blurb == null || blurb.isBlank()) {
            throw new IllegalArgumentException("Nome e descrizione del ponte sono obbligatori.");
        }
        if (arrowIntervalMs <= 0 || responseMs <= 0 || advancePerSecond <= 0) {
            throw new IllegalArgumentException("I parametri del ponte devono essere positivi.");
        }
    }
}
