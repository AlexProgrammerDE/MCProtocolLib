package ch.spacebase.mc.protocol.packet.ingame.client.entity.player;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

import java.io.IOException;

public class ClientSteerVehiclePacket implements Packet {

    private float sideways;
    private float forward;
    private boolean jump;
    private boolean dismount;

    @SuppressWarnings("unused")
    private ClientSteerVehiclePacket() {
    }

    public ClientSteerVehiclePacket(float sideways, float forward, boolean jump, boolean dismount) {
        this.sideways = sideways;
        this.forward = forward;
        this.jump = jump;
        this.dismount = dismount;
    }

    public float getSideways() {
        return this.sideways;
    }

    public float getForward() {
        return this.forward;
    }

    public boolean getJumping() {
        return this.jump;
    }

    public boolean getDismounting() {
        return this.dismount;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.sideways = in.readFloat();
        this.forward = in.readFloat();
        this.jump = in.readBoolean();
        this.dismount = in.readBoolean();
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeFloat(this.sideways);
        out.writeFloat(this.forward);
        out.writeBoolean(this.jump);
        out.writeBoolean(this.dismount);
    }

    @Override
    public boolean isPriority() {
        return false;
    }

}
