package it.unicam.cs.mpgc.rpg123465.questions;

import it.unicam.cs.mpgc.rpg123465.domain.ProfileTrait;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DilemmaSequenceTest {

    private static Dilemma dilemma(int id) {
        return new Dilemma(
                id,
                "domanda " + id,
                new DilemmaOption("prima " + id, ProfileTrait.CORAGGIO),
                new DilemmaOption("seconda " + id, ProfileTrait.CURIOSITA));
    }

    private static List<Dilemma> dilemmas(int count) {
        List<Dilemma> list = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            list.add(dilemma(i));
        }

        return list;
    }

    // --- avanzamento -----------------------------------------------------

    @Test
    void unaSequenzaNuovaPartePrimoDilemmaConNessunoRisolto() {
        DilemmaSequence sequence = new DilemmaSequence(dilemmas(3));

        assertTrue(sequence.hasNext());
        assertEquals(0, sequence.resolvedCount());
        assertEquals(dilemma(1), sequence.current());
    }

    @Test
    void risolvereAvanzaAlDilemmaSuccessivo() {
        DilemmaSequence sequence = new DilemmaSequence(dilemmas(3));

        assertTrue(sequence.resolve(sequence.current()));

        assertEquals(1, sequence.resolvedCount());
        assertEquals(dilemma(2), sequence.current());
    }

    /** L'ordine del catalogo è l'ordine in cui le domande vengono poste. */
    @Test
    void lOrdineDellaListaVienePreservato() {
        DilemmaSequence sequence = new DilemmaSequence(dilemmas(4));

        List<Integer> visti = new ArrayList<>();

        while (sequence.hasNext()) {
            Dilemma corrente = sequence.current();
            visti.add(corrente.id());
            sequence.resolve(corrente);
        }

        assertEquals(List.of(1, 2, 3, 4), visti);
    }

    @Test
    void interrogareIlCorrenteNonLoConsuma() {
        DilemmaSequence sequence = new DilemmaSequence(dilemmas(3));

        assertSame(sequence.current(), sequence.current());
        assertEquals(0, sequence.resolvedCount());
    }

    // --- esaurimento -----------------------------------------------------

    @Test
    void unaSequenzaDiUnSoloDilemmaSiEsaurisceDopoUnaRisposta() {
        DilemmaSequence sequence = new DilemmaSequence(dilemmas(1));

        sequence.resolve(sequence.current());

        assertFalse(sequence.hasNext());
        assertEquals(1, sequence.resolvedCount());
    }

    @Test
    void tuttiIDilemmiVengonoConsumatiUnaVoltaCiascuno() {
        for (int quanti : new int[] {1, 3, 4}) {
            DilemmaSequence sequence = new DilemmaSequence(dilemmas(quanti));

            while (sequence.hasNext()) {
                sequence.resolve(sequence.current());
            }

            assertEquals(quanti, sequence.resolvedCount());
        }
    }

    @Test
    void chiedereIlCorrenteAlleSequenzaEsauritaSollevaEccezione() {
        DilemmaSequence sequence = new DilemmaSequence(dilemmas(1));

        sequence.resolve(sequence.current());

        assertThrows(IllegalStateException.class, sequence::current);
    }

    /** Una lista vuota è ammessa: nasce una sequenza già esaurita. */
    @Test
    void unaSequenzaVuotaNasceEsaurita() {
        DilemmaSequence sequence = new DilemmaSequence(List.of());

        assertFalse(sequence.hasNext());
        assertEquals(0, sequence.resolvedCount());
        assertThrows(IllegalStateException.class, sequence::current);
    }

    @Test
    void risolvereUnaSequenzaEsauritaNonFaNulla() {
        DilemmaSequence sequence = new DilemmaSequence(dilemmas(1));

        Dilemma unico = sequence.current();
        sequence.resolve(unico);

        assertFalse(sequence.resolve(unico));
        assertEquals(1, sequence.resolvedCount());
    }

    // --- doppia risoluzione ----------------------------------------------

    /**
     * È l'invariante centrale: rispondere due volte allo stesso dilemma non
     * deve far avanzare l'indice due volte, altrimenti una domanda verrebbe
     * saltata e una scelta conteggiata due volte.
     */
    @Test
    void lStessoDilemmaNonPuoEssereRisoltoDueVolte() {
        DilemmaSequence sequence = new DilemmaSequence(dilemmas(3));

        Dilemma primo = sequence.current();

        assertTrue(sequence.resolve(primo));
        assertFalse(sequence.resolve(primo));

        assertEquals(1, sequence.resolvedCount());
        assertEquals(dilemma(2), sequence.current());
    }

    @Test
    void risolvereUnDilemmaNonCorrenteNonAvanza() {
        DilemmaSequence sequence = new DilemmaSequence(dilemmas(3));

        assertFalse(sequence.resolve(dilemma(3)));
        assertFalse(sequence.resolve(null));

        assertEquals(0, sequence.resolvedCount());
    }

    // --- costruzione -----------------------------------------------------

    @Test
    void laSequenzaRifiutaUnaListaNullOConElementiNull() {
        assertThrows(IllegalArgumentException.class, () -> new DilemmaSequence(null));

        List<Dilemma> conNull = new ArrayList<>();
        conNull.add(dilemma(1));
        conNull.add(null);

        assertThrows(IllegalArgumentException.class, () -> new DilemmaSequence(conNull));
    }

    /**
     * La sequenza copia la lista ricevuta: chi l'ha costruita non deve poterne
     * cambiare il contenuto a partita iniziata.
     */
    @Test
    void modificareLaListaOriginaleNonAlteraLaSequenza() {
        List<Dilemma> originale = dilemmas(2);

        DilemmaSequence sequence = new DilemmaSequence(originale);

        originale.clear();
        originale.add(dilemma(99));

        assertTrue(sequence.hasNext());
        assertEquals(dilemma(1), sequence.current());

        sequence.resolve(sequence.current());

        assertEquals(dilemma(2), sequence.current());
    }
}
