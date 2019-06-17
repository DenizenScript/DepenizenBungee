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

}
