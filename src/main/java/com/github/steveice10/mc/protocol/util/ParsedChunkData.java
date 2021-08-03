package com.github.steveice10.mc.protocol.util;

import com.github.steveice10.mc.protocol.data.game.Chunk;

public class ParsedChunkData {

    private Chunk chunks[];
    private byte biomes[];

    public ParsedChunkData(Chunk chunks[], byte biomes[]) {
        this.chunks = chunks;
        this.biomes = biomes;
    }

    public Chunk[] getChunks() {
        return this.chunks;
    }

    public byte[] getBiomes() {
        return this.biomes;
    }

}
