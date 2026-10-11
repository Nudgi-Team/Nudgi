package io.github.Nudgi;


import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

import io.github.Nudgi.network.GameClient;
import io.github.Nudgi.network.GameServer;

public class GameManager implements Disposable {
    private Player[] players = new Player[4];
    private Player selectedPlayer = null;
    private Board board;
    private int currentMovement;
    private final float playerOffsetFactor;
    private final Consumer<Runnable> mainThread;   // runs something on the render thread
    private volatile GameServer server;
    private volatile GameClient client;
    private volatile boolean started;              // host or join was chosen
    private volatile boolean rejected;             // server said FULL
    private volatile String status = "Press H to host, J to join";
    private volatile String hostInfo = "";
    private String shownStatus;
    private int myId = -1;
    private boolean typing;
    private final StringBuilder ipBuffer = new StringBuilder();

    public GameManager(Board board, Stage stage) {
        this(board, stage, r -> Gdx.app.postRunnable(r));
    }

    public GameManager(Board board, Stage stage, Consumer<Runnable> mainThread) {
        this.mainThread = mainThread;
        this.board = board;
        stage.addListener(new InputListener() {
            @Override public boolean keyTyped(InputEvent event, char c) { return handleTypedChar(c); }
            @Override public boolean keyDown(InputEvent event, int keycode) { return handleKeyDown(keycode); }
        });
        playerOffsetFactor = board.getTileSize()/5;
        Vector2[] cornerPositions = {
            Vector2.Zero,
            Vector2.X,
            Vector2.Y,
            Vector2.One
        };

        for (int i = 0; i < players.length; i++) {
            cornerPositions[i] = board.getGridSize()
                .scl(cornerPositions[i])
                .sub(cornerPositions[i]);

            Vector2 worldPos = board.toWorldPos(cornerPositions[i]);
            float tileSize = board.getTileSize();

            players[i] = new Player(worldPos.x + playerOffsetFactor, worldPos.y + playerOffsetFactor, tileSize*0.9f, tileSize*0.9f, stage, i);
            players[i].setListener(this::handlePlayerClick);
            board.initializePlayer(players[i], cornerPositions[i]);
        }
        board.setListenerAllTiles(this::handleTileClick);
        generateRandomMovement();
    }

    public void draw(SpriteBatch batch) {
        board.draw(batch);
    }

