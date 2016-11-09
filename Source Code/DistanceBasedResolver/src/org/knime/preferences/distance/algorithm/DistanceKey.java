package org.knime.preferences.distance.algorithm;
/**
 * Unique key for saving DataHolder objects
 * @author Stefan Wohlfart
 * @version 1.0
 *
 */
public class DistanceKey {

    private final int x;
    private final int y;

    /**
     * Constructor for the DistanceKey which initializes two variables with the inputed integers.
     * These integers are indexes and represent a DataPoint
     * @param x - index x
     * @param y - index y
     */
    protected DistanceKey(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DistanceKey)) return false;
        DistanceKey key = (DistanceKey) o;
        return x == key.x && y == key.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

}