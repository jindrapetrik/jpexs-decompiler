package com.jpexs.proxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

abstract class HttpConnection extends Connection implements HttpRelay {

    HttpConnection(String host, int port) throws IOException {
        super(host, port);
    }

    HttpConnection(Socket s) throws IOException {
        super(s);
    }

    public void sendRequest(Request request)
            throws IOException, RetryRequestException {
        request.write(getOutputStream());
    }

    public Reply recvReply(Request request)
            throws IOException, RetryRequestException {
        Reply reply = new Reply(getInputStream());
        reply.read();
        return reply;
    }

    public void setInputStream(InputStream in) {
        super.setInputStream(in);
    }

    public void setOutputStream(OutputStream out) {
        super.setOutputStream(out);
    }

    public InputStream getInputStream() {
        return super.getInputStream();
    }

    public OutputStream getOutputStream() {
        return super.getOutputStream();
    }

    public void close() {
        super.close();
    }
}
