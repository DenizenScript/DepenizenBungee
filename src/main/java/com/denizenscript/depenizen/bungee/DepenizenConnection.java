package com.denizenscript.depenizen.bungee;

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
                if (thisServer.getName() != null) {
                    DepenizenBungee.instance.broadcastPacket(new AddServerPacketOut(thisServer.getName()));
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    public void broadcastRemoval() {
        ProxyServer.getInstance().getScheduler().schedule(DepenizenBungee.instance, new Runnable() {
            @Override
            public void run() {
                if (thisServer.getName() != null) {
                    DepenizenBungee.instance.broadcastPacket(new RemoveServerPacketOut(thisServer.getName()));
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    public boolean isValid = false;

    public String connectionName;

    public Stage currentStage = Stage.AWAIT_HEADER;

    public Channel channel;

    public ByteBuf tmp;

    public int waitingLength;

    public int packetId;

    public int serverPort;

    public InetAddress serverAddress;

    public ServerInfo thisServer;

    public boolean controlsProxyPing = false;

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

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        tmp = ctx.alloc().buffer(4);
        lastPacketReceived = System.currentTimeMillis();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        DepenizenBungee.instance.getLogger().info("Connection '" + connectionName + "' ended.");
        tmp.release();
        tmp = null;
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
        tmp.writeBytes(m);
        m.release();
        if (currentStage == Stage.AWAIT_HEADER) {
            if (tmp.readableBytes() >= 8) {
                waitingLength = tmp.readInt();
                packetId = tmp.readInt();
                currentStage = Stage.AWAIT_DATA;
                if (!DepenizenBungee.instance.packets.containsKey(packetId)) {
                    fail("Invalid packet id: " + packetId);
                    return;
                }
            }
        }
        if (currentStage == Stage.AWAIT_DATA) {
            if (tmp.readableBytes() >= waitingLength) {
                try {
                    lastPacketReceived = System.currentTimeMillis();
                    PacketIn packet = DepenizenBungee.instance.packets.get(packetId);
                    packet.process(this, tmp);
                    currentStage = Stage.AWAIT_HEADER;
                }
                catch (Throwable ex) {
                    ex.printStackTrace();
                    fail("Internal exception.");
                    return;
                }
            }
        }
    }
}
