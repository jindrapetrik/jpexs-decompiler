package com.jpexs.proxy;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.DataOutputStream;
import java.util.List;

public class Server implements Runnable {

    ServerSocket server = null;
    boolean running = false;

    private List<String> catchedContentTypes;
    private CatchedListener catchedListener;
    private ReplacedListener replacedListener;
    private List<Replacement> replacements;

    static ThreadPool pool;

    static Server myServer;
    static boolean serverRunning = false;

    static boolean stopping = false;

    public static ReusableThread getThread() {
        return pool.get();
    }

    /**
     * Starts proxy server
     *
     * @param port Listening port
     * @param replacements List of replacements
     * @param catchedContentTypes Content types to sniff
     * @param catchedListener Catched listener
     */
    public static boolean startServer(int port, List<Replacement> replacements, List<String> catchedContentTypes, CatchedListener catchedListener, ReplacedListener replacedListener) {
        stopServer();
        stopping = false;
        try {
            myServer = new Server(port, replacements, catchedContentTypes, catchedListener, replacedListener);
        } catch (IOException ex) {
            return false;
        }
        pool = new ThreadPool(ProxyConfig.appName + " Threads");
        /* Startup the Janitor */
        Janitor j = new Janitor();
        j.add(pool);
        getThread().setRunnable(j);
        serverRunning = true;
        getThread().setRunnable(myServer);
        return true;
    }

    public static void stopServer() {
        if (serverRunning) {
            stopping = true;
            serverRunning = false;
            try {
                myServer.server.close();
            } catch (IOException ex) {

            }
            pool.clean();
        }
    }

    Server(int port, List<Replacement> replacements, List<String> catchedContentTypes, CatchedListener catchedListener, ReplacedListener replacedListener) throws IOException {

        this.replacements = replacements;
        this.catchedContentTypes = catchedContentTypes;
        this.catchedListener = catchedListener;
        this.replacedListener = replacedListener;

        try {
            String bindaddr = ProxyConfig.bindAddress;
            if (bindaddr != null && bindaddr.length() > 0) {
                server = new ServerSocket(port, 512,
                        InetAddress.getByName(bindaddr));
            } else {
                server = new ServerSocket(port, 512);
            }
        } catch (IOException e) {
            throw e;
        }

        /* Initialize internal Httpd */
    }

    synchronized void suspend() {
        running = false;
    }

    synchronized void resume() {
        running = true;
    }

    public void run() {
        Thread.currentThread().setName(ProxyConfig.appName + " Server");
        running = true;
        for (;;) {
            Socket socket;

            try {
                socket = server.accept();
            } catch (IOException e) {
                if (stopping) {
                    break;
                }
                continue;
            }

            if (stopping) {
                break;
            }

            if (running) {
                Handler h = new Handler(socket, replacements, catchedContentTypes, catchedListener, replacedListener);
                ReusableThread rt = getThread();
                rt.setRunnable(h);
            } else {
                error(socket, 503, ProxyConfig.appName + " proxy service is suspended.");
            }
        }
    }

    void error(Socket socket, int code, String message) {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes((new HttpError(code, message)).toString());
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
