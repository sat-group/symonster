package edu.cmu.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Provide utilities for timing multiple items together.
 */
public class TimerUtils {
    private static Map<String,Long> timers = new HashMap<>();
    private static Map<String,Long> cumulativeTimers = new HashMap<>();

    public static synchronized void startTimer(String name){
        timers.put(name, System.currentTimeMillis());
    }
    public static synchronized long stopTimer(String name){
        long ret = System.currentTimeMillis() - timers.get(name);
        timers.remove(name);
        //Update cumulative timer
        if (cumulativeTimers.containsKey(name)){
            cumulativeTimers.put(name,cumulativeTimers.get(name) + ret);
        }
        else{
            cumulativeTimers.put(name,ret);
        }
        return ret;
    }
    public static synchronized double getCumulativeTime(String name){
        return cumulativeTimers.get(name)/1000.0;
    }
}
