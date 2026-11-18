package io.github.Nudgi.entities;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

import io.github.Nudgi.ui.UIService;

public enum PowerUp {
    POWER_UP1,
    POWER_UP2,
    POWER_UP3;

    public Label spawnLabel() {
        Label label = UIService.createLabel("P" + (ordinal() + 1));
        label.setFontScale(0.7f);
        return label;
    }
}
