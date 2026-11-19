package io.github.Nudgi.ui.game;

import java.util.function.BiConsumer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import io.github.Nudgi.entities.Player;
import io.github.Nudgi.entities.PlayerManager;
import io.github.Nudgi.entities.PowerUp;
import io.github.Nudgi.entities.Player.PowerUpSlotChange;
import io.github.Nudgi.ui.OnClickListener;
import io.github.Nudgi.ui.Showable;
import io.github.Nudgi.ui.StageManager;

public class PowerUpUI implements Showable, Disposable {
    private final Texture slotTexture = new Texture("FourWayTile.png");
    private final Stack[] powerUpSlots = new Stack[3];
    private final Image[] slotImages = new Image[3];
    private final OnClickListener[] powerUpListeners = new OnClickListener[3];
    private final Label[] powerUpImages = new Label[3];

    private final Player you = PlayerManager.getYourself();

    public PowerUpUI() {
        final Vector2 worldSize = StageManager.getWorldSize();
        for (int i = 0; i < powerUpSlots.length; i++) {
            powerUpListeners[i] = new OnClickListener();
            slotImages[i] = new Image(slotTexture);
            powerUpSlots[i] = new Stack();
            powerUpSlots[i].setPosition(0, worldSize.y * (0.4f + i*0.1f));
            powerUpSlots[i].setSize(worldSize.x * 0.1f, worldSize.y * 0.1f);
            powerUpSlots[i].addListener(powerUpListeners[i]);
            powerUpSlots[i].addActor(slotImages[i]);
        }
        you.addPowerUpListener(this::update);
    }

    private void spawnInLabel(int slot, PowerUp power) {
        powerUpImages[slot] = power.spawnLabel();
        powerUpImages[slot].setAlignment(Align.center);
        powerUpSlots[slot].addActor(powerUpImages[slot]);
    }

    private void removeLabel(int slot) {
        powerUpSlots[slot].removeActor(powerUpImages[slot]);
        powerUpImages[slot] = null;
    }

    private void update(PowerUpSlotChange change) {
        switch(change.changeType()) {
            case COLLECTED -> spawnInLabel(change.slot(), change.powerUp());
            case USED -> removeLabel(change.slot());
        }
    }

    private void addPowerUpListener(final int slot, final BiConsumer<Integer, PowerUp> listener) {
        powerUpListeners[slot].chainAction(_ -> you.getPowerupInSlot(slot)
            .ifPresent(power -> listener.accept(slot, power))
        );
    }
    
    /**
     * Automatically filters scenarios where no power up exists
     * in the slot.
     */
    public void onPowerUpClicked(final BiConsumer<Integer, PowerUp> listener) {
        for (int i = 0; i < powerUpListeners.length; i++) {
            addPowerUpListener(i, listener);
        }
    }

    @Override 
    public void show() {
        StageManager.addActors(powerUpSlots);
    }

    @Override 
    public void dispose() {
        slotTexture.dispose();
    }
}
