package io.github.Nudgi;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

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

    public void draw(SpriteBatch batch) {
        board.draw(batch);
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

    private void handlePlayerClick(int playerID) {
        if (selectedPlayer == null || selectedPlayer.playerID != playerID) {
            System.out.println("Player " + playerID + " selected");
            selectedPlayer = findPlayerByID(playerID);

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
                    List<Vector2> adjacent = board.getAdjacentPositions(pos, true);
                    for (Vector2 item: adjacent) {
                        System.out.println("ADJACENT: " + item);
                        if (!visited.contains(item)) {
                            found.add(item);
                        }
                    }
                }
                explore.addAll(found);
            }
            for (Vector2 moveEligible: visited) {
                System.out.println(moveEligible);
                board.fetchTile(moveEligible).setHighlighted(true);
            }
            System.out.println("Available movement: " + currentMovement);
        }
        else {
            resetTurnContext();
        }
    }

    private void handleTileClick(int tileID) {
        Board.TileInfo target = board.fetchTile(tileID);
        if (target.tile.isHighlighted() && selectedPlayer != null && !target.tile.players.contains(selectedPlayer)) {
            movePlayer(selectedPlayer, target.pos);
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
