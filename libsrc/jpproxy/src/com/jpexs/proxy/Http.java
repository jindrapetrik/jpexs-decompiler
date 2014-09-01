package com.jpexs.proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

class Http extends HttpConnection {

    /* XXX - more than 1 should work now. */
    static final int MAX_PENDING_REQUESTS = 1;

    static Hashtable cache = new Hashtable(33);
    private static Object httpLock = new Object();

    String host;
    int port;
    boolean proxy = false;
    boolean persistent = false;
    boolean closed = false;
    long idle = 0;
    Vector queue = new Vector();

    public Http(String host, int port) throws IOException {
        this(host, port, false);
    }

    public Http(String host, int port, boolean isProxy) throws IOException {
        super(host, port);
        this.host = host;
        this.port = port;
        this.proxy = isProxy;
    }

    public Http(String host, int port, boolean isProxy, Socket sock) throws IOException {
        super(sock);
        this.host = host;
        this.port = port;
        this.proxy = isProxy;
    }

    public synchronized void sendRequest(Request request)
            throws IOException, RetryRequestException {
        queue.addElement(request);

        try {
            send(request);
        } catch (IOException e) {
            if (persistent) {
                persistent = false;
                throw new RetryRequestException();
            }
            throw e;
        }
    }

    public synchronized Reply recvReply(Request request)
            throws IOException, RetryRequestException {
        while (queue.firstElement() != request) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        if (closed) {
            throw new RetryRequestException();
        }

        try {
            return recv();
        } catch (IOException e) {
            if (persistent) {
                persistent = false;
                throw new RetryRequestException();
            }
            throw e;
        }
    }

    public void reallyClose() {
        persistent = false;
        close();
    }

    public synchronized void close() {
        if (persistent) {
            idle = System.currentTimeMillis();
        } else {
            cacheRemove(host, port, this);
            super.close();
            closed = true;
        }

        if (queue.size() > 0) {
            queue.removeElementAt(0);

            notify();
        }
    }

    private void send(Request request) throws IOException {

        /* Prepare HTTP/1.1 request */
        request.removeHeaderField("Proxy-Connection");

        if (!proxy) {
            if (request.containsHeaderField("Connection") && (request.getHeaderField("Connection").toLowerCase().equals("keep-alive"))) {

            } else {
                request.setHeaderField("Connection", "open");
            }
            if (!request.containsHeaderField("Host")) {
                request.setHeaderField("Host", request.getHost());
            }
        }

        if (proxy) {
            request.write(getOutputStream());
        } else {
            String oldStatusLine = request.statusLine;
            StringBuffer head = new StringBuffer();
            head.append(request.getCommand());
            head.append(" ");
            head.append(request.getPath());
            head.append(" ");
            head.append("HTTP/1.0");
            request.statusLine = head.toString();

            request.write(getOutputStream());

            /* flush? */
            request.statusLine = oldStatusLine;
        }
    }

    private Reply recv() throws IOException {
        Reply reply = new Reply(getInputStream());
        reply.read();

        String conn = reply.getHeaderField("Connection");

        if (reply.containsHeaderField("Connection")
                && reply.getHeaderField("Connection").equals("close")) {
            persistent = false;
        } else if (reply.getProtocol().equals("HTTP/1.1")) {
            persistent = true;
        } else {
            persistent = false;
        }

        /* Received HTTP/1.1 "Continue".  Read another Reply. */
        if (reply.getStatusCode() == 100) {
            reply = recv();
        }

        return reply;
    }

    protected boolean isBusy() {
        return queue.size() >= MAX_PENDING_REQUESTS;
    }

    protected boolean isPersistent() {
        return persistent;
    }

    private static String cacheKey(String host, int port) {
        return host.toLowerCase() + ":" + port;
    }

    private static Vector cacheLookup(String host, int port) {
        Vector v = (Vector) cache.get(cacheKey(host, port));
        return v;
    }

    private static boolean cacheContains(Http http) {
        Vector v = (Vector) cache.get(cacheKey(http.host, http.port));
        return v != null ? v.contains(http) : false;
    }

    private static void cacheInsert(String host, int port, Http http) {
        String key = cacheKey(host, port);
        Vector v = (Vector) cache.get(key);
        if (v == null) {
            v = new Vector();
        }
        v.addElement(http);
        cache.put(key, v);
    }

    private static void cacheRemove(String host, int port, Http http) {
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
                Http http = (Http) v.elementAt(i);
                if (http.idle > 0 && now - http.idle > 30000) /* 30 seconds */ {
                    http.persistent = false;
                    http.close();
                }
            }
        }
    }

    static Http open(String host, int port, boolean isProxy)
            throws IOException {
        Http http = null;

        synchronized (httpLock) {
            Vector v = cacheLookup(host, port);
            if (v != null) {
                for (int i = 0; i < v.size(); i++) {
                    Http pick = (Http) v.elementAt(i);

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
            http = new Http(host, port, isProxy);
            cacheInsert(host, port, http);
        }

        return http;
    }

    static Http open(String host, int port) throws IOException {
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

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("SERVER ");
        buf.append(super.toString());
        if (isPersistent()) {
            buf.append(" - ");
            if (queue.size() > 0) {
                buf.append(queue.size());
                buf.append(" pending");
            } else {
                buf.append("idle " + ((System.currentTimeMillis() - idle) / 1000.0) + " sec");
            }
        }
        return buf.toString();
    }
}
