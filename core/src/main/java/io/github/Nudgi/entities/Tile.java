package io.github.Nudgi.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.Scaling;

import io.github.Nudgi.Direction;
import io.github.Nudgi.UIService;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Tile extends Entity<Integer, Stack> {
    public final int tileID;

    private static final Texture highlight = new Texture("highlight.png");

    private Texture sprite = new Texture("EmptyTile.png");
    private int paths = 0;
    private int rotation = 1;
    private final Image highlightedImage = new Image(highlight);
    private final TileImage tileImage;
    private final Stack tileGraphics;
    private ArrayList<Player> players = new ArrayList<>();

    public Tile(boolean north, boolean south, boolean east, boolean west, int tileID){
        super(new Stack(), tileID);
        tileGraphics = getActor();
        this.tileID = tileID;

        int NORTH = 1, EAST = 2, SOUTH = 4, WEST = 8;
        paths |= north ? NORTH:0;
        paths |= east ? EAST:0;
        paths |= south ? SOUTH:0;
        paths |= west ? WEST:0;

        setSprite(Integer.bitCount(paths));
        tileImage = new TileImage(sprite);
        graphicsInit();
    }

    public Tile(int tileID){
        super(new Stack(), tileID);
        this.tileID = tileID;
        tileGraphics = getActor();
        generatePaths();
        tileImage = new TileImage(sprite);
        graphicsInit();
    }


    private void graphicsInit() {
        tileGraphics.add(tileImage);
        tileGraphics.add(highlightedImage);
        tileImage.setRotation(rotation*90);
        tileImage.setScaling(Scaling.stretch);
        highlightedImage.setScaling(Scaling.stretch);
        highlightedImage.setVisible(false);
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
            default: break;
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

    public int getPaths() {
        return paths;
    }

    public void setPaths(int newPaths) {
        if (newPaths == paths) return;
        paths = newPaths;
        rotation = 1; // same default a freshly built tile starts with
        setSprite(Integer.bitCount(paths));
        tileImage.setDrawable(UIService.drawableFromTexture(sprite));
        tileImage.setRotation(rotation * 90);
    }

    public ArrayList<Player> getPlayersOnTile() {
        return new ArrayList<>(players);
    }

    public boolean containsPlayer(Player p) {
        return players.contains(p);
    }

    public void addPlayer(Player p) {
        players.add(p);
    }

    public void removePlayer(Player p) {
        players.remove(p);
    }

    public boolean isHighlighted() {
        return highlightedImage.isVisible();
    }

    public void setHighlighted(boolean highlighted) {
        this.highlightedImage.setVisible(highlighted);
    }

    /**
     * Sets the graphical represention of this tile.
     */
    public void setGraphics(float worldX, float worldY, float width, float height) {
        tileGraphics.setPosition(worldX, worldY);
        tileGraphics.setWidth(width);
        tileGraphics.setHeight(height);
        tileImage.setOrigin(width/2, height/2);
    }

    /**    
     * Moves both this tile and all other entities on it.
     * @param to
     */
    @Override 
    public void setPosition(Vector2 to) {
        super.setPosition(to);
        for (Player player: players) {
            player.setPosition(to);
        }
    }


    private class TileImage extends Image {
        public TileImage(Texture t) {
            super(t);
        }

        @Override 
        protected void sizeChanged() {
            tileGraphics.setSize(this.getWidth(), this.getHeight());
        }

        @Override
        protected void scaleChanged() {
            tileGraphics.setScale(this.getScaleX(), this.getScaleY());
        }
    }
}
