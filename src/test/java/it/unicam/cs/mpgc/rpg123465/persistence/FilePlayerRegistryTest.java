package it.unicam.cs.mpgc.rpg123465.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilePlayerRegistryTest {

    @TempDir
    Path temp;

    private Path file() {
        return temp.resolve("players.txt");
    }

    private PlayerRegistry registry() {
        return new FilePlayerRegistry(file().toString());
    }

    @Test
    void unRegistroVuotoNonConosceAlcunNome() {
        assertFalse(registry().isTaken("Giulia"));
    }

    @Test
    void unNomeRegistratoRisultaOccupato() {
        PlayerRegistry registry = registry();

        registry.register("Giulia");

        assertTrue(registry.isTaken("Giulia"));
    }

    @Test
    void ilNomeSopravviveAUnNuovoRegistro() {
        registry().register("Giulia");

        assertTrue(registry().isTaken("Giulia"));
    }

    @Test
    void nomiDiversiConvivono() {
        PlayerRegistry registry = registry();

        registry.register("Giulia");
        registry.register("Marco");

        assertTrue(registry.isTaken("Giulia"));
        assertTrue(registry.isTaken("Marco"));
        assertFalse(registry.isTaken("Anna"));
    }

    /**
     * «Giulia» e « giulia » sono la stessa persona per chiunque tranne che per
     * {@code equals}: il confronto ignora maiuscole e spazi ai bordi.
     */
    @Test
    void ilConfrontoIgnoraMaiuscoleESpazi() {
        PlayerRegistry registry = registry();

        registry.register("Giulia");

        assertTrue(registry.isTaken("giulia"));
        assertTrue(registry.isTaken("GIULIA"));
        assertTrue(registry.isTaken("  Giulia  "));
    }

    @Test
    void registrareDueVolteNonDuplicaLaRiga() throws Exception {
        PlayerRegistry registry = registry();

        registry.register("Giulia");
        registry.register("giulia");
        registry.register("  GIULIA ");

        assertEquals(1, Files.readAllLines(file(), StandardCharsets.UTF_8)
                .stream().filter(line -> !line.isBlank()).count());
    }

    /** Il file conserva il nome come è stato scritto, non la chiave normalizzata. */
    @Test
    void ilNomeVieneConservatoNellaFormaOriginale() throws Exception {
        registry().register("Giulìa D'Amico");

        assertTrue(Files.readString(file(), StandardCharsets.UTF_8)
                .contains("Giulìa D'Amico"));
    }

    @Test
    void unNomeVuotoNonVieneRegistrato() throws Exception {
        PlayerRegistry registry = registry();

        registry.register("");
        registry.register("   ");
        registry.register(null);

        assertFalse(Files.exists(file()));
        assertFalse(registry.isTaken(""));
    }

    @Test
    void unRegistroConRigheVuoteVieneLettoCorrettamente() throws Exception {
        Files.writeString(file(), "\n\nGiulia\n\n  \nMarco\n", StandardCharsets.UTF_8);

        PlayerRegistry registry = registry();

        assertTrue(registry.isTaken("Giulia"));
        assertTrue(registry.isTaken("Marco"));
        assertFalse(registry.isTaken(""));
    }

    @Test
    void unPercorsoVuotoNonEAmmesso() {
        assertThrows(IllegalArgumentException.class, () -> new FilePlayerRegistry(""));
        assertThrows(IllegalArgumentException.class, () -> new FilePlayerRegistry(null));
    }
}
