package com.denizenscript.depenizen.bungee;

import io.netty.channel.*;

import java.net.SocketAddress;

public class NettyExceptionHandler extends ChannelDuplexHandler {

    public DepenizenConnection connection;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        DepenizenBungee.instance.getLogger().info("Connection " + connection.connectionName + " caught an exception");
        cause.printStackTrace();
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        ctx.connect(remoteAddress, localAddress, promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (!future.isSuccess()) {
                    DepenizenBungee.instance.getLogger().info("Connection " + connection.connectionName + " failed to operationComplete");
                    future.cause().printStackTrace();
                }
            }
        }));
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) {
        ctx.close(promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (!future.isSuccess()) {
                    DepenizenBungee.instance.getLogger().info("Connection " + connection.connectionName + " failed to close");
                    future.cause().printStackTrace();
                }
            }
        }));
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        ctx.write(msg, promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (!future.isSuccess()) {
                    DepenizenBungee.instance.getLogger().info("Connection " + connection.connectionName + " failed to write");
                    future.cause().printStackTrace();
                }
            }
        }));
    }
}
