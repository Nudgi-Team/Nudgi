package io.github.Nudgi;


import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

public class GameManager implements Disposable {
    private Player[] players = new Player[4];
    private Board board;
    int currentMovement;

    public GameManager(Board board, Stage stage) {
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

            Vector2 boardPos = board.getPosition();
            int x = (int)boardPos.x;
            int y = (int)boardPos.y;
            int cornerX = (int)cornerPositions[i].x;
            int cornerY = (int)cornerPositions[i].y;
            int tileSize = (int)board.getTileSize();
            
            players[i] = new Player(x + tileSize*cornerX, y + tileSize*cornerY, tileSize, tileSize, stage, i);
            players[i].addListener(this::handlePlayerClick);
        }
        this.board = board;
        generateRandomMovement();
    }

    public void draw(SpriteBatch batch) {
        board.draw(batch);
    }

    private void generateRandomMovement() {
        currentMovement = ThreadLocalRandom.current().nextInt(1, 7);
    }

    @Override 
    public void dispose() {
        Player.dispose();
        players = null;
    }

    public void handlePlayerClick(int playerID) {
        if (!players[playerID].selected) {
            System.out.println("Player " + playerID + " selected");
            players[playerID].selected = true;
            for (Player p: players) {
                if (p.equals(players[playerID])) { continue; }
                p.selected = false;
            }

            // Now, show all tiles that player can reach.

        }
        else {
            players[playerID].selected = false;
        }
    }
}
