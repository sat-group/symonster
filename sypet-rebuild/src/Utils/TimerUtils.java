package Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Provide utilities for timing multiple items together.
 */
public class TimerUtils {
    private static Map<String,Long> timers = new HashMap<>();

    public static void addTimer(String name){
        timers.put(name, System.currentTimeMillis());
    }
    public static long popTimer(String name){
        long ret = System.currentTimeMillis() - timers.get("name");
        timers.remove(name);
        return ret;
    }
}
