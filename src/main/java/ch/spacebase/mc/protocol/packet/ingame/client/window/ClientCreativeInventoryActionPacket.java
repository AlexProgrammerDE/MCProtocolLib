package ch.spacebase.mc.protocol.packet.ingame.client.window;

import ch.spacebase.mc.protocol.data.game.ItemStack;
import ch.spacebase.mc.util.NetUtil;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;

import java.io.IOException;

public class ClientCreativeInventoryActionPacket implements Packet {

    private int slot;
    private ItemStack clicked;

    @SuppressWarnings("unused")
    private ClientCreativeInventoryActionPacket() {
    }

    public ClientCreativeInventoryActionPacket(int slot, ItemStack clicked) {
        this.slot = slot;
        this.clicked = clicked;
    }

    public int getSlot() {
        return this.slot;
    }

    public ItemStack getClickedItem() {
        return this.clicked;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.slot = in.readShort();
        this.clicked = NetUtil.readItem(in);
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeShort(this.slot);
        NetUtil.writeItem(out, this.clicked);
    }

    @Override
    public boolean isPriority() {
        return false;
    }

}
