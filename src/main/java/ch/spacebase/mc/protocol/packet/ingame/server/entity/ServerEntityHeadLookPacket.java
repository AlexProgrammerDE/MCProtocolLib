package ch.spacebase.mc.protocol.packet.ingame.server.entity;

import java.io.IOException;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

public class ServerEntityHeadLookPacket implements Packet {
	
	protected int entityId;
	protected float headYaw;
	
	@SuppressWarnings("unused")
	private ServerEntityHeadLookPacket() {
	}
	
	public ServerEntityHeadLookPacket(int entityId, float headYaw) {
		this.entityId = entityId;
		this.headYaw = headYaw;
	}
	
	public float getHeadYaw() {
		return this.headYaw;
	}

	@Override
	public void read(NetInput in) throws IOException {
		this.entityId = in.readInt();
		this.headYaw = in.readByte() * 360 / 256f;
	}

	@Override
	public void write(NetOutput out) throws IOException {
		out.writeInt(this.entityId);
		out.writeByte((byte) (this.headYaw * 256 / 360));
	}
	
	@Override
	public boolean isPriority() {
		return false;
	}

}
