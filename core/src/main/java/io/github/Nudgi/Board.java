package io.github.Nudgi;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

public class Board {
    private Tile[][] tiles;
    private Vector2 position;
    private float size;
    private Vector2 gridSize;
    private float tileSize;

    public Board(Vector2 _position, float _size, Vector2 _gridSize){
        gridSize = _gridSize;

        tileSize = _size / Math.max(gridSize.x, gridSize.y);

        float boardWidth  = tileSize * gridSize.x;
        float boardHeight = tileSize * gridSize.y;

        position = new Vector2(
            _position.x + (_size - boardWidth)  / 2f,
            _position.y + (_size - boardHeight) / 2f
        );

        generateBoard(gridSize);
    }


    private void generateBoard(Vector2 gridSize){
        tiles = new Tile[(int) gridSize.x][(int) gridSize.y];
        for(int x = 0; x < gridSize.x; x++){
            for(int y = 0; y < gridSize.y; y++){
                if((x == 0 || x == gridSize.x-1) && (y == 0 || y == gridSize.y-1)){
                    tiles[x][y] = new Tile(y != 0, y == 0, x != 0, x == 0);
                    continue;
                }
                tiles[x][y] = new Tile();
            }
        }
    }

    public void draw(Batch batch){
        for(int x = 0; x < gridSize.x; x++){
            for(int y = 0; y < gridSize.y; y++){
                Vector2 thisTilePos = new Vector2(x * tileSize + position.x, y * tileSize + position.y);
                tiles[x][y].draw(batch, thisTilePos, tileSize);
            }
        }
    }
}
