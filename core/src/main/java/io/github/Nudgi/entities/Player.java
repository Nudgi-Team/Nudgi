package io.github.Nudgi.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class Player extends Entity<Integer, Image> {
    private static final Texture playerTexture = new Texture("Player.png");
    private static Vector2 imageOffset = Vector2.Zero.cpy();

    /**
     * Constructs a new player with its own {@link Image}.
     * @param x the position of the Image's bottom left corner.
     * @param y
     * @param width the width of the Image.
     * @param height the height of the Image.
     * @param stage the {@link Stage} to place the Image onto.
     * @param playerID a unique identifier for this instance.
     */
    public Player(float x, float y, float width, float height, Stage stage, int playerID) {   
        super(new Image(playerTexture), playerID);     
        Image playerImage = getActor();
        setPosition(new Vector2(x, y));
        playerImage.setWidth(width);
        playerImage.setHeight(height);

        stage.addActor(playerImage);
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
        return "Player " + (getID() + 1);
    }
}
