package net.catrainbow.feature;

public class Command {

    public static void dispatchCommand(String command, Player player) {
        switch (command) {
            case "help":
                player.sendMessage("Command Help List:");
                break;
        }
    }

}
