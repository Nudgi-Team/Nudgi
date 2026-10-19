package io.github.Nudgi;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Board {
    private static final Texture arrowTexture = new Texture("Arrow.png");

    private Tile[][] tiles;
    private Vector2 position;
    private Vector2 gridSize;
    private float tileSize;

    public Board(Vector2 _position, float _size, Vector2 _gridSize, Stage stage){
        gridSize = _gridSize;
        // Keep all tiles square while still fitting on the screen
        tileSize = _size / Math.max(gridSize.x, gridSize.y);

        // Keep board in the center of the screen
        float boardWidth  = tileSize * gridSize.x;
        float boardHeight = tileSize * gridSize.y;
        position = new Vector2(
            _position.x + (_size - boardWidth)  / 2f,
            _position.y + (_size - boardHeight) / 2f
        );
        generateBoard(gridSize);
        forEachTile((pos, target) -> {
            Vector2 worldPos = toWorldPos(pos);
            target.setGraphics(worldPos.x, worldPos.y, tileSize, tileSize);
            target.addToStage(stage);
        });
    }

    private void generateBoard(Vector2 gridSize){
        tiles = new Tile[(int) gridSize.x][(int) gridSize.y];
        int id = 0;
        for(int x = 0; x < gridSize.x; x++){
            for(int y = 0; y < gridSize.y; y++){
                id++;
                // If tile is at the corner, set the tiles shape
                if((x == 0 || x == gridSize.x-1) && (y == 0 || y == gridSize.y-1)){
                    tiles[x][y] = new Tile(y == 0, y != 0, x == 0, x != 0, id);
                    continue;
                }
                tiles[x][y] = new Tile(id);
            }
        }
    }

    public void shift(int index, Direction direction) {
        Tile wrap;

        switch (direction) {
            case DOWN:
                wrap = tiles[index][0];
                for (int y = 1; y < gridSize.y; y++) {
                    tiles[index][y - 1] = tiles[index][y];
                }
                tiles[index][(int) (gridSize.y - 1)] = wrap;
                break;

            case UP:
                wrap = tiles[index][(int) (gridSize.y - 1)];
                for (int y = (int) (gridSize.y - 2); y >= 0; y--) {
                    tiles[index][y + 1] = tiles[index][y];
                }
                tiles[index][0] = wrap;
                break;

            case LEFT:
                wrap = tiles[0][index];
                for (int x = 1; x < gridSize.x; x++) {
                    tiles[x - 1][index] = tiles[x][index];
                }
                tiles[(int) (gridSize.x - 1)][index] = wrap;
                break;

            case RIGHT:
                wrap = tiles[(int) (gridSize.x - 1)][index];
                for (int x = (int) (gridSize.x - 2); x >= 0; x--) {
                    tiles[x + 1][index] = tiles[x][index];
                }
                tiles[0][index] = wrap;
                break;
        }
    }

    // Only call for adjacent tiles.
    private boolean blockedByWall(Vector2 pos1, Vector2 pos2, Direction from1to2) {
        Tile tile1 = fetchTile(pos1);
        Tile tile2 = fetchTile(pos2);

        return (!tile1.hasPath(from1to2) || !tile2.hasPath(from1to2.inverse()));
    }

    /**
     *
     * @param fromPosition the starting position.
     * @param onlyNonBlocked if true, only returns positions not blocked by a wall.
     * @return the array of adjacent tiles.
     */
    public List<Vector2> getAdjacentPositions(Vector2 fromPosition, boolean onlyNonBlocked) {
        ArrayList<Vector2> adjacent = new ArrayList<>();
        for (Direction dir: Direction.values()) {
            Vector2 newPosition = dir.vector().add(fromPosition);
            if (inRange(newPosition) && !(onlyNonBlocked && blockedByWall(fromPosition, newPosition, dir))) {
                adjacent.add(newPosition);
            }
        }
        return adjacent;
    }


    /**
     * Checks if the provided vector is within the board's bounds.
     * @param v
     * @return true if {@code v} is within the board's bounds.
     */
    public boolean inRange(Vector2 v) {
        return v.x < gridSize.x && v.x >= 0 && v.y < gridSize.y && v.y >= 0;
    }

    /**
     * Converts a position on the board grid to a world position for rendering.
     * @param gridPos
     * @return the world position equivalent of the argument.
     */
    public Vector2 toWorldPos(Vector2 gridPos) {
        return gridPos.cpy().scl(tileSize).add(position);
    }

    public Vector2 getPlayerPosition(Player p) {
        return search(info -> info.tile.containsPlayer(p))
            .orElseThrow(IllegalArgumentException::new).pos;
    }

    /**
     * Moves the player from one tile to another.
     * Do not use this method if the player isn't already on the board (use {@code initializePlayer} instead).
     * This method does <i>not</i> move the player's {@code Image}.
     * @param p the player to be moved
     * @param newLocation where their new location will be
     * @see Board#initializePlayer(Player, Vector2)
     */
    public void movePlayer(Player p, Vector2 newLocation) {
        Tile startingTile = fetchTile(getPlayerPosition(p));
        Tile endingTile = fetchTile(newLocation);

        startingTile.removePlayer(p);
        endingTile.addPlayer(p);
    }

    public void setListenerAllTiles(IdentifyingListener listener) {
        forEachTile((pos, tile) -> tile.setListener(listener));
    } 


    public void clearHighlights() {
        forEachTile((pos, tile) -> tile.setHighlighted(false));
    }

    /**
     * Must be called exactly once for each player upon starting the game.
     * @param p the player to place onto the board.
     * @param startingLocation where the player will start.
     */
    public void initializePlayer(Player p, Vector2 startingLocation) {
        Tile start = fetchTile(startingLocation);
        start.addPlayer(p);
    }

    public Vector2 getPosition() {
        return new Vector2(position);
    }

    public float getTileSize() {
        return tileSize;
    }

    public Vector2 getGridSize() {
        return new Vector2(gridSize);
    }


    public Tile fetchTile(Vector2 pos) {
        return tiles[(int)pos.x][(int)pos.y];
    }


    public TileInfo fetchTile(int id) {
        Optional<TileInfo> container = search((info) -> info.tile.tileID == id);
        return container.orElseThrow(IllegalArgumentException::new);
    }


    /**
     * Searches through each tile, then if the provided predicate is true, returns
     * an Optional containing a TileInfo populated with both the matching tile and its position.
     * Otherwise, if none match, returns an empty Optional.
     * @param predicate the condition to check each tile against
     * @return an Optional containing the TileInfo of the first tile matching the predicate,
     * if any; otherwise an empty Optional.
     */
    public Optional<TileInfo> search(Predicate<TileInfo> predicate) {
        for (int x = 0; x < gridSize.x; x++) {
            for (int y = 0; y < gridSize.y; y++) {
                TileInfo info = new TileInfo(new Vector2(x, y), tiles[x][y]);
                if (predicate.test(info)) {
                    return Optional.of(info);
                }
            }
        }
        return Optional.empty();
    }


    /**
     * Iterates through every tile, passing in both its position and the {@code Tile} itself
     * to the provided argument for each tile.
     * @param func the method to be called once per tile
     */
    public void forEachTile(BiConsumer<Vector2, Tile> func) {
        for (int x = 0; x < gridSize.x; x++) {
            for (int y = 0; y < gridSize.y; y++) {
                func.accept(new Vector2(x, y), tiles[x][y]);
            }
        }
    }

    public Set<Vector2> pathFind(Vector2 startingSquare, int distance) {
        Set<Vector2> visited = new HashSet<>();
        Stack<Vector2> explore = new Stack<>();
        explore.add(startingSquare);
        for (int i = 0; i <= distance; i++) {
            // Start the queue and thus first iteration with the player's own tile.
            // On each subsequent iteration, the queue will begin with all adjacent tiles found
            // in the previous iteration, excluding tiles visited in prior iterations.
            // All tiles visited will be marked, including the starting square.
            // Each iteration uses up one movement, with the first iteration only marking the starting square.
            Stack<Vector2> found = new Stack<>();
            while (!explore.empty()) {
                Vector2 pos = explore.pop();
                visited.add(pos);
                List<Vector2> adjacent = getAdjacentPositions(pos, true);
                for (Vector2 item : adjacent) {
                    if (!visited.contains(item)) {
                        found.add(item);
                    }
                }
            }
            explore.addAll(found);
        }
        return visited;
    }

    // public void draw(Batch batch){
    //     for(int x = 0; x < gridSize.x; x++){
    //         for(int y = 0; y < gridSize.y; y++){
    //             Vector2 p = new Vector2(x * tileSize + position.x, y * tileSize + position.y);
    //             tiles[x][y].draw(batch, p, tileSize);
    //         }
    //     }

    //     for(int x = 1; x < gridSize.x-1; x++) {
    //         batch.draw(new TextureRegion(arrowTexture), x *tileSize + position.x, position.y - tileSize,
    //             tileSize / 2, tileSize / 2, tileSize, tileSize,
    //             1, 1, 0);
    //         batch.draw(new TextureRegion(arrowTexture), x *tileSize + position.x, position.y + (gridSize.y) * tileSize,
    //             tileSize / 2, tileSize / 2, tileSize, tileSize,
    //             1, 1, 180);
    //     }
    //     for(int y = 1; y < gridSize.x-1; y++) {
    //         batch.draw(new TextureRegion(arrowTexture), position.x - tileSize, y *tileSize + position.y,
    //             tileSize / 2, tileSize / 2, tileSize, tileSize,
    //             1, 1, 270);
    //         batch.draw(new TextureRegion(arrowTexture), position.x + (gridSize.x) * tileSize, y *tileSize + position.y,
    //             tileSize / 2, tileSize / 2, tileSize, tileSize,
    //             1, 1, 90);
    //     }
    // }


    public Vector2 toGrid(Vector2 worldPos) {
        float gx = (worldPos.x - position.x) / tileSize;
        float gy = (worldPos.y - position.y) / tileSize;

        int x = (int)Math.floor(gx);
        int y = (int)Math.floor(gy);

        return new Vector2(x, y);
    }


    public static class TileInfo {
        public final Vector2 pos;
        public final Tile tile;

        public TileInfo(Vector2 pos, Tile t) {
            this.pos = pos;
            this.tile = t;
        }
    }
}
