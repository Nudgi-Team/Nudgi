package io.github.Nudgi;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.scenes.scene2d.Stage;

public class GameManager implements Disposable {
    private Player[] players = new Player[4];
    private Player selectedPlayer = null;
    private Board board;
    private int currentMovement;
    private final float playerOffsetFactor;

    public GameManager(Board board, Stage stage) {
        this.board = board;
        playerOffsetFactor = board.getTileSize()/5;
        Vector2[] cornerPositions = {
            Vector2.Zero,
            Vector2.X,
            Vector2.Y,
            Vector2.One
        };

        for (int i = 0; i < players.length; i++) {
            cornerPositions[i] = board.getGridSize()
                .scl(cornerPositions[i])
                .sub(cornerPositions[i]);

            Vector2 worldPos = board.toWorldPos(cornerPositions[i]);
            float tileSize = board.getTileSize();

            players[i] = new Player(worldPos.x + playerOffsetFactor, worldPos.y + playerOffsetFactor, tileSize*0.9f, tileSize*0.9f, stage, i);
            players[i].setListener(this::handlePlayerClick);
            board.initializePlayer(players[i], cornerPositions[i]);
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
            System.out.println("Player " + playerID + " selected");
            selectedPlayer = findPlayerByID(playerID);

            Set<Vector2> visited = board.pathFind(board.getPlayerPosition(selectedPlayer), currentMovement);
            System.out.println("Targets:");
            for (Vector2 moveEligible: visited) {
                System.out.println(moveEligible);
                board.fetchTile(moveEligible).setHighlighted(true);
            }
            System.out.println("Available movement: " + currentMovement);
        }
        else {
            System.out.println("Player " + selectedPlayer.playerID + " stayed put!");
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
                resetTurnContext();
            }
        }
    }

    private void movePlayer(Player p, Vector2 newPos) {
        board.movePlayer(p, newPos);
        p.moveImage(board.toWorldPos(newPos).add(playerOffsetFactor, playerOffsetFactor));
        System.out.println("MOVED player id " + p.playerID + " to " + newPos);
        resetTurnContext();
    }

    private void resetTurnContext() {
        board.clearHighlights();
        generateRandomMovement();
        selectedPlayer = null;
    }
}
