package com.denizenscript.depenizen.bungee.packets.in;

import com.denizenscript.depenizen.bungee.DepenizenBungee;
import com.denizenscript.depenizen.bungee.DepenizenConnection;
import com.denizenscript.depenizen.bungee.PacketIn;
import io.netty.buffer.ByteBuf;

import java.util.concurrent.CompletableFuture;

public class ProxyCommandResultPacketIn extends PacketIn {

    @Override
    public String getName() {
        return "ProxyCommandResult";
    }

    @Override
    public void process(DepenizenConnection connection, ByteBuf data) {
        if (data.readableBytes() < 8 + 4) {
            connection.fail("Invalid ProxyCommandResultPacket (bytes available: " + data.readableBytes() + ")");
            return;
        }
        long id = data.readLong();
        String result = readString(connection, data, "result");
        if (result == null) {
            return;
        }
        CompletableFuture<String> future = DepenizenBungee.instance.proxyCommandWaiters.get(id);
        if (future == null) {
            return;
        }
        future.complete(result.length() > 0 ? result : null);
        DepenizenBungee.instance.proxyCommandWaiters.remove(id);
    }
}
