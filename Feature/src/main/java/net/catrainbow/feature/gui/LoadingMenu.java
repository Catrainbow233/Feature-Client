package net.catrainbow.feature.gui;

import net.catrainbow.feature.FeatureServer;
import net.catrainbow.feature.bedrock.BedrockData;
import net.catrainbow.feature.network.XBoxLogger;
import net.catrainbow.feature.utils.Config;
import net.catrainbow.feature.utils.GameHook;

import javax.swing.*;
import java.awt.*;
import java.net.InetSocketAddress;

public class LoadingMenu extends JFrame {

    private final int maxLoading = 26;
    private int nowLoadingValue = 0;
    private JLabel statusLabel;
    private JProgressBar progressBar = new JProgressBar(0, 100);

    public LoadingMenu() {
        this.setTitle("Feature Client");
        this.setResizable(false);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setBounds(300, 200, 650, 420);
        this.setLocation(500, 300);
        JPanel jPanel = new JPanel();
        jPanel.setBackground(Color.darkGray);
        jPanel.setLayout(null);

        JLabel jLabel = new JLabel("Feature Client");
        jLabel.setForeground(Color.WHITE);
        jLabel.setBounds(200, 50, 300, 80);
        jLabel.setFont(new Font("宋体", Font.BOLD, 30));
        jPanel.add(jLabel);

        statusLabel = new JLabel("                          Loading Resources...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBounds(100, 270, 500, 20);
        statusLabel.setFont(new Font("宋体", Font.BOLD, 10));
        jPanel.add(statusLabel);

        progressBar.setBackground(Color.DARK_GRAY);
        progressBar.setForeground(Color.WHITE);
        progressBar.setString(this.nowLoadingValue + "/" + maxLoading);
        progressBar.setFont(Font.getFont("宋体"));
        progressBar.setStringPainted(true);
        progressBar.setBorderPainted(false);
        progressBar.setBounds(240, 300, 150, 10);
        jPanel.add(progressBar);


        this.add(jPanel);
        this.setFont(Font.getFont("宋体"));
        this.getContentPane().setBackground(Color.darkGray);
        this.setVisible(true);
        this.startInject();
    }

    public void startInject() {
        new Thread() {
            @Override
            public void run() {
                boolean work = true;
                while (nowLoadingValue <= maxLoading) {
                    if (!work) break;
                    Dimension dimension = progressBar.getSize();
                    Rectangle rectangle = new Rectangle(0, 0, dimension.width, dimension.height);
                    progressBar.setValue(nowLoadingValue / maxLoading * 100);
                    progressBar.setString(nowLoadingValue + "/" + maxLoading);
                    progressBar.paintImmediately(rectangle);
                    switch (nowLoadingValue) {
                        case 1:
                            BedrockData.loadItemEntries();
                            BedrockData.loadBiomeDefinitions();
                            BedrockData.loadEntityIdentifiers();
                            break;
                        case 4:
                            if (!GameHook.getProcess()) {
                                statusLabel.setText("     Failed to hook Minecraft! Please check if the game is running.");
                                work = false;
                            }
                            break;
                        case 5:
                            Config config = new Config(FeatureServer.PATH + "settings.gc", Config.PROPERTIES);
                            if (!config.exists("xbox-user")) {
                                config.set("xbox-user", "steve");
                                config.set("password", "steve");
                                config.set("target-server-ip", "mc.fapixel.com");
                                config.set("target-server-port", "19132");
                                config.save(false);
                            }
                            break;
                        case 8:
                            Config config2 = new Config(FeatureServer.PATH + "settings.gc", Config.PROPERTIES);
                            XBoxLogger.user = config2.getString("xbox-user");
                            XBoxLogger.password = config2.getString("password");
                            XBoxLogger.ip = config2.getString("target-server-ip");
                            XBoxLogger.port = config2.getString("target-server-port");
                            break;
                        case 10:
                            statusLabel.setText("                            Injecting...");
                            FeatureServer server = new FeatureServer(new InetSocketAddress("0.0.0.0", 19132));
                            server.start();
                            break;
                    }
                    nowLoadingValue++;
                    try {
                        Thread.sleep(100);
                    } catch (Exception ignored) {
                    }
                }
                if (work) {
                    new ClickMenu();
                    dispose();
                }
            }
        }.start();
    }

}
