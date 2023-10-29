package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.packetlib.AbstractServer;
import com.github.steveice10.packetlib.BuiltinFlags;
import com.github.steveice10.packetlib.helper.TransportHelper;
import com.github.steveice10.packetlib.packet.PacketProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.incubator.channel.uring.IOUringEventLoopGroup;
import io.netty.incubator.channel.uring.IOUringServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

public class TcpServer extends AbstractServer {
    private EventLoopGroup group;
    private Channel channel;

    public TcpServer(String host, int port, Supplier<? extends PacketProtocol> protocol) {
        super(host, port, protocol);
    }

    @Override
    public boolean isListening() {
        return this.channel != null && this.channel.isOpen();
    }

    @Override
    public void bindImpl(boolean wait, final Runnable callback) {
        if (this.group != null || this.channel != null) {
            return;
        }

        group = TransportHelper.createEventLoopGroup();

        ChannelFuture future = new ServerBootstrap().channel(TransportHelper.SERVER_SOCKET_CHANNEL_CLASS).childHandler(new ChannelInitializer<>() {
            @Override
            public void initChannel(Channel channel) {
                InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
                PacketProtocol protocol = createPacketProtocol();

                TcpSession session = new TcpServerSession(address.getHostName(), address.getPort(), protocol, TcpServer.this);
                session.getPacketProtocol().newServerSession(TcpServer.this, session);

                channel.config().setOption(ChannelOption.IP_TOS, 0x18);
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException ignored) {
                }

                ChannelPipeline pipeline = channel.pipeline();

                session.refreshReadTimeoutHandler(channel);
                session.refreshWriteTimeoutHandler(channel);

                int size = protocol.getPacketHeader().getLengthSize();
                if (size > 0) {
                    pipeline.addLast("sizer", new TcpPacketSizer(session, size));
                }

                pipeline.addLast("codec", new TcpPacketCodec(session, false));
                pipeline.addLast("manager", session);
            }
        }).group(this.group).localAddress(this.getHost(), this.getPort()).bind();

        if (wait) {
            try {
                future.sync();
            } catch (InterruptedException e) {
            }

            channel = future.channel();
            if (callback != null) {
                callback.run();
            }
        } else {
            future.addListener((ChannelFutureListener) future1 -> {
                if (future1.isSuccess()) {
                    channel = future1.channel();
                    if (callback != null) {
                        callback.run();
                    }
                } else {
                    System.err.println("[ERROR] Failed to asynchronously bind connection listener.");
                    if (future1.cause() != null) {
                        future1.cause().printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void closeImpl(boolean wait, final Runnable callback) {
        if (this.channel != null) {
            if (this.channel.isOpen()) {
                ChannelFuture future = this.channel.close();
                if (wait) {
                    try {
                        future.sync();
                    } catch (InterruptedException e) {
                    }

                    if (callback != null) {
                        callback.run();
                    }
                } else {
                    future.addListener((ChannelFutureListener) future1 -> {
                        if (future1.isSuccess()) {
                            if (callback != null) {
                                callback.run();
                            }
                        } else {
                            System.err.println("[ERROR] Failed to asynchronously close connection listener.");
                            if (future1.cause() != null) {
                                future1.cause().printStackTrace();
                            }
                        }
                    });
                }
            }

            this.channel = null;
        }

        if (this.group != null) {
            Future<?> future = this.group.shutdownGracefully();
            if (wait) {
                try {
                    future.sync();
                } catch (InterruptedException e) {
                }
            } else {
                future.addListener((GenericFutureListener<? extends Future<Object>>) future12 -> {
                    if (!future12.isSuccess() && getGlobalFlag(BuiltinFlags.PRINT_DEBUG, false)) {
                        System.err.println("[ERROR] Failed to asynchronously close connection listener.");
                        if (future12.cause() != null) {
                            future12.cause().printStackTrace();
                        }
                    }
                });
            }

            this.group = null;
        }
    }
}
