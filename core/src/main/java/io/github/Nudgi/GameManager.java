package io.github.Nudgi;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public class GameManager {
    private Board board;

    GameManager(Board _board){
        board = _board;
    }

    public void handlePlayerClick(Vector2 pos) {
        Vector2 gridPos = board.toGrid(pos);

        System.out.println(pos);
        System.out.println(gridPos);
        Vector2 gridSize = board.getGridSize();

        if (gridPos.x == -1){
            if(gridPos.y > 0 && gridPos.y < gridSize.y - 1){
                board.shift((int) gridPos.y, Board.directions.Right);
            }
        }
        else if (gridPos.x == gridSize.x){
            if(gridPos.y > 0 && gridPos.y < gridSize.y - 1){
                board.shift((int) gridPos.y, Board.directions.Left);
            }
        }
        else if (gridPos.y == -1){
            if(gridPos.x > 0 && gridPos.x < gridSize.x - 1){
                board.shift((int) gridPos.x, Board.directions.Up);
            }
        }
        else if (gridPos.y == gridSize.y){
            if(gridPos.x > 0 && gridPos.x < gridSize.x - 1){
                board.shift((int) gridPos.x, Board.directions.Down);
            }

        }
    }
}
