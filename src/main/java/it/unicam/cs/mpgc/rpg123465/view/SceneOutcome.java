package it.unicam.cs.mpgc.rpg123465.view;

/**
 * Come prosegue il cammino una volta conclusa una scena.
 */
public enum SceneOutcome {

    /**
     * Si passa alla scena successiva; se era l'ultima, il piano è superato.
     */
    NEXT,

    /**
     * Si ricomincia la stessa scena, ricostruendola da capo.
     * <p>
     * Nessuna scena lo usa al momento: i piani che si ripetono — la serratura,
     * la traversata, il labirinto — gestiscono il proprio ritentativo al loro
     * interno, perché devono conservare le risposte già date. Resta qui per le
     * scene che dovessero invece ripartire davvero da zero.
     */
    REPEAT
}
