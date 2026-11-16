package io.github.Nudgi.ui.game;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import io.github.Nudgi.Corner;
import io.github.Nudgi.entities.Player;
import io.github.Nudgi.entities.PlayerManager;
import io.github.Nudgi.ui.StageManager;
import io.github.Nudgi.ui.UIService;


public class TreasureUI {
    private final NumberLabel[] treasureCounts;

    public TreasureUI() {
        treasureCounts = new NumberLabel[PlayerManager.MAX_PLAYERS];

        Label[] labels = new Label[PlayerManager.getPlayers().length];
        for (int i = 0; i < labels.length; i++) {
            Player player = PlayerManager.fetchPlayer(i);
            treasureCounts[i] = new NumberLabel(player);
            player.addTreasureListener(treasureCounts[i]::update);
            labels[i] = treasureCounts[i].baseLabel;
        }
        StageManager.addActors(labels);
    }

    private class NumberLabel {
        final Label baseLabel = UIService.createLabel(null);
        final Player player;

        NumberLabel(Player player) {
            this.player = player;

            Corner corner = player.getStartingCorner();
            Vector2 pos = corner.apply(0.05f, 0.90f)
                .scl(StageManager.getWorldSize());
            baseLabel.setPosition(pos.x, pos.y, Align.bottomLeft);
            player.addTreasureListener(this::update);
            update();
        }

        void update() {
            int current = player.getTreasureCount();
            int top = player.topTreasure().orElse(Integer.MIN_VALUE);
            baseLabel.setText("Left: " + current + "\nTop: " + ((top == Integer.MIN_VALUE) ? "None" : top + 1));
        }
    }
}