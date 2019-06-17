package com.denizenscript.depenizen.bungee;

import com.denizenscript.depenizen.bungee.packets.in.SendPlayerPacketIn;
import io.netty.channel.Channel;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
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
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DepenizenBungee extends Plugin implements Listener {

    public static DepenizenBungee instance;

    public YamlConfiguration config;

    public File configFile;

    public void saveDefaultConfig() {
        InputStream is = DepenizenBungee.class.getClassLoader().getResourceAsStream("config.yml");
    }

    public HashMap<Integer, PacketIn> packets = new HashMap<>();

    public void registerPackets() {
        packets.put(10, new SendPlayerPacketIn());
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("DepenizenBungee loading...");
        getProxy().getPluginManager().registerListener(this, this);
        configFile = new File(getDataFolder(), "config.yml");
        registerPackets();
    }

    @EventHandler
    public void onPlayerHandshake(PlayerHandshakeEvent event) {
        InitialHandler handler = (InitialHandler) event.getConnection();
        // Only operate on connections started by Depenizen
        if (!handler.getExtraDataInHandshake().equals("\0depen")) {
            return;
        }
        getLogger().info("Depenizen handshake seen from: " + handler.getAddress());
        InetAddress address = handler.getAddress().getAddress();
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
                    connection.build(channel);
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
