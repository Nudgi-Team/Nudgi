package io.github.Nudgi;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class Player {
    private static final Texture playerTexture = new Texture("Player.png");
    private Image playerImage;
    private final OnClickListener clickListener;

    /**
     * A unique identifier for the Player.
     */
    public final int playerID;

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
        playerImage = new Image(playerTexture);
        playerImage.setPosition(x, y);
        playerImage.setWidth(width);
        playerImage.setHeight(height);

        stage.addActor(playerImage);
        this.playerID = playerID;
        this.clickListener = new OnClickListener(playerID);
        playerImage.addListener(clickListener);
    }
    
    public static void dispose() {
        playerTexture.dispose();
    }

    /**
     * Sets the {@code listener} to this instance, invoked each time the player is clicked.
     * @param listener the {@link IdentifyingListener listener} to bind to this instance.
     */
    public void setListener(IdentifyingListener listener) {
        this.clickListener.setListener(listener);
    }

    
    public void moveImage(Vector2 newWorldPos) {
        playerImage.setPosition(newWorldPos.x, newWorldPos.y);
    }

    @Override 
    public String toString() {
        return "Player " + playerID;
    }
}
