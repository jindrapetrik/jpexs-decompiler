package com.jpexs.proxy;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Client which reads a Request and writes a Reply.
 *
 * @author Mark Boyns
 */
public class Client extends Connection {
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
     */
    Request read() throws IOException {
        Request request = new Request(this);
        request.read(getInputStream());
        return request;
    }

    /**
     * Write a Reply
     */
    void write(Reply reply) throws IOException {
        reply.write(getOutputStream());
    }
}