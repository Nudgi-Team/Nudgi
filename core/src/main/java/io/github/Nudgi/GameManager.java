package io.github.Nudgi;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Disposable;

import io.github.Nudgi.entities.ClickableEntity;
import io.github.Nudgi.entities.Player;

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
            players[i].addListener(this::handlePlayerClick);

            Vector2 winMapping = BASE_MAPPINGS.get(basePos[i]);
            winningPositions.put(players[i], winMapping.cpy().scl(board.getGridSize()).sub(winMapping));
            board.initializePlayer(players[i], boardCornerPos);
        }
        board.setListenerAllTiles(this::handleTileClick);
        generateRandomMovement();
    }

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

    public void handlePlayerClick(Vector2 pos) {
        Vector2 gridPos = board.toGrid(pos);

        System.out.println(pos);
        System.out.println(gridPos);
        Vector2 gridSize = board.getGridSize();

        if (gridPos.x == -1){
            if(gridPos.y > 0 && gridPos.y < gridSize.y - 1){
                board.shift((int) gridPos.y, Direction.RIGHT);
            }
        }
        else if (gridPos.x == gridSize.x){
            if(gridPos.y > 0 && gridPos.y < gridSize.y - 1){
                board.shift((int) gridPos.y, Direction.LEFT);
            }
        }
        else if (gridPos.y == -1){
            if(gridPos.x > 0 && gridPos.x < gridSize.x - 1){
                board.shift((int) gridPos.x, Direction.UP);
            }
        }
        else if (gridPos.y == gridSize.y){
            if(gridPos.x > 0 && gridPos.x < gridSize.x - 1){
                board.shift((int) gridPos.x, Direction.DOWN);
            }
        }
    }

    private void handlePlayerClick(int playerID) {
        if (selectedPlayer == null || selectedPlayer.playerID != playerID) {
            selectedPlayer = findPlayerByID(playerID);
            System.out.println(selectedPlayer + " selected");

            Set<Vector2> visited = board.pathFind(board.getPlayerPosition(selectedPlayer), currentMovement);
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
        Texture background = new Texture("EmptyTile.png");
        Label winnerText = UIService.createLabel(winner + " has won!");
        TextButton baseButton = UIService.createTextButton("Exit", background, null, null);
        ClickableEntity<Void, TextButton> button = new ClickableEntity<Void,TextButton>(baseButton, null) {};
        Vector2 topRight = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        winnerText.setFontScale(2);
        winnerText.setPosition(topRight.x/2 - winnerText.getWidth(), topRight.y/1.7f);

        baseButton.setTransform(true);
        baseButton.setScale(3);
        baseButton.setPosition(topRight.x/2 - baseButton.getWidth()*1.5f, topRight.y/2.5f - baseButton.getHeight());
        button.addListener(_void -> Gdx.app.exit());

        stage.clear();
        stage.addActor(winnerText);
        stage.addActor(baseButton);
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
