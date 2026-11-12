package io.github.Nudgi;


import static io.github.Nudgi.ui.stage.StageManager.STAGE;
import static io.github.Nudgi.ui.stage.StageManager.VIEW;

import java.io.IOException;
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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Disposable;

import io.github.Nudgi.entities.Player;
import io.github.Nudgi.entities.PlayerManager;
import io.github.Nudgi.entities.Tile;
import io.github.Nudgi.entities.TrivialEntity;
import io.github.Nudgi.entities.ShiftArrow.ShiftID;
import io.github.Nudgi.network.GameClient;
import io.github.Nudgi.network.GameServer;
import io.github.Nudgi.network.TurnStep;
import io.github.Nudgi.network.protocol.BoardMessage;
import io.github.Nudgi.ui.UIManager;
import io.github.Nudgi.ui.UIService;
import io.github.Nudgi.ui.stage.StageManager;
import io.github.Nudgi.ui.stage.StageManager.Mode;

import io.github.Nudgi.network.protocol.*;
import io.github.Nudgi.network.protocol.Message.MessageDeserializationException;
import io.github.Nudgi.network.protocol.TreasureMessage.Delta;

public class GameManager implements Disposable, MessageHandler {
    private static final int TREASURES_PER_PLAYER = TreasureManager.TREASURE_COUNT / PlayerManager.MAX_PLAYERS;
    private final Board board;
    private final Vector2[] winningPositions = new Vector2[4];
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
    private Label lobbyLabel = UIService.createLabel("See instructions above.");
    private Image lobbyPointer = new Image(new Texture("Arrow.png"));
    private final Stage stage;
    
    public GameManager(Board board) {
        this(board, r -> Gdx.app.postRunnable(r));
    }

    public GameManager(Board board, Consumer<Runnable> mainThread) {
        this.mainThread = mainThread;
        this.board = board;
        
        STAGE.addListener(new InputListener() {
            @Override public boolean keyTyped(InputEvent event, char c) { return handleTypedChar(c); }
            @Override public boolean keyDown(InputEvent event, int keycode) { return handleKeyDown(keycode); }
        });
        StageManager.setStageMode(Mode.LOBBY);
        UIManager.onDevMode(this::beginDevMode);
    }

