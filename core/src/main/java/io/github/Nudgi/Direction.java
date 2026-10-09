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

    private Direction fromDeg(int deg) {
        for (Direction dir: Direction.values()) {
            if (deg == dir.degrees) {
                return dir;
            }
        }
        throw new AssertionError("Failed to find Direction from degree " + deg);
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


    /**
     * Rotates a direction 90 degrees counter-clockwise n times.
     * @param n how many times to rotate
     * @return the rotated Direction.
     */
    public Direction rotated(int n) {
        int target = (degrees + (n*90)) % 360;
        return fromDeg(target);  
    }   
}
