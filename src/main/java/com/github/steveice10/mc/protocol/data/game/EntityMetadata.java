package com.github.steveice10.mc.protocol.data.game;

public class EntityMetadata {

    private final int id;
    private final Type type;
    private final Object value;

    public EntityMetadata(int id, Type type, Object value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public int getId() {
        return this.id;
    }

    public Type getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }

    public enum Type {
        BYTE,
        SHORT,
        INT,
        FLOAT,
        STRING,
        ITEM,
        COORDINATES
    }

}
