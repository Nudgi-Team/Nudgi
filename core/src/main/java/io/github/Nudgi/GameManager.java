package io.github.Nudgi;


import static io.github.Nudgi.ui.StageManager.STAGE;
import static io.github.Nudgi.ui.StageManager.VIEW;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;

import io.github.Nudgi.entities.Player;
import io.github.Nudgi.entities.PlayerManager;
import io.github.Nudgi.entities.ShiftID;
import io.github.Nudgi.entities.Tile;
import io.github.Nudgi.network.GameClient;
import io.github.Nudgi.network.GameServer;
import io.github.Nudgi.network.TurnStep;
import io.github.Nudgi.ui.StageManager;
import io.github.Nudgi.ui.UIManager;
import io.github.Nudgi.ui.UIService;
import io.github.Nudgi.ui.StageManager.Mode;
import io.github.Nudgi.ui.game.BoardUI;
import io.github.Nudgi.ui.game.FullScreenPopUpUI;
import io.github.Nudgi.ui.lobby.LobbyUI;
import io.github.Nudgi.network.protocol.*;
import io.github.Nudgi.network.protocol.Message.MessageDeserializationException;
import io.github.Nudgi.network.protocol.TreasureMessage.Delta;

public class GameManager implements Disposable, MessageHandler {
    private final Board board;
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
    private boolean typing;
    private final StringBuilder ipBuffer = new StringBuilder();
    private final Label turnPlayerLabel = UIService.createLabel(null);
    private final Label turnStepLabel = UIService.createLabel(null);
    private boolean devMode = false;
    private Consumer<Message> dispatcher;
    private UIManager ui;
    
    
    public GameManager(Board board) {
        this(board, r -> Gdx.app.postRunnable(r));
    }

    public GameManager(Board board, Consumer<Runnable> mainThread) {
        this.mainThread = mainThread;
        this.board = board;
        this.ui = new UIManager(board);
        
        StageManager.setStageMode(Mode.LOBBY);
        LobbyUI lobby = ui.getLobbyUI();
        lobby.onDevModeClick(this::beginDevMode);
        lobby.onStartClick(() -> client.send(new StartNotification()));

        STAGE.addListener(new InputListener() {
            @Override public boolean keyTyped(InputEvent event, char c) { return handleTypedChar(c); }
            @Override public boolean keyDown(InputEvent event, int keycode) { return handleKeyDown(keycode); }
        });
    }

