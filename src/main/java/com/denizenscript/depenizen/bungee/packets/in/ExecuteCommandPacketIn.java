package com.denizenscript.depenizen.bungee.packets.in;

import com.denizenscript.depenizen.bungee.DepenizenConnection;
import com.denizenscript.depenizen.bungee.PacketIn;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.api.ProxyServer;

public class ExecuteCommandPacketIn extends PacketIn {

    @Override
    public String getName() {
        return "ExecuteCommand";
    }

    @Override
    public void process(DepenizenConnection connection, ByteBuf data) {
        if (data.readableBytes() < 4) {
            connection.fail("Invalid ExecuteCommandPacket (bytes available: " + data.readableBytes() + ")");
            return;
        }
        int commandLength = data.readInt();
        if (data.readableBytes() < commandLength || commandLength < 0) {
            connection.fail("Invalid ExecuteCommandPacket (version bytes requested: " + commandLength + ")");
            return;
        }
        String command = readString(data, commandLength);
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
    }
}
