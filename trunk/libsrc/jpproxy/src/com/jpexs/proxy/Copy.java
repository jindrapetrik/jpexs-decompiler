package com.jpexs.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Copy implements Runnable {
    InputStream in = null;
    OutputStream out = null;

    Copy(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    public void run() {
        int n;
        byte buffer[] = new byte[8192];

        try {
            while ((n = in.read(buffer, 0, buffer.length)) > 0) {
                out.write(buffer, 0, n);
                out.flush();
            }
            out.flush();
        }
        catch (IOException e) {
            String s = e.toString();
            // ignore socket closed exceptions
            if (s.toLowerCase().indexOf("socket closed") == -1) {
                e.printStackTrace();
            }
        }
    }
}