    public void update() {
        if (!started && !typing) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.H)) host();
            else if (Gdx.input.isKeyJustPressed(Input.Keys.J)) startTyping();
        }
        String s = status;
        if (networked() && turnId >= 0) {
            s += myTurn() ? "  |  YOUR TURN" : "  |  " + PlayerManager.fetchPlayer(turnId) + "'s turn";
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
    }

    private TreasureTileInfo[] generateTreasures() {
        TreasureTileInfo[] results = new TreasureTileInfo[TreasureManager.TREASURE_COUNT];
        Tile[] tiles = board.randomTilesWithPaths(results.length, true);
        for (int i = 0; i < results.length; i++) {
            results[i] = new TreasureTileInfo(i, tiles[i].getID());
        }
        return results;
    }

    private void beginDevMode() {
        devMode = true;
        host();
        status = "Developer Mode";
    }

    private void startGame() {
        StageManager.setStageMode(Mode.GAME);

        BoardUI boardUI = ui.getGameUI().board();
        boardUI.onTileClicked(this::handleTileClick);
        boardUI.onarrowClicked(this::handleArrowClick);

        uiInit();
    }

    private void startTyping() {
        typing = true;
        ipBuffer.setLength(0);
        showTyping();
    }


    private void uiInit() {
        STAGE.addActor(turnPlayerLabel);
        STAGE.addActor(turnStepLabel);

        turnPlayerLabel.setPosition(0.05f*VIEW.getWorldWidth(), VIEW.getWorldHeight()*0.95f);
        turnStepLabel.setY(VIEW.getWorldHeight()*0.05f);
        UIService.centerWidget(turnStepLabel, true, false);
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
                if (devMode) {
                    client.send(new StartNotification());
                }
            } catch (IOException e) {
                status = "Could not connect: " + e.getMessage();
                started = false;
            }
        }, "GameClient-connect").start();
    }

    // Standard TCP LAN Connection
    public void host() {
        if (started) return;
        started = true;
        try {
            Vector2 size = board.getGridSize();
            server = new GameServer(GameServer.DEFAULT_PORT, PlayerManager.MAX_PLAYERS, new BoardMessage(board.getAllPaths(), generateTreasures()), (int) (size.x * size.y));
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

    @Override 
    public void onWelcome(int from, WelcomeMessage m) {
        myId = m.getNewID();
        status = "You are player " + getPlayerDisplayID(myId) + hostInfo;
        if (server != null) {
            LobbyUI lobby = ui.getLobbyUI();
            lobby.waitingMode();
        }
    }

    @Override 
    public void onBoard(int from, BoardMessage m) {
        int[] paths = m.getPaths();
        TreasureTileInfo[] treasures = m.getTreasureInfo();
        board.applyAllPaths(paths);
        board.placeTreasures(treasures);
    }

    @Override 
    public void onPlayerMoved(int from, PlayerMovedMessage m) {
        Player player = PlayerManager.fetchPlayer(m.getPlayerID());
        TileInfo target = board.fetchTile(m.getTileID());
        applyMove(player, target.pos());
        clearHighlights();
        generateRandomMovement();
        if (playerWon(player) && myId == player.getID()) {
            client.send(new WinMessage(player.getID()));
        }
    }

    @Override 
    public void onTurn(int from, TurnMessage m) {
        turnId = m.getTurnPlayerID();
        clearHighlights();
    }

    @Override 
    public void onFull(int from) {
        rejected = true;
        started = false;
        status = "Game room is full";
    }

    @Override  
    public void onWin(int from, WinMessage m) {
        Player player = PlayerManager.fetchPlayer(m.getWinnerID());
        endGame(player + " has won!");
    }

    @Override 
    public void onShift(int from, ShiftMessage m) {
        ShiftID id = m.getShiftID();
        board.shift(id);
    }

    @Override
    public void onStep(int from, StepMessage m) {
        updateStep(m.getStep());
        if (myTurn()) {
            handleTurnStep(step);
        }
    }

    @Override 
    public void onStart(int from) {
        startGame();
    }

    @Override 
    public void onNewPlayer(int from, NewPlayerMessage m) {
        int newPlayerID = m.getNewPlayerID();
        Player newPlayer = PlayerManager.create(newPlayerID == 0, newPlayerID == myId, newPlayerID);
        board.initializePlayer(newPlayer);
    }

    @Override 
    public void onTreasure(int from, TreasureMessage m) {
        int collectorId = m.getCollectorID();
        Player targetPlayer = PlayerManager.fetchPlayer(collectorId);
        switch (m.getTreasureCountDelta()) {
            case LOSE -> targetPlayer.loseTreasure(); // Supports enemies
            case COLLECT -> targetPlayer.collectTreasure();
        }
    }

    @Override 
    public void onTreasureInit(int from, TreasureInitMessage m) {
        int targetPlayer = m.getPlayerID();
        int[] treasures = m.getTreasures();
        PlayerManager.fetchPlayer(targetPlayer).assignTreasures(treasures);
    }

    // Simple communication dictionary for communicating between server and clients
    // Client receiver
    private void handleMessage(String line) {
        try {
            Message msg = Message.deserialize(line);
            System.out.println("Message received of type " + msg.getCategory());
            dispatcher.accept(msg);
        }
        catch (MessageDeserializationException e) {
            System.err.println("Bad message from server: " + line + " (" + e + ")");
        }
    }

    public void setDispatcher(Consumer<Message> dispatcher) {
        this.dispatcher = dispatcher;
    }

    private void generateRandomMovement() {
        int currentMovement = ThreadLocalRandom.current().nextInt(1, 7);

        Set<Vector2> visited = board.pathFind(board.getPlayerPosition(PlayerManager.getYourself()), currentMovement);
        for (Vector2 moveEligible: visited) {
            board.fetchTile(moveEligible).setHighlighted(true);
        }
    }

    private void clearHighlights() {
        board.forEachTile((pos, tile) -> tile.setHighlighted(false));
    }

    private void updateStep(TurnStep newStep) {
        step = newStep;
        turnStepLabel.setText(step.name().toLowerCase());
    }

    private void handleTurnStep(TurnStep newStep) {
        switch(newStep) {
            case SHIFTING -> {
                // In the future, you could put an animation for the arrows here
                // to indicate that they can be clicked
            }
            case MOVING -> {
                generateRandomMovement();
            }
        }
    }

    private void handleTileClick(int tileID) {
        if ((!myTurn() || step != TurnStep.MOVING) && !devMode) { return; }

        TileInfo target = board.fetchTile(tileID);
        if (!target.tile().isHighlighted()) { return; }

        client.send(new PlayerMovedMessage(tileID));
        
        Player player = PlayerManager.getYourself();
        target.tile().getTreasure().ifPresent(value -> {
            System.out.println("Landed on treasure " + (value + 1));
            if (player.canCollectTreasure(value)) {
                client.send(new TreasureMessage(myId, Delta.COLLECT));
            }
        });
    }

    private void handleArrowClick(ShiftID sid) {
        if ((!myTurn() || step != TurnStep.SHIFTING) && !devMode) { return; }
        client.send(new ShiftMessage(sid));
    }


    private void endGame(String withMessage) {
        status = "Game has ended";
        STAGE.clear();
        var exitScreen = new FullScreenPopUpUI(withMessage, "Exit", Gdx.app::exit);
        exitScreen.show();
    }

    private void applyMove(Player p, Vector2 newPos) {
        board.movePlayer(p, newPos);
    }

    private boolean playerWon(Player p) {
        return p.getTreasureCount() == 0 && board.playerOnPosition(p, getPlayerStartPos(p));
    }

    private Vector2 getPlayerStartPos(Player player) {
        return board.cornerToGrid(player.getStartingCorner());
    }

    private int getPlayerDisplayID(int fromId) {
        return ++fromId;
    }

    // Debugging
    int getMyId() { return myId; }
}