    public void update() {
        if (!started && !typing) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.H)) host();
            else if (Gdx.input.isKeyJustPressed(Input.Keys.J)) startTyping();
        }
        String s = status;
        if (networked() && turnId >= 0) {
            s += myTurn() ? "  |  YOUR TURN" : "  |  Player " + getPlayerDisplayID(turnId) + "'s turn";
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

    // Standard TCP LAN Connection
    public void host() {
        if (started) return;
        started = true;
        try {
            Vector2 size = board.getGridSize();
            server = new GameServer(GameServer.DEFAULT_PORT, PlayerManager.MAX_PLAYERS, new BoardMessage(board.getAllPaths()), (int) (size.x * size.y));
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

    private void beginDevMode() {
        devMode = true;
        host();
        onStart(0);
    }

    private void startGame() {
        status = "";
        playerSetup();
        stageBoard();
        uiInit();
        placeTreasuresOnBoard();
    }

    private void playerSetup() {
        // TODO also move to BoardUI
        // Corner[] basePos = Corner.values();
            // Vector2 boardCornerPos = getBoardCornerPos(basePos[i]);
            // Vector2 worldPos = board.toWorldPos(boardCornerPos);
            // float tileSize = board.getTileSize();
            
            // TODO move this to BoardUI
            // players[i].setPosition(worldPos);
            // players[i].setSizeSquare(tileSize*0.9f);
            // players[i].addListener(this::handlePlayerClick);

    }

    public void stageBoard() {
        StageManager.setStageMode(Mode.GAME);
        // TODO goes into BoardUI
        // for (int i = 0; i < players.length; i++) {
        //     STAGE.addActor(players[i].getActor());
        // }

        board.forEachTile((pos, tile) -> tile.addListener(this::handleTileClick));
        board.forEachTile((pos, tile) -> STAGE.addActor(tile.getActor()));
        board.forEachArrow(arrow -> arrow.addListener(this::handleArrowClick));
        board.forEachArrow(arrow -> STAGE.addActor(arrow.getActor()));
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


    private void placeTreasuresOnBoard() {
        Tile[] treasureSpots = board.randomTilesWithPaths(TreasureManager.TREASURE_COUNT, true);
        for (int i = 0; i < treasureSpots.length; i++) {
            treasureSpots[i].addTreasure(i);
            Vector2 pos = board.toWorldPos(board.fetchTile(treasureSpots[i].tileID).pos());
            TreasureManager.TREASURES[i].setPosition(pos.x, pos.y);
        }
        TreasureManager.addLabelsToStage(STAGE);
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

    @Override 
    public void onWelcome(int from, WelcomeMessage m) {
        myId = m.getNewID();
        status = "You are player " + getPlayerDisplayID(myId) + hostInfo;
        if (server != null) {
            ui.lobbyWaitingMode();
        }
    }

    @Override 
    public void onBoard(int from, BoardMessage m) {
        int[] paths = m.getPaths();
        board.applyAllPaths(paths);
    }

    @Override 
    public void onPlayerMoved(int from, PlayerMovedMessage m) {
        Player player = PlayerManager.fetchPlayer(m.getPlayerID());
        TileInfo target = board.fetchTile(m.getTileID());
        applyMove(player, target.pos());
        clearHighlights();
        generateRandomMovement();
        if (playerWon(player) && myId == player.getID().intValue()) {
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
        Player newPlayer = PlayerManager.create(false, newPlayerID == myId, newPlayerID);
        board.initializePlayer(newPlayer);
    }

    @Override 
    public void onTreasure(int from, TreasureMessage m) {
        int collectorId = m.getCollectorID();
        Player targetPlayer = PlayerManager.fetchPlayer(collectorId);
        Vector2 playerPos = board.getPlayerPosition(targetPlayer);
        Tile playerTile = board.fetchTile(playerPos);
        targetPlayer.collectTreasure(playerTile.getTreasure().get());
    }

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
        System.out.println("Targets:");
        for (Vector2 moveEligible: visited) {
            System.out.println(moveEligible);
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
        System.out.println("STEP: " + newStep.name());
        switch(newStep) {
            case SHIFTING: {
                // In the future, you could put an animation for the arrows here
                // to indicate that they can be clicked
                break;
            }
            case MOVING: {
                generateRandomMovement();
                break;
            }
        }
    }

    private void handlePlayerClick(int targetPlayerID) {
        // Possibly change this to a tooltip in the future showing the player id and their
        // existing powerups
        System.out.println("Player clicked " + targetPlayerID);
    }

    private void handleTileClick(int tileID) {
        if ((!myTurn() || step != TurnStep.MOVING) && !devMode) { return; }

        TileInfo target = board.fetchTile(tileID);
        if (!target.tile().isHighlighted()) { return; }

        client.send(new PlayerMovedMessage(tileID));
        
        Player player = PlayerManager.getYourself();
        target.tile().getTreasure().ifPresent(value -> {
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
        Texture background = new Texture("EmptyTile.png");
        Label message = UIService.createLabel(withMessage);
        TextButton baseButton = UIService.createTextButton("Exit", background, null, null);
        Vector2 topRight = StageManager.getWorldSize();
        new TrivialEntity<>(baseButton, Gdx.app::exit);

        message.setFontScale(1.7f);
        UIService.centerWidget(message, true, false);
        message.setY(topRight.y/1.7f);

        baseButton.setTransform(true);
        baseButton.setScale(3);
        baseButton.setPosition(topRight.x/2 - baseButton.getWidth()*1.5f, topRight.y/2.5f - baseButton.getHeight());
        UIService.modifyTextButtonStyle(baseButton, style -> style.fontColor = Color.BLACK);

        STAGE.clear();
        STAGE.addActor(message);
        STAGE.addActor(baseButton);
    }

    private void applyMove(Player p, Vector2 newPos) {
        board.movePlayer(p, newPos);
        System.out.println("MOVED " + p + " to " + newPos);
    }


    private boolean playerWon(Player p) {
        return board.getPlayerPosition(p).epsilonEquals(winningPositions[p.getID()]);
    }

    private int getPlayerDisplayID(int fromId) {
        return ++fromId;
    }

    // Debugging
    int getMyId() { return myId; }
}
