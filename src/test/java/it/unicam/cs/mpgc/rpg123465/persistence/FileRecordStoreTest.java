package it.unicam.cs.mpgc.rpg123465.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileRecordStoreTest {

    private static final String BUIO = "buio.tempo";
    private static final String ALTEZZE = "altezze.punteggio";

    @TempDir
    Path temp;

    private RecordStore store() {
        return new FileRecordStore(temp.resolve("records.properties").toString());
    }

    @Test
    void allInizioNonEsisteAlcunRecord() {
        assertTrue(store().best(BUIO).isEmpty());
    }

    @Test
    void unRecordSalvatoSiRilegge() {
        RecordStore store = store();

        store.save(BUIO, 24);

        assertEquals(OptionalInt.of(24), store.best(BUIO));
    }

    @Test
    void ilRecordSopravviveAUnNuovoArchivio() {
        store().save(BUIO, 19);

        assertEquals(OptionalInt.of(19), store().best(BUIO));
    }

    @Test
    void salvareDiNuovoSostituisceIlValorePrecedente() {
        RecordStore store = store();

        store.save(BUIO, 30);
        store.save(BUIO, 21);

        assertEquals(OptionalInt.of(21), store.best(BUIO));
    }

    /**
     * L'archivio conserva e basta: non sa che per il tempo vince il valore più
     * basso e per il punteggio il più alto. Il confronto appartiene al piano.
     */
    @Test
    void lArchivioAccettaAnchePeggioramentiSenzaGiudicare() {
        RecordStore store = store();

        store.save(BUIO, 20);
        store.save(BUIO, 45);

        assertEquals(OptionalInt.of(45), store.best(BUIO));
    }

    @Test
    void chiaviDiverseNonSiInfluenzano() {
        RecordStore store = store();

        store.save(BUIO, 24);
        store.save(ALTEZZE, 1300);

        assertEquals(OptionalInt.of(24), store.best(BUIO));
        assertEquals(OptionalInt.of(1300), store.best(ALTEZZE));
    }

    @Test
    void unaChiaveMaiUsataRestaVuotaAncheConAltriRecordPresenti() {
        RecordStore store = store();

        store.save(BUIO, 24);

        assertTrue(store.best("piano.inesistente").isEmpty());
    }

    @Test
    void unaRigaRovinataValeComeRecordMaiStabilito() throws Exception {
        Path path = temp.resolve("records.properties");
        Files.writeString(path, BUIO + "=non-un-numero\n", StandardCharsets.UTF_8);

        assertTrue(store().best(BUIO).isEmpty());
    }

    /**
     * I record sono una comodità: un archivio illeggibile non deve impedire di
     * giocare, quindi la lettura ricade sul «nessun record».
     */
    @Test
    void unArchivioIllegibileNonSollevaEccezioni() throws Exception {
        Path path = temp.resolve("records.properties");
        Files.write(path, new byte[] {(byte) 0xFF, (byte) 0xFE, 0x00});

        assertFalse(store().best(BUIO).isPresent());
    }

    @Test
    void salvareSuUnArchivioRovinatoLoRimetteInSesto() throws Exception {
        Path path = temp.resolve("records.properties");
        Files.writeString(path, BUIO + "=guasto\n", StandardCharsets.UTF_8);

        RecordStore store = store();
        store.save(BUIO, 18);

        assertEquals(OptionalInt.of(18), store.best(BUIO));
    }

    @Test
    void unaChiaveVuotaNonEAmmessa() {
        RecordStore store = store();

        assertThrows(IllegalArgumentException.class, () -> store.best(""));
        assertThrows(IllegalArgumentException.class, () -> store.save(null, 1));
    }

    @Test
    void unPercorsoVuotoNonEAmmesso() {
        assertThrows(IllegalArgumentException.class, () -> new FileRecordStore(""));
    }
}
