package io.github.Nudgi.control.network.protocol;

import java.util.Arrays;

public final class TreasureInitMessage extends Message {
    private final int[] treasures;
    private final int playerID;

    public TreasureInitMessage(int[] treasures, int playerID) {
        super(SourceContext.ANY);
        this.treasures = Arrays.copyOf(treasures, treasures.length);
        this.playerID = playerID;
    }

    private TreasureInitMessage(SourceContext context, String[] args) {
        super(context);
        playerID = Integer.parseInt(args[0]);
        treasures = Arrays.stream(Arrays.copyOfRange(args, 1, args.length))
            .mapToInt(Integer::parseInt)
            .toArray();
    }

    public int[] getTreasures() {
        return treasures.clone();
    }

    public int getPlayerID() {
        return playerID;
    }

    @Override 
    protected String[] serialized() {
        String[] result = new String[treasures.length+1];
        result[0] = String.valueOf(playerID);
        for (int i = 0; i < treasures.length; i++) {
            result[i+1] = String.valueOf(treasures[i]);
        }
        return result;
    }
}
