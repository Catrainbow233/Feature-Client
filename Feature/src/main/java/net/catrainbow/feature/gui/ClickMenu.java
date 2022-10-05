package net.catrainbow.feature.gui;

import net.catrainbow.feature.FeatureClient;
import net.catrainbow.feature.FeatureServer;
import net.catrainbow.feature.Player;
import net.catrainbow.feature.gui.image.ImageClass;
import net.catrainbow.feature.network.XBoxLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ClickMenu extends JFrame implements ActionListener {

    private ClickType clickType;
    private ArrayList<JComponent> mainList = new ArrayList<>();

    public ClickMenu() {
        this.clickType = ClickType.Main;
        this.setTitle("Feature Client");
        this.setResizable(false);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setBounds(300, 200, 650, 420);
        this.setLocation(500, 300);
        JPanel jPanel = new JPanel();
        jPanel.setBackground(Color.decode("#2b313b"));
        jPanel.setLayout(null);

        JPanel tool = new JPanel();
        tool.setBackground(Color.decode("#1F2630"));
        tool.setBounds(0, 0, 650, 40);
        tool.setLayout(null);

        JButton main = getQuickButton("Main", 10, 5, 45, 28);
        tool.add(main);
        JButton movement = getQuickButton("Move", 65, 5, 45, 28);
        tool.add(movement);

        // 登录Xbox
        JTextField jTextField = new JTextField(XBoxLogger.user);
        jTextField.setEditable(true);
        jTextField.setFont(Font.getFont("宋体"));
        jTextField.setBorder(null);
        jTextField.setBounds(25, 30, 100, 25);
        jTextField.setForeground(Color.WHITE);
        jTextField.setSelectionColor(Color.WHITE);
        jTextField.setBackground(Color.decode("#1F2630"));
        jTextField.setVisible(true);
        jTextField.setActionCommand("user");
        jTextField.addActionListener(this);

        JPasswordField jPasswordField = new JPasswordField(XBoxLogger.password);
        jPasswordField.setEditable(true);
        jPasswordField.setFont(Font.getFont("宋体"));
        jPasswordField.setBorder(null);
        jPasswordField.setBounds(25, 60, 100, 25);
        jPasswordField.setForeground(Color.WHITE);
        jPasswordField.setSelectionColor(Color.WHITE);
        jPasswordField.setBackground(Color.decode("#1F2630"));
        jPasswordField.setVisible(true);
        jPasswordField.setActionCommand("password");
        jPasswordField.addActionListener(this);

        JLabel jLabel = new JLabel("登录到你的XBox账户");
        jLabel.setForeground(Color.WHITE);
        jLabel.setFont(new Font("宋体", Font.PLAIN, 10));
        jLabel.setBounds(20, 10, 100, 20);

        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(Color.decode("#1F2630"));
        loginPanel.setBounds(20, 100, 150, 200);
        loginPanel.setLayout(null);

        JButton loginButton = getQuickButton("登录", 25, 90, 100, 25);
        loginButton.addActionListener(this);

        loginPanel.add(jLabel);
        loginPanel.add(jTextField);
        loginPanel.add(jPasswordField);
        loginPanel.add(loginButton);
        mainList.add(loginPanel);

        // 连接多人游戏

        JTextField addressInput = new JTextField(XBoxLogger.ip);
        addressInput.setEditable(true);
        addressInput.setFont(Font.getFont("宋体"));
        addressInput.setBorder(null);
        addressInput.setBounds(25, 30, 100, 25);
        addressInput.setForeground(Color.WHITE);
        addressInput.setSelectionColor(Color.WHITE);
        addressInput.setBackground(Color.decode("#1F2630"));
        addressInput.setVisible(true);
        addressInput.setActionCommand("ip");
        addressInput.addActionListener(this);

        JTextField portInput = new JTextField(XBoxLogger.port);
        portInput.setEditable(true);
        portInput.setFont(Font.getFont("宋体"));
        portInput.setBorder(null);
        portInput.setBounds(25, 60, 100, 25);
        portInput.setForeground(Color.WHITE);
        portInput.setSelectionColor(Color.WHITE);
        portInput.setBackground(Color.decode("#1F2630"));
        portInput.setVisible(true);
        portInput.setActionCommand("port");
        portInput.addActionListener(this);

        JLabel loginToServerLabel = new JLabel("连接到多人服务器");
        loginToServerLabel.setForeground(Color.WHITE);
        loginToServerLabel.setFont(new Font("宋体", Font.PLAIN, 10));
        loginToServerLabel.setBounds(20, 10, 100, 20);

        JPanel multiplayerPanel = new JPanel();
        multiplayerPanel.setBackground(Color.decode("#1f2630"));
        multiplayerPanel.setBounds(200, 100, 150, 200);
        multiplayerPanel.setLayout(null);
        mainList.add(multiplayerPanel);

        JButton connectButton = getQuickButton("连接", 25, 90, 100, 25);
        connectButton.addActionListener(this);

        multiplayerPanel.add(addressInput);
        multiplayerPanel.add(portInput);
        multiplayerPanel.add(loginToServerLabel);
        multiplayerPanel.add(connectButton);

        main.addActionListener(this);
        movement.addActionListener(this);

        jPanel.add(loginPanel);
        jPanel.add(multiplayerPanel);
        jPanel.add(tool);
        this.add(jPanel);
        this.setFont(Font.getFont("宋体"));
        this.getContentPane().setBackground(Color.decode("#2b313b"));
        this.setVisible(true);
    }

    public JButton getQuickButton(String text, int x, int y, int width, int height) {
        JButton main = new JButton(text);
        main.setBorderPainted(false);
        main.setBackground(Color.decode("#046DE1"));
        main.setForeground(Color.WHITE);
        main.setHideActionText(true);
        main.setActionCommand(text);
        main.setFont(new Font("宋体", Font.PLAIN, 10));
        main.setFocusPainted(false);
        main.setMargin(new Insets(0, 0, 0, 0));
        main.setBounds(x, y, width, height);
        return main;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(clickType.toString()))
            return;
        switch (e.getActionCommand()) {
            case "Main":
                hideAll();
                for (JComponent component : mainList) {
                    component.setVisible(true);
                }
                clickType = ClickType.Main;
                break;
            case "Move":
                hideAll();
                clickType = ClickType.MOVEMENT;
                break;
            case "连接":
                for (Player player : FeatureServer.players) {
                    player.connectToServer(XBoxLogger.ip, Integer.valueOf(XBoxLogger.port));
                }
                break;
            case "登录":
                for (Player player : FeatureServer.players) {
                    if (player.loginToXbox()) {
                        player.sendMessage("§a登录Xbox账户成功!");
                    }else player.sendMessage("§c无法登录到你的XBox账户,请检查输入!");
                }
                break;
            case "user":
                JTextField textField = (JTextField) e.getSource();
                XBoxLogger.user = textField.getText();
                XBoxLogger.save();
                break;
            case "password":
                JPasswordField password = (JPasswordField) e.getSource();
                XBoxLogger.password = new String(password.getPassword());
                XBoxLogger.save();
                break;
            case "ip":
                JTextField ip = (JTextField) e.getSource();
                XBoxLogger.ip = ip.getText();
                XBoxLogger.save();
                break;
            case "port":
                JTextField port = (JTextField) e.getSource();
                XBoxLogger.port = port.getText();
                XBoxLogger.save();
                break;
        }
    }

    public void hideAll() {
        for (JComponent component : mainList) {
            component.setVisible(false);
        }
    }

    public static void main(String[] args) {
        new ClickMenu();
    }

}
