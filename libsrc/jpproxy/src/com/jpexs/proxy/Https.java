package com.jpexs.proxy;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author JPEXS
 */
public class Https extends Http {
    /* XXX - more than 1 should work now. */

    static final int MAX_PENDING_REQUESTS = 1;

    static Hashtable cache = new Hashtable(33);
    private static Object httpLock = new Object();

    public Https(String host, int port) throws IOException {
        this(host, port, false);
    }

    public Https(String host, int port, boolean isProxy) throws IOException {
        super(host, port, isProxy);
    }

    private static String cacheKey(String host, int port) {
        return host.toLowerCase() + ":" + port;
    }

    private static Vector cacheLookup(String host, int port) {
        Vector v = (Vector) cache.get(cacheKey(host, port));
        return v;
    }

    private static boolean cacheContains(Https http) {
        Vector v = (Vector) cache.get(cacheKey(http.host, http.port));
        return v != null ? v.contains(http) : false;
    }

    private static void cacheInsert(String host, int port, Https http) {
        String key = cacheKey(host, port);
        Vector v = (Vector) cache.get(key);
        if (v == null) {
            v = new Vector();
        }
        v.addElement(http);
        cache.put(key, v);
    }

    private static void cacheRemove(String host, int port, Https http) {
        Vector v = (Vector) cache.get(cacheKey(host, port));
        if (v != null) {
            v.removeElement(http);
            if (v.isEmpty()) {
                cache.remove(cacheKey(host, port));
            }
        }
    }

    private static void cacheClean() {
        long now = System.currentTimeMillis();
        Enumeration e = cache.keys();
        while (e.hasMoreElements()) {
            Vector v = (Vector) cache.get(e.nextElement());
            for (int i = 0; i < v.size(); i++) {
                Https http = (Https) v.elementAt(i);
                if (http.idle > 0 && now - http.idle > 30000) /* 30 seconds */ {
                    http.persistent = false;
                    http.close();
                }
            }
        }
    }

    static Https open(String host, int port, boolean isProxy)
            throws IOException {
        Https http = null;

        synchronized (httpLock) {
            Vector v = cacheLookup(host, port);
            if (v != null) {
                for (int i = 0; i < v.size(); i++) {
                    Https pick = (Https) v.elementAt(i);

                    /* find an http connection that isn't busy */
                    if (pick.isPersistent() && !pick.isBusy()) {
                        http = pick;
                        break;
                    }
                }

                if (http != null) {
                    http.idle = 0;
                }
            }
        }

        if (http == null) {
            http = new Https(host, port, isProxy);
            cacheInsert(host, port, http);
        }

        return http;
    }

    static Https open(String host, int port) throws IOException {
        return open(host, port, false);
    }

    static Enumeration enumerate() {
        Vector list = new Vector();
        Enumeration e = cache.keys();
        while (e.hasMoreElements()) {
            Vector v = (Vector) cache.get(e.nextElement());
            for (int i = 0; i < v.size(); i++) {
                list.addElement(v.elementAt(i));
            }
        }
        return list.elements();
    }

    static synchronized void clean() {
        cacheClean();
    }
}
