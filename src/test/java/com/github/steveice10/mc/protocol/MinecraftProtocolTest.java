package com.github.steveice10.mc.protocol;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.codec.MinecraftCodec;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.data.status.PlayerInfo;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.mc.protocol.data.status.VersionInfo;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoBuilder;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoHandler;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.DoubleTag;
import com.github.steveice10.opennbt.tag.builtin.FloatTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpClientSession;
import com.github.steveice10.packetlib.tcp.TcpServer;
import net.kyori.adventure.text.Component;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static com.github.steveice10.mc.protocol.MinecraftConstants.SERVER_COMPRESSION_THRESHOLD;
import static com.github.steveice10.mc.protocol.MinecraftConstants.SERVER_INFO_BUILDER_KEY;
import static com.github.steveice10.mc.protocol.MinecraftConstants.SERVER_INFO_HANDLER_KEY;
import static com.github.steveice10.mc.protocol.MinecraftConstants.SERVER_LOGIN_HANDLER_KEY;
import static com.github.steveice10.mc.protocol.MinecraftConstants.VERIFY_USERS_KEY;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MinecraftProtocolTest {
    private static final String HOST = "localhost";
    private static final int PORT = 25562;

    private static final ServerStatusInfo SERVER_INFO = new ServerStatusInfo(
            new VersionInfo(MinecraftCodec.CODEC.getMinecraftVersion(), MinecraftCodec.CODEC.getProtocolVersion()),
            new PlayerInfo(100, 0, new GameProfile[0]),
            Component.text("Hello world!"),
            null,
            false
    );
    private static final ClientboundLoginPacket JOIN_GAME_PACKET = new ClientboundLoginPacket(0, false, GameMode.SURVIVAL, GameMode.SURVIVAL, 1, new String[]{"minecraft:world"}, getDimensionTag(), "overworld", "minecraft:world", 100, 0, 16, 16, false, false, false, false, null);

    private static Server server;

    @BeforeClass
    public static void setupServer() {
        server = new TcpServer(HOST, PORT, MinecraftProtocol::new);
        server.setGlobalFlag(VERIFY_USERS_KEY, false);
        server.setGlobalFlag(SERVER_COMPRESSION_THRESHOLD, 100);
        server.setGlobalFlag(SERVER_INFO_BUILDER_KEY, (ServerInfoBuilder) session -> SERVER_INFO);
        server.setGlobalFlag(SERVER_LOGIN_HANDLER_KEY, (ServerLoginHandler) session -> session.send(JOIN_GAME_PACKET));

        assertTrue("Could not bind server.", server.bind(true).isListening());
    }

    @AfterClass
    public static void tearDownServer() {
        if (server != null) {
            server.close(true);
            server = null;
        }
    }

    @Test
    public void testStatus() throws InterruptedException {
        Session session = new TcpClientSession(HOST, PORT, new MinecraftProtocol());
        try {
            ServerInfoHandlerTest handler = new ServerInfoHandlerTest();
            session.setFlag(SERVER_INFO_HANDLER_KEY, handler);
            session.addListener(new DisconnectListener());
            session.connect();

            handler.status.await(4, SECONDS);
            assertNotNull("Failed to get server info.", handler.info);
            assertEquals("Received incorrect server info.", SERVER_INFO, handler.info);
        } finally {
            session.disconnect("Status test complete.");
        }
    }

    @Test
    public void testLogin() throws InterruptedException {
        Session session = new TcpClientSession(HOST, PORT, new MinecraftProtocol("Username"));
        try {
            LoginListenerTest listener = new LoginListenerTest();
            session.addListener(listener);
            session.addListener(new DisconnectListener());
            session.connect();

            listener.login.await(4, SECONDS);
            assertNotNull("Failed to log in.", listener.packet);
            assertEquals("Received incorrect join packet.", JOIN_GAME_PACKET, listener.packet);
        } finally {
            session.disconnect("Login test complete.");
        }
    }

    private static class ServerInfoHandlerTest implements ServerInfoHandler {
        public CountDownLatch status = new CountDownLatch(1);
        public ServerStatusInfo info;

        @Override
        public void handle(Session session, ServerStatusInfo info) {
            this.info = info;
            this.status.countDown();
        }
    }

    private static class LoginListenerTest extends SessionAdapter {
        public CountDownLatch login = new CountDownLatch(1);
        public ClientboundLoginPacket packet;

        @Override
        public void packetReceived(Session session, Packet packet) {
            if (packet instanceof ClientboundLoginPacket) {
                this.packet = (ClientboundLoginPacket) packet;
                this.login.countDown();
            }
        }
    }

    private static class DisconnectListener extends SessionAdapter {
        @Override
        public void disconnected(DisconnectedEvent event) {
            System.err.println("Disconnected: " + event.getReason());
            if (event.getCause() != null) {
                event.getCause().printStackTrace();
            }
        }
    }

    private static CompoundTag getDimensionTag() {
        CompoundTag overworldTag = getOverworldTag();
        CompoundTag tag = new CompoundTag("minecraft:dimension_type");
        tag.put(new StringTag("type", "minecraft:dimension_type"));
        ListTag list = new ListTag("value");
        list.add(overworldTag);
        tag.put(list);
        return tag;
    }

    private static CompoundTag getOverworldTag() {
        CompoundTag overworldTag = new CompoundTag("");
        CompoundTag element = new CompoundTag("element");
        element.put(new FloatTag("ambient_light", 0f));
        element.put(new ByteTag("bed_works", (byte) 1));
        element.put(new DoubleTag("coordinate_scale", 1d));
        element.put(new StringTag("effects", "minecraft:overworld"));
        element.put(new ByteTag("has_ceiling", (byte) 0));
        element.put(new ByteTag("has_raids", (byte) 1));
        element.put(new ByteTag("has_skylight", (byte) 1));
        element.put(new IntTag("height", 384));
        element.put(new StringTag("infiniburn", "#minecraft:infiniburn_overworld"));
        element.put(new IntTag("logical_height", 384));
        element.put(new IntTag("min_y", -64));
        element.put(new IntTag("monster_spawner_block_light_limit", 0));
        CompoundTag spawnLightLevel = new CompoundTag("monster_spawn_light_level");
        spawnLightLevel.put(new StringTag("type", "minecraft:uniform"));
        CompoundTag value = new CompoundTag("value");
        value.put(new IntTag("max_inclusive", 7));
        value.put(new IntTag("min_inclusive", 0));
        spawnLightLevel.put(value);
        element.put(spawnLightLevel);
        element.put(new ByteTag("natural", (byte) 1));
        element.put(new ByteTag("piglin_safe", (byte) 0));
        element.put(new ByteTag("respawn_anchor_works", (byte) 0));
        element.put(new ByteTag("ultrawarm", (byte) 0));
        overworldTag.put(element);
        overworldTag.put(new IntTag("id", 0));
        overworldTag.put(new StringTag("name", "minecraft:overworld"));
        return overworldTag;
    }
}
