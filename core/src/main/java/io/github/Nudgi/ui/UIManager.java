package io.github.Nudgi.ui;

import io.github.Nudgi.ui.game.TreasureUI;
import io.github.Nudgi.ui.lobby.LobbyUI;
import io.github.Nudgi.ui.stage.StageManager;
import io.github.Nudgi.ui.stage.StageManager.Mode;


public final class UIManager {
    private static LobbyUI lobby;
    private static TreasureUI treasureUi; 

    static {
        StageManager.onModeChanged(UIManager::setMode);
    }

    private UIManager() {}

    private static void setMode(Mode mode) {
        switch(mode) {
            case LOBBY: setToLobby(); break;
            case GAME: setToGame(); break;
        }   
    }

    private static void setToLobby() {
        lobby = new LobbyUI();
    }

    private static void setToGame() {
        treasureUi = new TreasureUI();
    }

    public static void onDevMode(Runnable... listeners) {
        lobby.onDevMode(listeners);
    }

    public static void lobbyWaitingMode() {
        lobby.waitingMode();
    }
}
