package com.denizenscript.depenizen.bungee.packets.out;

import com.denizenscript.depenizen.bungee.PacketOut;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class PlayerSwitchServerPacketOut extends PacketOut {

    public String name;

    public UUID uuid;

    public String newServer;

    @Override
    public int getPacketId() {
        return 55;
    }

    @Override
    public void writeTo(ByteBuf buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
        byte[] nameBytes = name.getBytes(Charsets.UTF_8);
        buf.writeInt(nameBytes.length);
        buf.writeBytes(nameBytes);
        byte[] serverNameBytes = newServer.getBytes(Charsets.UTF_8);
        buf.writeInt(serverNameBytes.length);
        buf.writeBytes(serverNameBytes);
    }
}
