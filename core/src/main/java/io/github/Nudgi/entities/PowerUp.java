package io.github.Nudgi.entities;

import java.util.concurrent.ThreadLocalRandom;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import io.github.Nudgi.ui.UIService;

public enum PowerUp {
    SPEED_BOOST,
    POWER_UP2,
    POWER_UP3;

    public Label spawnLabel() {
        String display = (this == SPEED_BOOST) ? "SPD" : ("P" + (ordinal() + 1));
        Label label = UIService.createLabel(display);
        label.setFontScale(0.7f);
        label.setAlignment(Align.center);
        label.setColor(Color.RED);
        return label;
    }

    public static PowerUp random() {
        return values()[ThreadLocalRandom.current().nextInt(0, values().length)];
    }
}
