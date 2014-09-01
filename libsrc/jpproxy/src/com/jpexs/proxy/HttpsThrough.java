package com.jpexs.proxy;

import java.io.IOException;

class HttpsThrough extends HttpConnection {

    boolean proxy = false;

    HttpsThrough(String host, int port) throws IOException {
        super(host, port);
    }

    HttpsThrough(String host, int port, boolean isProxy) throws IOException {
        this(host, port);
        proxy = isProxy;
    }

    public void sendRequest(Request request)
            throws IOException, RetryRequestException {
        if (proxy) {
            super.sendRequest(request);
        } else {
            /* nothing */
        }
    }

    public Reply recvReply(Request request)
            throws java.io.IOException, RetryRequestException {
        Reply reply = new Reply(getInputStream());
        if (proxy) {
            reply.read();
        } else {
            reply.statusLine = "HTTP/1.0 200 Connection established";
            reply.setHeaderField("Proxy-agent", ProxyConfig.appName);
        }

        return reply;
    }
}
