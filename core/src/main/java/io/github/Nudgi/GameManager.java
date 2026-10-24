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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.Nudgi.entities.Player;
import io.github.Nudgi.entities.TrivialEntity;
import io.github.Nudgi.entities.ShiftArrow.ShiftID;
import io.github.Nudgi.network.GameClient;
import io.github.Nudgi.network.GameServer;
import io.github.Nudgi.network.Message;
import io.github.Nudgi.network.TurnStep;

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
    private final Vector2[] winningPositions = new Vector2[4];
    private Player[] players = new Player[4];
    private final Consumer<Runnable> mainThread;   // runs something on the render thread
    private volatile GameServer server;
    private volatile GameClient client;
    private volatile boolean started;              // host or join was chosen
    private volatile boolean rejected;             // server said FULL
    private volatile String status = "Press H to host, J to join";
    private volatile String hostInfo = "";
    private TurnStep step;
    private String shownStatus;
    private int myId = -1;
    private int turnId = -1;
    private long turnDeadline;
    private boolean typing;
    private final StringBuilder ipBuffer = new StringBuilder();
    private final Label turnPlayerLabel = UIService.createLabel(null);
    private final Label turnStepLabel = UIService.createLabel(null);
    private final Label lobbyLabel = UIService.createLabel("See above for instructions.");
    private final Image lobbyPointer = new Image(new Texture("Arrow.png"));

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

        Viewport vp = stage.getViewport();
        lobbyLabel.setFontScale(1.7f);
        UIService.centerWidget(vp, lobbyLabel, true, true);
        lobbyPointer.setPosition(vp.getWorldWidth()*0.1f, vp.getWorldHeight()*0.8f);
        lobbyPointer.setSize(vp.getWorldWidth()*0.15f, vp.getWorldHeight()*0.15f);

        stage.addActor(lobbyLabel);
        stage.addActor(lobbyPointer);
    }

    public void update() {
        if (!started && !typing) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.H)) host();
            else if (Gdx.input.isKeyJustPressed(Input.Keys.J)) startTyping();
        }
        String s = status;
        if (networked() && turnId >= 0) {
            long secs = Math.max(0, (turnDeadline - System.currentTimeMillis() + 999) / 1000);
            s += myTurn() ? "  |  YOUR TURN (" + secs + "s)" : "  |  Player " + getPlayerDisplayID(turnId) + "'s turn (" + secs + "s)";
            turnPlayerLabel.setText(s);
        }
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

    private void startGame() {
        stage.clear();

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
            players[i] = new Player(worldPos.x, worldPos.y, tileSize*0.9f, tileSize*0.9f, stage, i);
            players[i].addListener(this::handlePlayerClick);

            Vector2 winMapping = BASE_MAPPINGS.get(basePos[i]);
            winningPositions[i] = winMapping.cpy().scl(board.getGridSize()).sub(winMapping);
            board.initializePlayer(players[i], boardCornerPos);
        }

        board.forEachTile((pos, tile) -> tile.addListener(this::handleTileClick));
        board.forEachTile((pos, tile) -> stage.addActor(tile.getActor()));
        board.forEachArrow(arrow -> arrow.addListener(this::handleArrowClick));
        board.forEachArrow(arrow -> stage.addActor(arrow.getActor()));

        stage.addActor(turnPlayerLabel);
        stage.addActor(turnStepLabel);

        Viewport vp = stage.getViewport();

        turnPlayerLabel.setPosition(0.05f*vp.getWorldWidth(), vp.getWorldHeight()*0.95f);
        turnStepLabel.setY(vp.getWorldHeight()*0.05f);
        UIService.centerWidget(vp, turnStepLabel, true, false);
    }

    private void startTyping() {
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

    private boolean handleTypedChar(char c) {
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

    private boolean handleKeyDown(int keycode) {
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
        turnId = -1;
        if (!rejected) {
            status = "Disconnected from host";
            endGame("You have been disconnected from the server.");
        }
    }

    private boolean networked() {
        return client != null && myId >= 0;
    }

    private boolean myTurn() {
        return networked() && turnId == myId;
    }

    // Throw it into a string and decode on the other side
    // Not as efficient as communicating only changes but prevents partial desyncs
    private String encodeBoard() {
        StringBuilder sb = new StringBuilder(Message.BOARD.name());
        for (int p : board.getAllPaths()) sb.append(' ').append(p);
        return sb.toString();
    }

    // Simple communication dictionary for communicating between server and clients
    private void handleMessage(String line) {
        if (players == null) return;
        String[] p = line.split(" ");
        try {
            Message m = Message.valueOf(p[0]);
            switch (m) {
                case WELCOME:
                    myId = Integer.parseInt(p[1]);
                    status = "You are player " + getPlayerDisplayID(myId) + hostInfo;
                    if (server != null) {
                        lobbyLabel.setText("Waiting for players...");
                    }
                    break;
                case BOARD: {
                    int[] paths = new int[p.length - 1];
                    for (int i = 0; i < paths.length; i++) paths[i] = Integer.parseInt(p[i + 1]);
                    board.applyAllPaths(paths);
                    break;
                }
                case PLAYER_MOVED: {
                    Player player = findPlayerByID(Integer.parseInt(p[1]));
                    Board.TileInfo target = board.fetchTile(Integer.parseInt(p[2]));
                    applyMove(player, target.pos);
                    board.forEachTile((pos, tile) -> tile.setHighlighted(false));
                    if (playerWon(player) && myId == player.getID().intValue()) {
                        client.send(Message.WIN.name() + " " + player.getID());
                    }
                    break;
                }
                case TURN:
                    turnId = Integer.parseInt(p[1]);
                    turnDeadline = System.currentTimeMillis() + Long.parseLong(p[2]);
                    break;
                case FULL:
                    rejected = true;
                    started = false;
                    status = "Game room is full";
                    break;
                case WIN: {
                    Player player = findPlayerByID(Integer.parseInt(p[1]));
                    endGame(player + " has won!");
                    break;
                }
                case SHIFT: {
                    String[] shiftid = p[1].split("\\.");
                    Direction dir = Direction.valueOf(shiftid[0]);
                    int index = Integer.parseInt(shiftid[1]);
                    board.shift(index, dir);
                    break;
                }
                case STEP: {
                    updateStep(TurnStep.valueOf(p[1]));
                    if (myTurn()) {
                        handleTurnStep(step);
                    }
                    break;
                }
                case START: 
                    startGame();
                    break;
            }
        } catch (RuntimeException e) {
            System.err.println("Bad message from server: " + line + " (" + e + ")");
        }
    }

    private void generateRandomMovement() {
        int currentMovement = ThreadLocalRandom.current().nextInt(1, 7);

        Set<Vector2> visited = board.pathFind(board.getPlayerPosition(getTurnPlayer()), currentMovement);
        System.out.println("Targets:");
        for (Vector2 moveEligible: visited) {
            System.out.println(moveEligible);
            board.fetchTile(moveEligible).setHighlighted(true);
        }
    }

    private void updateStep(TurnStep newStep) {
        step = newStep;
        turnStepLabel.setText(step.name().toLowerCase());
    }

    private void handleTurnStep(TurnStep newStep) {
        System.out.println("STEP: " + newStep.name());
        switch(newStep) {
            case SHIFTING: {
                // In the future, you could put an animation for the arrows here
                // to indicate that they can be clicked
                break;
            }
            case MOVING: {
                board.forEachArrow(arrow -> arrow.getActor().clearActions());
                generateRandomMovement();
                break;
            }
        }
    }

    private Player findPlayerByID(int id) {
        for (Player p: players) {
            if (p != null && p.getID().equals(id)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Failed to find player with id " + id);
    }

    private void handlePlayerClick(int targetPlayerID) {
        // Possibly change this to a tooltip in the future showing the player id and their
        // existing powerups 
        System.out.println("Player clicked " + targetPlayerID);
    }

    private void handleTileClick(int tileID) {
        if (!myTurn() || step != TurnStep.MOVING) { return; }

        Board.TileInfo target = board.fetchTile(tileID);
        if (!target.tile.isHighlighted()) { return; }

        client.send(Message.PLAYER_MOVED.name() + " " + tileID);
    }

    private void handleArrowClick(ShiftID sid) {
        if (!myTurn() || step != TurnStep.SHIFTING) { return; }
        client.send(Message.SHIFT.name() + " " + sid.dir.name() + "." + sid.index);
    }


    private void endGame(String withMessage) {
        status = "Game has ended";
        Texture background = new Texture("EmptyTile.png");
        Label message = UIService.createLabel(withMessage);
        TextButton baseButton = UIService.createTextButton("Exit", background, null, null);
        Viewport view = stage.getViewport();
        Vector2 topRight = new Vector2(view.getWorldWidth(), view.getWorldHeight());
        new TrivialEntity<>(baseButton, Gdx.app::exit);
        
        message.setFontScale(1.7f);
        UIService.centerWidget(view, message, true, false);
        message.setY(topRight.y/1.7f);

        baseButton.setTransform(true);
        baseButton.setScale(3);
        baseButton.setPosition(topRight.x/2 - baseButton.getWidth()*1.5f, topRight.y/2.5f - baseButton.getHeight());      
        UIService.modifyTextButtonStyle(baseButton, style -> style.fontColor = Color.BLACK);  

        stage.clear();
        stage.addActor(message);
        stage.addActor(baseButton);
    }

    private void applyMove(Player p, Vector2 newPos) {
        board.movePlayer(p, newPos);
        System.out.println("MOVED " + p + " to " + newPos);
    }


    private boolean playerWon(Player p) {
        return board.getPlayerPosition(p).epsilonEquals(winningPositions[p.getID()]);
    }

    private Player getTurnPlayer() {
        return findPlayerByID(turnId);
    }

    private int getPlayerDisplayID(int fromId) {
        return ++fromId;
    }

    // Debugging
    int getMyId() { return myId; }
}
