package net.catrainbow.feature.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ModuleManager {

    public static HashMap<String, Boolean> modules = new HashMap<>();
    public static HashMap<String, BaseModule> modulesHandler = new HashMap<>();

    static {
        modules.put("FastJoin", false);
        modules.put("AntiBan", false);

        modulesHandler.put("base", new BaseModule());
    }

    public static String getModuleList() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : modules.keySet()) {
            Random random = new Random();
            stringBuilder.append("\n                                                              §").append(random.nextInt(9)).append(str);
        }
        return stringBuilder.toString();
    }

}
