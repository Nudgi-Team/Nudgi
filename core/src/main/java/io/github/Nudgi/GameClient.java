package io.github.Nudgi.network;

import java.io.IOException;
import java.util.function.Consumer;

// Game-facing client. Sockets and threads live in NetworkingBase.Client.
// Main gameplay loop is cut off and replaced with the GameServer connection once a connection is made
public class GameClient {
    private final NetworkingBase.Client net;

    public GameClient(Consumer<String> onMessage, Runnable onDisconnect) {
        this.net = new NetworkingBase.Client(onMessage, onDisconnect);
    }

    public void connect(String host, int port) throws IOException {
        net.connect(host, port);
    }

    // Connects to the default port without blocking the caller
    public void connectAsync(String host, Runnable onConnected, Consumer<IOException> onFailed) {
        net.connectAsync(host, GameServer.DEFAULT_PORT, onConnected, onFailed);
    }

    public void sendMove(int tile) {
        net.send(NetworkingBase.move(tile));
    }

    public void send(String msg) {
        net.send(msg);
    }

    public void close() {
        net.close();
    }
}
