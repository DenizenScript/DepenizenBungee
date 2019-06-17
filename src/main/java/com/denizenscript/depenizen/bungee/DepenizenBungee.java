package com.denizenscript.depenizen.bungee;

import com.denizenscript.depenizen.bungee.packets.in.ControlProxyPingPacketIn;
import com.denizenscript.depenizen.bungee.packets.in.MyInfoPacketIn;
import com.denizenscript.depenizen.bungee.packets.in.ProxyPingResultPacketIn;
import com.denizenscript.depenizen.bungee.packets.in.SendPlayerPacketIn;
import com.denizenscript.depenizen.bungee.packets.out.PlayerJoinPacketOut;
import com.denizenscript.depenizen.bungee.packets.out.PlayerQuitPacketOut;
import com.denizenscript.depenizen.bungee.packets.out.PlayerSwitchServerPacketOut;
import com.denizenscript.depenizen.bungee.packets.out.ProxyPingPacketOut;
import io.netty.channel.Channel;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.netty.ChannelWrapper;

import java.io.File;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DepenizenBungee extends Plugin implements Listener {

    public static DepenizenBungee instance;

    public YamlConfiguration config;

    public File configFile;

    public void saveDefaultConfig() {
        InputStream is = DepenizenBungee.class.getClassLoader().getResourceAsStream("config.yml");
    }

    public HashMap<Integer, PacketIn> packets = new HashMap<>();

    public final List<DepenizenConnection> connections = new ArrayList<>();

    public void addConnection(DepenizenConnection connection) {
        synchronized (connections) {
            connections.add(connection);
        }
    }

    public void removeConnection(DepenizenConnection connection) {
        synchronized (connections) {
            connections.remove(connection);
        }
    }

    public List<DepenizenConnection> getConnections() {
        synchronized (connections) {
            return new ArrayList<>(connections);
        }
    }

    public void registerPackets() {
        packets.put(10, new SendPlayerPacketIn());
        packets.put(11, new MyInfoPacketIn());
        packets.put(12, new ControlProxyPingPacketIn());
        packets.put(13, new ProxyPingResultPacketIn());
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("DepenizenBungee loading...");
        getProxy().getPluginManager().registerListener(this, this);
        configFile = new File(getDataFolder(), "config.yml");
        registerPackets();
    }

    public void broadcastPacket(PacketOut packet) {
        for (DepenizenConnection connection : getConnections()) {
            connection.sendPacket(packet);
        }
    }

    @EventHandler
    public void onProxyPing(ProxyPingEvent event) {
        for (DepenizenConnection connection : getConnections()) {
            if (connection.controlsProxyPing && connection.isValid) {
                event.registerIntent(this);
                long id = connection.proxyPingId++;
                connection.proxyEventMap.put(id, event);
                ProxyPingPacketOut packet = new ProxyPingPacketOut();
                packet.id = id;
                packet.address = event.getConnection().getAddress().toString();
                packet.currentPlayers = event.getResponse().getPlayers().getOnline();
                packet.maxPlayers = event.getResponse().getPlayers().getMax();
                packet.motd = event.getResponse().getDescriptionComponent().toLegacyText();
                packet.protocol = event.getResponse().getVersion().getProtocol();
                packet.version = event.getResponse().getVersion().getName();
                connection.sendPacket(packet);
            }
        }
    }

    @EventHandler
    public void onServerSwitch(ServerConnectEvent event) {
        PlayerSwitchServerPacketOut packet = new PlayerSwitchServerPacketOut();
        packet.name = event.getPlayer().getName();
        packet.uuid = event.getPlayer().getUniqueId();
        packet.newServer = event.getTarget().getName();
        broadcastPacket(packet);
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        PlayerJoinPacketOut packet = new PlayerJoinPacketOut();
        packet.name = event.getPlayer().getName();
        packet.uuid = event.getPlayer().getUniqueId();
        broadcastPacket(packet);
    }

    @EventHandler
    public void onPlayerQuit(PlayerDisconnectEvent event) {
        PlayerQuitPacketOut packet = new PlayerQuitPacketOut();
        packet.name = event.getPlayer().getName();
        packet.uuid = event.getPlayer().getUniqueId();
        broadcastPacket(packet);
    }

    @EventHandler
    public void onPlayerHandshake(PlayerHandshakeEvent event) {
        InitialHandler handler = (InitialHandler) event.getConnection();
        // Only operate on connections started by Depenizen
        if (!handler.getExtraDataInHandshake().equals("\0depen")) {
            return;
        }
        getLogger().info("Depenizen handshake seen from: " + handler.getAddress());
        final InetAddress address = handler.getAddress().getAddress();
        if (!address.isLoopbackAddress()) { // Localhost is always allowed
            boolean isValid = false;
            for (ServerInfo info : getProxy ().getServers().values()) {
                if (info.getAddress().getAddress().equals(address)) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                getLogger().warning("INVALID/FAKE Depenizen connection denied from: " + handler.getAddress());
                return;
            }
        }
        final Channel channel;
        try {
            // Set 'closed' to true, so Bungee ignores the connection
            ChannelWrapper wrapper = (ChannelWrapper) INITIALHANDLER_GET_CH.invoke(handler);
            CHANNELWRAPPER_SET_CLOSED.invoke(wrapper, true);
            // Get the underlying netty channel to do with as we please
            channel = (Channel) CHANNELWRAPPER_GET_CH.invoke(wrapper);
            // Add it to the list, for the network thread to handle
            final DepenizenConnection connection = new DepenizenConnection();
            getProxy().getScheduler().schedule(this, new Runnable() {
                @Override
                public void run() {
                    connection.build(channel, address);
                }
            }, 500, TimeUnit.MILLISECONDS);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            return;
        }
    }

    public static MethodHandle INITIALHANDLER_GET_CH = ReflectionHelper.getGetter(InitialHandler.class, "ch");
    public static MethodHandle CHANNELWRAPPER_SET_CLOSED = ReflectionHelper.getSetter(ChannelWrapper.class, "closed");
    public static MethodHandle CHANNELWRAPPER_GET_CH = ReflectionHelper.getGetter(ChannelWrapper.class, "ch");

}
