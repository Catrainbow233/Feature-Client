package net.catrainbow.feature.network;

import net.catrainbow.feature.FeatureServer;
import net.catrainbow.feature.utils.Config;

public class XBoxLogger {

    public static String user;
    public static String password;

    public static String ip;

    public static String port;

    public static void save() {
        Config config = new Config(FeatureServer.PATH + "settings.gc", Config.PROPERTIES);
        config.set("xbox-user", user);
        config.set("password", password);
        config.set("target-server-ip", ip);
        config.set("target-server-port", port);
        config.save(false);
    }

}
