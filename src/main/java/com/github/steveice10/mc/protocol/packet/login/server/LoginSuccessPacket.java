package com.github.steveice10.mc.protocol.packet.login.server;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

import java.io.IOException;

public class LoginSuccessPacket implements Packet {

    private String id;
    private String username;

    @SuppressWarnings("unused")
    private LoginSuccessPacket() {
    }

    public LoginSuccessPacket(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public String getPlayerId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.id = in.readString();
        this.username = in.readString();
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeString(this.id);
        out.writeString(this.username);
    }

    @Override
    public boolean isPriority() {
        return true;
    }

}
