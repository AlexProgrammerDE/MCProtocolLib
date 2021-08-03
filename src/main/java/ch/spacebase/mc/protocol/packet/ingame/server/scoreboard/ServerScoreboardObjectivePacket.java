package ch.spacebase.mc.protocol.packet.ingame.server.scoreboard;

import java.io.IOException;

import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

public class ServerScoreboardObjectivePacket implements Packet {
	
	private String name;
	private String value;
	private Action action;
	
	@SuppressWarnings("unused")
	private ServerScoreboardObjectivePacket() {
	}
	
	public ServerScoreboardObjectivePacket(String name, String value, Action action) {
		this.name = name;
		this.value = value;
		this.action = action;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public Action getAction() {
		return this.action;
	}

	@Override
	public void read(NetInput in) throws IOException {
		this.name = in.readString();
		this.value = in.readString();
		this.action = Action.values()[in.readByte()];
	}

	@Override
	public void write(NetOutput out) throws IOException {
		out.writeString(this.name);
		out.writeString(this.value);
		out.writeByte(this.action.ordinal());
	}
	
	@Override
	public boolean isPriority() {
		return false;
	}
	
	public static enum Action {
		ADD,
		REMOVE,
		UPDATE;
	}

}
