package io.github.Nudgi.ui.game;


import java.util.Optional;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import io.github.Nudgi.Corner;
import io.github.Nudgi.entities.Player;
import io.github.Nudgi.entities.PlayerManager;
import io.github.Nudgi.ui.Showable;
import io.github.Nudgi.ui.StageManager;
import io.github.Nudgi.ui.UIService;


public class TreasureUI implements Showable {
    private final NumberLabel[] treasureCounts;
    private final Label[] originalLabels;
    public TreasureUI() {
        treasureCounts = new NumberLabel[PlayerManager.MAX_PLAYERS];

        originalLabels = new Label[PlayerManager.getPlayers().length];
        for (int i = 0; i < originalLabels.length; i++) {
            Player player = PlayerManager.fetchPlayer(i);
            treasureCounts[i] = new NumberLabel(player);
            player.addTreasureListener(treasureCounts[i]::update);
            originalLabels[i] = treasureCounts[i].baseLabel;
        }
    }

    @Override 
    public void show() {
        StageManager.addActors(originalLabels);
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

        String getTopDisplay(Optional<Integer> topValue) {
            return (topValue.isEmpty() || PlayerManager.getYourself() != player) 
                ? ""
                : "Top: " + (topValue.get() + 1);
        }

        void update() {
            int current = player.getTreasureCount();
            Optional<Integer> top = player.topTreasure();
            baseLabel.setText("Left: " + current + "\n" + getTopDisplay(top));
        }
    }
}