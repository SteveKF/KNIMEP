package org.knime.preferences.distance.algorithm;
public class DistanceKey {

    private final int x;
    private final int y;

    public DistanceKey(int x, int y) {
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