    public void update() {
        if (!started && !typing) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.H)) host();
            else if (Gdx.input.isKeyJustPressed(Input.Keys.J)) startTyping();
        }
        String s = status;
        if (!s.equals(shownStatus)) {
            shownStatus = s;
            Gdx.graphics.setTitle("Nudgi - " + s);
        }
    }

    @Override
    public void dispose() {
        GameClient c = client;
        if (c != null) c.close();
        GameServer s = server;
        if (s != null) s.stop();
        Player.dispose();
        players = null;
    }

    // Standard TCP LAN Connection
    public void host() {
        if (started) return;
        started = true;
        try {
            Vector2 size = board.getGridSize();
            server = new GameServer(GameServer.DEFAULT_PORT, players.length, encodeBoard(), (int) (size.x * size.y));
            server.start();
            hostInfo = " - hosting on " + GameServer.getLanAddresses();
            System.out.println("Hosting. Others join with one of: " + GameServer.getLanAddresses());
            connectTo("127.0.0.1"); // Lets the game start hosted "offline" so the game doesn't just freeze
        } catch (IOException e) {
            status = "Host failed: " + e.getMessage();
            server = null;
            started = false;
        }
    }


    public void join(String hostAddress) {
        if (started) return;
        started = true;
        connectTo(hostAddress);
    }

    void startTyping() {
        typing = true;
        ipBuffer.setLength(0);
        showTyping();
    }

    private void showTyping() {
        status = "Join IP: " + ipBuffer + "_   (Enter = connect, Esc = cancel)";
    }

    private void appendFiltered(CharSequence s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '.' || c == '-') ipBuffer.append(c);
        }
    }

    boolean handleTypedChar(char c) {
        if (!typing) return false;
        if (c == '\r' || c == '\n') {
            typing = false;
            String ip = ipBuffer.toString().trim();
            join(ip.isEmpty() ? "localhost" : ip);
        } else if (c == '\b') {
            if (ipBuffer.length() > 0) ipBuffer.setLength(ipBuffer.length() - 1);
            showTyping();
        } else {
            appendFiltered(String.valueOf(c));
            showTyping();
        }
        return true;
    }

    boolean handleKeyDown(int keycode) {
        if (!typing) return false;
        if (keycode == Input.Keys.ESCAPE) {
            typing = false;
            status = "Press H to host, J to join";
            return true;
        }
        boolean ctrl = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
        // Code for handling copy and paste since it's not a true text bar
        if (keycode == Input.Keys.V && ctrl) {
            String clip = Gdx.app.getClipboard().getContents();
            if (clip != null) appendFiltered(clip.trim());
            showTyping();
            return true;
        }
        return false;
    }

    private void connectTo(String host) {
        status = "Connecting to " + host + "...";
        GameClient c = new GameClient(
            line -> mainThread.accept(() -> handleMessage(line)),
            () -> mainThread.accept(this::onDisconnected));
        new Thread(() -> {
            try {
                c.connect(host, GameServer.DEFAULT_PORT);
                client = c;
            } catch (IOException e) {
                status = "Could not connect: " + e.getMessage();
                started = false;
            }
        }, "GameClient-connect").start();
    }

    private void onDisconnected() {
        client = null;
        myId = -1;
        if (!rejected) status = "Disconnected from host";
    }

    private boolean networked() {
        return client != null && myId >= 0;
    }

    // Throw it into a string and decode on the other side
    // Not as efficient as communicating only changes but prevents partial desyncs
    private String encodeBoard() {
        StringBuilder sb = new StringBuilder("BOARD");
        for (int p : board.getAllPaths()) sb.append(' ').append(p);
        return sb.toString();
    }

    // Simple communication dictionary for communicating between server and clients
    void handleMessage(String line) {
        if (players == null) return;
        String[] p = line.split(" ");
        try {
            switch (p[0]) {
                case "WELCOME":
                    myId = Integer.parseInt(p[1]);
                    status = "You are player " + myId + hostInfo;
                    break;
                case "BOARD": {
                    int[] paths = new int[p.length - 1];
                    for (int i = 0; i < paths.length; i++) paths[i] = Integer.parseInt(p[i + 1]);
                    board.applyAllPaths(paths);
                    break;
                }
                case "MOVED": {
                    Player player = findPlayerByID(Integer.parseInt(p[1]));
                    Board.TileInfo target = board.fetchTile(Integer.parseInt(p[2]));
                    applyMove(player, target.pos);
                    break;
                }
                case "FULL":
                    rejected = true;
                    started = false;
                    status = "Game room is full";
                    break;
            }
        } catch (RuntimeException e) {
            System.err.println("Bad message from server: " + line + " (" + e + ")");
        }
    }

    private void generateRandomMovement() {
        currentMovement = ThreadLocalRandom.current().nextInt(1, 7);
    }

    Player findPlayerByID(int id) {
        for (Player p: players) {
            if (id == p.playerID) {
                return p;
            }
        }
        throw new IllegalArgumentException("Failed to find player with id " + id);
    }

    void handlePlayerClick(int playerID) {
        if (started && (!networked() || playerID != myId)) { return; }
        if (selectedPlayer == null || selectedPlayer.playerID != playerID) {
            System.out.println("Player " + playerID + " selected");
            selectedPlayer = findPlayerByID(playerID);

            Set<Vector2> visited = new HashSet<>();
            Stack<Vector2> explore = new Stack<>();
            explore.add(board.getPlayerPosition(selectedPlayer));
            for (int i = 0; i <= currentMovement; i++) {
                // Start the queue and thus first iteration with the player's own tile.
                // On each subsequent iteration, the queue will begin with all adjacent tiles found
                // in the previous iteration, excluding tiles visited in prior iterations.
                // All tiles visited will be marked, including the starting square.
                // Each iteration uses up one movement, with the first iteration only marking the starting square.
                Stack<Vector2> found = new Stack<>();
                while (!explore.empty()) {
                    Vector2 pos = explore.pop();
                    visited.add(pos);
                    List<Vector2> adjacent = board.getAdjacentPositions(pos, true);
                    for (Vector2 item: adjacent) {
                        if (!visited.contains(item)) {
                            found.add(item);
                        }
                    }
                }
                explore.addAll(found);
            }
            System.out.println("Targets:");
            for (Vector2 moveEligible: visited) {
                System.out.println(moveEligible);
                board.fetchTile(moveEligible).setHighlighted(true);
            }
            System.out.println("Available movement: " + currentMovement);
        }
        else {
            System.out.println("Player " + selectedPlayer.playerID + " stayed put!");
            resetTurnContext();
        }
    }

    void handleTileClick(int tileID) {
        if (selectedPlayer == null) { return; }
        Board.TileInfo target = board.fetchTile(tileID);
        if (target.tile.isHighlighted()) {
            if (!target.tile.containsPlayer(selectedPlayer)) {
                if (started) {
                    if (networked()) client.send("MOVE " + tileID);
                    resetTurnContext();
                } else {
                    movePlayer(selectedPlayer, target.pos);
                }
            }
            else {
                System.out.println("Player " + selectedPlayer.playerID + " stayed put!");
                resetTurnContext();
            }
        }
    }

    private void applyMove(Player p, Vector2 newPos) {
        board.movePlayer(p, newPos);
        p.moveImage(board.toWorldPos(newPos).add(playerOffsetFactor, playerOffsetFactor));
        System.out.println("MOVED player id " + p.playerID + " to " + newPos);
    }

    private void movePlayer(Player p, Vector2 newPos) {
        applyMove(p, newPos);
        resetTurnContext();
    }

    private void resetTurnContext() {
        board.clearHighlights();
        generateRandomMovement();
        selectedPlayer = null;
    }

    // Debugging
    int getMyId() { return myId; }
    Player getSelectedPlayer() { return selectedPlayer; }
}
