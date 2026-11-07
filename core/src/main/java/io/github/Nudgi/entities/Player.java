package io.github.Nudgi.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Null;

import io.github.Nudgi.Corner;

/**
 * Represents a client connected to the game.
 * <p>
 * Players may have the following additional properties, which if true, mean the following:
 * - host: This player represents the host, or server, of the session.
 * - you: This player represents "you", defined as the Player object representing this client.
 */
public class Player extends Entity<Integer, Image> {
    private static final Texture playerTexture = new Texture("Player.png");

    public final int playerID;

    private final boolean host;
    private final boolean you;
    private final Corner startingCorner;
    private final Stack<Integer> treasuresToHunt = new Stack<>();
    private final Stack<Integer> treasuresCollected = new Stack<>();
    private final List<Runnable> treasuresChangedListeners = new ArrayList<>();
    
    /**
     * Creates a new Player.
     * @param playerID a unique identifier for this instance.
     * @param corner the starting corner for the player.
     * @param isHost whether this player is the host.
     * @param isYou whether this player is "you".
     */
    public Player(int playerID, Corner corner, boolean isHost, boolean isYou) {   
        super(new Image(playerTexture), playerID);
        this.playerID = playerID;
        startingCorner = corner;
        host = isHost;
        you = isYou;
    }

    @Override 
    public String toString() {
        return "Player " + (getID() + 1);
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

    public void assignTreasures(int[] treasures) {
        for (int i = 0; i < treasures.length; i++) {
            treasuresToHunt.add(treasures[i]);
        }
    }


    private <T> void moveTop(Stack<T> from, Stack<T> to) {
        to.add(from.pop());
        treasuresChangedListeners.forEach(Runnable::run);
    }

    /**
     * Attempts to collect the target treasure, succeeding if it matches the player's top treasure card.
     * @param target the treasure being collected.
     * @return {@code true} if the treasure was 
     * successfully collected by the player. Always {@code false} if {@code target == null}.
     */
    public boolean collectTreasure(@Null Integer target) {
        if (treasuresToHunt.empty() || !treasuresToHunt.peek().equals(target)) { return false; }
        moveTop(treasuresToHunt, treasuresCollected);
        System.out.println("Treasure collected!");
        return true;
    }

    /**
     * Loses the most recent treasure previously collected, if the player has collected treasures.
     * @return true if a treasure was lost.
     */
    public boolean loseTreasure() {
        if (treasuresCollected.empty()) { return false; }
        moveTop(treasuresCollected, treasuresToHunt);
        return true;
    }

    /**
     * Retrieves the top treasure (i.e. the one the player is currently hunting), if one exists.
     * @return An Optional containing the top treasure for this player, if any; otherwise, an empty Optional.
     */
    public Optional<Integer> topTreasure() {
        return (treasuresToHunt.empty()) ? Optional.empty() : Optional.of(treasuresToHunt.peek());
    }

    /**
     * Returns the amount of treasures the player has left to hunt.
     */
    public int numTreasuresLeft() {
        return treasuresToHunt.size();
    }
}
