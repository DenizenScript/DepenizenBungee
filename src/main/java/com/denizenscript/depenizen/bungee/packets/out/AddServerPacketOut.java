package com.denizenscript.depenizen.bungee.packets.out;

import com.denizenscript.depenizen.bungee.PacketOut;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;

public class AddServerPacketOut extends PacketOut {

    public AddServerPacketOut(String name) {
        this.name = name;
    }

    public String name;

    @Override
    public int getPacketId() {
        return 51;
    }

    @Override
    public void writeTo(ByteBuf buf) {
        byte[] nameBytes = name.getBytes(Charsets.UTF_8);
        buf.writeInt(nameBytes.length);
        buf.writeBytes(nameBytes);
    }
}
