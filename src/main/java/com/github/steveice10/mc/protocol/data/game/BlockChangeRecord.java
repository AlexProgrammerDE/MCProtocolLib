package com.github.steveice10.mc.protocol.data.game;

public class BlockChangeRecord {

    private final int x;
    private final int y;
    private final int z;
    private final int id;
    private final int metadata;

    public BlockChangeRecord(int x, int y, int z, int id, int metadata) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.metadata = metadata;
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

    public int getId() {
        return this.id;
    }

    public int getMetadata() {
        return this.metadata;
    }

}
