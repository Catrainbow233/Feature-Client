package net.catrainbow.feature.module;

import com.nukkitx.protocol.bedrock.BedrockPacket;

public class BaseModule {

    public String name;

    public void handlePacket(BedrockPacket packet) {

    }

    public BedrockPacket onDataPacket(BedrockPacket packet) {
        return packet;
    }

}
