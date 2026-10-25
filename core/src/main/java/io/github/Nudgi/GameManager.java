package io.github.Nudgi;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.Nudgi.entities.Player;
import io.github.Nudgi.entities.TrivialEntity;

public class GameManager implements Disposable {
    private static final Map<Vector2, Vector2> BASE_MAPPINGS = new HashMap<>();
    static {
        BASE_MAPPINGS.put(Vector2.Zero, Vector2.One);
        BASE_MAPPINGS.put(Vector2.One, Vector2.Zero);
        BASE_MAPPINGS.put(Vector2.X, Vector2.Y);
        BASE_MAPPINGS.put(Vector2.Y, Vector2.X);
    }

    private final Stage stage;
    private final Board board;
    private Map<Player, Vector2> winningPositions = new HashMap<>();
    private Player[] players = new Player[4];
    private Player selectedPlayer = null;
    private int currentMovement;

    public GameManager(Board board, Stage stage){
        this.board = board;
        this.stage = stage;
        float PLAYER_IMAGE_OFFSET = board.getTileSize()/5;
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
            Player.setPositionOffset(new Vector2(PLAYER_IMAGE_OFFSET, PLAYER_IMAGE_OFFSET));
            players[i] = new Player(worldPos.x, worldPos.y, tileSize*0.9f, tileSize*0.9f, stage, i+1);
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
        players = null;
    }

    private void generateRandomMovement() {
        currentMovement = ThreadLocalRandom.current().nextInt(1, 7);
    }

    private Player findPlayerByID(int id) {
        for (Player p: players) {
            if (p.getID().equals(id)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Failed to find player with id " + id);
    }

    private void handlePlayerClick(int playerID) {
        if (selectedPlayer == null) {
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
        else if (selectedPlayer.getID().equals(playerID)) {
            System.out.println(selectedPlayer + " stayed put!");
            resetTurnContext();
        }
        else {
            Player other = findPlayerByID(playerID);
            Vector2 otherPos = board.getPlayerPosition(other);
            handleTileClick(board.fetchTile(otherPos).getID());
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
                System.out.println(selectedPlayer + " stayed put!");
            }
            resetTurnContext();
        }
    }


    private void generateWinScreen(Player winner) {
        Texture background = new Texture("EmptyTile.png");
        Label winnerText = UIService.createLabel(winner + " has won!");
        TextButton baseButton = UIService.createTextButton("Exit", background, null, null);
        Viewport view = stage.getViewport();
        Vector2 topRight = new Vector2(view.getWorldWidth(), view.getWorldHeight());
        new TrivialEntity<>(baseButton, _void -> Gdx.app.exit());

        winnerText.setFontScale(2);
        winnerText.setPosition(topRight.x/2 - winnerText.getWidth(), topRight.y/1.7f);

        baseButton.setTransform(true);
        baseButton.setScale(3);
        baseButton.setPosition(topRight.x/2 - baseButton.getWidth()*1.5f, topRight.y/2.5f - baseButton.getHeight());      
        UIService.modifyTextButtonStyle(baseButton, style -> style.fontColor = Color.BLACK);  

        stage.clear();
        stage.addActor(winnerText);
        stage.addActor(baseButton);
    }

    private void movePlayer(Player p, Vector2 newPos) {
        board.movePlayer(p, newPos);
        p.setPosition(board.toWorldPos(newPos));
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
