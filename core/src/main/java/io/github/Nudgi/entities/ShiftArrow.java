package io.github.Nudgi.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import io.github.Nudgi.Direction;

public class ShiftArrow extends Entity<ShiftArrow.ShiftID, Image> {
    private static final Texture arrowTexture = new Texture("Arrow.png");

    public ShiftArrow(int index, Direction dir) {
        super(new Image(arrowTexture), new ShiftID(index, dir));
    }

    @Override 
    public String toString() {
        return "Shift arrow with " + getID().toString()
        + "\nBelonging to entity " + super.toString();
    }

    public record ShiftID(int index, Direction dir) {}
}
