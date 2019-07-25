package com.denizenscript.depenizen.bungee.packets.in;

import com.denizenscript.depenizen.bungee.DepenizenConnection;
import com.denizenscript.depenizen.bungee.PacketIn;
import io.netty.buffer.ByteBuf;

public class ControlProxyCommandPacketIn extends PacketIn {

    @Override
    public String getName() {
        return "ControlProxyCommand";
    }

    @Override
    public void process(DepenizenConnection connection, ByteBuf data) {
        if (data.readableBytes() < 1) {
            connection.fail("Invalid ControlProxyCommandPacket (bytes available: " + data.readableBytes() + ")");
            return;
        }
        byte b = data.readByte();
        connection.controlsProxyCommand = b != 0;
    }
}
