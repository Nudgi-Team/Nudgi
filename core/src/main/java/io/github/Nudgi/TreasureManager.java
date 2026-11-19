package io.github.Nudgi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import io.github.Nudgi.ui.UIService;

public final class TreasureManager {
    public static final int TREASURE_COUNT = 24;
    public static final Label[] TREASURES = new Label[TREASURE_COUNT];
    
    private static final Integer[] TREASURE_IDS = new Integer[TREASURE_COUNT];
    private static List<Integer> available = new ArrayList<>();
    
    static {
        for (int i = 0; i < TREASURES.length; i++) {
            TREASURES[i] = UIService.createLabel("T" + (i+1));
            TREASURES[i].setFontScale(0.7f);
            TREASURES[i].setAlignment(Align.center);
            TREASURES[i].setColor(Color.YELLOW);
            TREASURE_IDS[i] = i;
            available.add(i);
        }
    }

    private TreasureManager() {}

    private static void refresh() {
        if (available.size() == 0) {
            available = Arrays.stream(TREASURE_IDS)
                .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    /**
     * Returns an array consisting of n unique treasure values. Between calls 
     * of this method, duplicates will not occur, until all Treasures have been picked from this
     * method at least once.
     * @param n the amount of treasures to generate.
     * @return an array of size n, representing the treasures for a player.
     */
    public static int[] randomTreasures(int n) {
        int[] results = new int[n];
        for (int i = 0; i < n; i++) {
            int selected = ThreadLocalRandom.current().nextInt(0, available.size());
            results[i] = available.remove(selected);
            refresh();
        }
        return results;
    }

    public static void addLabelsToStage(Stage stage) {
        for (Label label: TREASURES) {
            stage.addActor(label);
        }
    }
}
