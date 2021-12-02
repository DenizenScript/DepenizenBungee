package com.denizenscript.depenizen.bungee.packets.in;

import com.denizenscript.depenizen.bungee.DepenizenBungee;
import com.denizenscript.depenizen.bungee.DepenizenConnection;
import com.denizenscript.depenizen.bungee.PacketIn;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;

import java.util.UUID;

public class ProxyPingResultPacketIn extends PacketIn {

    @Override
    public String getName() {
        return "ProxyPingResult";
    }

    @Override
    public void process(DepenizenConnection connection, ByteBuf data) {
        if (data.readableBytes() < 8 + 4 + 4 + 4) {
            connection.fail("Invalid ProxyPingResultPacket (bytes available: " + data.readableBytes() + ")");
            return;
        }
        long id = data.readLong();
        int maxPlayers = data.readInt();
        String version = readString(connection, data, "version");
        String motd = readString(connection, data, "motd");
        if (version == null || motd == null) {
            return;
        }
        int playerListCount = data.readInt();
        if (playerListCount < -1 || playerListCount > 1024) {
            connection.fail("Invalid ProxyPingResultPacket (playerListCount requested: " + playerListCount + ")");
            return;
        }
        ServerPing.PlayerInfo[] playerInfo = playerListCount == -1 ? null : new ServerPing.PlayerInfo[playerListCount];
        for (int i = 0; i < playerListCount; i++) {
            String name = readString(connection, data, "name");
            if (name == null) {
                return;
            }
            long idMost = data.readLong();
            long idLeast = data.readLong();
            UUID uuid = new UUID(idMost, idLeast);
            playerInfo[i] = new ServerPing.PlayerInfo(name, uuid);
        }
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
        if (playerInfo != null) {
            event.getResponse().getPlayers().setSample(playerInfo);
        }
        event.getResponse().getVersion().setName(version);
        event.completeIntent(DepenizenBungee.instance);
        connection.proxyEventMap.remove(id);
    }
}
