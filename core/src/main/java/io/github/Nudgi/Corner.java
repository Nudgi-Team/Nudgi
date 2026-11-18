package io.github.Nudgi;

import com.badlogic.gdx.math.Vector2;


/**
 * Contains corner representations, relative to the bottom left corner.
 * Each corner has a Vector2 representation specified as:
 * <ul>
 * <li>x value: 0 for left, 1 for right
 * <li>y value: 0 for bottom, 1 for top
 * </ul>
 */
public enum Corner {
    BOTTOM_LEFT (Vector2.Zero),
    BOTTOM_RIGHT (Vector2.X),
    TOP_LEFT (Vector2.Y),
    TOP_RIGHT (Vector2.One);

    private Vector2 v;

    private Corner(Vector2 vector) {
        v = vector;
    }

    public Vector2 vector() {
        return v.cpy();
    }

    // public static Corner fromVector(Vector2 target) {
    //     return Arrays.stream(values())
    //         .filter(corner -> corner.vector().epsilonEquals(target))
    //         .findAny()
    //         .orElseThrow(IllegalArgumentException::new);
    // }

    /**
     * Returns a Vector2 determined via mapping each dimension of the 
     * Vector2 representation of this corner, {@code vector}, to the corresponding
     * {@code when} argument, such that if that dimension of {@code vector} contains a value of X,
     * then the result will contain a value of {@code whenX} in that same dimension.
     * In other words, this method returns a Vector2 equal to {@code result}, given {@code vector},
     * in the following table:
     * <pre>
     * | vector | result                    |
     * |--------|---------------------------|
     * | (0, 0) | new Vector2(when0, when0) |
     * | (0, 1) | new Vector2(when0, when1) |
     * | (1, 0) | new Vector2(when1, when0) |
     * | (1, 1) | new Vector2(when1, when1) |
     * </pre>
     * 
     * @param when0 the value to resolve to when a corner dimension is 0.
     * @param when1 the value to resolve to when a corner dimension is 1.
     * @return the Vector2 result after applying this corner to the provided arguments.
     */
    public Vector2 apply(float when0, float when1) {
        return new Vector2((v.x == 0) ? when0 : when1, (v.y == 0) ? when0 : when1);
    }

    public Corner opposite() {
        Corner value = this;
        switch(value) {
            case BOTTOM_LEFT: value = TOP_RIGHT; break;
            case BOTTOM_RIGHT: value = TOP_LEFT; break;
            case TOP_LEFT: value = BOTTOM_RIGHT; break;
            case TOP_RIGHT: value = BOTTOM_LEFT; break;
        }
        return value;
    }
}
