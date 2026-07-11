package it.unicam.cs.mpgc.rpg123465.domain;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Lo stato interiore del protagonista: ciò che la Torre gli fa, e ciò che lui
 * diventa.
 * <p>
 * Tiene insieme due cose di natura diversa:
 * <ul>
 *   <li><b>gli stati</b> ({@code lucidita}, {@code stress}), che salgono e
 *       scendono di continuo mentre si gioca;</li>
 *   <li><b>i contatori delle scelte</b>, che <em>non si azzerano mai</em> e da
 *       cui, in cima alla Torre, si forma l'{@link AlterEgo}.</li>
 * </ul>
 * Vive nel dominio e non nell'interfaccia: è ciò che permette al gioco di
 * ricordare, da un piano all'altro, chi è diventato il protagonista.
 */
public class MindState implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Valore massimo di Lucidità e Stress. */
    public static final int MAX = 100;

    /** Quanta Lucidità si recupera sulle scale, fra un piano e l'altro. */
    private static final int RIPOSO_LUCIDITA = 20;

    /** Quanto Stress si scarica sulle scale, fra un piano e l'altro. */
    private static final int RIPOSO_STRESS = 25;

    /**
     * Quota minima, in percentuale sul totale delle scelte, perché un
     * atteggiamento si dica dominante.
     * <p>
     * È una proporzione e non uno scarto secco: così la regola vale sia dopo
     * poche scelte sia a fine partita. Con quattro atteggiamenti, chi scegliesse
     * a caso si fermerebbe intorno al 25%: sopra il 45% c'è una vera inclinazione.
     * Sotto, nessuna forma prevale e l'alter ego è il Riflesso.
     */
    private static final int QUOTA_DOMINANZA = 45;

    private int lucidita = MAX;
    private int stress = 0;
    private final Map<Attitude, Integer> scelte = new EnumMap<>(Attitude.class);

    // --- Stati --------------------------------------------------------------

    public int getLucidita() {
        return lucidita;
    }

    public int getStress() {
        return stress;
    }

    /** Il protagonista ha ceduto: la Torre ha avuto la meglio. */
    public boolean isSopraffatto() {
        return lucidita <= 0;
    }

    /**
     * Entrare in una stanza aggiunge tensione a quella che ci si porta già
     * dietro dai piani precedenti.
     *
     * @param tensione stress che la stanza incute all'ingresso
     */
    public void enterRoom(int tensione) {
        stress = clamp(stress + tensione);
    }

    /**
     * Le scale fra un piano e l'altro: si tira il fiato, ma non si guarisce.
     * Le conseguenze delle scelte pesano ancora, semplicemente non condannano.
     */
    public void rest() {
        lucidita = clamp(lucidita + RIPOSO_LUCIDITA);
        stress = clamp(stress - RIPOSO_STRESS);
    }

    // --- Scelte -------------------------------------------------------------

    /**
     * Applica una reazione: sposta gli stati e registra per sempre
     * l'atteggiamento con cui il giocatore ha risposto alla paura.
     *
     * @param lucidityDelta variazione di Lucidità
     * @param stressDelta   variazione di Stress
     * @param attitude      atteggiamento della reazione
     */
    public void apply(int lucidityDelta, int stressDelta, Attitude attitude) {
        if (attitude == null) {
            throw new IllegalArgumentException("L'atteggiamento non può essere null.");
        }

        lucidita = clamp(lucidita + lucidityDelta);
        stress = clamp(stress + stressDelta);
        scelte.merge(attitude, 1, Integer::sum);
    }

    /**
     * @return quante volte il giocatore ha reagito con questo atteggiamento
     */
    public int count(Attitude attitude) {
        return scelte.getOrDefault(attitude, 0);
    }

    /** @return quante scelte sono state compiute in tutto */
    public int getTotalChoices() {
        int totale = 0;
        for (Attitude attitude : Attitude.values()) {
            totale += count(attitude);
        }
        return totale;
    }

    // --- Tratti (letture derivate dai contatori) -----------------------------

    /** Affrontare la paura senza scappare: resistere o accoglierla, non fuggirla. */
    public int getCoraggio() {
        return Math.max(0, count(Attitude.RESISTI)
                + count(Attitude.ACCOGLI)
                - count(Attitude.FUGGI));
    }

    /** Reagire senza violenza, avvicinandosi a ciò che si teme. */
    public int getEmpatia() {
        return count(Attitude.ACCOGLI);
    }

    /** Agire d'istinto, colpendo prima di capire. */
    public int getImpulsivita() {
        return count(Attitude.AGGREDISCI);
    }

    // --- Alter ego ----------------------------------------------------------

    /**
     * La forma che Chimeris assumerà: l'atteggiamento dominante del percorso,
     * oppure il {@link AlterEgo#RIFLESSO} se nessuno ha davvero prevalso.
     * <p>
     * Perché un atteggiamento prevalga deve essere l'unico più frequente e
     * coprire almeno la {@link #QUOTA_DOMINANZA} delle scelte compiute. Chi si
     * è distribuito fra più modi di reagire — o li ha usati in egual misura —
     * non incontra un mostro: incontra il Riflesso.
     *
     * @return l'alter ego costruito dalle scelte compiute finora
     */
    public AlterEgo alterEgo() {
        Attitude dominante = null;
        int primo = 0;
        int secondo = 0;

        for (Attitude attitude : Attitude.values()) {
            int quante = count(attitude);
            if (quante > primo) {
                secondo = primo;
                primo = quante;
                dominante = attitude;
            } else if (quante > secondo) {
                secondo = quante;
            }
        }

        boolean nessunaScelta = dominante == null || primo == 0;
        boolean pareggio = primo == secondo;
        boolean inclinazioneDebole = primo * 100 < QUOTA_DOMINANZA * getTotalChoices();

        if (nessunaScelta || pareggio || inclinazioneDebole) {
            return AlterEgo.RIFLESSO;
        }

        return switch (dominante) {
            case AGGREDISCI -> AlterEgo.BESTIA;
            case FUGGI -> AlterEgo.OMBRA;
            case RESISTI -> AlterEgo.CUSTODE;
            case ACCOGLI -> AlterEgo.MEDIATORE;
        };
    }

    // --- Persistenza --------------------------------------------------------

    /**
     * Ripristina questo stato da quello di una partita salvata.
     *
     * @param salvato stato caricato dal salvataggio
     */
    public void restoreFrom(MindState salvato) {
        if (salvato == null) {
            return;
        }

        this.lucidita = clamp(salvato.lucidita);
        this.stress = clamp(salvato.stress);
        this.scelte.clear();
        this.scelte.putAll(salvato.scelte);
    }

    private int clamp(int valore) {
        return Math.max(0, Math.min(MAX, valore));
    }
}
