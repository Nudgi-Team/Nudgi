package io.github.Nudgi.entities;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

import io.github.Nudgi.Corner;

/**
 * A class that oversees the lifecycle of Players.
 * Many methods within this class refer to a PlayerID. IDs are always within the range of
 * 0 (inclusive) and {@link PlayerManager#MAX_PLAYERS} (exclusive).
 * @see Player
 */
public final class PlayerManager {
    public static final int MAX_PLAYERS = 4;

    private static final Corner[] CORNERS = Corner.values();
    private static final IntConsumer DEFAULT_ACTION = _ -> {};

    // players may include null spots
    private static Player[] players = new Player[MAX_PLAYERS];
    private static IntConsumer onPlayerAdded = DEFAULT_ACTION, onPlayerRemoved = DEFAULT_ACTION;

    private PlayerManager() {}

    /**
     * Creates a new player with the specified ID.
     * @param hosting whether this player is the host.
     * @param isYou whether this player represents "you".
     * @param playerID the ID of the player, between [0, {@link #MAX_PLAYERS}).
     * @return the Player that was created.
     * @throws IndexOutOfBoundsException if {@code byID < 0 || byID >= MAX_PLAYERS} 
     * @throws DuplicateIDException if a player already exists for the given ID.
     */
    public static Player create(boolean hosting, boolean isYou, int playerID) {
        if (players[playerID] != null) { throw new DuplicateIDException(playerID); }
        players[playerID] = new Player(playerID, CORNERS[playerID], hosting, isYou);
        onPlayerAdded.accept(playerID);
        return players[playerID];
    }

    /**
     * Removes a player from the game. Has no effect if the targeted ID isn't in the game.
     * @param playerID the ID to remove, between [0, {@link #MAX_PLAYERS}).
     * @throws IndexOutOfBoundsException if {@code byID < 0 || byID >= MAX_PLAYERS} 
     */
    public static void remove(int playerID) {
        if (players[playerID] != null) { 
            onPlayerRemoved.accept(playerID);
            players[playerID].dispose();
        }
        players[playerID] = null;
    }


    private static Stream<Player> existingPlayerStream() {
        return Arrays.stream(players).filter(Objects::nonNull);
    }

    /**
     * Returns all player IDs currently existing within the game.
     * @return an array consisting of each valid ID within the game.
     * @apiNote This is the ID version of {@link #getPlayers()}.
     */
    public static int[] getPlayerIDs() {
        return existingPlayerStream()
            .mapToInt(Player::getID)
            .toArray();
    }

    public static void onPlayerAdded(IntConsumer listener, boolean chain) {
        Objects.requireNonNull(listener);
        onPlayerAdded = (chain) ? onPlayerAdded.andThen(listener) : listener;
    }

    public static void onPlayerRemoved(IntConsumer listener, boolean chain) {
        Objects.requireNonNull(listener);
        onPlayerRemoved = (chain) ? onPlayerRemoved.andThen(listener) : listener;
    }


    public static void resetOnPlayerAdded() {
        onPlayerAdded = DEFAULT_ACTION;
    }

    public static void resetOnPlayerRemoved() {
        onPlayerRemoved = DEFAULT_ACTION;
    }


    /**
     * Fetches a player based on their ID.
     * @param byID the ID of the player to fetch.
     * @return the Player object with the same ID, if one exists.
     * @throws IndexOutOfBoundsException if {@code byID < 0 || byID >= MAX_PLAYERS} 
     * @throws NoSuchElementException if a player cannot be found with the given ID.
     */
    public static Player fetchPlayer(int byID) {
        if (players[byID] == null) {
            throw new NoSuchElementException("For input player id " + byID);
        }
        return players[byID];
    }

    /**
     * Returns the Player object representing "you", aka this client.
     * @return The "you" Player object.
     * @throws IllegalStateException if no Player object is present as "you".
     */
    public static Player getYourself() {
        return existingPlayerStream()
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
        return existingPlayerStream().toArray(Player[]::new);
    }

    /**
     * Removes all Players from the game.
     */
    public static void clear() {
        existingPlayerStream().forEach(Player::dispose);
        players = new Player[MAX_PLAYERS];
    }


    public static class DuplicateIDException extends RuntimeException {
        public DuplicateIDException(int id) {
            super("Duplicate ID of " + id);
        }
    }
}
