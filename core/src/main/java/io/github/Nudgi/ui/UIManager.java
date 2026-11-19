package io.github.Nudgi.ui;

import io.github.Nudgi.Board;
import io.github.Nudgi.ui.StageManager.Mode;
import io.github.Nudgi.ui.game.BoardUI;
import io.github.Nudgi.ui.game.PowerUpUI;
import io.github.Nudgi.ui.game.TreasureUI;
import io.github.Nudgi.ui.lobby.LobbyUI;


public class UIManager {
    private LobbyUI lobby;
    private TreasureUI treasureUi; 
    private BoardUI boardUi;
    private PowerUpUI powerUpUI;
    private Board board;

    public UIManager(Board board) {
        this.board = board;
        StageManager.onModeChanged(this::setMode);
    }

    public GameUI getGameUI() {
        if (StageManager.getStageMode() != Mode.GAME) {
            throw new IllegalStateException("Not in game");
        }
        return new GameUI(treasureUi, boardUi, powerUpUI);
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
        lobby = null;
        treasureUi = new TreasureUI();
        boardUi = new BoardUI(board);
        powerUpUI = new PowerUpUI();
        
        treasureUi.show();
        boardUi.show();
        powerUpUI.show();
    }

    public record GameUI(TreasureUI treasure, BoardUI board, PowerUpUI powerUpUI) {}
}
