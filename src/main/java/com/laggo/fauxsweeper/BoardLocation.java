package com.laggo.fauxsweeper;

public class BoardLocation implements Cloneable {
    private final int x;
    private final int y;

    public BoardLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public BoardLocation offsetBy(int x, int y) {
        return new BoardLocation(this.x + x, this.y + y);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BoardLocation)) {
            return false;
        }

        BoardLocation other = (BoardLocation) obj;

        return other.x == this.x && other.y == this.y;
    }

    @Override
    public BoardLocation clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return new BoardLocation(this.x, this.y);
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}
