package net.catrainbow.feature;

import com.nukkitx.math.vector.Vector2f;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.nbt.NBTOutputStream;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtUtils;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.data.*;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlags;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import com.nukkitx.protocol.bedrock.packet.*;
import com.nukkitx.protocol.bedrock.v419.Bedrock_v419;
import com.nukkitx.protocol.bedrock.v431.Bedrock_v431;
import com.nukkitx.protocol.bedrock.v440.Bedrock_v440;
import com.nukkitx.protocol.bedrock.v448.Bedrock_v448;
import com.nukkitx.protocol.bedrock.v465.Bedrock_v465;
import com.nukkitx.protocol.bedrock.v471.Bedrock_v471;
import com.nukkitx.protocol.bedrock.v475.Bedrock_v475;
import com.nukkitx.protocol.bedrock.v486.Bedrock_v486;
import com.nukkitx.protocol.bedrock.v503.Bedrock_v503;
import com.nukkitx.protocol.bedrock.v527.Bedrock_v527;
import com.nukkitx.protocol.bedrock.v534.Bedrock_v534;
import com.nukkitx.protocol.bedrock.v544.Bedrock_v544;
import lombok.Getter;
import lombok.Setter;
import net.catrainbow.feature.bedrock.BedrockData;
import net.catrainbow.feature.module.ModuleManager;
import net.catrainbow.feature.network.FeaturePacketHandler;
import net.catrainbow.feature.network.PlayerBatchPacketHandler;
import net.catrainbow.feature.network.ProxyBetweenServerHandler;
import net.catrainbow.feature.network.XBoxLogger;
import net.catrainbow.feature.network.auth.LoginPacketGenerator;
import net.catrainbow.feature.network.auth.XboxLogin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Player {

    public static final String PROXY_PREFIX = "§b[Feature]§r ";

    @Getter
    private final FeaturePacketHandler serverHandler;

    @Getter
    @Setter
    private ArrayList<LevelChunkPacket> levelChunkPackets = new ArrayList<>();
    @Getter
    @Setter
    private PlayerBatchPacketHandler clientBatchHandler;

    @Getter
    private final FeatureServer proxyServer;

    @Getter
    @Setter
    private ProxyBetweenServerHandler serverBatchHandler;

    @Getter
    private final BedrockSession clientSession;

    @Getter
    private BedrockSession serverSession;

    @Getter
    @Setter
    private int playerId;

    @Getter
    @Setter
    private boolean initialized;

    @Getter
    @Setter
    private boolean connectedToServer;

    @Getter
    public String accessToken;

    @Getter
    @Setter
    private LoginPacket loginPacket;

    public ECPublicKey publicKey;
    public ECPrivateKey privateKey;

    public String username;

    public String xuid;

    public String UUID;

    @Getter
    @Setter
    private int playerIdServer;

    @Getter
    @Setter
    private ModuleManager moduleManager;

    @Getter
    @Setter
    private Queue<BedrockPacket> fakeLagQueuedPackets = new ArrayDeque<>();

    @Getter
    @Setter
    private boolean connectionProcess = false;

    @Getter
    @Setter
    private boolean loginProcess = false;

    @Setter
    @Getter
    private Vector3f position = Vector3f.ZERO;

    public Player(BedrockSession clientSession, FeaturePacketHandler serverHandler, FeatureServer proxy) {
        this.serverHandler = serverHandler;
        this.proxyServer = proxy;
        this.clientSession = clientSession;
        this.moduleManager = new ModuleManager();
        FeatureServer.players.add(this);
    }

    public void close() {
        FeatureServer.players.remove(this);
    }

    public void connectToServer(String address, Integer port) {
        if (isConnectedToServer()) {
            return;
        }
        if (connectionProcess) {
            sendMessage("你的客户都正在连接到目标服务器...");
            return;
        }
        connectionProcess = true;
        InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", 0);
        BedrockClient future = new BedrockClient(bindAddress);
        future.setRakNetVersion(clientSession.getPacketCodec().getRaknetProtocolVersion());
        future.bind().thenApply((client) -> client).thenAccept(client -> future.connect(new InetSocketAddress(address, port)).whenComplete((session, throwable) -> {
            if (throwable != null) {
                connectionProcess = false;
                sendMessage("无法连接到" + address + ":" + port + "!");
                return;
            }
            sendMessage("Feature正在建立你和目标服务器的连接... [1/3]");
            try {
                session.sendPacket(LoginPacketGenerator.create(this, address, port));
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage("无法连接到目标服务器!");
                connectionProcess = false;
                session.disconnect();
                return;
            }
            sendMessage("你的协议版本: " + loginPacket.getProtocolVersion());
            switch (loginPacket.getProtocolVersion()) {
                case 448:
                    session.setPacketCodec(Bedrock_v448.V448_CODEC);
                    break;
                case 465:
                    session.setPacketCodec(Bedrock_v465.V465_CODEC);
                    break;
                case 471:
                    session.setPacketCodec(Bedrock_v471.V471_CODEC);
                    break;
                case 475:
                    session.setPacketCodec(Bedrock_v475.V475_CODEC);
                    break;
                case 486:
                    session.setPacketCodec(Bedrock_v486.V486_CODEC);
                    break;
                case 503:
                    session.setPacketCodec(Bedrock_v503.V503_CODEC);
                    break;
                case 527:
                    session.setPacketCodec(Bedrock_v527.V527_CODEC);
                    break;
                case 534:
                    session.setPacketCodec(Bedrock_v534.V534_CODEC);
                    break;
                case 544:
                    session.setPacketCodec(Bedrock_v544.V544_CODEC);
                    break;
                default:
                    session.setPacketCodec(Bedrock_v486.V486_CODEC);
                    break;
            }
            sendMessage("数据包校验和解码中... [2/3]");
            session.setPacketCodec(Bedrock_v431.V431_CODEC);
            session.addDisconnectHandler((reason) -> {
                connectionProcess = false;
                if (!clientSession.isClosed()) {
                    sendMessage("§c无法连接到目标服务器! 原因:" + reason.name());
                }
                this.disconnectedFromServer();
            });
            sendMessage("发送登录握手包中... [3/3]");
            session.setBatchHandler(new ProxyBetweenServerHandler(session, this));
            serverSession = session;
        })).whenComplete((ignore, error) -> {
            if (error != null) {
                error.printStackTrace();
            }
        });
    }

    public boolean loginToXbox() {
        if (accessToken != null) {
            return true;
        }
        if (loginProcess) {
            return true;
        }
        loginProcess = true;
        try {
            accessToken = XboxLogin.getAccessToken(XBoxLogger.user, XBoxLogger.password);
        } catch (Exception e) {
            loginProcess = false;
            return true;
        }
        loginProcess = false;
        return true;

    }

    public void sendTip(String msg) {
        TextPacket textPacket = new TextPacket();
        textPacket.setParameters(new ArrayList<>());
        textPacket.setXuid("");
        textPacket.setSourceName("");
        textPacket.setMessage(msg);
        textPacket.setPlatformChatId("");
        textPacket.setType(TextPacket.Type.TIP);
        textPacket.setNeedsTranslation(false);
        clientSession.sendPacket(textPacket);
    }

    public void disconnectedFromServer() {
        this.setServerBatchHandler(null);
        this.setConnectedToServer(false);
    }

    public void onJoinProxy() {
        sendMessage("欢迎使用 Feature Client!");
        sendMessage("请在客户都中登录你的XBox账户! 以便进入更多服务器进行游玩.");
        setPlayerFlag(EntityFlag.BREATHING, true);
    }

    public void sendMove(Vector3f vector3f, MovePlayerPacket.Mode mode) {
        MovePlayerPacket packet = new MovePlayerPacket();
        packet.setRuntimeEntityId(playerId);
        packet.setTeleportationCause(MovePlayerPacket.TeleportationCause.UNKNOWN);
        packet.setRotation(vector3f);
        packet.setPosition(vector3f);
        packet.setMode(mode);
        packet.setTick(0);
        packet.setOnGround(false);
        packet.setEntityType(0);
        packet.setRidingRuntimeEntityId(0);
        clientSession.sendPacket(packet);
    }

    public boolean handleChat(String chat) {
        if (chat.startsWith("/.")) {
            String[] args = chat.substring(2).split(" ");
            String cmd = args[0];
            switch (cmd) {
                case "help":
                    sendMessage("/.help 获得帮助"
                    );
                    return true;
            }
            sendMessage("未知的命令!");
            return true;
        }
        return false;
    }

    public void sendMessage(String str) {
        TextPacket packet = new TextPacket();
        packet.setMessage(PROXY_PREFIX + str);
        packet.setType(TextPacket.Type.RAW);
        packet.setPlatformChatId("");
        packet.setNeedsTranslation(false);
        packet.setXuid("");
        packet.setParameters(new LinkedList<>());
        packet.setSourceName("");
        clientSession.sendPacket(packet);
    }

    public void sendEmptyChunk(BedrockSession session) {
        Vector3i position = Vector3i.from(99999, 60, 99999);
        int radius = 0;
        int chunkX = position.getX() >> 4;
        int chunkZ = position.getZ() >> 4;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                LevelChunkPacket data = new LevelChunkPacket();
                data.setChunkX(chunkX + x);
                data.setChunkZ(chunkZ + z);
                data.setSubChunksLength(0);
                data.setData(getEmptyChunkData(session));
                data.setCachingEnabled(false);
                session.sendPacket(data);
            }
        }
    }

    public void sendStartGame(BedrockSession session) {
        int entityId = ThreadLocalRandom.current().nextInt(10000, 15000);
        this.setPlayerId(entityId);
        StartGamePacket startGamePacket = new StartGamePacket();
        startGamePacket.setUniqueEntityId(entityId);
        startGamePacket.setRuntimeEntityId(entityId);
        startGamePacket.setPlayerGameType(GameType.SURVIVAL);
        startGamePacket.setPlayerPosition(Vector3f.from(99999, 69, 99999));
        startGamePacket.setRotation(Vector2f.from(1, 1));

        startGamePacket.setSeed(-1);
        startGamePacket.setDimensionId(0);
        startGamePacket.setGeneratorId(1);
        startGamePacket.setLevelGameType(GameType.SURVIVAL);
        startGamePacket.setDifficulty(1);
        startGamePacket.setDefaultSpawn(Vector3i.ZERO);
        startGamePacket.setAchievementsDisabled(true);
        startGamePacket.setCurrentTick(-1);
        startGamePacket.setEduEditionOffers(0);
        startGamePacket.setEduFeaturesEnabled(false);
        startGamePacket.setRainLevel(0);
        startGamePacket.setLightningLevel(0);
        startGamePacket.setMultiplayerGame(true);
        startGamePacket.setBroadcastingToLan(true);
        startGamePacket.setPlatformBroadcastMode(GamePublishSetting.PUBLIC);
        startGamePacket.setXblBroadcastMode(GamePublishSetting.PUBLIC);
        startGamePacket.setCommandsEnabled(true);
        startGamePacket.setTexturePacksRequired(false);
        startGamePacket.setBonusChestEnabled(false);
        startGamePacket.setStartingWithMap(false);
        startGamePacket.setTrustingPlayers(false);
        startGamePacket.setDefaultPlayerPermission(PlayerPermission.MEMBER);
        startGamePacket.setServerChunkTickRange(4);
        startGamePacket.setBehaviorPackLocked(false);
        startGamePacket.setResourcePackLocked(false);
        startGamePacket.setFromLockedWorldTemplate(false);
        startGamePacket.setUsingMsaGamertagsOnly(false);
        startGamePacket.setFromWorldTemplate(false);
        startGamePacket.setWorldTemplateOptionLocked(false);

        String serverName = "Feature Client";
        startGamePacket.setLevelId(serverName);
        startGamePacket.setLevelName(serverName);

        startGamePacket.setPremiumWorldTemplateId("00000000-0000-0000-0000-000000000000");

        startGamePacket.setEnchantmentSeed(0);
        startGamePacket.setMultiplayerCorrelationId("");
        startGamePacket.setItemEntries(BedrockData.ITEM_ENTRIES);
        startGamePacket.setVanillaVersion("*");
        startGamePacket.setInventoriesServerAuthoritative(false);
        startGamePacket.setServerEngine("");

        SyncedPlayerMovementSettings settings = new SyncedPlayerMovementSettings();
        settings.setMovementMode(AuthoritativeMovementMode.CLIENT);
        settings.setRewindHistorySize(0);
        settings.setServerAuthoritativeBlockBreaking(false);
        startGamePacket.setPlayerMovementSettings(settings);

        session.sendPacket(startGamePacket);
    }

    public void spawn(BedrockSession session) {
        BiomeDefinitionListPacket biomeDefinitionListPacket = new BiomeDefinitionListPacket();
        biomeDefinitionListPacket.setDefinitions(BedrockData.BIOMES);
        session.sendPacket(biomeDefinitionListPacket);

        AvailableEntityIdentifiersPacket entityPacket = new AvailableEntityIdentifiersPacket();
        entityPacket.setIdentifiers(BedrockData.ENTITY_IDENTIFIERS);
        session.sendPacket(entityPacket);

        CreativeContentPacket packet = new CreativeContentPacket();
        packet.setContents(new ItemData[0]);
        session.sendPacket(packet);

        PlayStatusPacket playStatusPacket = new PlayStatusPacket();
        playStatusPacket.setStatus(PlayStatusPacket.Status.PLAYER_SPAWN);
        session.sendPacket(playStatusPacket);

        UpdateAttributesPacket attributesPacket = new UpdateAttributesPacket();
        attributesPacket.setRuntimeEntityId(playerId);
        // Default move speed
        // Bedrock clients move very fast by default until they get an attribute packet correcting the speed
        attributesPacket.setAttributes(Collections.singletonList(
                new AttributeData("minecraft:movement", 0.0f, 1024f, 0.1f, 0.1f)));
        session.sendPacket(attributesPacket);

        GameRulesChangedPacket gamerulePacket = new GameRulesChangedPacket();
        gamerulePacket.getGameRules().add(new GameRuleData<>("naturalregeneration", false));
        session.sendPacket(gamerulePacket);
    }

    public byte[] getEmptyChunkData(BedrockSession session) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(new byte[258]); // Biomes + Border Size + Extra Data Size

            try (NBTOutputStream stream = NbtUtils.createNetworkWriter(outputStream)) {
                stream.writeTag(NbtMap.EMPTY);
            }

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new AssertionError("Unable to generate empty level chunk data");
        }
    }

    public void setPlayerFlag(EntityFlag flags, boolean value) {
        SetEntityDataPacket packet = new SetEntityDataPacket();
        packet.setRuntimeEntityId(playerId);
        packet.setTick(0);
        EntityFlags flag = new EntityFlags();
        flag.setFlag(flags, value);
        packet.getMetadata().putFlags(flag);
        clientSession.sendPacket(packet);
    }
}
