package io.github.Nudgi.entities;

import java.util.Arrays;
import java.util.Objects;
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

    // players may include null spots
    private static Player[] players = new Player[MAX_PLAYERS];
    private static final Corner[] corners = Corner.values();

    private PlayerManager() {}

    /**
     * Creates a new player with the specified ID.
     * @param hosting whether this player is the host.
     * @param isYou whether this player represents "you".
     * @param playerID the ID of the player, between 0 and {@link #MAX_ID}.
     * @return the Player that was created.
     * @throws DuplicateIDException if a player already exists for the given ID.
     */
    public static Player create(boolean hosting, boolean isYou, int playerID) {
        if (players[playerID] != null) { throw new DuplicateIDException(playerID); }
        players[playerID] = new Player(playerID, corners[playerID], hosting, isYou);
        return players[playerID];
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
     * Returns the Player object representing "you", aka this client.
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

    public static class DuplicateIDException extends RuntimeException {
        public DuplicateIDException(int id) {
            super("Duplicate ID of " + id);
        }
    }
}
