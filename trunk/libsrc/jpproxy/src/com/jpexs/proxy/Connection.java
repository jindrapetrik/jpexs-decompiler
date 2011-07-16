package com.jpexs.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.KeyStoreException;

class Connection
{
    Socket socket = null;
    InputStream in = null;
    OutputStream out = null;
    
    /**
     * Create a Connection from a Socket.
     *
     * @param socket a socket
     */
    Connection(Socket socket) throws IOException
    {
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
    Connection(String host, int port) throws IOException
    {
	this(new Socket(InetAddress.getByName(host), port));
    }

    Connection()
    {
    }

    /**
     * Return the input stream.
     */
    InputStream getInputStream()
    {
	return in;
    }

    /**
     * Return the output stream.
     */
    OutputStream getOutputStream()
    {
	return out;
    }

    void setInputStream(InputStream in)
    {
	this.in = in;
    }
    
    void setOutputStream(OutputStream out)
    {
	this.out = out;
    }

    /**
     * Close the connection.
     */
    void close()
    {
	if (socket != null)
	{
	    try
	    {
		socket.close();
	    }
	    catch (IOException e)
	    {
		System.out.println("Connection: " + e);
	    }
	}
    }

    public Socket getSocket()
    {
	return socket;
    }

    public InetAddress getInetAddress()
    {
	return socket.getInetAddress();
    }
    
    public int getPort()
    {
	return socket.getPort();
    }

    public String toString()
    {
	return getInetAddress().getHostAddress() + ":" + getPort();
    }

    public void setTimeout(int timeout)
	throws SocketException
    {
	socket.setSoTimeout(timeout);
    }
}
