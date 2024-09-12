package org.geysermc.mcprotocollib.network.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageCodec;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.codec.PacketCodecHelper;
import org.geysermc.mcprotocollib.network.codec.PacketDefinition;
import org.geysermc.mcprotocollib.network.event.session.PacketErrorEvent;
import org.geysermc.mcprotocollib.network.packet.FakeFlushPacket;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.packet.PacketProtocol;
import org.geysermc.mcprotocollib.network.packet.PacketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TcpPacketCodec extends MessageToMessageCodec<ByteBuf, Packet> {
    private static final Logger log = LoggerFactory.getLogger(TcpPacketCodec.class);

    private final Session session;
    private final boolean client;

    public TcpPacketCodec(Session session, boolean client) {
        this.session = session;
        this.client = client;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, List<Object> out) {
        if (packet == FakeFlushPacket.INSTANCE) {
            log.debug("Fake flush packet reached");
            // Use this buffer to avoid allocating new buffers for each flush packet
            out.add(Unpooled.EMPTY_BUFFER);
            return;
        }

        if (log.isTraceEnabled()) {
            log.trace("Encoding packet: {}", packet.getClass().getSimpleName());
        }

        PacketProtocol packetProtocol = this.session.getPacketProtocol();
        PacketRegistry packetRegistry = packetProtocol.getOutboundPacketRegistry();
        PacketCodecHelper codecHelper = this.session.getCodecHelper();
        try {
            int packetId = this.client ? packetRegistry.getServerboundId(packet) : packetRegistry.getClientboundId(packet);
            PacketDefinition definition = this.client ? packetRegistry.getServerboundDefinition(packetId) : packetRegistry.getClientboundDefinition(packetId);

            ByteBuf buf = ctx.alloc().buffer();
            packetProtocol.getPacketHeader().writePacketId(buf, codecHelper, packetId);
            definition.getSerializer().serialize(buf, codecHelper, packet);

            out.add(buf);

            if (log.isDebugEnabled()) {
                log.debug("Encoded packet {} ({})", packet.getClass().getSimpleName(), packetId);
            }
        } catch (Throwable t) {
            log.debug("Error encoding packet", t);

            PacketErrorEvent e = new PacketErrorEvent(this.session, t);
            this.session.callEvent(e);
            if (!e.shouldSuppress()) {
                throw new EncoderException(t);
            }
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
        // Vanilla also checks for 0 length
        if (buf.readableBytes() == 0) {
            return;
        }

        int initial = buf.readerIndex();

        PacketProtocol packetProtocol = this.session.getPacketProtocol();
        PacketRegistry packetRegistry = packetProtocol.getInboundPacketRegistry();
        PacketCodecHelper codecHelper = this.session.getCodecHelper();
        Packet packet = null;
        try {
            int id = packetProtocol.getPacketHeader().readPacketId(buf, codecHelper);
            if (id == -1) {
                buf.readerIndex(initial);
                return;
            }

            log.trace("Decoding packet with id: {}", id);

            packet = this.client ? packetRegistry.createClientboundPacket(id, buf, codecHelper) : packetRegistry.createServerboundPacket(id, buf, codecHelper);

            if (buf.readableBytes() > 0) {
                throw new IllegalStateException("Packet \"" + packet.getClass().getSimpleName() + "\" not fully read.");
            }

            out.add(packet);

            if (log.isDebugEnabled()) {
                log.debug("Decoded packet {} ({})", packet.getClass().getSimpleName(), id);
            }
        } catch (Throwable t) {
            log.debug("Error decoding packet", t);

            // Advance buffer to end to make sure remaining data in this packet is skipped.
            buf.readerIndex(buf.readerIndex() + buf.readableBytes());

            PacketErrorEvent e = new PacketErrorEvent(this.session, t);
            this.session.callEvent(e);
            if (!e.shouldSuppress()) {
                throw new DecoderException(t);
            }
        } finally {
            if (packet != null && packet.isTerminal()) {
                // Next packets are in a different protocol state, so we must
                // disable auto-read to prevent reading wrong packets.
                session.setAutoRead(false);
            }
        }
    }
}
