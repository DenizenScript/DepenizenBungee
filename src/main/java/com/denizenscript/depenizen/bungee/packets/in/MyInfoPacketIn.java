package com.denizenscript.depenizen.bungee.packets.in;

import com.denizenscript.depenizen.bungee.DepenizenConnection;
import com.denizenscript.depenizen.bungee.PacketIn;
import com.denizenscript.depenizen.bungee.packets.out.YourInfoPacketOut;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class MyInfoPacketIn extends PacketIn {

    public static int PACKET_ID = 11;

    @Override
    public String getName() {
        return "MyInfo";
    }

    @Override
    public void process(DepenizenConnection connection, ByteBuf data) {
        if (data.readableBytes() < 4) {
            connection.fail("Invalid MyInfoPacket (bytes available: " + data.readableBytes() + ")");
            return;
        }
        int port = data.readInt();
        connection.serverPort = port;
        for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
            if (server.getAddress().getAddress().equals(connection.serverAddress) && server.getAddress().getPort() == port) {
                connection.thisServer = server;
                break;
            }
        }
        if (connection.thisServer == null) {
            connection.fail("Invalid MyInfoPacket (unknown server)");
            return;
        }
        connection.sendPacket(new YourInfoPacketOut(connection.thisServer.getName()));
        connection.broadcastIdentity();
    }
}
