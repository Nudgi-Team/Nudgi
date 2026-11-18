package io.github.Nudgi.control;

import io.github.Nudgi.control.network.protocol.TreasureMessage.Delta;
import io.github.Nudgi.entities.Player;

public class TreasureController {
    public void handleTreasureEvent(Player p, Delta change) {
        switch (change) {
            case LOSE -> p.loseTreasure(); // Supports enemies
            case COLLECT -> p.collectTreasure();
        }
    }
}
