package io.github.Nudgi.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

// Game-side server logic: who gets which id, move validation, turn order.
// All socket work is delegated to NetworkingBase.Server.
public class GameServer {
    public static final int DEFAULT_PORT = NetworkingBase.DEFAULT_PORT;

    private final int maxPlayers, tileCount;
    private final String boardMessage;
    private final Map<Integer, Integer> lastTile = new LinkedHashMap<>();
    private final Object lock = new Object();
    private final NetworkingBase.Server net;
    private final TurnManager turnManager;

    // Standard server attributes
    public GameServer(int port, int maxPlayers, String boardMessage, int tileCount) {
        this.maxPlayers = maxPlayers;
        this.boardMessage = boardMessage;
        this.tileCount = tileCount; // Allows for dynamic board sizes in the future
        this.net = new NetworkingBase.Server(port, new Handler());
        this.turnManager = new TurnManager(lock, TurnManager.DEFAULT_TURN_MILLIS, this::connectedIds,
            (id, ms) -> net.broadcast(NetworkingBase.turn(id, ms)));
    }

    public void start() throws IOException {
        net.start();
    }

    public void stop() {
        synchronized (lock) { turnManager.stop(); }
        net.stop();
    }

    private int freeId() {
        for (int id = 0; id < maxPlayers; id++) {
            boolean used = false;
            for (NetworkingBase.Peer c : net.peers()) if (c.id == id) used = true;
            if (!used) return id;
        }
        return -1;
    }

    private List<Integer> connectedIds() {
        List<Integer> ids = new ArrayList<>();
        for (NetworkingBase.Peer c : net.peers()) ids.add(c.id);
        return ids;
    }

    private String currentTurnMessage() {
        int cur = turnManager.getCurrentPlayer();
        return cur == -1 ? null : NetworkingBase.turn(cur, turnManager.remainingMillis());
    }

    // This is the other side of the switch/case statement from GameManager, handles the "commands" for each client
    // This is also where the Lock happens
    private void handleMove(NetworkingBase.Peer from, String line) {
        OptionalInt parsed = NetworkingBase.parseMove(line);
        if (!parsed.isPresent()) return;
        int tile = parsed.getAsInt();
        if (tile < 1 || tile > tileCount) return;
        synchronized (lock) {
            if (!turnManager.isCurrent(from.id)) return;
            lastTile.put(from.id, tile);
            net.broadcast(NetworkingBase.moved(from.id, tile));
            turnManager.endTurn(from.id);
        }
    }

    private class Handler implements NetworkingBase.Server.Listener {
        @Override
        public void onConnect(NetworkingBase.Peer peer) {
            synchronized (lock) {
                int newId = freeId();
                if (newId < 0) {
                    peer.send(NetworkingBase.FULL);
                    peer.close();
                    return;
                }
                peer.id = newId;
                peer.send(NetworkingBase.welcome(newId));
                if (boardMessage != null) peer.send(boardMessage);
                for (Map.Entry<Integer, Integer> e : lastTile.entrySet())
                    peer.send(NetworkingBase.moved(e.getKey(), e.getValue()));
                net.register(peer);
                turnManager.refresh();
                String turn = currentTurnMessage();
                if (turn != null) peer.send(turn);
            }
        }

        @Override
        public void onLine(NetworkingBase.Peer peer, String line) {
            handleMove(peer, line);
        }

        @Override
        public void onDisconnect(NetworkingBase.Peer peer) {
            synchronized (lock) { turnManager.refresh(); }
        }
    }
}
