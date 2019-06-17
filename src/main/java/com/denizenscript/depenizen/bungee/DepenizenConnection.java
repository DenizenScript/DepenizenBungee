package com.denizenscript.depenizen.bungee;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class DepenizenConnection extends ChannelInboundHandlerAdapter {

    public void build(Channel channel) {
        this.channel = channel;
        connectionName = channel.remoteAddress().toString();
        while (channel.pipeline().firstContext() != null) {
            channel.pipeline().removeFirst();
        }
        channel.pipeline().addLast(this).addLast(new NettyExceptionHandler());
        DepenizenBungee.instance.getLogger().info("Connection '" + connectionName + "' now under control of Depenizen.");
    }

    public void fail(String reason) {
        DepenizenBungee.instance.getLogger().info("Connection '" + connectionName + "' failed: " + reason);
        channel.close();
    }

    public static enum Stage {
        AWAIT_HEADER,
        AWAIT_DATA
    }

    public String connectionName;

    public Stage currentStage = Stage.AWAIT_HEADER;

    public Channel channel;

    public ByteBuf tmp;

    public int waitingLength;

    public int packetId;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        tmp = ctx.alloc().buffer(4);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        DepenizenBungee.instance.getLogger().info("Connection '" + connectionName + "' ended.");
        tmp.release();
        tmp = null;
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
                DepenizenBungee.instance.getLogger().info("Received packet " + DepenizenBungee.instance.packets.get(packetId).getName()); // TODO: Temp!
            }
        }
        if (currentStage == Stage.AWAIT_DATA) {
            if (tmp.readableBytes() >= waitingLength) {
                try {
                    DepenizenBungee.instance.getLogger().info("Process packet " + DepenizenBungee.instance.packets.get(packetId).getName()); // TODO: Temp!
                    DepenizenBungee.instance.packets.get(packetId).process(this, tmp);
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
