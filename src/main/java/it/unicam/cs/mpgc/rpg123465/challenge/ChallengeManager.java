package it.unicam.cs.mpgc.rpg123465.challenge;

import java.util.List;
import java.util.Optional;

/**
 * Gestisce le prove narrative associate ai piani della torre.
 */
public class ChallengeManager {

    private final Challenge fearChallenge = createFearChallenge();

    public Optional<Challenge> getChallengeForFloor(int floorNumber) {
        if (floorNumber == 1) {
            return Optional.of(fearChallenge);
        }

        return Optional.empty();
    }

    private Challenge createFearChallenge() {
        return new Challenge(
                "Paura del Fallimento",
                List.of(
                        new ChallengeQuestion(
                                "La Paura ti sussurra: \"E se fallissi davanti a tutti?\"",
                                List.of(
                                        "Allora significherebbe che non valgo abbastanza.",
                                        "Fallire è possibile, ma non definisce chi sono.",
                                        "Meglio rinunciare prima ancora di iniziare."
                                ),
                                1,
                                "Accetti la possibilità di sbagliare senza lasciarti definire da essa.",
                                "La Paura cresce: hai confuso un errore con il tuo valore."
                        ),
                        new ChallengeQuestion(
                                "\"Gli altri sono tutti più capaci di te.\"",
                                List.of(
                                        "Probabilmente è vero.",
                                        "Posso imparare e migliorare con il tempo.",
                                        "Non dovrei nemmeno provarci."
                                ),
                                1,
                                "Riconosci che il confronto non decide il tuo cammino.",
                                "La Paura si rafforza: ti misuri solo attraverso gli altri."
                        ),
                        new ChallengeQuestion(
                                "\"Se sbagli una volta, ti ricorderanno solo quello.\"",
                                List.of(
                                        "Uno sbaglio non cancella tutto il resto.",
                                        "Meglio non esporsi mai.",
                                        "Ogni errore dimostra che non sono all'altezza."
                                ),
                                0,
                                "Riesci a vedere l'errore come una parte, non come tutto.",
                                "La Paura ti colpisce: trasformi un singolo errore in una condanna."
                        ),
                        new ChallengeQuestion(
                                "\"Hai già fallito in passato.\"",
                                List.of(
                                        "Quindi fallirò sempre.",
                                        "Anche il passato può insegnarmi qualcosa.",
                                        "È inutile riprovarci."
                                ),
                                1,
                                "Trasformi il passato in esperienza invece che in catena.",
                                "La Paura sorride: lasci che il passato decida anche il futuro."
                        ),
                        new ChallengeQuestion(
                                "\"Sei davvero sicuro di meritare di arrivare fin qui?\"",
                                List.of(
                                        "No, forse sono solo stato fortunato.",
                                        "Non lo so.",
                                        "Lo scoprirò continuando a salire."
                                ),
                                2,
                                "Scegli di continuare anche senza avere tutte le certezze.",
                                "La Paura ti blocca: aspetti una certezza che non arriverà mai."
                        ),
                        new ChallengeQuestion(
                                "La Paura sorride: \"Cos'è davvero il fallimento?\"",
                                List.of(
                                        "Perdere.",
                                        "Essere giudicato.",
                                        "Smettere di provarci."
                                ),
                                2,
                                "La Paura vacilla: capisci che cadere non è la fine.",
                                "La Paura si nutre della tua risposta e diventa più intensa."
                        )
                )
        );
    }
}