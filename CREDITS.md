# Crediti degli asset

Tower of Self usa grafica e suoni di terze parti. Questo file elenca la
provenienza e la licenza di ciascun asset.

Le copie originali, prima delle elaborazioni descritte qui sotto, sono
conservate in `asset-originali/` ed escluse dal repository.

## Grafica

### Ritratti dei profili — `images/profili/`

- **Stock Art — Grimdark Fantasy Characters**, di **nacnudllah**
- <https://nacnudllah.itch.io/stock-art-grimdark-fantasy-characters>
- Licenza: **CC0 1.0** (pubblico dominio, uso personale e commerciale, senza
  obbligo di attribuzione)

Sette dei tredici disegni del pacchetto, uno per ciascun profilo. Elaborazione:
ritaglio dei margini trasparenti e riduzione su una tela comune di 420×560 px.

| file | disegno originale |
| --- | --- |
| `coraggioso.png` | Anya |
| `curioso.png` | Bad Dog |
| `avventuriero.png` | Lazy Marauder 1 |
| `esploratore.png` | Sir Lickingtoad |
| `risoluto.png` | Flail Knight |
| `visionario.png` | Raven Knight |
| `imprevedibile.png` | Fowl Nobleman |

### Topi del Piano I — `images/topi/rats/`

- **PixelRats (Free)**, di **Carysaurus**
- Licenza: gratuita, uso consentito nei progetti — *da verificare sulla pagina
  itch.io del pacchetto prima della consegna*

Tre delle sei varianti di colore incluse nel pacchetto.

### Pietra e portatore di luce del Piano I — `images/topi/tiles/`, `images/topi/player/`

- **Waxlight Dungeon — Free Asset Pack v1.0**
- Licenza (dal `read_me.txt` del pacchetto): uso e modifica liberi per progetti
  personali e commerciali; l'attribuzione non è obbligatoria ma è gradita

Elaborazione del tileset: conversione in scala di grigi e moltiplicazione per
l'oro `#b99a5e`, per portare la pietra nella palette della Torre.

### Fondali, cancello e icona — `images/bg/`, `images/scenes/`, `images/icon/`

- Fonte: **[Pixabay](https://pixabay.com/)**
- Licenza: **Pixabay Content License** — uso libero, anche commerciale, senza
  obbligo di attribuzione; non è consentito ridistribuire i file come tali su
  altre piattaforme di asset

| File | Dove compare nel gioco |
| --- | --- |
| `bg/forest.jpg` | lo sfondo che scorre |
| `bg/fog.jpg` | la nebbia |
| `scenes/gate.jpg` | il cancello della schermata iniziale |
| `scenes/floor1-topi.jpg` | Piano I — I Topi |
| `scenes/floor2-buio.jpg` | Piano II — Il Buio |
| `scenes/floor2-lock.jpg` | il primo piano sulla serratura |
| `scenes/floor3-altezze.png` | Piano III — Le Altezze |
| `icon/tower-16…256.png` | l'icona della finestra, nelle sette misure che Windows richiede |

Elaborazione: adattamento alla palette in bianco e nero della Torre e
ridimensionamento. Il cancello è stato inoltre rielaborato con l'assistenza di
ChatGPT.

## Audio — `audio/`

- Fonte: **[Pixabay](https://pixabay.com/sound-effects/)**
- Licenza: **Pixabay Content License** — uso libero, anche commerciale, senza
  obbligo di attribuzione; non è consentito ridistribuire i file come tali su
  altre piattaforme di asset

Ventuno tracce, tutte usate dal gioco. Elaborazione: conversione in MP3,
normalizzazione del volume e taglio della durata sulla lunghezza richiesta
dalla scena.

| ambito | file |
| --- | --- |
| Ambienti | `ambience-dark.mp3`, `ambience-forest.mp3`, `ambience-night.mp3`, `ambience-topi.mp3`, `ambience-wind.mp3` |
| Piano I — I Topi | `mousetrap-snap.mp3`, `rats-many.mp3`, `scurrying.mp3`, `squeak.mp3` |
| Piano II — Il Buio | `fire-crackle.mp3`, `padlock-unlock.mp3`, `torch-whoosh.mp3` |
| Piano III — Le Altezze | `arrow-tap.mp3`, `arrow-wrong.mp3`, `fall.mp3`, `wood-break.mp3`, `wood-step.mp3` |
| Interfaccia e atmosfera | `gate-open.mp3`, `heartbeat.mp3`, `owl-hoot.mp3`, `scream.mp3` |

## Codice

Il codice del gioco è opera dell'autrice del progetto. La stesura si è avvalsa
dell'assistenza di ChatGPT.
