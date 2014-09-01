package com.jpexs.proxy;

import java.io.*;
import java.net.Socket;

public class Client extends Connection {

    @Override
    public void promoteToServerSSL() {
        super.promoteToServerSSL();
        in = new BufferedInputStream(in);
        out = new BufferedOutputStream(out);
    }

    /**
     * Create a Client from a Socket.
     */
    Client(Socket s) throws IOException {
        super(s);
        in = new BufferedInputStream(in);
        //out = new DebugOutputStream(new BufferedOutputStream(out));
        out = new BufferedOutputStream(out);
    }

    /**
     * Read a Request.
     *
     * @returns a Request.
     * @see Request
     */
    Request read() throws IOException {
        Request request = new Request(this);
        request.read(getInputStream());
        return request;
    }

    /**
     * Write a Reply
     *
     * @see Reply
     */
    void write(Reply reply) throws IOException {
        reply.write(getOutputStream());
    }
}
