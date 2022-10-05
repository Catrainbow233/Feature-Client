package net.catrainbow.feature.network;

import com.nukkitx.protocol.bedrock.BedrockPong;
import com.nukkitx.protocol.bedrock.BedrockServerEventHandler;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.packet.DisconnectPacket;
import com.nukkitx.protocol.bedrock.v544.Bedrock_v544;
import net.catrainbow.feature.FeatureServer;
import net.catrainbow.feature.Player;

import java.net.InetSocketAddress;

public class FeaturePacketHandler implements BedrockServerEventHandler {

    private FeatureServer proxy;

    public FeaturePacketHandler(FeatureServer proxyServer) {
        proxy = proxyServer;
    }

    @Override
    public boolean onConnectionRequest(InetSocketAddress address) {
        return true;
    }

    @Override
    public BedrockPong onQuery(InetSocketAddress address) {
        BedrockPong pong = new BedrockPong();
        pong.setEdition("MCPE");
        pong.setMotd("Feature Client");
        pong.setPlayerCount(FeatureServer.players.size());
        pong.setMaximumPlayerCount(1);
        pong.setGameType("Survival");
        pong.setVersion("2.0.0");
        pong.setProtocolVersion(Bedrock_v544.V544_CODEC.getProtocolVersion());
        pong.setIpv4Port(19132);
        pong.setIpv6Port(19132);
        pong.setNintendoLimited(false);
        pong.setSubMotd("Feature Client");
        return pong;
    }

    public DisconnectPacket createDisconnect(String message) {
        DisconnectPacket dc = new DisconnectPacket();
        dc.setKickMessage(message);
        dc.setMessageSkipped(false);
        return dc;
    }

    @Override
    public void onSessionCreation(BedrockServerSession serverSession) {
        if (FeatureServer.players.size() > 10) {
            serverSession.sendPacketImmediately(createDisconnect("Proxy full!"));
            serverSession.disconnect();
            return;
        }
        Player player = new Player(serverSession, this, proxy);
        serverSession.addDisconnectHandler((reason) -> {
            if (player.getServerSession() != null) {
                player.getServerSession().disconnect();
                player.setConnectedToServer(false);
            }
            player.close();
        });
        serverSession.setBatchHandler(new PlayerBatchPacketHandler(serverSession, player));
    }

}
