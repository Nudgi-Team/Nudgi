package io.github.Nudgi.network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class NetworkingBase {
    private NetworkingBase() {}

    public static final int DEFAULT_PORT = 5000;
    public static final int CONNECT_TIMEOUT_MILLIS = 5000;


    public static final String FULL = "FULL";

    public static String welcome(int id) { return "WELCOME " + id; }

    public static String board(int[] paths) {
        StringBuilder sb = new StringBuilder("BOARD");
        for (int p : paths) sb.append(' ').append(p);
        return sb.toString();
    }

    public static String board(Iterable<Integer> paths) {
        StringBuilder sb = new StringBuilder("BOARD");
        for (int p : paths) sb.append(' ').append(p);
        return sb.toString();
    }

    public static String moved(int playerId, int tile) { return "MOVED " + playerId + " " + tile; }

    public static String turn(int playerId, long millis) { return "TURN " + playerId + " " + millis; }

    public static String move(int tile) { return "MOVE " + tile; }


    public interface ServerMessageHandler {
        void onWelcome(int id);
        void onBoard(int[] paths);
        void onMoved(int playerId, int tile);
        void onTurn(int playerId, long millis);
        void onFull();
    }

    public static void dispatchServerMessage(String line, ServerMessageHandler h) {
        String[] p = line.split(" ");
        switch (p[0]) {
            case "WELCOME":
                h.onWelcome(Integer.parseInt(p[1]));
                break;
            case "BOARD": {
                int[] paths = new int[p.length - 1];
                for (int i = 0; i < paths.length; i++) paths[i] = Integer.parseInt(p[i + 1]);
                h.onBoard(paths);
                break;
            }
            case "MOVED":
                h.onMoved(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                break;
            case "TURN":
                h.onTurn(Integer.parseInt(p[1]), Long.parseLong(p[2]));
                break;
            case "FULL":
                h.onFull();
                break;
        }
    }

    public static OptionalInt parseMove(String line) {
        String[] p = line.split(" ");
        if (p.length != 2 || !p[0].equals("MOVE")) return OptionalInt.empty();
        try {
            return OptionalInt.of(Integer.parseInt(p[1]));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    public static List<String> getLanAddresses() {
        List<String> result = new ArrayList<>();
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback()) continue;
                for (InetAddress a : Collections.list(ni.getInetAddresses()))
                    if (a instanceof Inet4Address) result.add(a.getHostAddress());
            }
        } catch (SocketException ignored) {}
        return result;
    }

    public static final class Peer {
        private final Socket socket;
        private final PrintWriter out;
        private final BufferedReader in;
        public volatile int id = -1;

        Peer(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        }

        public void send(String msg) { out.println(msg); }

        public void close() { try { socket.close(); } catch (IOException ignored) {} }
    }

    public static final class Server {
        public interface Listener {
            void onConnect(Peer peer);
            void onLine(Peer peer, String line);
            void onDisconnect(Peer peer);
        }

        private final int port;
        private final Listener listener;
        private final List<Peer> peers = new CopyOnWriteArrayList<>();
        private ServerSocket serverSocket;
        private volatile boolean running;

        public Server(int port, Listener listener) {
            this.port = port;
            this.listener = listener;
        }

        public void start() throws IOException {
            serverSocket = new ServerSocket(port);
            running = true;
            Thread t = new Thread(this::acceptLoop, "GameServer-accept");
            t.setDaemon(true);
            t.start();
        }

        public void stop() {
            running = false;
            try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
            for (Peer p : peers) p.close();
        }

        public void register(Peer peer) { peers.add(peer); }

        public List<Peer> peers() { return peers; }

        public void broadcast(String msg) {
            for (Peer p : peers) p.send(msg);
        }

        private void acceptLoop() {
            while (running) {
                try {
                    Socket s = serverSocket.accept();
                    s.setTcpNoDelay(true); // Establish the TCP connection and throw it on a new thread
                    Peer peer = new Peer(s);
                    Thread t = new Thread(() -> serve(peer), "GameServer-client");
                    t.setDaemon(true);
                    t.start();
                } catch (IOException e) {
                    if (running) e.printStackTrace();
                }
            }
        }

        private void serve(Peer peer) {
            try {
                listener.onConnect(peer);
                String line;
                while ((line = peer.in.readLine()) != null) listener.onLine(peer, line);
            } catch (IOException ignored) {
            } finally {
                if (peers.remove(peer)) listener.onDisconnect(peer);
                peer.close();
            }
        }
    }

    public static final class Client {
        private final Consumer<String> onLine;
        private final Runnable onDisconnect;
        private Socket socket;
        private volatile PrintWriter out;
        private volatile boolean closing;

        public Client(Consumer<String> onLine, Runnable onDisconnect) {
            this.onLine = onLine;
            this.onDisconnect = onDisconnect;
        }

        // Blocks until connected, then reads on a background thread
        public void connect(String host, int port) throws IOException {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);
            s.setTcpNoDelay(true);
            socket = s;
            out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8)), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));

            Thread t = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) onLine.accept(line);
                } catch (IOException ignored) {
                } finally {
                    if (!closing) onDisconnect.run();
                    close();
                }
            }, "GameClient-reader");
            t.setDaemon(true);
            t.start();
        }

        // Same as connect() but on its own thread so the render thread never blocks
        public void connectAsync(String host, int port, Runnable onConnected, Consumer<IOException> onFailed) {
            Thread t = new Thread(() -> {
                try {
                    connect(host, port);
                    onConnected.run();
                } catch (IOException e) {
                    onFailed.accept(e);
                }
            }, "GameClient-connect");
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
}
