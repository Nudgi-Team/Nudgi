package io.github.Nudgi.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.github.Nudgi.Board;

import java.util.List;
import java.util.Random;

public class Enemy extends Entity<Integer, Image> {
    private static Vector2 imageOffset = Vector2.Zero.cpy();

    /**
     * Constructs a new enemy with its own {@link Image}.
     *
     * @param x       the position of the Image's bottom left corner.
     * @param y
     * @param width   the width of the Image.
     * @param height  the height of the Image.
     * @param stage   the {@link Stage} to place the Image onto.
     * @param enemyID a unique identifier for this instance.
     */
    public Enemy(float x, float y, float width, float height, Stage stage, int enemyID, String pathToTexture) {
        super(new Image(new Texture(pathToTexture)), enemyID);
        Image enemyImage = (Image) getActor();
        setPosition(new Vector2(x, y));
        enemyImage.setWidth(width);
        enemyImage.setHeight(height);

        stage.addActor(enemyImage);
    }

    public static void setPositionOffset(Vector2 offset) {
        imageOffset = offset;
    }

    @Override
    public void setPosition(Vector2 v) {
        super.setPosition(v.add(imageOffset));
    }

    @Override
    public String toString() {
        return "Enemy: " + getID();
    }


    public void move(Board board){
        // Get random move in any direction
        List<Vector2> tiles = board.getAdjacentPositions(board.getEnemyPosition(this), true);

        Random random = new Random();

        Vector2 newPos = tiles.get(random.nextInt(tiles.size()));
        board.moveEnemy(this, newPos);
    }
}
