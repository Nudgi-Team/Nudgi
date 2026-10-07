package io.github.Nudgi;

import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import java.util.ArrayList;
public class Player {
    private static final Texture playerTexture = new Texture("Player.png");
    private Image playerImage;
    private ArrayList<PlayerListener> listeners = new ArrayList<>();
    public final int playerID;
    public boolean turnPlayer = false;
    public boolean selected = false;

    public Player(int x, int y, int width, int height, Stage stage, int playerID) {        
        playerImage = new Image(playerTexture);
        playerImage.setPosition(x, y);
        playerImage.setWidth(width);
        playerImage.setHeight(height);
        playerImage.addListener(new PlayerClickListener());

        stage.addActor(playerImage);
        this.playerID = playerID;
    }
    
    public static void dispose() {
        playerTexture.dispose();
    }

    public void addListener(PlayerListener listener) {
        listeners.add(listener);
    }

    private class PlayerClickListener extends ClickListener {	
        @Override
        public void clicked(InputEvent event, float x, float y) {
            for (PlayerListener listener: listeners) {
                listener.onClick(playerID);
                event.handle();
            }
        }
    }
}
