package com.jpexs.proxy;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mark Boyns
 */
public class Request extends Message {
    private static Pattern httpRegex;
    private String command = null;
    private String url = null;
    private String protocol = null;
    private byte[] data = null;
    private Hashtable log;
    private Vector logHeaders;
    private Client client;

    static {
        httpRegex = Pattern.compile("^(http|https):", Pattern.CASE_INSENSITIVE);
    }

    public Request(Client client) {
        this.client = client;
    }

    void read(InputStream in) throws IOException {
        statusLine = readLine(in);
        if (statusLine == null || statusLine.length() == 0) {
            throw new IOException("Empty request");
        }

        StringTokenizer st = new StringTokenizer(statusLine);
        command = (String) st.nextToken();
        url = (String) st.nextToken();
        protocol = (String) st.nextToken();

        if (!url.startsWith("http")) {
            Matcher match = httpRegex.matcher(url);
            if (match.matches()) {
                url = url.substring(match.start(),
                        match.end()).toLowerCase()
                        + url.substring(match.end());
            }
        }

        readHeaders(in);

        if ("POST".equals(command) || "PUT".equals(command)) {
            try {
                int n = Integer.parseInt(getHeaderField("Content-length"));
                data = new byte[n];
                int offset = 0;
                while (offset < data.length) {
                    n = in.read(data, offset, data.length - offset);
                    if (n < 0) {
                        throw new IOException("Not enough " + command + " data");
                    }
                    offset += n;
                }
            }
            catch (NumberFormatException e) {
                System.out.println("Malformed or missing " + command + " Content-length");
            }
        }
    }

    public void write(OutputStream out)
            throws IOException {
        super.write(out);
        if (data != null) {
            out.write(data);
            out.flush();
        }
    }

    public String getRequest() {
        return statusLine;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        String url = getURL();
        String s;

        if (url.startsWith("http://")) {
            s = url.substring(7, url.indexOf('/', 7));
        } else {
            s = url;
        }

        int at = s.indexOf('@');
        if (at != -1) {
            s = s.substring(at + 1);
        }

        if (s.indexOf(':') != -1) {
            return s.substring(0, s.indexOf(':'));
        }

        return s;
    }

    public int getPort() {
        int port = 80;
        String url = getURL();
        String s;

        if (url.startsWith("http://")) {
            s = url.substring(7, url.indexOf('/', 7));
        } else {
            s = url;
        }

        int at = s.indexOf('@');
        if (at != -1) {
            s = s.substring(at + 1);
        }

        if (s.indexOf(':') != -1) {
            try {
                port = Integer.parseInt(s.substring(s.indexOf(':') + 1));
            }
            catch (NumberFormatException e) {
                System.out.println("Invalid port in " + url);
            }
        }
        return port;
    }

    public String getData() {
        if (data == null) {
            return null;
        }
        return new String(data);
    }

    public byte[] getDataBytes() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public String getPath() {
        String str = getURL();
        int pos = 0;
        for (int i = 0; i < 3; i++) {
            pos = str.indexOf('/', pos);
            pos++;
        }
        pos--;
        return str.substring(pos);
    }

    public String getDocument() {
        String path = getPath();
        int n = path.lastIndexOf('/');
        if (n == path.length() - 1) {
            n = path.lastIndexOf('/', n - 1);
        }
        if (n < 0) {
            return "/";
        } else {
            return path.substring(n + 1);
        }
    }


    public String getQueryString() {
        String path = getPath();
        int n = path.indexOf('?');
        if (n < 0) {
            return null;
        }
        return path.substring(n + 1);
    }

    public synchronized void addLogEntry(String header,
                                         String message) {
        if (log == null) {
            log = new Hashtable();
            logHeaders = new Vector();
        }

        Vector v = (Vector) log.get(header);
        if (log.get(header) == null) {
            v = new Vector();
            log.put(header, v);
            logHeaders.addElement(header);
        }
        v.addElement(message);
    }

    public Enumeration getLogHeaders() {
        return logHeaders != null ? logHeaders.elements() : null;
    }

    public Enumeration getLogEntries(String header) {
        return log != null ? ((Vector) log.get(header)).elements() : null;
    }
}