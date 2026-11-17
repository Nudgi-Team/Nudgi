package io.github.Nudgi.ui;

import io.github.Nudgi.Board;
import io.github.Nudgi.ui.StageManager.Mode;
import io.github.Nudgi.ui.game.BoardUI;
import io.github.Nudgi.ui.game.TreasureUI;
import io.github.Nudgi.ui.lobby.LobbyUI;


public class UIManager {
    private LobbyUI lobby;
    private TreasureUI treasureUi; 
    private BoardUI boardUi;
    private Board board;

    public UIManager(Board board) {
        this.board = board;
        StageManager.onModeChanged(this::setMode);
    }

    public GameUI getGameUI() {
        if (StageManager.getStageMode() != Mode.GAME) {
            throw new IllegalStateException("Not in game");
        }
        return new GameUI(treasureUi, boardUi);
    }

    public LobbyUI getLobbyUI() {
        if (StageManager.getStageMode() != Mode.LOBBY) {
            throw new IllegalStateException("Not in lobby");
        }
        return lobby;
    }

    private void setMode(Mode mode) {
        switch(mode) {
            case LOBBY -> setToLobby();
            case GAME -> setToGame();
        }   
    }

    private void setToLobby() {
        lobby = new LobbyUI();
        lobby.show();
    }

    private void setToGame() {
        if (lobby != null) { lobby.dispose(); }
        lobby = null;
        treasureUi = new TreasureUI();
        boardUi = new BoardUI(board);
        
        treasureUi.show();
        boardUi.show();
    }

    public record GameUI(TreasureUI treasure, BoardUI board) {}
}
