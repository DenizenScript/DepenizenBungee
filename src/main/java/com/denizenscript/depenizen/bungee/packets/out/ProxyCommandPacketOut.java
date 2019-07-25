package com.denizenscript.depenizen.bungee.packets.out;

import com.denizenscript.depenizen.bungee.PacketOut;
import io.netty.buffer.ByteBuf;

public class ProxyCommandPacketOut extends PacketOut {

    public ProxyCommandPacketOut(long id ,String sender, String command) {
        this.id = id;
        this.sender = sender;
        this.command = command;
    }

    public long id;

    public String sender;

    public String command;

    @Override
    public int getPacketId() {
        return 61;
    }

    @Override
    public void writeTo(ByteBuf buf) {
        buf.writeLong(id);
        writeString(buf, sender);
        writeString(buf, command);
    }
}
