package ch.spacebase.mc.protocol.packet.ingame.server.world;

import ch.spacebase.mc.protocol.data.game.BlockChangeRecord;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

import java.io.IOException;

public class ServerBlockChangePacket implements Packet {

    private BlockChangeRecord record;

    @SuppressWarnings("unused")
    private ServerBlockChangePacket() {
    }

    public ServerBlockChangePacket(BlockChangeRecord record) {
        this.record = record;
    }

    public BlockChangeRecord getRecord() {
        return this.record;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.record = new BlockChangeRecord(in.readInt(), in.readUnsignedByte(), in.readInt(), in.readVarInt(), in.readUnsignedByte());
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeInt(this.record.getX());
        out.writeByte(this.record.getY());
        out.writeInt(this.record.getZ());
        out.writeVarInt(this.record.getId());
        out.writeByte(this.record.getMetadata());
    }

    @Override
    public boolean isPriority() {
        return false;
    }

}
