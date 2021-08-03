package com.github.steveice10.mc.protocol.packet.ingame.server.entity;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

import java.io.IOException;

public class ServerRemoveEffectPacket implements Packet {

    private int entityId;
    private Effect effect;

    @SuppressWarnings("unused")
    private ServerRemoveEffectPacket() {
    }

    public ServerRemoveEffectPacket(int entityId, Effect effect) {
        this.entityId = entityId;
        this.effect = effect;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Effect getEffect() {
        return this.effect;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.entityId = in.readInt();
        this.effect = Effect.values()[in.readByte()];
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeInt(this.entityId);
        out.writeByte(this.effect.ordinal());
    }

    @Override
    public boolean isPriority() {
        return false;
    }

    public enum Effect {
        SPEED,
        SLOWNESS,
        DIG_SPEED,
        DIG_SLOWNESS,
        DAMAGE_BOOST,
        HEAL,
        DAMAGE,
        ENHANCED_JUMP,
        CONFUSION,
        REGENERATION,
        RESISTANCE,
        FIRE_RESISTANCE,
        WATER_BREATHING,
        INVISIBILITY,
        BLINDNESS,
        NIGHT_VISION,
        HUNGER,
        WEAKNESS,
        POISON,
        WITHER_EFFECT,
        HEALTH_BOOST,
        ABSORPTION,
        SATURATION
    }

}
