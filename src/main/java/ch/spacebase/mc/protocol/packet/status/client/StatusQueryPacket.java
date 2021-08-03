package ch.spacebase.mc.protocol.packet.status.client;

import java.io.IOException;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

public class StatusQueryPacket implements Packet {
	
	public StatusQueryPacket() {
	}

	@Override
	public void read(NetInput in) throws IOException {
	}

	@Override
	public void write(NetOutput out) throws IOException {
	}
	
	@Override
	public boolean isPriority() {
		return false;
	}
	
}
