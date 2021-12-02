package com.denizenscript.depenizen.bungee.packets.in;

import com.denizenscript.depenizen.bungee.DepenizenBungee;
import com.denizenscript.depenizen.bungee.DepenizenConnection;
import com.denizenscript.depenizen.bungee.PacketIn;
import com.denizenscript.depenizen.bungee.packets.out.RedirectedPacketOut;
import io.netty.buffer.ByteBuf;

public class RedirectPacketIn extends PacketIn {

    @Override
    public String getName() {
        return "Redirect";
    }

    @Override
    public void process(DepenizenConnection connection, ByteBuf data) {
        if (data.readableBytes() < 12) {
            connection.fail("Invalid RedirectPacket (bytes available: " + data.readableBytes() + ")");
            return;
        }
        String serverName = readString(connection, data, "serverName");
        if (serverName == null) {
            return;
        }
        int newPacketLen = data.readInt();
        if (data.readableBytes() < newPacketLen || newPacketLen < 0) {
            connection.fail("Invalid RedirectPacket (packet bytes requested: " + newPacketLen + ")");
            return;
        }
        int newId = data.readInt();
        byte[] newPacket = new byte[newPacketLen];
        data.readBytes(newPacket, 0, newPacketLen);
        DepenizenConnection targetConnection = DepenizenBungee.instance.getConnectionByName(serverName);
        if (targetConnection == null) {
            DepenizenBungee.instance.getLogger().warning("Invalid server name '" + serverName + "'");
            return;
        }
        targetConnection.sendPacket(new RedirectedPacketOut(newId, newPacket));
    }
}
