package com.denizenscript.depenizen.bungee.packets.out;

import com.denizenscript.depenizen.bungee.PacketOut;
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
        writeString(buf, address);
        buf.writeInt(currentPlayers);
        buf.writeInt(maxPlayers);
        writeString(buf, motd);
        buf.writeInt(protocol);
        writeString(buf, version);
    }
}
