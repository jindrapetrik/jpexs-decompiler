package com.jpexs.proxy;

/**
 *
 * @author JPEXS
 */
public class ProxyConfig {

   public static boolean dontLogFilters=false;
   public static long maxLogFileSize=1024*5;
   public static int maxLogFileHistory=500;

   public static String appVersion="1.1";
   public static String appName="JPProxy";
   public static int port=55555;
   public static String bindAddress;

   public static int readTimeout=50000;
   public static boolean proxyKeepAlive=false;
   public static boolean dontUncompress=false;

   public static boolean useHTTPSProxy=false;
   public static String httpsProxyHost="";
   public static int httpsProxyPort=0;

   public static boolean useHTTPProxy=false;
   public static String httpProxyHost="";
   public static int httpProxyPort=0;

}
