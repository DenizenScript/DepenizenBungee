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
        // Do nothing
    }
}
