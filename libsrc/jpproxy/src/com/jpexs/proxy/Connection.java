package com.jpexs.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class Connection {

    Socket socket = null;
    InputStream in = null;
    OutputStream out = null;
    static SSLSocketFactory sf;

    static {
        String ksName = ProxyConfig.httpsKeyStoreFile;
        if (ksName != null) {
            char[] ksPass = ProxyConfig.httpsKeyStorePass.toCharArray();
            char[] ctPass = ProxyConfig.httpsKeyPass.toCharArray();
            try {
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(new FileInputStream(ksName), ksPass);
                KeyManagerFactory kmf
                        = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, ctPass);
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(kmf.getKeyManagers(), null, null);
                sf = sc.getSocketFactory();
            } catch (Exception ex) {

            }
        }
    }

    public void promoteToClientSSL() {
        SSLSocketFactory f = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            socket = (SSLSocket) f.createSocket(socket, null, socket.getPort(), false);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException ex) {

        }

    }

    public void promoteToServerSSL() {
        try {
            socket = sf.createSocket(socket, null, socket.getPort(), false);
            ((SSLSocket) socket).setUseClientMode(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException ex) {

        }

    }

    /**
     * Create a Connection from a Socket.
     *
     * @param socket a socket
     */
    Connection(Socket socket) throws IOException {
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    /**
     * Create a Connection from a hostname and port.
     *
     * @param host remote hostname
     * @param port remote port
     */
    Connection(String host, int port) throws IOException {
        this(new Socket(InetAddress.getByName(host), port));
    }

    Connection() {
    }

    /**
     * Return the input stream.
     */
    InputStream getInputStream() {
        return in;
    }

    /**
     * Return the output stream.
     */
    OutputStream getOutputStream() {
        return out;
    }

    void setInputStream(InputStream in) {
        this.in = in;
    }

    void setOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * Close the connection.
     */
    void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {

            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    public int getPort() {
        return socket.getPort();
    }

    public String toString() {
        return getInetAddress().getHostAddress() + ":" + getPort();
    }

    public void setTimeout(int timeout)
            throws SocketException {
        socket.setSoTimeout(timeout);
    }
}
