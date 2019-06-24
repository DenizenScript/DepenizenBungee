package com.denizenscript.depenizen.bungee.packets.in;

import com.denizenscript.depenizen.bungee.DepenizenConnection;
import com.denizenscript.depenizen.bungee.PacketIn;
import io.netty.buffer.ByteBuf;

public class KeepAlivePacketIn extends PacketIn {

    @Override
    public String getName() {
        return "KeepAlive";
    }

    @Override
    public void process(DepenizenConnection connection, ByteBuf data) {
        if (data.readableBytes() < 512) {
            connection.fail("Invalid KeepAlivePacket (bytes available: " + data.readableBytes() + ")");
            return;
        }
        // Read and ignore empty buffer
        data.readBytes(512);
    }
}
