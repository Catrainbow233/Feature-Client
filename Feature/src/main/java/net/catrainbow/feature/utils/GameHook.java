package net.catrainbow.feature.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class GameHook {

    public static boolean getProcess() {
        boolean flag = false;
        try {
            Process p = Runtime.getRuntime().exec("cmd /c tasklist ");
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            InputStream os = p.getInputStream();
            byte[] b = new byte[256];
            while (os.read(b) > 0)
                byteArray.write(b);
            String s = byteArray.toString();
            if (s.contains("Minecraft")) {
                flag = true;
            }
        } catch (java.io.IOException ignored) {
        }
        return flag;
    }

}
