package it.unicam.cs.mpgc.rpg123465.questions;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Repository delle domande salvate in un file JSON nel classpath.
 */
public final class JsonQuestionRepository
        implements QuestionRepository {

    public static final String DEFAULT_RESOURCE =
            "/data/questions.json";

    private final Map<String, List<Dilemma>> questions;

    /**
     * Carica il catalogo predefinito.
     */
    public JsonQuestionRepository() {
        this(DEFAULT_RESOURCE);
    }

    /**
     * @param resourcePath percorso del JSON nel classpath
     */
    public JsonQuestionRepository(
            String resourcePath
    ) {
        if (resourcePath == null
                || resourcePath.isBlank()) {

            throw new IllegalArgumentException(
                    "Il percorso del catalogo non può essere vuoto."
            );
        }

        questions =
                load(
                        resourcePath
                );
    }

    @Override
    public List<Dilemma> randomQuestions(
            String category,
            int count
    ) {
        if (category == null
                || category.isBlank()) {

            throw new IllegalArgumentException(
                    "La categoria non può essere vuota."
            );
        }

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Il numero di domande deve essere positivo."
            );
        }

        String key =
                category
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        List<Dilemma> source =
                questions.get(
                        key
                );

        if (source == null) {
            throw new QuestionCatalogException(
                    "Categoria di domande sconosciuta: "
                            + category
            );
        }

        if (source.size() < count) {
            throw new QuestionCatalogException(
                    "La categoria "
                            + category
                            + " contiene "
                            + source.size()
                            + " domande, ma ne sono state richieste "
                            + count
                            + "."
            );
        }

        List<Dilemma> shuffled =
                new ArrayList<>(
                        source
                );

        Collections.shuffle(
                shuffled
        );

        return List.copyOf(
                shuffled.subList(
                        0,
                        count
                )
        );
    }

    private Map<String, List<Dilemma>> load(
            String resourcePath
    ) {
        InputStream stream =
                JsonQuestionRepository.class
                        .getResourceAsStream(
                                resourcePath
                        );

        if (stream == null) {
            throw new QuestionCatalogException(
                    "Catalogo delle domande non trovato: "
                            + resourcePath
            );
        }

        Type type =
                new TypeToken<
                        Map<String, List<Dilemma>>
                        >() {
                }.getType();

        try (
                InputStreamReader reader =
                        new InputStreamReader(
                                stream,
                                StandardCharsets.UTF_8
                        )
        ) {
            Map<String, List<Dilemma>> loaded =
                    new Gson().fromJson(
                            reader,
                            type
                    );

            if (loaded == null
                    || loaded.isEmpty()) {

                throw new QuestionCatalogException(
                        "Il catalogo delle domande è vuoto: "
                                + resourcePath
                );
            }

            return Map.copyOf(
                    loaded
            );

        } catch (QuestionCatalogException exception) {
            throw exception;

        } catch (IOException exception) {
            throw new QuestionCatalogException(
                    "Impossibile leggere il catalogo delle domande: "
                            + resourcePath,
                    exception
            );

        } catch (RuntimeException exception) {
            /*
             * Qui dentro succede una cosa sola: Gson legge il file. Qualunque
             * eccezione non controllata riguarda quindi i dati, non il codice
             * — JsonSyntaxException per una virgola di troppo, ma anche le
             * validazioni di Dilemma e DilemmaOption, che Gson esegue quando
             * costruisce i record e che scattano per un tratto sconosciuto o
             * una risposta mancante.
             *
             * Vengono tradotte tutte in un errore del catalogo, conservando la
             * causa: è l'unico modo per risalire alla riga da correggere.
             */
            throw new QuestionCatalogException(
                    "Il catalogo delle domande non è valido: "
                            + resourcePath
                            + " ("
                            + exception.getMessage()
                            + ")",
                    exception
            );
        }
    }
}
