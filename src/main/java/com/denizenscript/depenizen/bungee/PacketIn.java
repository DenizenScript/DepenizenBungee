package com.denizenscript.depenizen.bungee;

import io.netty.buffer.ByteBuf;

public abstract class PacketIn {

    public abstract String getName();

    public abstract void process(DepenizenConnection connection, ByteBuf data);
}
