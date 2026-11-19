package io.github.Nudgi.entities;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

import io.github.Nudgi.Corner;

/**
 * Represents a client connected to the game.
 * <p>
 * Players may have the following additional properties, which if true, mean the following:
 * - host: This player represents the host, or server, of the session.
 * - you: This player represents "you", defined as the Player object representing this client.
 */
public class Player extends Entity<Image> implements Disposable {
    private final Texture playerTexture = new Texture("Player.png");
    private final int playerID;
    private final boolean host;
    private final boolean you;
    private final Corner startingCorner;
    private final Deque<Integer> treasuresToHunt = new ArrayDeque<>();
    private final Deque<Integer> treasuresCollected = new ArrayDeque<>();
    private final List<Runnable> treasuresChangedListeners = new ArrayList<>();
    private final List<Consumer<PowerUpSlotChange>> powerUpChangedListeners = new ArrayList<>();
    private final PowerUp[] powerUps = new PowerUp[3];

    /**
     * Creates a new Player.
     * @param playerID a unique identifier for this instance.
     * @param corner the starting corner for the player.
     * @param isHost whether this player is the host.
     * @param isYou whether this player is "you".
     */
    public Player(int playerID, Corner corner, boolean isHost, boolean isYou) {   
        super(new Image());
        getActor().setDrawable(new TextureRegionDrawable(playerTexture));
        this.playerID = playerID;
        startingCorner = corner;
        host = isHost;
        you = isYou;
    }

    @Override 
    public String toString() {
        return "Player " + (playerID + 1);
    }

    public int getID() {
        return playerID;
    }
    
    public boolean isHost() {
        return host;
    }

    public boolean isYou() {
        return you;
    }

    public Corner getStartingCorner() {
        return startingCorner;
    }

    /**
     * Adds a new listener, notified whenever this player's remaining treasure count changes.
     * @param listener the listener to be called.
     */
    public void addTreasureListener(Runnable listener) {
        treasuresChangedListeners.add(listener);
    }

    public void addPowerUpListener(Consumer<PowerUpSlotChange> listener) {
        powerUpChangedListeners.add(listener);
    }

    public void assignTreasures(int[] treasures) {
        for (int i = 0; i < treasures.length; i++) {
            treasuresToHunt.add(treasures[i]);
        }
        treasuresChangedListeners.forEach(Runnable::run);
    }


    private <T> void moveTop(Deque<T> from, Deque<T> to) {
        to.add(from.pop());
        treasuresChangedListeners.forEach(Runnable::run);
    }

    /**
     * Collects the top treasure card of this player.
     */
    public void collectTreasure() {
        moveTop(treasuresToHunt, treasuresCollected);
        System.out.println("Treasure collected!");
    }

    private OptionalInt nextFreePowerSlot() {
        for (int i = 0; i < powerUps.length; i++) {
            if (powerUps[i] == null) { return OptionalInt.of(i); }
        }
        return OptionalInt.empty();
    }

    public void collectPowerup(PowerUp powerUp) {
        nextFreePowerSlot().ifPresent(i -> {
            powerUps[i] = powerUp;
            for (Consumer<PowerUpSlotChange> listener: powerUpChangedListeners) {
                listener.accept(new PowerUpSlotChange(powerUp, i, SlotChange.COLLECTED));
            }
        });
    }

    /**
     * Uses a powerup in the target slot.
     * @param slot the slot number, between 0 and 2.
     */
    public void usePowerUp(int slot) {
        if (powerUps[slot] != null) {
            for (Consumer<PowerUpSlotChange> listener: powerUpChangedListeners) {
                listener.accept(new PowerUpSlotChange(powerUps[slot], slot, SlotChange.USED));
            }
        }
        powerUps[slot] = null;
    }

    /**
     * Gets a power up in a specific slot, if one exists.
     * @param slot the slot number, between 0 and 2.
     * @return An Optional containing the power up in this slot, if any;
     * otherwise an empty Optional.
     */
    public Optional<PowerUp> getPowerupInSlot(int slot) {
        return Optional.ofNullable(powerUps[slot]);
    }

    /**
     * Checks to see if the treasure can be collected.
     * @param target the treasure to check.
     * @return true if the treasure can be collected. Always {@code false} if {@code target == null}.
     */
    public boolean canCollectTreasure(@Null Integer target) {
        return (!treasuresToHunt.isEmpty() && treasuresToHunt.peek().equals(target));
    }

    /**
     * Loses the most recent treasure previously collected, if the player has collected treasures.
     * @return true if a treasure was lost.
     */
    public boolean loseTreasure() {
        if (treasuresCollected.isEmpty()) { return false; }
        moveTop(treasuresCollected, treasuresToHunt);
        return true;
    }

    /**
     * Retrieves the top treasure (i.e. the one the player is currently hunting), if one exists.
     * @return An Optional containing the top treasure for this player, if any; otherwise, an empty Optional.
     */
    public Optional<Integer> topTreasure() {
        return (treasuresToHunt.isEmpty()) ? Optional.empty() : Optional.of(treasuresToHunt.peek());
    }

    /**
     * Returns the amount of treasures the player has left to hunt.
     */
    public int getTreasureCount() {
        return treasuresToHunt.size();
    }

    @Override 
    public void dispose() {
        playerTexture.dispose();
    }

    public record PowerUpSlotChange(PowerUp powerUp, int slot, SlotChange changeType) {}

    public enum SlotChange {
        COLLECTED,
        USED,
    }
}
