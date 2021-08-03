package com.github.steveice10.mc.protocol.data.message;

public class HoverEvent implements Cloneable {

    private final HoverAction action;
    private final Message value;

    public HoverEvent(HoverAction action, Message value) {
        this.action = action;
        this.value = value;
    }

    public HoverAction getAction() {
        return this.action;
    }

    public Message getValue() {
        return this.value;
    }

    @Override
    public HoverEvent clone() {
        return new HoverEvent(this.action, this.value.clone());
    }

}
