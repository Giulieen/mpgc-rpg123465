package it.unicam.cs.mpgc.rpg123465.persistence;

import java.io.IOException;

/**
 * Definisce le operazioni per salvare e caricare una partita.
 */
public interface SaveManager {

    /**
     * Salva i dati della partita.
     *
     * @param gameSave dati da salvare
     * @throws IOException se si verifica un errore durante il salvataggio
     */
    void save(GameSave gameSave) throws IOException;

    /**
     * Carica i dati della partita.
     *
     * @return dati della partita salvata
     * @throws IOException se si verifica un errore durante il caricamento
     * @throws ClassNotFoundException se il file contiene dati non compatibili
     */
    GameSave load() throws IOException, ClassNotFoundException;

    /**
     * Verifica se è presente una partita salvata.
     *
     * @return {@code true} se esiste un salvataggio da poter caricare
     */
    boolean exists();
}