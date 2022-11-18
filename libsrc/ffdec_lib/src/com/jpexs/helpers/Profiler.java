package com.jpexs.helpers;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class Profiler {

    private static final boolean PROFILER_ENABLED = false;

    private Map<String, Long> times = new HashMap<>();
    private static Map<String, Long> maxTimes = new HashMap<>();
    private Map<String, Long> startTimes = new HashMap<>();

    static {
        if (PROFILER_ENABLED) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    System.out.println("PROFILER MAX TIMES:");
                    for (String action : maxTimes.keySet()) {
                        System.out.println("" + action + ": " + maxTimes.get(action) + " ms");
                    }
                }
            });
        }
    }

    public void start(String action) {
        startTimes.put(action, System.currentTimeMillis());
    }

    public void stop(String action) {
        if (!startTimes.containsKey(action)) {
            throw new RuntimeException("action " + action + " not started, cannot be stopped");
        }
        long time = System.currentTimeMillis() - startTimes.get(action);
        if (!maxTimes.containsKey(action)) {
            maxTimes.put(action, time);
        } else if (time > maxTimes.get(action)) {
            maxTimes.put(action, time);
        }
        times.put(action, time);
        startTimes.remove(action);
    }
}
