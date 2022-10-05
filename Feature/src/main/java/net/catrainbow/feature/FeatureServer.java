package net.catrainbow.feature;

import com.nukkitx.protocol.bedrock.BedrockServer;
import com.nukkitx.protocol.bedrock.packet.BossEventPacket;
import com.nukkitx.protocol.bedrock.packet.LevelChunkPacket;
import net.catrainbow.feature.module.ModuleManager;
import net.catrainbow.feature.network.FeaturePacketHandler;
import net.catrainbow.feature.utils.GameHook;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class FeatureServer {

    public static final String PATH = System.getProperty("user.dir") + "/";
    private InetSocketAddress address;

    public static ArrayList<Player> players = new ArrayList<>();

    public FeatureServer(InetSocketAddress address) {
        this.address = address;
    }

    public void start() {
        InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", 19132);
        BedrockServer server = new BedrockServer(bindAddress);
        server.setHandler(new FeaturePacketHandler(this));
        server.bind().join();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                tick();
            }
        };
        Timer timer = new Timer("main");
        timer.scheduleAtFixedRate(timerTask, 50, 50);
    }

    public void tick() {
        if (!GameHook.getProcess()) {
            System.exit(0);
        }
        for (Player player : players) {
            if (player.getLevelChunkPackets().size() >= 1) {
                LevelChunkPacket levelChunkPacket = player.getLevelChunkPackets().get(0);
                player.getLevelChunkPackets().remove(0);
                player.getClientSession().sendPacketImmediately(levelChunkPacket);
            }
        }
    }


}
