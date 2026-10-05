package io.github.Nudgi;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.concurrent.ThreadLocalRandom;

public class Tile {
    Texture sprite = new Texture("EmptyTile.png");
    int paths = 0;
    int rotation = 1;

    public Tile(boolean north, boolean south, boolean east, boolean west){
        paths |= north ? 1:0;
        paths |= east ? 2:0;
        paths |= south ? 4:0;
        paths |= west ? 8:0;

        int NORTH = 1, EAST = 2, SOUTH = 4, WEST = 8;
        setSprite(Integer.bitCount(paths));
    }

    public Tile(){
        generatePaths();
    }

    private void generatePaths(){
        // Choose some path amount, more likely to be 2 or 3
        int[] allowed = {2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 0};
        int pathAmount = allowed[ThreadLocalRandom.current().nextInt(allowed.length)];

        // Flip paths bits to 1 if they are a path
        while(Integer.bitCount(paths) < pathAmount){
            int dir = ThreadLocalRandom.current().nextInt(4);
            paths |= (1<<dir);
        }

        setSprite(pathAmount);
    }

    private void setSprite(int pathAmount){
        int NORTH = 1, EAST = 2, SOUTH = 4, WEST = 8;
        switch(pathAmount){
            case 0: sprite = new Texture("EmptyTile.png");break; // No Rotations
            case 2:
                if(hasPath(NORTH) && hasPath(SOUTH) || hasPath(EAST) && hasPath(WEST)){
                    sprite = new Texture("StraightTile.png");
                    rotation = getStraightRotation();
                } else {
                    sprite = new Texture("BentTile.png");
                    rotation = getBentRotation();
                }
                break;
            case 3:
                sprite = new Texture("ForkTile.png");
                if(!hasPath(NORTH)){
                    rotation = 0;
                } else if(!hasPath(EAST)){
                    rotation = 1;
                } else if(!hasPath(WEST)){
                    rotation = 2;
                } else{
                    rotation = 3;
                }
                break;
            case 4: sprite = new Texture("FourWayTile.png");break;
        }
    }

    private int getStraightRotation() {
        return hasPath(1) ? 1 : 0;
    }

    private int getBentRotation() {
        int NORTH = 1, EAST = 2, SOUTH = 4, WEST = 8;

        if(hasPath(SOUTH) && hasPath(EAST)) return 0;
        if(hasPath(NORTH) && hasPath(EAST)) return 1;
        if(hasPath(NORTH) && hasPath(WEST)) return 2;
        return 3;
    }



    private boolean hasPath(int dir){
        return (paths & dir) != 0;
    }

    public void draw(Batch batch, Vector2 position, float tileSize){
        batch.draw(new TextureRegion(sprite), position.x, position.y, tileSize/2, tileSize/2, tileSize, tileSize, 1,1,rotation * 90);
    }
}
