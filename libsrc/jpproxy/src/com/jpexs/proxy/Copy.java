package com.jpexs.proxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.SocketException;

class Copy implements Runnable {

    InputStream in = null;
    OutputStream out = null;

    Copy(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    public void run() {
        int n;
        byte[] buffer = new byte[8192];

        try {
            while ((n = in.read(buffer, 0, buffer.length)) > 0) {
                out.write(buffer, 0, n);
                out.flush();
            }
            out.flush();
        } catch (SocketException e) {
        } catch (IOException e) {
            //Ignore errors
        }
    }
}
