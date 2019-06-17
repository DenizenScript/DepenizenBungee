package com.denizenscript.depenizen.bungee.packets.out;

import com.denizenscript.depenizen.bungee.PacketOut;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class PlayerQuitPacketOut extends PacketOut {

    public String name;

    public UUID uuid;

    @Override
    public int getPacketId() {
        return 54;
    }

    @Override
    public void writeTo(ByteBuf buf) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
        byte[] nameBytes = name.getBytes(Charsets.UTF_8);
        buf.writeInt(nameBytes.length);
        buf.writeBytes(nameBytes);
    }
}
