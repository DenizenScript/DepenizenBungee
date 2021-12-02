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
        String command = readString(connection, data, "command");
        if (command == null) {
            return;
        }
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
    }
}
