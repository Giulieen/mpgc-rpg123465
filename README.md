# 📌 Tower of Self

RPG psicologico a piani, scritto in Java con JavaFX.

Il giocatore sale una Torre: ogni piano è una prova diversa — riflessi, memoria,
equilibrio — e fra una prova e l'altra deve rispondere a dei dilemmi che non
hanno una risposta giusta. Le prove misurano le sue abilità, le domande misurano
lui: dalle risposte emerge alla fine uno di sette profili.

Questa è la **prima release**: tre piani giocabili e sette profili
raggiungibili, una demo della Torre immaginata. La schermata finale mostra per
questo la *direzione* verso cui il profilo si sta orientando, non un verdetto.

Documentazione completa nella **[Wiki](../../wiki)**.
Asset di terze parti e relative licenze in **[CREDITS.md](CREDITS.md)**.

---

## 🚀 Come eseguire il progetto

### Prerequisiti

- **Un JDK installato**, dalla 21 alla 25: serve ad avviare Gradle 9, che non
  supporta ancora versioni successive
- Il codice viene compilato con **Java 25 (LTS)**, che Gradle scarica da sé alla
  prima compilazione se non è già presente
- **Gradle** — non serve installarlo, il progetto include il wrapper

### Istruzioni

Clonare il repository:

```bash
git clone https://github.com/Giulieen/mpgc-rpg123465.git
cd mpgc-rpg123465
```

### Build del progetto

```bash
./gradlew build
```

### Esecuzione

```bash
./gradlew run
```

Su Windows usare `gradlew.bat` al posto di `./gradlew`.

---

## 🤖 Uso di strumenti di AI

Durante lo sviluppo è stato utilizzato **ChatGPT** come supporto, in questi
ambiti:

- **analisi della specifica**: chiarimenti e confronto sui requisiti;
- **architettura**: proposte di suddivisione in package e di interfacce,
  valutate e adattate prima dell'adozione;
- **codice**: generazione di porzioni su indicazione precisa, sempre lette e
  verificate prima dell'integrazione;
- **revisione**: segnalazione di duplicazioni e di violazioni dei principi SOLID;
- **test**: proposte di casi di prova, verificate eseguendole;
- **documentazione**: supporto alla stesura dei testi.

La dichiarazione dettagliata, richiesta dalla specifica, è nella
[Wiki](../../wiki/Uso-di-strumenti-di-AI).

L'ideazione del gioco — piani, meccaniche, dilemmi, profili e testi narrativi —
e le scelte di progettazione sono state svolte in autonomia; gli strumenti di AI
hanno avuto un ruolo di supporto alla realizzazione. La cronologia dei commit ne
documenta i passaggi.
