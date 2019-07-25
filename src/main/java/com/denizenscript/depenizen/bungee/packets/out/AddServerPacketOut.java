package com.denizenscript.depenizen.bungee.packets.out;

import com.denizenscript.depenizen.bungee.PacketOut;
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
        writeString(buf, name);
    }
}
