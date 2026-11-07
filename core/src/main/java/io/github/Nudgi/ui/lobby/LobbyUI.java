package io.github.Nudgi.ui.lobby;

import static io.github.Nudgi.ui.stage.StageManager.VIEW;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import io.github.Nudgi.entities.TrivialEntity;
import io.github.Nudgi.ui.UIService;
import io.github.Nudgi.ui.stage.StageManager;

public class LobbyUI {
    private final Label lobbyLabel = UIService.createLabel("See instructions above.");
    private final Image lobbyPointer = new Image(new Texture("Arrow.png"));
    private final TrivialEntity<TextButton> devModeButton = 
        new TrivialEntity<>(UIService.createTextButton("DevMode", new Texture("EmptyTile.png"), null, null));

 
    public LobbyUI() {
        UIService.centerWidget(lobbyLabel, true, true);
        lobbyLabel.setFontScale(1.7f);
        lobbyPointer.setPosition(VIEW.getWorldWidth()*0.1f, VIEW.getWorldHeight()*0.8f);
        lobbyPointer.setSize(VIEW.getWorldWidth()*0.15f, VIEW.getWorldHeight()*0.15f);

        TextButton button = devModeButton.getActor();
        UIService.modifyTextButtonStyle(button, style -> style.fontColor = Color.BLUE);

        button.setPosition(VIEW.getWorldWidth()*0.9f, VIEW.getWorldHeight()*0.1f);
        button.setSize(VIEW.getWorldWidth()*0.1f, VIEW.getWorldHeight()*0.1f);
        StageManager.addActors(lobbyLabel, lobbyPointer, button);
    }

    public void onDevMode(Runnable... listeners) {
        devModeButton.addListener(listeners);
    }

    public void waitingMode() {
        lobbyLabel.setText("Waiting for players...");
    }
}
