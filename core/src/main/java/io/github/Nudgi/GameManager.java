package io.github.Nudgi;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Disposable;

public class GameManager implements Disposable {
    private static final Map<Vector2, Vector2> BASE_MAPPINGS = new HashMap<>();
    static {
        BASE_MAPPINGS.put(Vector2.Zero, Vector2.One);
        BASE_MAPPINGS.put(Vector2.One, Vector2.Zero);
        BASE_MAPPINGS.put(Vector2.X, Vector2.Y);
        BASE_MAPPINGS.put(Vector2.Y, Vector2.X);
    }

    private final float PLAYER_IMAGE_OFFSET;
    private final Stage stage;
    private final Board board;
    private Map<Player, Vector2> winningPositions = new HashMap<>();
    private Player[] players = new Player[4];
    private Player selectedPlayer = null;
    private int currentMovement;

    private boolean testMode = false;

    public GameManager(Board board, Stage stage, boolean _testMode){
        this(board, stage);
        testMode = _testMode;
    }

    public GameManager(Board board, Stage stage){
        this.board = board;
        this.stage = stage;
        PLAYER_IMAGE_OFFSET = board.getTileSize()/5;
        Vector2[] basePos = {
            Vector2.Zero,
            Vector2.X,
            Vector2.Y,
            Vector2.One
        };

        for (int i = 0; i < players.length; i++) {
            Vector2 boardCornerPos = board.getGridSize()
                .scl(basePos[i])
                .sub(basePos[i]);

            Vector2 worldPos = board.toWorldPos(boardCornerPos);
            float tileSize = board.getTileSize();

            players[i] = new Player(worldPos.x + PLAYER_IMAGE_OFFSET, worldPos.y + PLAYER_IMAGE_OFFSET, tileSize*0.9f, tileSize*0.9f, stage, i);
            players[i].setListener(this::handlePlayerClick);

            Vector2 winMapping = BASE_MAPPINGS.get(basePos[i]);
            winningPositions.put(players[i], winMapping.cpy().scl(board.getGridSize()).sub(winMapping));
            board.initializePlayer(players[i], boardCornerPos);
        }
        board.setListenerAllTiles(this::handleTileClick);
        generateRandomMovement();
    }

    // public void draw(SpriteBatch batch) {
    //     board.draw(batch);
    // }

    @Override
    public void dispose() {
        Player.dispose();
        players = null;
    }

    private void generateRandomMovement() {
        currentMovement = ThreadLocalRandom.current().nextInt(1, 7);
    }

    private Player findPlayerByID(int id) {
        for (Player p: players) {
            if (id == p.playerID) {
                return p;
            }
        }
        throw new IllegalArgumentException("Failed to find player with id " + id);
    }

    private void handlePlayerClick(int playerID) {
        if (selectedPlayer == null || selectedPlayer.playerID != playerID) {
            selectedPlayer = findPlayerByID(playerID);
            System.out.println(selectedPlayer + " selected");

            Set<Vector2> visited = new HashSet<>();
            Stack<Vector2> explore = new Stack<>();
            explore.add(board.getPlayerPosition(selectedPlayer));
            for (int i = 0; i <= currentMovement; i++) {
                // Start the queue and thus first iteration with the player's own tile.
                // On each subsequent iteration, the queue will begin with all adjacent tiles found
                // in the previous iteration, excluding tiles visited in prior iterations.
                // All tiles visited will be marked, including the starting square.
                // Each iteration uses up one movement, with the first iteration only marking the starting square.
                Stack<Vector2> found = new Stack<>();
                while (!explore.empty()) {
                    Vector2 pos = explore.pop();
                    visited.add(pos);
                    List<Vector2> adjacent = board.getAdjacentPositions(pos, !testMode);
                    for (Vector2 item: adjacent) {
                        if (!visited.contains(item)) {
                            found.add(item);
                        }
                    }
                }
                explore.addAll(found);
            }
            System.out.println("Targets:");
            for (Vector2 moveEligible: visited) {
                System.out.println(moveEligible);
                board.fetchTile(moveEligible).setHighlighted(true);
            }
            System.out.println("Available movement: " + currentMovement);
        }
        else {
            System.out.println(selectedPlayer + " stayed put!");
            resetTurnContext();
        }
    }

    private void handleTileClick(int tileID) {
        if (selectedPlayer == null) { return; }
        Board.TileInfo target = board.fetchTile(tileID);
        if (target.tile.isHighlighted()) {
            if (!target.tile.containsPlayer(selectedPlayer)) {
                movePlayer(selectedPlayer, target.pos);
            }
            else {
                System.out.println("Player " + selectedPlayer.playerID + " stayed put!");
            }
            resetTurnContext();
        }
    }


    private void generateWinScreen(Player winner) {
        Skin skin = new Skin();
        Texture background = new Texture("EmptyTile.png");
        skin.add("MazeBackground", background);

        Label.LabelStyle lstyle = new Label.LabelStyle();
        lstyle.font = new BitmapFont();
        lstyle.fontColor = Color.WHITE;

        TextButton.TextButtonStyle tbStyle = new TextButton.TextButtonStyle();
        tbStyle.font = new BitmapFont();
        tbStyle.fontColor = Color.BLACK;
        tbStyle.up = skin.getDrawable("MazeBackground");


        Label winnerText = new Label(winner + " has won!", lstyle);
        TextButton button = new TextButton("Exit", tbStyle);
        Vector2 center = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        OnClickListener listener = new OnClickListener(0);

        winnerText.setFontScale(2);
        winnerText.setPosition(center.x/2 - winnerText.getWidth(), center.y/1.7f);
        button.setTransform(true);
        button.setScale(2);
        button.setPosition(center.x/2 - button.getWidth(), center.y/2.5f);
        button.addListener(listener);
        listener.setListener(id -> Gdx.app.exit());

        stage.clear();
        stage.addActor(winnerText);
        stage.addActor(button);
    }

    private void movePlayer(Player p, Vector2 newPos) {
        board.movePlayer(p, newPos);
        p.moveImage(board.toWorldPos(newPos).add(PLAYER_IMAGE_OFFSET, PLAYER_IMAGE_OFFSET));
        System.out.println("MOVED " + p + "to " + newPos);
        if (playerWon(p)) {
            System.out.println(p + " wins!");
            generateWinScreen(p);
        }
    }


    private boolean playerWon(Player p) {
        return board.getPlayerPosition(p).epsilonEquals(winningPositions.get(p));
    }

    private void resetTurnContext() {
        board.clearHighlights();
        generateRandomMovement();
        selectedPlayer = null;
    }
}
