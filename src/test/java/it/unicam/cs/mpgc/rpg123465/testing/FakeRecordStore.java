package it.unicam.cs.mpgc.rpg123465.testing;

import it.unicam.cs.mpgc.rpg123465.persistence.RecordStore;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

/** Archivio dei record in memoria, che conta anche quante volte ha scritto. */
public final class FakeRecordStore implements RecordStore {

    private final Map<String, Integer> values = new HashMap<>();

    private int writes;

    /** @return quante scritture ha ricevuto l'archivio */
    public int writes() {
        return writes;
    }

    @Override
    public OptionalInt best(String key) {
        Integer value = values.get(key);

        return value == null
                ? OptionalInt.empty()
                : OptionalInt.of(value);
    }

    @Override
    public void save(String key, int value) {
        values.put(key, value);
        writes++;
    }
}
