package io.github.Nudgi.network;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class GameClient {
    private final Consumer<String> onMessage;
    private final Runnable onDisconnect;
    private Socket socket;
    private volatile PrintWriter out;
    private volatile boolean closing;

    public GameClient(Consumer<String> onMessage, Runnable onDisconnect) {
        this.onMessage = onMessage;
        this.onDisconnect = onDisconnect;
    }

    // Main gameplay loop is cut off and replaced with the GameServer connection once a connection is made
    // Sent to a new thread
    public void connect(String host, int port) throws IOException {
        Socket s = new Socket();
        s.connect(new InetSocketAddress(host, port), 5000);
        s.setTcpNoDelay(true);
        socket = s;
        out = new PrintWriter(new BufferedWriter(
            new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8)), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));

        Thread t = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) onMessage.accept(line);
            } catch (IOException ignored) {
            } finally {
                if (!closing) onDisconnect.run();
                close();
            }
        }, "GameClient-reader");
        t.setDaemon(true);
        t.start();
    }

    public void send(String msg) {
        PrintWriter o = out;
        if (o != null) o.println(msg);
    }

    public void close() {
        closing = true;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }
}
