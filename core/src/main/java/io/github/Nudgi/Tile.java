package io.github.Nudgi;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Tile {
    Texture sprite = new Texture("EmptyTile.png");
    static Texture highlight = new Texture("highlight.png");
    int paths = 0;
    int rotation = 1;
    public final int tileID;
    public ArrayList<Player> players = new ArrayList<>();
    public Image image;
    private boolean highlighted = false;
    private final OnClickListener clickListener;

    public Tile(boolean north, boolean south, boolean east, boolean west, int tileID){
        this.tileID = tileID;

        int NORTH = 1, EAST = 2, SOUTH = 4, WEST = 8;
        paths |= north ? NORTH:0;
        paths |= east ? EAST:0;
        paths |= south ? SOUTH:0;
        paths |= west ? WEST:0;

        setSprite(Integer.bitCount(paths));
        image = new Image(sprite);
        clickListener = new OnClickListener(tileID);
        image.addListener(clickListener);
    }

    public Tile(int tileID){
        this.tileID = tileID;
        generatePaths();
        image = new Image(sprite);
        clickListener = new OnClickListener(tileID);
        clickListener.bindToActor(image);
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
                if(!hasPath(SOUTH)){
                    rotation = 0;
                } else if(!hasPath(EAST)){
                    rotation = 1;
                } else if(!hasPath(NORTH)){
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

        if(hasPath(NORTH) && hasPath(WEST)) return 0;
        if(hasPath(SOUTH) && hasPath(WEST)) return 1;
        if(hasPath(SOUTH) && hasPath(EAST)) return 2;
        return 3;
    }



    private boolean hasPath(int dir){
        return (paths & dir) != 0;
    }

    public boolean hasPath(Direction dir) {
        return hasPath(dir.mask());
    }

    public void setListener(IdentifyingListener listener) {
        clickListener.setListener(listener);
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public void draw(Batch batch, Vector2 position, float tileSize){
        batch.draw(new TextureRegion(sprite), position.x, position.y, tileSize/2, tileSize/2, tileSize, tileSize, 1,1,rotation * 90);
        batch.draw(highlight, position.x, position.y, tileSize, tileSize);
    }
}
