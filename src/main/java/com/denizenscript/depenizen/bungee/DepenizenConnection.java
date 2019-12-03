package com.denizenscript.depenizen.bungee;

import com.denizenscript.depenizen.bungee.packets.in.MyInfoPacketIn;
import com.denizenscript.depenizen.bungee.packets.out.AddServerPacketOut;
import com.denizenscript.depenizen.bungee.packets.out.RemoveServerPacketOut;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ProxyPingEvent;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DepenizenConnection extends ChannelInboundHandlerAdapter {

    public void build(Channel channel, InetAddress address) {
        this.serverAddress = address;
        this.channel = channel;
        connectionName = channel.remoteAddress().toString();
        while (channel.pipeline().firstContext() != null) {
            channel.pipeline().removeFirst();
        }
        channel.pipeline().addLast(this).addLast(new NettyExceptionHandler());
        for (DepenizenConnection server : DepenizenBungee.instance.getConnections()) {
            if (server.thisServer != null && server.thisServer.getName() != null) {
                sendPacket(new AddServerPacketOut(server.thisServer.getName()));
            }
        }
        DepenizenBungee.instance.addConnection(this);
        DepenizenBungee.instance.getLogger().info("Connection '" + connectionName + "' now under control of Depenizen.");
        isValid = true;
    }

    public void fail(String reason) {
        DepenizenBungee.instance.getLogger().info("Connection '" + connectionName + (thisServer == null ? "" : (" / " + thisServer.getName())) + "' failed: " + reason);
        channel.close();
    }

    public static enum Stage {
        AWAIT_HEADER,
        AWAIT_DATA
    }

    public void broadcastIdentity() {
        ProxyServer.getInstance().getScheduler().schedule(DepenizenBungee.instance, new Runnable() {
            @Override
            public void run() {
                if (thisServer != null) {
                    DepenizenBungee.instance.broadcastPacket(new AddServerPacketOut(thisServer.getName()));
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    public void broadcastRemoval() {
        ProxyServer.getInstance().getScheduler().schedule(DepenizenBungee.instance, new Runnable() {
            @Override
            public void run() {
                if (thisServer != null) {
                    DepenizenBungee.instance.broadcastPacket(new RemoveServerPacketOut(thisServer.getName()));
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    public boolean isValid = false;

    public String connectionName;

    public Stage currentStage = Stage.AWAIT_HEADER;

    public Channel channel;

    public ByteBuf packetBuffer;

    public int waitingLength;

    public int packetId;

    public int serverPort;

    public InetAddress serverAddress;

    public ServerInfo thisServer;

    public boolean controlsProxyPing = false;

    public boolean controlsProxyCommand = false;

    public long proxyPingId = 1;

    public long lastPacketReceived = 0;

    public HashMap<Long, ProxyPingEvent> proxyEventMap = new HashMap<>();

    public void sendPacket(PacketOut packet) {
        ByteBuf buf = channel.alloc().buffer();
        packet.writeTo(buf);
        ByteBuf header = channel.alloc().buffer();
        header.writeInt(buf.writerIndex());
        header.writeInt(packet.getPacketId());
        channel.writeAndFlush(header);
        channel.writeAndFlush(buf);
    }

    public void reallocateBuf(ChannelHandlerContext ctx) {
        ByteBuf newBuf = ctx.alloc().buffer(32);
        if (packetBuffer != null) {
            newBuf.writeBytes(packetBuffer);
            packetBuffer.release();
        }
        packetBuffer = newBuf;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        if (packetBuffer != null) {
            packetBuffer.release();
        }
        packetBuffer = ctx.alloc().buffer(32);
        lastPacketReceived = System.currentTimeMillis();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        DepenizenBungee.instance.getLogger().info("Connection '" + connectionName + "' ended.");
        packetBuffer.release();
        packetBuffer = null;
        isValid = false;
        DepenizenBungee.instance.removeConnection(this);
        broadcastRemoval();
        for (ProxyPingEvent event : proxyEventMap.values()) {
            event.completeIntent(DepenizenBungee.instance);
        }
        proxyEventMap.clear();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg;
        packetBuffer.writeBytes(m);
        m.release();
        while (true) {
            if (currentStage == Stage.AWAIT_HEADER) {
                if (packetBuffer.readableBytes() < 8) {
                    return;
                }
                waitingLength = packetBuffer.readInt();
                packetId = packetBuffer.readInt();
                currentStage = Stage.AWAIT_DATA;
                if (thisServer == null && packetId != MyInfoPacketIn.PACKET_ID) {
                    fail("Invalid FIRST packet id (must be MyInfoPacket): " + packetId + ", data length thus far = " + packetBuffer.readableBytes());
                    return;
                }
                if (!DepenizenBungee.instance.packets.containsKey(packetId)) {
                    fail("Invalid packet id: " + packetId);
                    return;
                }
            }
            else if (currentStage == Stage.AWAIT_DATA) {
                if (packetBuffer.readableBytes() < waitingLength) {
                    return;
                }
                try {
                    lastPacketReceived = System.currentTimeMillis();
                    PacketIn packet = DepenizenBungee.instance.packets.get(packetId);
                    packet.process(this, packetBuffer);
                    currentStage = Stage.AWAIT_HEADER;
                    reallocateBuf(ctx);
                }
                catch (Throwable ex) {
                    ex.printStackTrace();
                    fail("Internal exception.");
                    return;
                }
            }
            else {
                return;
            }
        }
    }
}
