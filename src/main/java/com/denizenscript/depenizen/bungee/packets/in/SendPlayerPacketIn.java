package com.denizenscript.depenizen.bungee.packets.in;

import com.denizenscript.depenizen.bungee.DepenizenBungee;
import com.denizenscript.depenizen.bungee.DepenizenConnection;
import com.denizenscript.depenizen.bungee.PacketIn;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class SendPlayerPacketIn extends PacketIn {

    @Override
    public String getName() {
        return "SendPlayer";
    }

    @Override
    public void process(DepenizenConnection connection, ByteBuf data) {
        if (data.readableBytes() < 12) {
            connection.fail("Invalid SendPlayerPacket (bytes available: " + data.readableBytes() + ")");
            return;
        }
        long mostSigBits = data.readLong();
        long leastSigBits = data.readLong();
        UUID uuid = new UUID(mostSigBits, leastSigBits);
        int targetNameLength = data.readInt();
        if (data.readableBytes() < targetNameLength || targetNameLength < 0) {
            connection.fail("Invalid SendPlayerPacket (name bytes requested: " + targetNameLength + ")");
            return;
        }
        byte[] serverNameBytes = new byte[targetNameLength];
        data.readBytes(serverNameBytes, 0, targetNameLength);
        String serverName = new String(serverNameBytes, Charsets.UTF_8);
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        if (player == null) {
            DepenizenBungee.instance.getLogger().warning("Invalid player uuid '" + uuid + "'");
            return;
        }
        ServerInfo info = ProxyServer.getInstance().getServerInfo(serverName);
        if (info == null) {
            DepenizenBungee.instance.getLogger().warning("Invalid server name '" + serverName + "'");
            return;
        }
        player.connect(info);
    }
}
