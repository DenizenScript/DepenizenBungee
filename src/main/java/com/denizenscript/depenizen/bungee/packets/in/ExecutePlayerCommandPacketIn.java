package com.denizenscript.depenizen.bungee.packets.in;

import com.denizenscript.depenizen.bungee.DepenizenConnection;
import com.denizenscript.depenizen.bungee.PacketIn;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class ExecutePlayerCommandPacketIn extends PacketIn {

    @Override
    public String getName() {
        return "ExecutePlayerCommand";
    }

    @Override
    public void process(DepenizenConnection connection, ByteBuf data) {
        String player = readString(connection, data, "player");
        String command = readString(connection, data, "command");
        if (command == null || player == null) {
            return;
        }
        ProxiedPlayer playerObj = ProxyServer.getInstance().getPlayer(UUID.fromString(player));
        if (playerObj == null) {
            return;
        }
        ProxyServer.getInstance().getPluginManager().dispatchCommand(playerObj, command);
    }
}
