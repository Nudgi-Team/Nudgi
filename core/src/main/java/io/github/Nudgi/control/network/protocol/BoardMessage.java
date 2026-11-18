package io.github.Nudgi.control.network.protocol;

import java.util.Arrays;
import java.util.stream.Stream;

import io.github.Nudgi.entities.TreasureTileInfo;

public final class BoardMessage extends Message {
    private final int[] paths;
    private final TreasureTileInfo[] treasureSpots;

    /**
     * Creates a new BoardMessage. All BoardMessages
     * always originate from the server.
     * @param paths the paths of the tiles on the board.
     * @param tileIDsWithTreasure the tile IDs which contain treasure.
     */
    public BoardMessage(int[] paths, TreasureTileInfo[] tilesWithTreasure) {
        super(SourceContext.SERVER);
        this.paths = paths.clone();
        treasureSpots = tilesWithTreasure;
    }

    private BoardMessage(SourceContext source, String[] args) {
        super(source);
        String[][] sliced = slice(args, 2);
        
        paths = deserializePaths(sliced[0]);
        treasureSpots = deserializeTreasureInfo(sliced[1]);
    }

    @Override 
    protected String[] serialized() {
        String[] strPaths = serializePaths();
        String[] strTreasureInfo = serializeTreasureInfo();
        return concat(true, strPaths, strTreasureInfo);
    }

    public int[] getPaths() {
        return paths.clone();
    }

    public TreasureTileInfo[] getTreasureInfo() {
        return treasureSpots.clone();
    }

    private String[] serializePaths() {
        return Arrays.stream(paths)
            .boxed()
            .map(String::valueOf)
            .toArray(String[]::new);
    }

    private static int[] deserializePaths(String[] slice) {
        return Arrays.stream(slice)
            .mapToInt(Integer::parseInt)
            .toArray();
    }

    private String[] serializeTreasureInfo() {
        return Arrays.stream(treasureSpots)
            .flatMap(info -> Stream.of(info.treasureKind(), info.targetTileID()))
            .map(String::valueOf)
            .toArray(String[]::new);
    }

    private static TreasureTileInfo[] deserializeTreasureInfo(String[] slice) {
        TreasureTileInfo[] results = new TreasureTileInfo[slice.length/2];
        for (int i = 0; i < slice.length; i += 2) {
            int treasureKind = Integer.parseInt(slice[i]);
            int onTile = Integer.parseInt(slice[i+1]);
            results[i/2] = new TreasureTileInfo(treasureKind, onTile);
        }
        return results;
    }
}
