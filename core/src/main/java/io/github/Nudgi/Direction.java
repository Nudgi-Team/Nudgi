package io.github.Nudgi;

import com.badlogic.gdx.math.Vector2;

public enum Direction {
    UP (new Vector2(0, 1), 0x1, 0),
    RIGHT (new Vector2(1, 0), 0x2, 270),
    DOWN (new Vector2(0, -1), 0x4, 180),
    LEFT (new Vector2(-1, 0), 0x8, 90);


    private final Vector2 dirValue;
    private final int mask;
    private final int degrees;

    private Direction(Vector2 dir, int pathMask, int degrees) {
        dirValue = dir;
        mask = pathMask;
        this.degrees = degrees;
    }

    public Vector2 vector() {
        return dirValue.cpy();
    }

    public int mask() {
        return mask;
    }

    public int degrees() {
        return degrees;
    }

    public Direction inverse() {
        return rotated(2);
    }

    public boolean isVertical() {
        return this == Direction.UP || this == Direction.DOWN;
    }

    /**
     * Rotates a direction 90 degrees counter-clockwise n times.
     * @param n how many times to rotate
     * @return the rotated Direction.
     */
    public Direction rotated(int n) {
        n %= Direction.values().length;
        
        Direction value = this;
        for (int i = 0; i < n; i++) {
            switch(value) {
                case UP: value = LEFT; break;
                case LEFT: value = DOWN; break;
                case DOWN: value = RIGHT; break;
                case RIGHT: value = UP; break;
            }
        }
        return value;
    }   
}
