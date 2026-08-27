package it.unicam.cs.mpgc.rpg123465.view.floors.rats;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Le immagini della stanza dei Topi: le pietre del labirinto, il portatore di
 * luce e i topi.
 *
 * <p>
 * Ogni foglio viene letto una volta sola e condiviso da tutti i nodi che lo
 * usano: le viste si distinguono per il ritaglio ({@link ImageView#setViewport}),
 * non per l'immagine, così duecento passi di animazione non costruiscono
 * duecento immagini.
 *
 * <p>
 * Nessun foglio è indispensabile. Se una risorsa manca, il metodo corrispondente
 * dice di no e la scena disegna la sua forma di ripiego: la prova resta
 * giocabile anche senza grafica.
 */
final class MazeSprites {

    // --- Fogli -----------------------------------------------------------

    private static final String TILESET = "/images/tiles/tileset_world_1.png";

    private static final String PLAYER = "/images/player/character_sheet.png";

    private static final String[] RAT_SHEETS = {
            "/images/rats/Rat-DarkGrey-Walk.png",
            "/images/rats/Rat-LightGrey-Walk.png",
            "/images/rats/Rat-Chocolate-Walk.png"
    };

    // --- Misure lette dai fogli ------------------------------------------

    /** Lato di una casella del tileset, che è 320x112 cioè 20x7 caselle. */
    private static final int TILE = 16;

    /** Lato di un fotogramma del portatore di luce: 240x288 cioè 5x6. */
    private static final int PLAYER_FRAME = 48;

    /** Lato di un fotogramma di topo: 128x32 cioè quattro passi in fila. */
    private static final int RAT_FRAME = 32;

    /** Passi della camminata dei topi. */
    static final int RAT_WALK_FRAMES = 4;

    /** Passi della camminata del portatore di luce. */
    static final int PLAYER_WALK_FRAMES = 4;

    /** Pose da fermo del portatore di luce. */
    static final int PLAYER_IDLE_FRAMES = 5;

    /*
     * Caselle scelte dal tileset, guardando il foglio.
     *
     * La banda di sinistra è la variante fredda: pietra blu notte per i muri
     * e lastricato chiaro per i corridoi. È la coppia con il contrasto più
     * netto delle tre disponibili, e in un labirinto contare le pareti a colpo
     * d'occhio conta più dell'atmosfera.
     */
    private static final int WALL_COLUMN = 3;
    private static final int WALL_ROW = 2;

    private static final int FLOOR_COLUMN = 2;
    private static final int FLOOR_ROW = 6;

    /**
     * Le tre righe del foglio del personaggio, nell'ordine in cui compaiono:
     * di fronte, di spalle, di profilo. Le prime tre righe sono le pose da
     * fermo, le tre successive la camminata — si riconoscono perché il
     * personaggio rimbalza e occupa qualche pixel in più in altezza.
     */
    enum Facing {
        FRONT(0),
        BACK(1),
        SIDE(2);

        private final int row;

        Facing(int row) {
            this.row = row;
        }

        int idleRow() {
            return row;
        }

        int walkRow() {
            return row + 3;
        }
    }

    private final Image tileset;
    private final Image player;
    private final List<Image> rats = new ArrayList<>();

    private MazeSprites(Image tileset, Image player, List<Image> rats) {
        this.tileset = tileset;
        this.player = player;
        this.rats.addAll(rats);
    }

    /**
     * Legge i fogli disponibili.
     *
     * @return l'atlante, con dentro solo ciò che è stato trovato davvero
     */
    static MazeSprites load() {
        List<Image> ratSheets = new ArrayList<>();

        for (String sheet : RAT_SHEETS) {
            Image image = read(sheet);

            if (image != null) {
                ratSheets.add(image);
            }
        }

        return new MazeSprites(read(TILESET), read(PLAYER), ratSheets);
    }

    boolean hasTiles() {
        return tileset != null;
    }

    boolean hasPlayer() {
        return player != null;
    }

    boolean hasRats() {
        return !rats.isEmpty();
    }

    /**
     * @return quante varianti di topo sono disponibili
     */
    int ratVariants() {
        return rats.size();
    }

    /**
     * @param side lato del nodo in pixel
     * @return la pietra dei muri
     */
    ImageView wall(double side) {
        return tile(WALL_COLUMN, WALL_ROW, side);
    }

    /**
     * @param side lato del nodo in pixel
     * @return il lastricato dei corridoi
     */
    ImageView floor(double side) {
        return tile(FLOOR_COLUMN, FLOOR_ROW, side);
    }

    /**
     * Crea la vista di un topo, pronta per essere animata.
     *
     * @param variant indice della variante di colore
     * @param side lato del nodo in pixel
     * @return la vista, già posizionata sul primo fotogramma
     */
    ImageView rat(int variant, double side) {
        ImageView view = new ImageView(rats.get(Math.floorMod(variant, rats.size())));

        view.setSmooth(false);
        view.setFitWidth(side);
        view.setFitHeight(side);
        view.setViewport(ratViewport(0));

        return view;
    }

    /**
     * @param frame passo della camminata
     * @return il ritaglio del fotogramma richiesto
     */
    Rectangle2D ratViewport(int frame) {
        return new Rectangle2D(
                Math.floorMod(frame, RAT_WALK_FRAMES) * RAT_FRAME,
                0,
                RAT_FRAME,
                RAT_FRAME
        );
    }

    /**
     * Crea la vista del portatore di luce.
     *
     * @param side lato del nodo in pixel
     * @return la vista, già posizionata sulla posa da fermo di fronte
     */
    ImageView player(double side) {
        ImageView view = new ImageView(player);

        view.setSmooth(false);
        view.setFitWidth(side);
        view.setFitHeight(side);
        view.setViewport(playerViewport(Facing.FRONT, 0, false));

        return view;
    }

    /**
     * @param facing verso in cui guarda
     * @param frame passo dell'animazione
     * @param walking {@code true} per la camminata, {@code false} da fermo
     * @return il ritaglio del fotogramma richiesto
     */
    Rectangle2D playerViewport(Facing facing, int frame, boolean walking) {
        int columns = walking
                ? PLAYER_WALK_FRAMES
                : PLAYER_IDLE_FRAMES;

        int row = walking
                ? facing.walkRow()
                : facing.idleRow();

        return new Rectangle2D(
                Math.floorMod(frame, columns) * PLAYER_FRAME,
                (double) row * PLAYER_FRAME,
                PLAYER_FRAME,
                PLAYER_FRAME
        );
    }

    private ImageView tile(int column, int row, double side) {
        ImageView view = new ImageView(tileset);

        view.setSmooth(false);
        view.setFitWidth(side);
        view.setFitHeight(side);

        view.setViewport(new Rectangle2D((double) column * TILE, (double) row * TILE, TILE, TILE));

        return view;
    }

    /**
     * Legge un foglio dal classpath.
     *
     * @return l'immagine, oppure {@code null} se manca o è illeggibile
     */
    private static Image read(String resource) {
        try (InputStream stream = MazeSprites.class.getResourceAsStream(resource)) {

            if (stream == null) {
                System.getLogger(MazeSprites.class.getName())
                        .log(System.Logger.Level.WARNING,
                                "Sprite non trovato, si usa il ripiego: " + resource);
                return null;
            }

            Image image = new Image(stream);

            return image.isError() ? null : image;

        } catch (Exception exception) {
            System.getLogger(MazeSprites.class.getName())
                    .log(System.Logger.Level.WARNING,
                            "Sprite illeggibile, si usa il ripiego: " + resource);
            return null;
        }
    }
}
