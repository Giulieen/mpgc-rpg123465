# Tower of Self

RPG psicologico a piani, sviluppato per l'esame di **Metodologie di Programmazione / Modellazione e Gestione della Conoscenza** (AA 2025/26).

Il giocatore sale una Torre di tre piani. Ogni piano è una prova diversa — riflessi, memoria, equilibrio — e fra una prova e l'altra pone domande senza risposta giusta. **Le prove misurano le abilità; le domande misurano il giocatore**: dalle risposte, e solo da quelle, emerge alla fine uno di sette profili.

La documentazione completa — responsabilità delle classi, organizzazione dei dati, meccanismi di estensione — è nella **[Wiki](../../wiki)** del repository.

---

## Requisiti

- **Java 21**
- Nient'altro: Gradle arriva con il wrapper incluso nel repository.

## Compilare ed eseguire

```bash
./gradlew build     # compila ed esegue i test
./gradlew run       # avvia il gioco
```

Su Windows: `.\gradlew.bat build` e `.\gradlew.bat run`.

---

## I tre piani

| piano | prova | comandi | misura |
| --- | --- | --- | --- |
| **I — I Topi** | catturare 12 topi prima che raggiungano le uscite | frecce direzionali | topi presi, tempo |
| **II — Il Buio** | trovare al buio le cifre sulle pareti e comporre la combinazione in 60 secondi | mouse | tempo impiegato |
| **III — Le Altezze** | attraversare tre ponti reagendo alle frecce senza perdere l'equilibrio | frecce direzionali | punteggio, errori |

Ogni piano conserva il proprio **record personale**, che sopravvive a «Nuova partita».

## Il profilo

Le risposte ai dilemmi alimentano tre tratti nascosti — **Coraggio**, **Curiosità**, **Avventura** — che il giocatore non vede mai. Dal loro equilibrio nasce uno di sette profili: *Il Coraggioso, Il Curioso, L'Avventuriero, L'Esploratore, Il Risoluto, Il Visionario, L'Imprevedibile*.

Ciò che **non** influisce sul profilo: punteggi, record, tentativi persi e cadute. Sbagliare una prova non dice nulla su chi sta giocando, ed è verificato dai test.

## I tentativi

La Torre concede **tre tentativi** complessivi. Si perdono cadendo, sbagliando la combinazione o lasciando fuggire troppi topi. Esauriti, la prova del piano ricomincia da capo — ma le domande già risposte non vengono riproposte, così il profilo non viene mai conteggiato due volte.

---

## Architettura

Il progetto segue **MVC** con una regola precisa, verificabile a colpo d'occhio:

> i package `domain`, `engine`, `controller`, `persistence` e `questions` **non contengono un solo import JavaFX**.

È ciò che rende il progetto pronto per altre viste — web, mobile — sopra lo stesso motore e lo stesso controller.

```text
it.unicam.cs.mpgc.rpg123465
├── domain        modello: giocatore, tratti, profili, Torre, piani, tentativi
├── engine        motore della partita e fabbrica della Torre
├── controller    GameController: unico tramite fra viste e modello
├── questions     catalogo dei dilemmi e sequenza di avanzamento
├── persistence   salvataggi, record delle prove, registro dei nomi
├── floors        i tre piani: encounter (topi), buio, altezze
├── ui            schermate JavaFX
│   └── support   componenti grafici riusabili fra i piani
├── audio         suoni e ambienti
└── app           Navigator: composizione e passaggio fra le schermate
```

### Estendibilità

- **Aggiungere un piano** significa scrivere un `FloorContent` e una `FloorScene`, e registrarli in `FloorSceneFactory`. Il motore e il controller non cambiano.
- **Cambiare persistenza** significa implementare `SaveManager`, `RecordStore` o `PlayerRegistry`. Il gioco dipende dalle interfacce, mai dai file: passare a XML o a un database non tocca una riga di gioco.
- **Cambiare catalogo di domande** significa implementare `QuestionRepository`, iniettato tramite costruttore.

## Persistenza

| dato | dove | come |
| --- | --- | --- |
| partita in corso | `saves/save.dat` | serializzazione Java |
| record delle prove | `saves/records.properties` | file properties |
| nomi già usati | `saves/players.txt` | file di testo |
| catalogo dei dilemmi | `resources/data/questions.json` | JSON (Gson) |

Il salvataggio fotografa **l'ingresso del piano**, non l'istante in cui si preme Salva: ricaricare riporta all'inizio della prova, mai a metà.

## Test

```bash
./gradlew test
```

**272 test** con JUnit 5, tutti eseguibili senza avviare l'interfaccia grafica. Coprono dominio, motore, controller, persistenza, catalogo delle domande e le regole di gioco estratte dalle scene (sequenza dei dilemmi, prova delle frecce, percorso fra i ponti).

---

## Tecnologie

Java 21 · Gradle · JavaFX 21.0.2 · Gson · Ikonli · JUnit 5 · Git e GitHub

## Asset

Grafica e suoni di terze parti sono elencati con autore e licenza in **[CREDITS.md](CREDITS.md)**.

## Uso di strumenti di Intelligenza Artificiale

Durante lo sviluppo è stato utilizzato **ChatGPT** come supporto per:

- analisi della specifica e definizione dell'idea progettuale;
- progettazione dell'architettura e revisione del codice;
- pianificazione dello sviluppo e supporto alla documentazione;
- suggerimenti sulle buone pratiche di programmazione.

Il codice prodotto è stato sempre letto, compreso e verificato prima di essere integrato. La dichiarazione dettagliata è nella [Wiki](../../wiki).

## Licenza

Progetto realizzato a scopo didattico per un esame universitario.
