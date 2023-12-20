package com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity;

import com.github.steveice10.mc.protocol.codec.MinecraftCodecHelper;
import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.checkerframework.checker.nullness.qual.NonNull;

@Data
@With
@AllArgsConstructor
public class ClientboundRemoveEntitiesPacket implements MinecraftPacket {
    private final int @NonNull [] entityIds;

    public ClientboundRemoveEntitiesPacket(ByteBuf in, MinecraftCodecHelper helper) {
        this.entityIds = new int[helper.readVarInt(in)];
        for (int i = 0; i < this.entityIds.length; i++) {
            this.entityIds[i] = helper.readVarInt(in);
        }
    }

    @Override
    public void serialize(ByteBuf out, MinecraftCodecHelper helper) {
        helper.writeVarInt(out, this.entityIds.length);
        for (int entityId : this.entityIds) {
            helper.writeVarInt(out, entityId);
        }
    }
}
