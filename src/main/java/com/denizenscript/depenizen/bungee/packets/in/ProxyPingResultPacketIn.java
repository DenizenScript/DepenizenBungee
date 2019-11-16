package com.denizenscript.depenizen.bungee.packets.in;

import com.denizenscript.depenizen.bungee.DepenizenBungee;
import com.denizenscript.depenizen.bungee.DepenizenConnection;
import com.denizenscript.depenizen.bungee.PacketIn;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.api.chat.BaseComponent;
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
        String version = readString(data, versionLength);
        int motdLength = data.readInt();
        if (data.readableBytes() < motdLength || motdLength < 0) {
            connection.fail("Invalid ProxyPingResultPacket (motd bytes requested: " + motdLength + ")");
            return;
        }
        String motd = readString(data, motdLength);
        ProxyPingEvent event = connection.proxyEventMap.get(id);
        if (event == null) {
            return;
        }
        event.getResponse().getPlayers().setMax(maxPlayers);
        if (!motd.equals(event.getResponse().getDescriptionComponent().toLegacyText())) {
            TextComponent result = new TextComponent();
            for (BaseComponent comp : TextComponent.fromLegacyText(motd)) {
                result.addExtra(comp);
            }
            event.getResponse().setDescriptionComponent(result);
        }
        event.getResponse().getVersion().setName(version);
        event.completeIntent(DepenizenBungee.instance);
        connection.proxyEventMap.remove(id);
    }
}
