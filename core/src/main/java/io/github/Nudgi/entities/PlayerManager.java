package io.github.Nudgi.entities;

import java.util.Arrays;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Stream;

import io.github.Nudgi.Corner;

/**
 * A class that oversees the lifecycle of Players.
 * Many methods within this class refer to a PlayerID. IDs are always within the range of
 * [0, {@link PlayerManager#MAX_ID MAX_ID}]
 * @see Player
 */
public final class PlayerManager {
    public static final int MAX_PLAYERS = 4;
    public static final int MAX_ID = MAX_PLAYERS - 1;

    // players may include null spots.
    private static Player[] players = new Player[MAX_PLAYERS];
    private static final Corner[] corners = Corner.values();

    private PlayerManager() {}

    private static OptionalInt nextFreeID() {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }
    /**
     * Adds a new player to the game, if space is available.
     * If created, the new player will have an ID equal to the next available ID, between
     * 0 and {@link #MAX_ID}.
     * @param hosting true if this player is the host.
     * @param isYou true if this player represents "you".
     * @return true if a player was created.
     */
    public static boolean create(boolean hosting, boolean isYou) {
        OptionalInt target = nextFreeID(); 
        target.ifPresent(id -> players[id] = new Player(id, corners[id], hosting, isYou));
        return target.isPresent();
    }

    /**
     * Removes a player from the game. Has no effect if the targeted ID isn't in the game.
     * @param playerID the ID to remove, between 0 and {@link #MAX_ID}.
     */
    public static void remove(int playerID) {
        players[playerID] = null;
    }


    private static Stream<Player> existingPlayers() {
        return Arrays.stream(players).filter(Objects::nonNull);
    }

    /**
     * Returns all player IDs currently existing within the game.
     * @return an array consisting of each valid ID within the game.
     * @apiNote This is the ID version of {@link #getPlayers()}.
     */
    public static int[] getPlayerIDs() {
        return existingPlayers()
            .mapToInt(Player::getID)
            .toArray();
    }


    public static Player fetchPlayer(int byID) {
        return players[byID];
    }

    /**
     * Returns the Player object representing "you".
     * @return The "you" Player object.
     * @throws IllegalStateException if no Player object is present as "you".
     */
    public static Player getYourself() {
        return existingPlayers()
            .filter(Player::isYou)
            .findFirst()
            .orElseThrow(IllegalStateException::new);
    }

    /**
     * Returns all players currently in the game.
     * @return an array containing all players in game.
     * @see #getPlayerIDs()
     */
    public static Player[] getPlayers() {
        return existingPlayers().toArray(Player[]::new);
    }

    /**
     * Removes all Players from the game.
     */
    public static void clear() {
        players = new Player[MAX_PLAYERS];
    }

    /**
     * Returns true if no Players are currently in game.
     * @return true if no Players are in game, and false otherwise.
     */
    public static boolean empty() {
        return getPlayers().length == 0;
    }
}
