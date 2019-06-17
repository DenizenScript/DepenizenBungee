package com.denizenscript.depenizen.bungee.packets.out;

import com.denizenscript.depenizen.bungee.PacketOut;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;

public class ProxyPingPacketOut extends PacketOut {

    public long id;

    public String address;

    public int currentPlayers;

    public int maxPlayers;

    public String motd;

    public int protocol;

    public String version;

    @Override
    public int getPacketId() {
        return 56;
    }

    @Override
    public void writeTo(ByteBuf buf) {
        buf.writeLong(id);
        byte[] addrBytes = address.getBytes(Charsets.UTF_8);
        buf.writeInt(addrBytes.length);
        buf.writeBytes(addrBytes);
        buf.writeInt(currentPlayers);
        buf.writeInt(maxPlayers);
        byte[] motdBytes = motd.getBytes(Charsets.UTF_8);
        buf.writeInt(motdBytes.length);
        buf.writeBytes(motdBytes);
        buf.writeInt(protocol);
        byte[] versionBytes = version.getBytes(Charsets.UTF_8);
        buf.writeInt(versionBytes.length);
        buf.writeBytes(versionBytes);
    }
}
