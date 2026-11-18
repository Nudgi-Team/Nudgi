package io.github.Nudgi.ui.lobby;

import static io.github.Nudgi.ui.StageManager.VIEW;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Disposable;

import io.github.Nudgi.entities.PlayerManager;
import io.github.Nudgi.ui.OnClickListener;
import io.github.Nudgi.ui.Showable;
import io.github.Nudgi.ui.StageManager;
import io.github.Nudgi.ui.UIService;

public class LobbyUI implements Disposable, Showable {
    private final Texture lobbyArrowTexture = new Texture("Arrow.png");
    private final Texture buttonTexture = new Texture("EmptyTile.png");

    private final Label lobbyLabel = UIService.createLabel("See instructions above.");
    private final Label playerCountLabel = UIService.createLabel(null);
    private final Image lobbyPointer = new Image(lobbyArrowTexture);
    private final TextButton devModeButton = UIService.createTextButton("DevMode", buttonTexture, null, null);
    private final TextButton startButton = UIService.createTextButton("Start", buttonTexture, null, null);

    private final OnClickListener devModeClicker = new OnClickListener();
    private final OnClickListener startClicker = new OnClickListener();
    
    public LobbyUI() {
        Vector2 buttonSize = new Vector2(VIEW.getWorldWidth()*0.1f, VIEW.getWorldHeight()*0.1f);

        UIService.centerWidget(lobbyLabel, true, true);
        lobbyLabel.setFontScale(1.7f);

        UIService.centerWidget(playerCountLabel, true, false);
        playerCountLabel.setFontScale(1.7f);
        playerCountLabel.setY(VIEW.getWorldHeight()*0.7f);
        PlayerManager.onPlayerAdded(_ -> update(), false);
        PlayerManager.onPlayerRemoved(_ -> update(), false);
        
        lobbyPointer.setPosition(VIEW.getWorldWidth()*0.1f, VIEW.getWorldHeight()*0.8f);
        lobbyPointer.setSize(VIEW.getWorldWidth()*0.15f, VIEW.getWorldHeight()*0.15f);

        devModeButton.setPosition(VIEW.getWorldWidth()*0.9f, VIEW.getWorldHeight()*0.1f);
        devModeButton.setSize(buttonSize.x, buttonSize.y);
        devModeButton.addListener(devModeClicker);
        UIService.modifyTextButtonStyle(devModeButton, style -> style.fontColor = Color.BLUE);

        startButton.setPosition(VIEW.getWorldWidth()*0.5f, VIEW.getWorldHeight()*0.3f);
        startButton.setSize(buttonSize.x, buttonSize.y);
        startButton.addListener(startClicker);
        startButton.setVisible(false);
        UIService.modifyTextButtonStyle(devModeButton, style -> style.fontColor = Color.BLACK);
    }

    private void update() {
        playerCountLabel.setText(PlayerManager.getPlayers().length + " players");
        toggleStartButton(PlayerManager.getYourself().isHost() && PlayerManager.getPlayers().length >= 2);
    }

    private void toggleStartButton(boolean visible) {
        startButton.setVisible(visible);
    }

    @Override 
    public void show() {
        StageManager.addActors(lobbyLabel, playerCountLabel, lobbyPointer, devModeButton, startButton);
    } 

    @Override 
    public void dispose() {
        lobbyArrowTexture.dispose();
        buttonTexture.dispose();
    }

    public void onDevModeClick(Runnable listener) {
        devModeClicker.switchAction(_ -> dispose())
            .chainAction(_ -> listener.run());
    }

    public void onStartClick(Runnable listener) {
        startClicker.switchAction(_ -> dispose())
            .chainAction(_ -> listener.run());
    }

    public void waitingMode() {
        lobbyLabel.setText("Waiting for players...");
    }
}
