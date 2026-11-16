package io.github.Nudgi.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class ShiftArrow extends Entity<Image> {
    private static final Texture arrowTexture = new Texture("Arrow.png");

    private final ShiftID id;

    public ShiftArrow(ShiftID id) {
        super(new Image(arrowTexture));
        this.id = id;
    }

    public ShiftID getID() {
        return id;
    }

    @Override 
    public String toString() {
        return "Shift arrow with id " + id;
    }
}
