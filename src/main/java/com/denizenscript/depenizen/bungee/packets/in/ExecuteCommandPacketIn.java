package com.denizenscript.depenizen.bungee.packets.in;

import com.denizenscript.depenizen.bungee.DepenizenConnection;
import com.denizenscript.depenizen.bungee.PacketIn;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

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
        if (data.readableBytes() < 16) {
            ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
            return;
        }
        UUID executingPlayerUUID = new UUID(data.readLong(), data.readLong());
        ProxiedPlayer executingPlayer = ProxyServer.getInstance().getPlayer(executingPlayerUUID);
        if (executingPlayer != null) {
            ProxyServer.getInstance().getPluginManager().dispatchCommand(executingPlayer, command);
        }
    }
}
