package io.github.Nudgi.network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.Nudgi.MessageHandler;
import io.github.Nudgi.NetworkController;
import io.github.Nudgi.network.protocol.*;
import io.github.Nudgi.network.protocol.Message.MessageDeserializationException;

public class GameServer implements MessageHandler {
    public static final int DEFAULT_PORT = 5000;

    private final int port, maxPlayers, tileCount;
    private final BoardMessage boardMessage;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final Object lock = new Object();
    private final TurnManager turnManager;
    private ServerSocket serverSocket;
    private volatile boolean running;
    private boolean gameStarted = false;
    
    // Standard server attributes
    public GameServer(int port, int maxPlayers, BoardMessage boardMessage, int tileCount) {
        this.port = port;
        this.maxPlayers = maxPlayers;
        this.boardMessage = boardMessage;
        this.tileCount = tileCount; // Allows for dynamic board sizes in the future
        this.turnManager = new TurnManager(this::connectedIds,
            id -> {
                broadcast(new TurnMessage(id));
                broadcast(new StepMessage(TurnStep.SHIFTING));
            });
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
    private void broadcast(Message msg) {
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

    private List<Integer> connectedIds() {
        List<Integer> ids = new ArrayList<>();
        for (ClientHandler c : clients) ids.add(c.id);
        return ids;
    }

    @Override
    public void onPlayerMoved(int from, PlayerMovedMessage m) {
        if (!turnManager.isCurrent(from) || turnManager.getStep() != TurnStep.MOVING) return;

        int tile = m.getTileID();
        broadcast(new PlayerMovedMessage(tile, from));
        turnManager.endTurn(from);
    }

    @Override 
    public void onWin(int from, WinMessage m) {
        broadcast(m);
        turnManager.setStep(null);
    }

    @Override 
    public void onShift(int from, ShiftMessage m) {
        if (!turnManager.isCurrent(from) || turnManager.getStep() != TurnStep.SHIFTING) { return; }
        broadcast(m);
        turnManager.setStep(TurnStep.MOVING);
        broadcast(new StepMessage(TurnStep.MOVING));
    }

    @Override 
    public void onStart(int from) {
        if (from == 0) {
            broadcast(new StartNotification());
            turnManager.refresh();
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
        private NetworkController controller;

        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        }

        void send(Message msg) { out.println(msg); }

        void close() { try { socket.close(); } catch (IOException ignored) {} }

        @Override
        public void run() {
            try {
                synchronized (lock) {
                    int newId = freeId();
                    if (newId < 0) { send(new FullNotification()); return; }
                    id = newId;
                    send(new WelcomeMessage(id));
                    if (boardMessage != null) send(boardMessage);
                    clients.add(this);
                    if (clients.size() >= 2) { // Don't start until 2+ players
                        if (!gameStarted) { // On first start, notify both host and the other player 
                            gameStarted = true;
                            broadcast(new StartNotification());
                        }
                        else { // Afterwards, notify only new players entering
                            send(new StartNotification());
                        }
                        turnManager.refresh();
                    }
                    controller = new NetworkController(GameServer.this, id);
                    controller.synchronize(lock); 
                }
                String line;
                while ((line = in.readLine()) != null) { 
                    try {
                        controller.fire(Message.deserialize(line));
                    }
                    catch (MessageDeserializationException _) {}
                }
            } catch (IOException ignored) {
            } finally {
                clients.remove(this);
                synchronized (lock) { turnManager.refresh(); }
                close();
            }
        }
    }
}
