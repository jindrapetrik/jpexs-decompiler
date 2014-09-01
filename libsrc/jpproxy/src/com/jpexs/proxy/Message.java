package com.jpexs.proxy;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.io.*;

public abstract class Message {

    /**
     * Hashtable used to store message headers.
     */
    private Hashtable headers = new Hashtable(33);

    /**
     *
     */
    String statusLine = null;

    public String readLine(InputStream in) throws IOException {
        char[] buf = new char[128];
        int offset = 0;
        int ch;

        for (;;) {
            ch = in.read();
            if (ch == -1 || ch == '\n') {
                break;
            } else if (ch == '\r') {
                int tmpch = in.read();
                if (tmpch != '\n') {
                    if (!(in instanceof PushbackInputStream)) {
                        in = new PushbackInputStream(in);
                    }
                    ((PushbackInputStream) in).unread(tmpch);
                }
                break;
            } else {
                if (offset == buf.length) {
                    char[] tmpbuf = buf;
                    buf = new char[tmpbuf.length * 2];
                    System.arraycopy(tmpbuf, 0, buf, 0, offset);
                }
                buf[offset++] = (char) ch;
            }
        }
        return String.copyValueOf(buf, 0, offset);
    }

    /**
     * Read headers and store them in the hashtable.
     */
    void readHeaders(InputStream in) throws IOException {
        int i;
        Key key = null;

        for (;;) {
            String s = readLine(in);
            if (s == null) {
                break;
            }
            i = s.indexOf(':');
            if (i == -1) {
                /* end of header */
                if (s.length() == 0) {
                    break;
                } /* multi-line headers */ else if (key != null
                        && (s.startsWith(" ") || s.startsWith("\t"))) {
                    int index = getHeaderValueCount(key.toString());
                    index--;
                    Vector v = (Vector) headers.get(key);
                    v.setElementAt(v.elementAt(index) + "\n" + s, index);
                }
            } else {
                key = new Key(s.substring(0, i));
                Vector v;
                if (headers.containsKey(key)) {
                    v = (Vector) headers.get(key);
                } else {
                    v = new Vector();
                }
                v.addElement(s.substring(i + 1).trim());
                headers.put(key, v);
            }
        }
    }

    public int headerCount() {
        return headers.size();
    }

    /**
     * Set the Status line.
     */
    public void setStatusLine(String l) {
        statusLine = l;
    }

    public int getHeaderValueCount(String name) {
        Vector v = (Vector) headers.get(new Key(name));
        return v.size();
    }

    public String getHeaderField(String name) {
        return getHeaderField(name, 0);
    }

    public String getHeaderField(String name, int index) {
        Vector v = (Vector) headers.get(new Key(name));
        if (v == null) {
            return null;
        }
        return (String) v.elementAt(index);
    }

    public void setHeaderField(String name, String value) {
        setHeaderField(name, value, 0);
    }

    public void setHeaderField(String name, String value, int index) {
        Vector v;
        Key key = new Key(name);

        if (headers.containsKey(key)) {
            v = (Vector) headers.get(key);
        } else {
            v = new Vector();
            if (index == 0) {
                v.addElement("");
            }
            headers.put(key, v);
        }
        v.setElementAt(value, index);
    }

    public void setHeaderField(String name, int value) {
        setHeaderField(name, value, 0);
    }

    public void setHeaderField(String name, int value, int index) {
        setHeaderField(name, new Integer(value).toString(), index);
    }

    /**
     * Set all header fields with the give name to the specified value.
     */
    public void setHeaderFields(String name, String value) {
        Vector v;
        Key key = new Key(name);

        v = (Vector) headers.get(key);
        if (v != null) {
            for (int i = 0; i < v.size(); i++) {
                v.setElementAt(value, i);
            }
        }
    }

    public void appendHeaderField(String name, String value) {
        appendHeaderField(name, value, 0);
    }

    public void appendHeaderField(String name, String value, int index) {
        setHeaderField(name, getHeaderField(name, index) + value, index);
    }

    public void removeHeaderField(String name) {
        headers.remove(new Key(name));
    }

    /**
     * Return whether or not a header exists.
     *
     * @param name header name
     */
    public boolean containsHeaderField(String name) {
        return headers.containsKey(new Key(name));
    }

    /**
     * @return an Enumeration of Strings
     */
    public Enumeration getHeaders() {
        Vector v = new Vector();

        for (Enumeration e = headers.keys(); e.hasMoreElements();) {
            v.addElement(e.nextElement().toString());
        }

        return v.elements();
    }

    private final static byte[] COLON_SPACE = ": ".getBytes();
    private final static byte[] CRLF = "\r\n".getBytes();

    private ByteArray toByteArray(byte[] sep) {
        ByteArray buf = new ByteArray();
        Key key;
        String value;
        Vector v;
        int i = 0;

        buf.append(statusLine);
        buf.append(sep);

        for (Enumeration e = headers.keys(); e.hasMoreElements();) {
            key = (Key) e.nextElement();
            v = (Vector) headers.get(key);
            for (i = 0; i < v.size(); i++) {
                buf.append(key.toString());
                buf.append(COLON_SPACE);
                buf.append(v.elementAt(i).toString());
                buf.append(sep);
            }
        }
        buf.append(sep);

        return buf;
    }

    private ByteArray toByteArray() {
        return toByteArray(CRLF);
    }

    private ByteArray toByteArray(String sep) {
        return toByteArray(sep.getBytes());
    }

    public String toString() {
        return toByteArray().toString();
    }

    public String toString(String sep) {
        return toByteArray(sep).toString();
    }

    public void write(OutputStream out)
            throws IOException {
        toByteArray().writeTo(out);
        out.flush();
    }
}
