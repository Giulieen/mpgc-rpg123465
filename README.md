# Tower of Self RPG

Tower of Self è un progetto Java sviluppato per l'esame di Metodologie di Programmazione / Modellazione e Gestione della Conoscenza.

Il progetto consiste in un RPG psicologico in cui il giocatore esplora una torre composta da piani simbolici, ognuno legato a un aspetto della mente. La versione attuale include due piani giocabili: **I Topi** e **Il Buio**. I piani successivi (Rabbia, Ansia, Solitudine, Speranza, Alter Ego) sono previsti per le prossime iterazioni.

La documentazione dettagliata (responsabilità delle classi, organizzazione dei dati, meccanismi di estensione) è disponibile nella **Wiki** del repository.

---

## Stato del progetto

🚧 In sviluppo attivo — **Piani 1 e 2 giocabili**

---

## Funzionalità implementate

- Menu iniziale con inserimento del nome del giocatore e introduzione narrativa
- Modello del dominio (personaggi, statistiche, oggetti, inventario)
- Piano 1 completo e giocabile: scelta di reazione alla paura dei topi
- Piano 2 completo e giocabile: puzzle a tempo nella stanza buia, con cifre binarie e serratura
- Stato interiore persistente: Lucidità, Stress e memoria delle reazioni che determinano l'alter ego
- Persistenza tramite serializzazione: salvataggio dalla barra di gioco e caricamento dal menu
- Interfaccia grafica JavaFX con tema dedicato (CSS)
- Separazione della logica dalla vista tramite pattern MVC
- Suite di test automatici con JUnit 5

---

## Funzionalità previste

- Aggiunta dei piani successivi (Rabbia, Ansia, Solitudine, Speranza, Alter Ego)
- Nuove prove e reazioni specifiche per ciascun piano
- Persistenza dell'inventario e di uno stato di gioco più ricco
- Ulteriori viste (es. web, mobile) sopra lo stesso controller
- Persistenza alternativa (XML, database)

---

## Roadmap

- [x] Configurazione del progetto
- [x] Modello del dominio
- [x] Sistema di combattimento
- [x] Struttura della torre ed eventi
- [x] Motore di gioco
- [x] Persistenza (serializzazione)
- [x] Interfaccia grafica (JavaFX)
- [x] Separazione MVC
- [x] Menu iniziale e introduzione narrativa
- [x] Meccanica "Confrontati" (package challenge)
- [x] Rifinitura dei Piani 1 e 2
- [x] Test automatici (JUnit 5)
- [ ] Aggiunta dei piani successivi
- [ ] Documentazione completa nella Wiki

---

## Requisiti

- Java 21
- Gradle Wrapper incluso nel progetto

---

## Build

### Windows

```powershell
.\gradlew.bat build
```

### Linux / macOS

```bash
./gradlew build
```

---

## Esecuzione

### Windows

```powershell
.\gradlew.bat run
```

### Linux / macOS

```bash
./gradlew run
```

---

## Test

```bash
./gradlew test
```

---

## Struttura del progetto

```text
it.unicam.cs.mpgc.rpg123465
│
├── domain        // modello: personaggi, statistiche, oggetti, torre, piani
├── fear          // contenuti e reazioni delle paure, inclusa la stanza buia
├── engine        // motore della partita, factory e stato di gioco
├── persistence   // salvataggio/caricamento tramite serializzazione
├── controller    // GameController: fa da tramite tra vista e modello (MVC)
├── ui            // schermate JavaFX (menu, intro, gioco)
└── MainApp       // entry point e navigazione tra schermate
```

---

## Architettura

Il progetto segue il pattern **MVC (Model-View-Controller)**:

- **Model** — i package `domain`, `fear`, `engine` e `persistence` contengono la logica di gioco e non dipendono da alcuna libreria grafica.
- **Controller** — `GameController` coordina eventi, combattimento e persistenza, esponendo alla vista solo messaggi e dati in sola lettura. Non dipende da JavaFX, quindi è riutilizzabile da viste diverse e testabile senza avviare la GUI.
- **View** — `MainApp` coordina le schermate JavaFX e inoltra le azioni dell'utente al controller.

---

## Tecnologie utilizzate

- Java 21
- Gradle
- JavaFX
- JUnit 5
- Git e GitHub

---

## Uso di strumenti di Intelligenza Artificiale

Durante lo sviluppo del progetto è stato utilizzato ChatGPT come supporto per:

- analisi della specifica;
- definizione dell'idea progettuale;
- pianificazione dello sviluppo;
- progettazione dell'architettura software;
- revisione del codice;
- supporto alla documentazione;
- suggerimenti sulle buone pratiche di sviluppo.

Il codice prodotto viene sempre verificato, compreso e validato dallo studente prima di essere integrato nel progetto.

---

## Licenza

Progetto sviluppato esclusivamente a scopo didattico per l'esame universitario.
