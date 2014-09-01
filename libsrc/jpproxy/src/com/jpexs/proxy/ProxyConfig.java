package com.jpexs.proxy;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ProxyConfig {

    public static boolean dontLogFilters = false;

    public static String appVersion = "1.1";
    public static String appName = "JPProxy";
    public static int port = 55555;
    public static String bindAddress;

    public static int readTimeout = 50000;
    public static boolean proxyKeepAlive = false;
    public static boolean dontUncompress = false;

    public static final int HTTPS_PASSTHRU = 0;
    public static final int HTTPS_FILTER = 1;
    public static final int HTTPS_FILTERLIST = 2;

    public static int httpsMode = HTTPS_PASSTHRU;

    public static List<String> enabledHttpsServers = new ArrayList<String>();

    public static boolean useHTTPSProxy = false;
    public static String httpsProxyHost = "";
    public static int httpsProxyPort = 0;

    public static boolean useHTTPProxy = false;
    public static String httpProxyHost = "";
    public static int httpProxyPort = 0;

    public static String httpsKeyStoreFile = null;
    public static String httpsKeyStorePass = null;
    public static String httpsKeyPass = null;

}
