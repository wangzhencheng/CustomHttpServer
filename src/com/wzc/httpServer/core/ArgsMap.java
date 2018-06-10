package com.wzc.httpServer.core;

import java.util.HashMap;

/**
 * 从main函数的args解析成map
 */
public class ArgsMap extends HashMap<String, String> {
    public ArgsMap(String[] args) {
        if (args != null && args.length > 0) {
            for (String arg : args) {
                if (arg != null && arg.length() > 0 && arg.indexOf("=") > -1 && arg.startsWith("--")) {
                    arg = arg.substring(2);
                    String[] kvs = arg.split("=");
                    this.put(kvs[0], kvs[1]);
                }
            }
        }
    }


    public String get(Object key, String defaultVal) {
        return get(key) == null ? defaultVal : get(key);
    }

    public int getInt(Object key, int defaultVal) {
        String val = get(key);
        return val == null ? defaultVal : Integer.parseInt(val);
    }

}
