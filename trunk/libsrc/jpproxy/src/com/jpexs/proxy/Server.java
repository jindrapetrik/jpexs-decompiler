package com.jpexs.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Proxy server
 *
 * @author JPEXS
 */
public class Server extends Thread {
    private ServerSocket ssocket = null;
    private boolean stoppped = false;
    private List<Replacement> replacements;
    private static Server server;
    private List<String> catchedContentTypes;
    private CatchedListener catchedListener;
    private int port;
    private ReplacedListener replacedListener;

    private Server(int port, List<Replacement> replacements, List<String> catchedContentTypes, CatchedListener catchedListener, ReplacedListener replacedListener) {
        this.replacements = replacements;
        this.catchedContentTypes = catchedContentTypes;
        this.catchedListener = catchedListener;
        this.replacedListener = replacedListener;
        this.port = port;
    }

    private void stopRun() {
        stoppped = true;
        if (ssocket != null) {
            try {
                ssocket.close();
            } catch (IOException e) {

            }
        }
    }

    /**
     * Starts proxy server
     *
     * @param port                Listening port
     * @param replacements        List of replacements
     * @param catchedContentTypes Content types to sniff
     * @param catchedListener     Catched listener
     */
    public static void startServer(int port, List<Replacement> replacements, List<String> catchedContentTypes, CatchedListener catchedListener, ReplacedListener replacedListener) {
        stopServer();
        server = new Server(port, replacements, catchedContentTypes, catchedListener, replacedListener);
        //WorkerThread.assignThread(server, "Proxy server");
        server.start();
    }

    /**
     * Stops proxy server
     */
    public static void stopServer() {
        if (server != null) server.stopRun();
        server = null;
    }

    /**
     * Runs the server
     */
    public void run() {

        try {
            ssocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Cannot bind to port");
            return;
        }
        for (; ;) {
            try {
                Socket sock = ssocket.accept();
                Handler handler = new Handler(sock, replacements, catchedContentTypes, catchedListener, replacedListener);
                WorkerThread.assignThread(handler, "Proxy handler");
            } catch (IOException e) {

            }
            if (stoppped)
                break;
        }
    }
}
