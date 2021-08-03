package com.github.steveice10.mc.protocol.packet.ingame.client;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

import java.io.IOException;

public class ClientRequestPacket implements Packet {

    private Request request;

    @SuppressWarnings("unused")
    private ClientRequestPacket() {
    }

    public ClientRequestPacket(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return this.request;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.request = Request.values()[in.readByte()];
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeByte(this.request.ordinal());
    }

    @Override
    public boolean isPriority() {
        return false;
    }

    public enum Request {
        RESPAWN,
        STATS,
        OPEN_INVENTORY_ACHIEVEMENT
    }

}
