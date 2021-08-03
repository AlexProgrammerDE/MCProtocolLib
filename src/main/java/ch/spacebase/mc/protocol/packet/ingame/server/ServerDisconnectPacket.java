package ch.spacebase.mc.protocol.packet.ingame.server;

import java.io.IOException;

import ch.spacebase.mc.protocol.data.message.Message;
import ch.spacebase.mc.protocol.data.message.TextMessage;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

public class ServerDisconnectPacket implements Packet {
	
	private Message message;
	
	@SuppressWarnings("unused")
	private ServerDisconnectPacket() {
	}
	
	public ServerDisconnectPacket(String text) {
		this(new TextMessage(text));
	}
	
	public ServerDisconnectPacket(Message message) {
		this.message = message;
	}
	
	public Message getReason() {
		return this.message;
	}

	@Override
	public void read(NetInput in) throws IOException {
		this.message = Message.fromString(in.readString());
	}

	@Override
	public void write(NetOutput out) throws IOException {
		out.writeString(this.message.toJsonString());
	}
	
	@Override
	public boolean isPriority() {
		return true;
	}

}
