package io.github.Nudgi.ui.game;

import static io.github.Nudgi.ui.StageManager.STAGE;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Disposable;

import io.github.Nudgi.ui.OnClickListener;
import io.github.Nudgi.ui.Showable;
import io.github.Nudgi.ui.StageManager;
import io.github.Nudgi.ui.UIService;

public class FullScreenPopUpUI implements Disposable, Showable {
    private final Texture buttonTexture = new Texture("EmptyTile.png");
    private final Label message = UIService.createLabel(null);
    private final TextButton exitButton = UIService.createTextButton(null, buttonTexture, null, null);
    private final OnClickListener buttonClickListener = new OnClickListener();

    public FullScreenPopUpUI(String labelMessage, String exitButtonMessage, Runnable exitButtonAction) {
        Vector2 topRight = StageManager.getWorldSize();

        message.setText(labelMessage);
        message.setFontScale(1.7f);
        message.setY(topRight.y/1.7f);
        UIService.centerWidget(message, true, false);

        exitButton.setText(exitButtonMessage);
        exitButton.setTransform(true);
        exitButton.setScale(3);
        exitButton.setSize(topRight.x * 0.05f, topRight.y * 0.05f);
        exitButton.setPosition(topRight.x/2 - exitButton.getWidth()*1.5f, topRight.y/2.5f - exitButton.getHeight());
        exitButton.addListener(buttonClickListener);
        UIService.modifyTextButtonStyle(exitButton, style -> style.fontColor = Color.BLACK);

        buttonClickListener.switchAction(_ -> dispose())
            .chainAction(_ -> exitButtonAction.run());
    }

    @Override 
    public void show() {
        STAGE.addActor(message);
        STAGE.addActor(exitButton);
    }

    @Override 
    public void dispose() {
        buttonTexture.dispose();
    }
}
