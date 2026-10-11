package io.github.Nudgi;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Board {
    private Tile[][] tiles;
    private Vector2 position;
    private float size;
    private Vector2 gridSize;
    private float tileSize;
    Texture arrowTexture = new Texture("Arrow.png");

    public enum directions{
        Up,
        Down,
        Left,
        Right
    }

    public Board(Vector2 _position, float _size, Vector2 _gridSize){
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
    }

    public Vector2 getGridSize(){
        return gridSize;
    }

    private void generateBoard(Vector2 gridSize){
        tiles = new Tile[(int) gridSize.x][(int) gridSize.y];

        for(int x = 0; x < gridSize.x; x++){
            for(int y = 0; y < gridSize.y; y++){
                // If tile is at the corner, set the tiles shape
                if((x == 0 || x == gridSize.x-1) && (y == 0 || y == gridSize.y-1)){
                    tiles[x][y] = new Tile(y != 0, y == 0, x != 0, x == 0);
                    continue;
                }
                tiles[x][y] = new Tile();
            }
        }
    }

    public void shift(int index, directions direction) {
        Tile wrap;

        switch (direction) {
            case Down:
                wrap = tiles[index][0];
                for (int y = 1; y < gridSize.y; y++) {
                    tiles[index][y - 1] = tiles[index][y];
                }
                tiles[index][(int) (gridSize.y - 1)] = wrap;
                break;

            case Up:
                wrap = tiles[index][(int) (gridSize.y - 1)];
                for (int y = (int) (gridSize.y - 2); y >= 0; y--) {
                    tiles[index][y + 1] = tiles[index][y];
                }
                tiles[index][0] = wrap;
                break;

            case Left:
                wrap = tiles[0][index];
                for (int x = 1; x < gridSize.x; x++) {
                    tiles[x - 1][index] = tiles[x][index];
                }
                tiles[(int) (gridSize.x - 1)][index] = wrap;
                break;

            case Right:
                wrap = tiles[(int) (gridSize.x - 1)][index];
                for (int x = (int) (gridSize.x - 2); x >= 0; x--) {
                    tiles[x + 1][index] = tiles[x][index];
                }
                tiles[0][index] = wrap;
                break;
        }
    }


    public void draw(Batch batch){
        for(int x = 0; x < gridSize.x; x++){
            for(int y = 0; y < gridSize.y; y++){
                Vector2 p = new Vector2(x * tileSize + position.x, y * tileSize + position.y);
                tiles[x][y].draw(batch, p, tileSize);
            }
        }

        for(int x = 1; x < gridSize.x-1; x++) {
            batch.draw(new TextureRegion(arrowTexture), x *tileSize + position.x, position.y - tileSize,
                tileSize / 2, tileSize / 2, tileSize, tileSize,
                1, 1, 0);
            batch.draw(new TextureRegion(arrowTexture), x *tileSize + position.x, position.y + (gridSize.y) * tileSize,
                tileSize / 2, tileSize / 2, tileSize, tileSize,
                1, 1, 180);
        }
        for(int y = 1; y < gridSize.x-1; y++) {
            batch.draw(new TextureRegion(arrowTexture), position.x - tileSize, y *tileSize + position.y,
                tileSize / 2, tileSize / 2, tileSize, tileSize,
                1, 1, 270);
            batch.draw(new TextureRegion(arrowTexture), position.x + (gridSize.x) * tileSize, y *tileSize + position.y,
                tileSize / 2, tileSize / 2, tileSize, tileSize,
                1, 1, 90);
        }
    }


    public Vector2 toGrid(Vector2 worldPos) {
        float gx = (worldPos.x - position.x) / tileSize;
        float gy = (worldPos.y - position.y) / tileSize;

        int x = (int)Math.floor(gx);
        int y = (int)Math.floor(gy);

        return new Vector2(x, y);
    }


}
