# 📌 Tower of Self

RPG psicologico a piani. Il giocatore sale una Torre in cui ogni piano è una prova diversa — riflessi, memoria, equilibrio — e fra una prova e l'altra risponde a dilemmi senza risposta giusta.
Le prove misurano le abilità, le domande misurano il giocatore: dalle risposte emerge alla fine uno di sette profili.

Documentazione completa nella **[Wiki](../../wiki)** · asset di terze parti in **[CREDITS.md](CREDITS.md)**

---

## 🚀 Come eseguire il progetto

### Prerequisiti
- Java 25 (LTS)
- Gradle (incluso nel progetto tramite il wrapper, non serve installarlo)

### Istruzioni

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

Su Windows: `.\gradlew.bat build` e `.\gradlew.bat run`.

---

## 🤖 Uso di strumenti di AI

Durante lo sviluppo è stato utilizzato **ChatGPT**, con questo livello di intervento:

| attività | contributo dell'AI | intervento personale |
| --- | --- | --- |
| Idea e design del gioco | nessuno | interamente mio: piani, meccaniche, dilemmi, profili, testi narrativi |
| Analisi della specifica | confronto e chiarimenti | scelte finali mie |
| Architettura del software | proposte di organizzazione in package e interfacce | valutate, discusse e adattate da me |
| Scrittura del codice | generazione di porzioni su mia indicazione | lette, comprese e verificate prima di ogni integrazione |
| Revisione del codice | segnalazione di duplicazioni e violazioni dei principi SOLID | decisioni di refactoring mie |
| Test | proposte di casi di prova | verificati eseguendoli |
| Documentazione | supporto alla stesura | contenuti e verifica miei |

Nessuna parte del progetto è stata integrata senza essere stata letta e compresa. Le decisioni di progettazione — quali piani, quali meccaniche, quali profili, cosa rimuovere — sono mie, e sono tracciabili nella cronologia dei commit.
