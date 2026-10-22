package io.github.Nudgi;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.Nudgi.entities.Player;
import io.github.Nudgi.entities.TrivialEntity;
import io.github.Nudgi.network.GameClient;
import io.github.Nudgi.network.GameServer;

public class GameManager implements Disposable {
    private static final Map<Vector2, Vector2> BASE_MAPPINGS = new HashMap<>();
    static {
        BASE_MAPPINGS.put(Vector2.Zero, Vector2.One);
        BASE_MAPPINGS.put(Vector2.One, Vector2.Zero);
        BASE_MAPPINGS.put(Vector2.X, Vector2.Y);
        BASE_MAPPINGS.put(Vector2.Y, Vector2.X);
    }

    private final Stage stage;
    private final Board board;
    private Map<Player, Vector2> winningPositions = new HashMap<>();
    private Player[] players = new Player[4];
    private Player selectedPlayer = null;
    private int currentMovement;
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
        this.stage = stage;
        stage.addListener(new InputListener() {
            @Override public boolean keyTyped(InputEvent event, char c) { return handleTypedChar(c); }
            @Override public boolean keyDown(InputEvent event, int keycode) { return handleKeyDown(keycode); }
        });

        float PLAYER_IMAGE_OFFSET = board.getTileSize()/5;
        Vector2[] basePos = {
            Vector2.Zero,
            Vector2.X,
            Vector2.Y,
            Vector2.One
        };

        for (int i = 0; i < players.length; i++) {
            Vector2 boardCornerPos = board.getGridSize()
                .scl(basePos[i])
                .sub(basePos[i]);

            Vector2 worldPos = board.toWorldPos(boardCornerPos);
            float tileSize = board.getTileSize();
            Player.setPositionOffset(new Vector2(PLAYER_IMAGE_OFFSET, PLAYER_IMAGE_OFFSET));
            players[i] = new Player(worldPos.x, worldPos.y, tileSize*0.9f, tileSize*0.9f, stage, i+1);
            players[i].addListener(this::handlePlayerClick);

            Vector2 winMapping = BASE_MAPPINGS.get(basePos[i]);
            winningPositions.put(players[i], winMapping.cpy().scl(board.getGridSize()).sub(winMapping));
            board.initializePlayer(players[i], boardCornerPos);
        }
        board.setListenerAllTiles(this::handleTileClick);
        generateRandomMovement();
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
            if (p.getID().equals(id)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Failed to find player with id " + id);
    }

    void handlePlayerClick(int playerID) {
        if (started && (!networked() || playerID != myId)) { return; }
        if (selectedPlayer == null || selectedPlayer.getID().intValue() != playerID) {
            System.out.println("Player " + playerID + " selected");
            selectedPlayer = findPlayerByID(playerID);
            System.out.println(selectedPlayer + " selected");

            Set<Vector2> visited = board.pathFind(board.getPlayerPosition(selectedPlayer), currentMovement);
            System.out.println("Targets:");
            for (Vector2 moveEligible: visited) {
                System.out.println(moveEligible);
                board.fetchTile(moveEligible).setHighlighted(true);
            }
            System.out.println("Available movement: " + currentMovement);
        }
        else if (selectedPlayer.getID().equals(playerID)) {
            System.out.println(selectedPlayer + " stayed put!");
            resetTurnContext();
        }
        else {
            Player other = findPlayerByID(playerID);
            Vector2 otherPos = board.getPlayerPosition(other);
            handleTileClick(board.fetchTile(otherPos).getID());
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
                System.out.println(selectedPlayer + " stayed put!");
            }
            resetTurnContext();
        }
    }


    private void generateWinScreen(Player winner) {
        Texture background = new Texture("EmptyTile.png");
        Label winnerText = UIService.createLabel(winner + " has won!");
        TextButton baseButton = UIService.createTextButton("Exit", background, null, null);
        Viewport view = stage.getViewport();
        Vector2 topRight = new Vector2(view.getWorldWidth(), view.getWorldHeight());
        new TrivialEntity<>(baseButton, _void -> Gdx.app.exit());

        winnerText.setFontScale(2);
        winnerText.setPosition(topRight.x/2 - winnerText.getWidth(), topRight.y/1.7f);

        baseButton.setTransform(true);
        baseButton.setScale(3);
        baseButton.setPosition(topRight.x/2 - baseButton.getWidth()*1.5f, topRight.y/2.5f - baseButton.getHeight());      
        UIService.modifyTextButtonStyle(baseButton, style -> style.fontColor = Color.BLACK);  

        stage.clear();
        stage.addActor(winnerText);
        stage.addActor(baseButton);
    }

    private void applyMove(Player p, Vector2 newPos) {
        board.movePlayer(p, newPos);
        p.setPosition(board.toWorldPos(newPos));
        System.out.println("MOVED " + p + "to " + newPos);
        if (playerWon(p)) {
            System.out.println(p + " wins!");
            generateWinScreen(p);
        }
    }

    private void movePlayer(Player p, Vector2 newPos) {
        applyMove(p, newPos);
        resetTurnContext();
    }


    private boolean playerWon(Player p) {
        return board.getPlayerPosition(p).epsilonEquals(winningPositions.get(p));
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
