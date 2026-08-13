package it.unicam.cs.mpgc.rpg123465.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerProfileTest {

    @Test
    void ogniProfiloHaUnNomeNonVuoto() {
        for (PlayerProfile profilo : PlayerProfile.values()) {
            assertNotNull(profilo.getNome(), profilo.name());
            assertFalse(profilo.getNome().isBlank(), profilo.name());
        }
    }

    @Test
    void ogniProfiloHaUnaDescrizioneNonVuota() {
        for (PlayerProfile profilo : PlayerProfile.values()) {
            assertNotNull(profilo.getDescrizione(), profilo.name());
            assertFalse(profilo.getDescrizione().isBlank(), profilo.name());
        }
    }

    /**
     * Due profili con lo stesso testo indicherebbero un copia-incolla non
     * completato, non una scelta narrativa.
     */
    @Test
    void nessunProfiloCondivideNomeODescrizioneConUnAltro() {
        Set<String> nomi = new HashSet<>();
        Set<String> descrizioni = new HashSet<>();

        for (PlayerProfile profilo : PlayerProfile.values()) {
            assertTrue(nomi.add(profilo.getNome()),
                    "nome ripetuto: " + profilo.getNome());
            assertTrue(descrizioni.add(profilo.getDescrizione()),
                    "descrizione ripetuta in " + profilo.name());
        }
    }

    /**
     * L'algoritmo di {@link MindState#profile()} può restituire uno qualsiasi
     * dei sette valori: se l'enum cambiasse dimensione senza aggiornare le
     * regole, resterebbero profili irraggiungibili.
     */
    @Test
    void esistonoSetteProfili() {
        assertEquals(7, PlayerProfile.values().length);
    }

    @Test
    void tuttiIProfiliSonoRaggiungibiliDaQualcheCombinazione() {
        Set<PlayerProfile> raggiunti = new HashSet<>();

        for (int coraggio = 0; coraggio <= 8; coraggio++) {
            for (int curiosita = 0; curiosita <= 8 - coraggio; curiosita++) {
                MindState mind = new MindState();

                for (int i = 0; i < coraggio; i++) {
                    mind.registerTrait(ProfileTrait.CORAGGIO);
                }
                for (int i = 0; i < curiosita; i++) {
                    mind.registerTrait(ProfileTrait.CURIOSITA);
                }
                for (int i = 0; i < 8 - coraggio - curiosita; i++) {
                    mind.registerTrait(ProfileTrait.AVVENTURA);
                }

                raggiunti.add(mind.profile());
            }
        }

        assertEquals(
                new HashSet<>(Arrays.asList(PlayerProfile.values())),
                raggiunti,
                "alcuni profili non sono ottenibili con otto risposte");
    }
}
