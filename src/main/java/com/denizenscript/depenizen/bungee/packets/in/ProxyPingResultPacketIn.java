package com.denizenscript.depenizen.bungee.packets.in;

import com.denizenscript.depenizen.bungee.DepenizenBungee;
import com.denizenscript.depenizen.bungee.DepenizenConnection;
import com.denizenscript.depenizen.bungee.PacketIn;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;

public class ProxyPingResultPacketIn extends PacketIn {

    @Override
    public String getName() {
        return "ProxyPingResult";
    }

    @Override
    public void process(DepenizenConnection connection, ByteBuf data) {
        if (data.readableBytes() < 8 + 4 + 4) {
            connection.fail("Invalid ProxyPingResultPacket (bytes available: " + data.readableBytes() + ")");
            return;
        }
        long id = data.readLong();
        int maxPlayers = data.readInt();
        int versionLength = data.readInt();
        if (data.readableBytes() < versionLength || versionLength < 0) {
            connection.fail("Invalid ProxyPingResultPacket (version bytes requested: " + versionLength + ")");
            return;
        }
        byte[] versionBytes = new byte[versionLength];
        data.readBytes(versionBytes, 0, versionLength);
        String version = new String(versionBytes, Charsets.UTF_8);
        int motdLength = data.readInt();
        if (data.readableBytes() < motdLength || motdLength < 0) {
            connection.fail("Invalid ProxyPingResultPacket (version bytes requested: " + motdLength + ")");
            return;
        }
        byte[] motdBytes = new byte[motdLength];
        data.readBytes(motdBytes, 0, motdLength);
        String motd = new String(motdBytes, Charsets.UTF_8);
        ProxyPingEvent event = connection.proxyEventMap.get(id);
        if (event == null) {
            return;
        }
        event.getResponse().getPlayers().setMax(maxPlayers);
        if (!motd.equals(event.getResponse().getDescriptionComponent().toLegacyText())) {
            event.getResponse().setDescriptionComponent(TextComponent.fromLegacyText(motd)[0]);
        }
        event.getResponse().getVersion().setName(version);
        event.completeIntent(DepenizenBungee.instance);
        connection.proxyEventMap.remove(id);
    }
}
