package org.geysermc.mcprotocollib.network.tcp;

import static com.velocitypowered.proxy.protocol.netty.MinecraftVarintLengthEncoder.IS_JAVA_CIPHER;

import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.DataFormatException;

/**
 * Handler for compressing Minecraft packets.
 */
public class MinecraftCompressorAndLengthEncoder extends MessageToByteEncoder<ByteBuf> {

    private int threshold;
    private final VelocityCompressor compressor;

    public MinecraftCompressorAndLengthEncoder(int threshold, VelocityCompressor compressor) {
        this.threshold = threshold;
        this.compressor = compressor;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int uncompressed = msg.readableBytes();
        if (uncompressed < threshold) {
            // Under the threshold, there is nothing to do.
            ProtocolUtils.writeVarInt(out, uncompressed + 1);
            ProtocolUtils.writeVarInt(out, 0);
            out.writeBytes(msg);
        } else {
            handleCompressed(ctx, msg, out);
        }
    }

    private void handleCompressed(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out)
            throws DataFormatException {
        int uncompressed = msg.readableBytes();

        ProtocolUtils.write21BitVarInt(out, 0); // Dummy packet length
        ProtocolUtils.writeVarInt(out, uncompressed);
        ByteBuf compatibleIn = MoreByteBufUtils.ensureCompatible(ctx.alloc(), compressor, msg);

        int startCompressed = out.writerIndex();
        try {
            compressor.deflate(compatibleIn, out);
        } finally {
            compatibleIn.release();
        }
        int compressedLength = out.writerIndex() - startCompressed;
        if (compressedLength >= 1 << 21) {
            throw new DataFormatException("The server sent a very large (over 2MiB compressed) packet.");
        }

        int writerIndex = out.writerIndex();
        int packetLength = out.readableBytes() - 3;
        out.writerIndex(0);
        ProtocolUtils.write21BitVarInt(out, packetLength); // Rewrite packet length
        out.writerIndex(writerIndex);
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect)
            throws Exception {
        int uncompressed = msg.readableBytes();
        if (uncompressed < threshold) {
            int finalBufferSize = uncompressed + 1;
            finalBufferSize += ProtocolUtils.varIntBytes(finalBufferSize);
            return IS_JAVA_CIPHER
                    ? ctx.alloc().heapBuffer(finalBufferSize)
                    : ctx.alloc().directBuffer(finalBufferSize);
        }

        // (maximum data length after compression) + packet length varint + uncompressed data varint
        int initialBufferSize = (uncompressed - 1) + 3 + ProtocolUtils.varIntBytes(uncompressed);
        return MoreByteBufUtils.preferredBuffer(ctx.alloc(), compressor, initialBufferSize);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        compressor.close();
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
