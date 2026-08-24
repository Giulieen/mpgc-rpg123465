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

- **Java 25** (LTS)
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

Durante lo sviluppo ho usato **ChatGPT** come supporto, in questi ambiti:

- **analisi della specifica**: chiarimenti e confronto sui requisiti;
- **architettura**: proposte di suddivisione in package e di interfacce, che ho
  valutato e adattato;
- **codice**: generazione di porzioni su mia indicazione, sempre lette e
  verificate prima di integrarle;
- **revisione**: segnalazione di duplicazioni e di violazioni dei principi SOLID;
- **test**: proposte di casi di prova, verificate eseguendole;
- **documentazione**: supporto alla stesura dei testi.

L'ideazione del gioco è mia: i piani, le meccaniche, i dilemmi, i profili e
tutti i testi narrativi. Sono mie anche le decisioni di progettazione, e la
cronologia dei commit ne conserva i passaggi.
