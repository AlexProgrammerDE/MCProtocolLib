package ch.spacebase.mc.util;

import com.github.steveice10.mc.protocol.data.game.*;
import com.github.steveice10.opennbt.NBTIO;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class NetUtil {

    /**
     * An unfortunately necessary hack value for chunk data packet checks as to whether a packet contains skylight values or not.
     */
    public static boolean hasSky = true;

    public static CompoundTag readNBT(NetInput in) throws IOException {
        byte b = in.readByte();
        if (b == 0) {
            return null;
        } else {
            return (CompoundTag) NBTIO.readTag(new NetInputStream(in, b));
        }
    }

    public static void writeNBT(NetOutput out, CompoundTag tag) throws IOException {
        if (tag == null) {
            out.writeByte(0);
        } else {
            NBTIO.writeTag(new NetOutputStream(out), tag);
        }
    }

    public static ItemStack readItem(NetInput in) throws IOException {
        short item = in.readShort();
        if (item < 0) {
            return null;
        } else {
            return new ItemStack(item, in.readByte(), in.readShort(), readNBT(in));
        }
    }

    public static void writeItem(NetOutput out, ItemStack item) throws IOException {
        if (item == null) {
            out.writeShort(-1);
        } else {
            out.writeShort(item.getId());
            out.writeByte(item.getAmount());
            out.writeShort(item.getData());
            writeNBT(out, item.getNBT());
        }
    }

    public static EntityMetadata[] readEntityMetadata(NetInput in) throws IOException {
        List<EntityMetadata> ret = new ArrayList<EntityMetadata>();
        byte b;
        while ((b = in.readByte()) != 127) {
            int typeId = (b & 224) >> 5;
            EntityMetadata.Type type = EntityMetadata.Type.values()[typeId];
            int id = b & 31;
            Object value = null;
            switch (type) {
                case BYTE:
                    value = in.readByte();
                    break;
                case SHORT:
                    value = in.readShort();
                    break;
                case INT:
                    value = in.readInt();
                    break;
                case FLOAT:
                    value = in.readFloat();
                    break;
                case STRING:
                    value = in.readString();
                    break;
                case ITEM:
                    value = readItem(in);
                    break;
                case COORDINATES:
                    value = new Coordinates(in.readInt(), in.readInt(), in.readInt());
                    break;
                default:
                    throw new IOException("Unknown metadata type id: " + typeId);
            }

            ret.add(new EntityMetadata(id, type, value));
        }

        return ret.toArray(new EntityMetadata[ret.size()]);
    }

    public static void writeEntityMetadata(NetOutput out, EntityMetadata[] metadata) throws IOException {
        for (EntityMetadata meta : metadata) {
            int id = (meta.getType().ordinal() << 5 | meta.getId() & 31) & 255;
            out.writeByte(id);
            switch (meta.getType()) {
                case BYTE:
                    out.writeByte((Byte) meta.getValue());
                    break;
                case SHORT:
                    out.writeShort((Short) meta.getValue());
                    break;
                case INT:
                    out.writeInt((Integer) meta.getValue());
                    break;
                case FLOAT:
                    out.writeFloat((Float) meta.getValue());
                    break;
                case STRING:
                    out.writeString((String) meta.getValue());
                    break;
                case ITEM:
                    writeItem(out, (ItemStack) meta.getValue());
                    break;
                case COORDINATES:
                    Coordinates coords = (Coordinates) meta.getValue();
                    out.writeInt(coords.getX());
                    out.writeInt(coords.getY());
                    out.writeInt(coords.getZ());
                    break;
                default:
                    throw new IOException("Unmapped metadata type: " + meta.getType());
            }
        }

        out.writeByte(127);
    }

    public static ParsedChunkData dataToChunks(NetworkChunkData data) {
        Chunk chunks[] = new Chunk[16];
        int pos = 0;
        // 0 = Create chunks from mask and get blocks.
        // 1 = Get metadata.
        // 2 = Get block light.
        // 3 = Get sky light.
        // 4 = Get extended block data.
        for (int pass = 0; pass < 5; pass++) {
            for (int ind = 0; ind < 16; ind++) {
                if ((data.getMask() & 1 << ind) != 0) {
                    if (pass == 0) {
                        chunks[ind] = new Chunk(data.hasSkyLight(), (data.getExtendedMask() & 1 << ind) != 0);
                        byte[] blocks = chunks[ind].getBlocks();
                        System.arraycopy(data.getData(), pos, blocks, 0, blocks.length);
                        pos += blocks.length;
                    }

                    if (pass == 1) {
                        NibbleArray metadata = chunks[ind].getMetadata();
                        System.arraycopy(data.getData(), pos, metadata.getData(), 0, metadata.getData().length);
                        pos += metadata.getData().length;
                    }

                    if (pass == 2) {
                        NibbleArray blocklight = chunks[ind].getBlockLight();
                        System.arraycopy(data.getData(), pos, blocklight.getData(), 0, blocklight.getData().length);
                        pos += blocklight.getData().length;
                    }

                    if (pass == 3 && data.hasSkyLight()) {
                        NibbleArray skylight = chunks[ind].getSkyLight();
                        System.arraycopy(data.getData(), pos, skylight.getData(), 0, skylight.getData().length);
                        pos += skylight.getData().length;
                    }
                }

                if (pass == 4) {
                    if ((data.getExtendedMask() & 1 << ind) != 0) {
                        if (chunks[ind] == null) {
                            pos += 2048;
                        } else {
                            NibbleArray extended = chunks[ind].getExtendedBlocks();
                            System.arraycopy(data.getData(), pos, extended.getData(), 0, extended.getData().length);
                            pos += extended.getData().length;
                        }
                    }
                }
            }
        }

        byte biomeData[] = null;
        if (data.isFullChunk()) {
            biomeData = new byte[256];
            System.arraycopy(data.getData(), pos, biomeData, 0, biomeData.length);
            pos += biomeData.length;
        }

        return new ParsedChunkData(chunks, biomeData);
    }

    public static NetworkChunkData chunksToData(ParsedChunkData chunks) {
        int chunkMask = 0;
        int extendedChunkMask = 0;
        boolean fullChunk = chunks.getBiomes() != null;
        boolean sky = false;
        int length = fullChunk ? chunks.getBiomes().length : 0;
        byte[] data = null;
        int pos = 0;
        // 0 = Determine length and masks.
        // 1 = Add blocks.
        // 2 = Add metadata.
        // 3 = Add block light.
        // 4 = Add sky light.
        // 5 = Add extended block data.
        for (int pass = 0; pass < 6; pass++) {
            for (int ind = 0; ind < chunks.getChunks().length; ++ind) {
                Chunk chunk = chunks.getChunks()[ind];
                if (chunk != null && (!fullChunk || !chunk.isEmpty())) {
                    if (pass == 0) {
                        chunkMask |= 1 << ind;
                        if (chunk.getExtendedBlocks() != null) {
                            extendedChunkMask |= 1 << ind;
                        }

                        length += chunk.getBlocks().length;
                        length += chunk.getMetadata().getData().length;
                        length += chunk.getBlockLight().getData().length;
                        if (chunk.getSkyLight() != null) {
                            length += chunk.getSkyLight().getData().length;
                        }

                        if (chunk.getExtendedBlocks() != null) {
                            length += chunk.getExtendedBlocks().getData().length;
                        }
                    }

                    if (pass == 1) {
                        byte[] blocks = chunk.getBlocks();
                        System.arraycopy(blocks, 0, data, pos, blocks.length);
                        pos += blocks.length;
                    }

                    if (pass == 2) {
                        byte meta[] = chunk.getMetadata().getData();
                        System.arraycopy(meta, 0, data, pos, meta.length);
                        pos += meta.length;
                    }

                    if (pass == 3) {
                        byte blocklight[] = chunk.getBlockLight().getData();
                        System.arraycopy(blocklight, 0, data, pos, blocklight.length);
                        pos += blocklight.length;
                    }

                    if (pass == 4 && chunk.getSkyLight() != null) {
                        byte skylight[] = chunk.getSkyLight().getData();
                        System.arraycopy(skylight, 0, data, pos, skylight.length);
                        pos += skylight.length;
                        sky = true;
                    }

                    if (pass == 5 && chunk.getExtendedBlocks() != null) {
                        byte extended[] = chunk.getExtendedBlocks().getData();
                        System.arraycopy(extended, 0, data, pos, extended.length);
                        pos += extended.length;
                    }
                }
            }

            if (pass == 0) {
                data = new byte[length];
            }
        }

        // Add biomes.
        if (fullChunk) {
            System.arraycopy(chunks.getBiomes(), 0, data, pos, chunks.getBiomes().length);
            pos += chunks.getBiomes().length;
        }

        return new NetworkChunkData(chunkMask, extendedChunkMask, fullChunk, sky, data);
    }

    private static class NetInputStream extends InputStream {
        private NetInput in;
        private boolean readFirst;
        private byte firstByte;

        public NetInputStream(NetInput in, byte firstByte) {
            this.in = in;
            this.firstByte = firstByte;
        }

        @Override
        public int read() throws IOException {
            if (!this.readFirst) {
                this.readFirst = true;
                return this.firstByte;
            } else {
                return this.in.readUnsignedByte();
            }
        }
    }

    private static class NetOutputStream extends OutputStream {
        private NetOutput out;

        public NetOutputStream(NetOutput out) {
            this.out = out;
        }

        @Override
        public void write(int b) throws IOException {
            this.out.writeByte(b);
        }
    }

}
