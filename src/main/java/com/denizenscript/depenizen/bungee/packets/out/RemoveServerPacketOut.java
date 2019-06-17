package com.denizenscript.depenizen.bungee.packets.out;

import com.denizenscript.depenizen.bungee.PacketOut;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;

public class RemoveServerPacketOut extends PacketOut {

    public RemoveServerPacketOut(String name) {
        this.name = name;
    }

    public String name;

    @Override
    public int getPacketId() {
        return 52;
    }

    @Override
    public void writeTo(ByteBuf buf) {
        byte[] nameBytes = name.getBytes(Charsets.UTF_8);
        buf.writeInt(nameBytes.length);
        buf.writeBytes(nameBytes);
    }
}
