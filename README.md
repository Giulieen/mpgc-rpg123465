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

- **Un JDK installato**, dalla 21 alla 25: serve soltanto ad avviare il wrapper
  di Gradle
- **Nient'altro.** Gradle, il JDK 25 con cui il progetto viene compilato e
  JavaFX 25 li scarica il wrapper alla prima esecuzione

Non serve installare Gradle, non serve scaricare l'SDK di JavaFX e non serve
impostare `JAVA_HOME` sul JDK 25: il file `gradle/gradle-daemon-jvm.properties`
dice a Gradle di procurarselo da sé, e lo fa per la piattaforma su cui gira —
Windows, macOS Intel e macOS Apple Silicon compresi.

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

> **Usare sempre il wrapper, non un Gradle installato sul computer.**
> `./gradlew` garantisce la versione di Gradle prevista dal progetto e il JDK
> con cui va compilato; `gradle run` userebbe invece la versione installata
> sulla macchina, che può essere incompatibile. Se accade, il progetto se ne
> accorge e lo dice, invece di fallire in modo oscuro.

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
