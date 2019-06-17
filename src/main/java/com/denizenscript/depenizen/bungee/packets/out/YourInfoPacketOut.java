package com.denizenscript.depenizen.bungee.packets.out;

import com.denizenscript.depenizen.bungee.PacketOut;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;

public class YourInfoPacketOut extends PacketOut {

    public YourInfoPacketOut(String name) {
        this.name = name;
    }

    public String name;

    @Override
    public int getPacketId() {
        return 50;
    }

    @Override
    public void writeTo(ByteBuf buf) {
        byte[] nameBytes = name.getBytes(Charsets.UTF_8);
        buf.writeInt(nameBytes.length);
        buf.writeBytes(nameBytes);
    }
}
