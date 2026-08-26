# 📌 Tower of Self

RPG psicologico a piani, scritto in Java con JavaFX.

Il giocatore sale una Torre: ogni piano è una prova diversa — riflessi, memoria,
equilibrio — e fra una prova e l'altra deve rispondere a dei dilemmi che non
hanno una risposta giusta. Le prove misurano le sue abilità, le domande misurano
lui: dalle risposte emerge alla fine uno di sette profili.

Documentazione completa nella **[Wiki](../../wiki)**.
Asset di terze parti e relative licenze in **[CREDITS.md](CREDITS.md)**.

---

## 🚀 Come eseguire il progetto

### Prerequisiti

- **Java 25** (LTS) — se manca, Gradle lo scarica da sé alla prima compilazione
- **Gradle** — non serve installarlo, il progetto include il wrapper

### Istruzioni

Clonare il repository:

```bash
git clone <url-del-repository>
cd tower-of-self
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

In una sessione finale di revisione è stato utilizzato **Claude Code**, per il
confronto del progetto con la specifica d'esame e per applicare le correzioni
concordate. Il dettaglio è nella
[Wiki](../../wiki/Uso-di-strumenti-di-AI).

L'ideazione del gioco — piani, meccaniche, dilemmi, profili e testi narrativi —
e le scelte di progettazione sono state svolte in autonomia; gli strumenti di AI
hanno avuto un ruolo di supporto alla realizzazione. La cronologia dei commit ne
documenta i passaggi.
