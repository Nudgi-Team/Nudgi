package io.github.Nudgi.network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {
    public static final int DEFAULT_PORT = 5000;

    private final int port, maxPlayers, tileCount;
    private final String boardMessage;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final Map<Integer, Integer> lastTile = new LinkedHashMap<>();
    private final Object lock = new Object();
    private ServerSocket serverSocket;
    private volatile boolean running;
    
    // Standard server attributes
    public GameServer(int port, int maxPlayers, String boardMessage, int tileCount) {
        this.port = port;
        this.maxPlayers = maxPlayers;
        this.boardMessage = boardMessage;
        this.tileCount = tileCount; // Allows for dynamic board sizes in the future
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
        for (ClientHandler c : clients) c.close();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket s = serverSocket.accept();
                s.setTcpNoDelay(true); // Establish the TCP connection and throw it on a new thread
                Thread t = new Thread(new ClientHandler(s), "GameServer-client");
                t.setDaemon(true);
                t.start();
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        }
    }

    // Threads are locked until everyone is synced
    private void broadcast(String msg) {
        for (ClientHandler c : clients) c.send(msg);
    }

    private int freeId() {
        for (int id = 0; id < maxPlayers; id++) {
            boolean used = false;
            for (ClientHandler c : clients) if (c.id == id) used = true;
            if (!used) return id;
        }
        return -1;
    }

    // This is the other side of the switch/case statement from GameManager, handles the "commands" for each client and sends to console for debugging
    // This is also where the Lock happens
    private void handleLine(ClientHandler from, String line) {
        String[] p = line.split(" ");
        if (p.length != 2) return;
        Message m;
        try { m = Message.valueOf(p[0]); }
        catch(IllegalArgumentException e) { return; }
        switch(m) {
            case MOVED: {
                int tile;
                try { tile = Integer.parseInt(p[1]); } catch (NumberFormatException e) { return; }
                if (tile < 1 || tile > tileCount) return;
                synchronized (lock) {
                    lastTile.put(from.id, tile);
                    broadcast(Message.MOVED.name() + " " + from.id +  " " + tile);
                }
                break;
            }
            case WIN:
                synchronized (lock) {
                    broadcast(Message.WIN.name() + " " + p[1]);
                }
                break;
            case SHIFT: 
                synchronized (lock) {
                    broadcast(Message.SHIFT.name() + " " + p[1]);
                }
                break;
            // (empty)
            case FULL:
            case BOARD:
            case WELCOME: 
        }
    }

    // Gets IP Address(s) for clients to type in, obviously needs to be communicated in person as it's posted in the title
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

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final PrintWriter out;
        private final BufferedReader in;
        volatile int id = -1;

        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        }

        void send(String msg) { out.println(msg); }

        void close() { try { socket.close(); } catch (IOException ignored) {} }

        @Override
        public void run() {
            try {
                synchronized (lock) {
                    int newId = freeId();
                    if (newId < 0) { send(Message.FULL.name()); return; }
                    id = newId;
                    send(Message.WELCOME.name() + " " + id);
                    if (boardMessage != null) send(boardMessage);
                    for (Map.Entry<Integer, Integer> e : lastTile.entrySet())
                        send(Message.MOVED.name() + " " + e.getKey() + " " + e.getValue());
                    clients.add(this);
                }
                String line;
                while ((line = in.readLine()) != null) handleLine(this, line);
            } catch (IOException ignored) {
            } finally {
                clients.remove(this);
                close();
            }
        }
    }
}
