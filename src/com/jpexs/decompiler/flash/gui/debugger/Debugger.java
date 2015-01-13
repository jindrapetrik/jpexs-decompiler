/*
 *  Copyright (C) 2010-2015 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.debugger;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 *
 * @author JPEXS
 */
public class Debugger {

    private static final Set<DebugListener> listeners = new HashSet<>();

    public synchronized void addMessageListener(DebugListener l) {
        listeners.add(l);
    }

    public synchronized void removeMessageListener(DebugListener l) {
        listeners.remove(l);
    }

    private static class DebugHandler extends Thread {

        private final Socket s;
        private final int serverPort;
        private static int maxid = 0;
        private final int id;
        public boolean finished = false;

        public DebugHandler(int serverPort, Socket s) {
            this.s = s;
            id = maxid++;
            this.serverPort = serverPort;
        }

        public void cancel() {
            try {
                s.close();
            } catch (IOException ex) {
                //ignore
            }
        }

        private String readString(InputStream is) throws IOException {
            int len = is.read();
            if (len == -1) {
                throw new EOFException();
            }
            int len2 = is.read();
            if (len2 == -1) {
                throw new EOFException();
            }
            len = (len << 8) + len2;

            byte buf[] = new byte[len];
            for (int i = 0; i < len; i++) {
                int rd = is.read();
                if (rd == -1) {
                    throw new EOFException();
                }
                buf[i] = (byte) rd;
            }
            return new String(buf, "UTF-8");
        }

        @Override
        public void run() {
            String clientName = Integer.toString(id);
            try (InputStream is = s.getInputStream()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                int c;
                do {
                    c = is.read();
                    if (c != 0) {
                        baos.write(c);
                    }

                } while (c > 0);
                String ret = baos.toString("UTF-8");

                if (ret.equals("<policy-file-request/>")) {
                    try (OutputStream os = s.getOutputStream()) {
                        os.write(("<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"" + serverPort + "\" /></cross-domain-policy>").getBytes("UTF-8"));
                    }
                } else {
                    String name = readString(is);
                    if (!name.isEmpty()) {
                        clientName = name;
                    }
                    while (true) {
                        ret = readString(is);
                        for (DebugListener l : listeners) {
                            l.onMessage(clientName, ret);
                        }
                    }
                }

            } catch (IOException ex) {
                //ignore
            }
            try {
                s.close();
            } catch (IOException ex) {
                //ignore
            }
            finished = true;
            for (DebugListener l : listeners) {
                l.onFinish(clientName);
            }
        }

    }

    private static class DebugServerThread extends Thread {

        private final int port;
        private ServerSocket ss;
        private final Map<Integer, DebugHandler> handlers = new WeakHashMap<>();

        public DebugServerThread(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try {
                ss = new ServerSocket(port, 50, InetAddress.getByName("localhost"));
                ss.setReuseAddress(true);
                while (true) {
                    Socket s = ss.accept();
                    if (s != null) {
                        DebugHandler h = new DebugHandler(port, s);
                        handlers.put(h.id, h);
                        h.start();
                    }
                }
            } catch (IOException ex) {
                //ignore
            }
        }

    }

    private final int port;

    public Debugger(int port) {
        this.port = port;
    }

    private DebugServerThread server = null;

    public synchronized void start() {
        if (server == null) {
            server = new DebugServerThread(port);
            server.start();
        }
    }

    public synchronized boolean isRunning() {
        return server != null;
    }

    public int getPort() {
        return port;
    }

    public synchronized void stop() {
        if (server != null) {
            try {
                server.ss.close();
            } catch (IOException ex) {
                //ignore
            }
            for (DebugHandler h : server.handlers.values()) {
                h.cancel();
            }
            server.handlers.clear();
            server = null;
        }
    }

}
