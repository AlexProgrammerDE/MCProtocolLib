package com.github.steveice10.mc.protocol.data.game;

public class Coordinates {

    private final int x;
    private final int y;
    private final int z;

    public Coordinates(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

}
