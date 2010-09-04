package com.jpexs.proxy;


import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;

/**
 * HTTP transaction handler.  A handler is created by Server for
 * each HTTP transaction.  Given a socket, the handler will deal with
 * the request, reply, and invoke request, reply, and content filters.
 *
 * @author Mark Boyns
 */
class Handler implements Runnable {
    static final boolean DEBUG = false;
    Socket socket = null;
    Request request = null;
    Reply reply = null;
    HttpRelay http = null;
    int currentLength = -1;
    int contentLength = -1;
    long idle = 0;
    double bytesPerSecond = 0;
    Client client;

    int MAXTIMEOUT = 1000 * 60 * 5;
    List<Replacement> replacements;
    CatchedListener catchedListener;
    List<String> catchedContentTypes;
    ReplacedListener replacedListener;

    /**
     * Create a Handler.
     */
    Handler(Socket socket, List<Replacement> replacements, List<String> catchedContentTypes, CatchedListener catchedListener, ReplacedListener replacedListener) {
        this.socket = socket;
        this.replacements = replacements;
        this.catchedListener = catchedListener;
        this.catchedContentTypes = catchedContentTypes;
        this.replacedListener = replacedListener;
    }

    /**
     * Close all connections associated with this handler.
     */
    synchronized void close() {
        if (client != null) {
            client.close();
            client = null;
        }
        if (http != null) {
            http.close();
            http = null;
        }
    }

    /**
     * Flush all data to the client.
     */
    void flush() {
        if (client != null) {
            try {
                client.getOutputStream().flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        boolean keepAlive = false;
        Exception reason = null;

        Thread.currentThread().setName("Handler("
                + socket.getInetAddress().getHostAddress()
                + ")");

        try {
            client = new Client(socket);
        } catch (IOException e) {

        }

        do {
            request = null;
            reply = null;

            idle = System.currentTimeMillis();


            try {
                request = client.read();
                if (Main.DEBUG_MODE) {
                    System.out.println("REQUEST: " + request.getURL());
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                break;
            }

            idle = 0;


            try {
                keepAlive = processRequest();
            }
            catch (IOException ioe) {
                reason = ioe;
                keepAlive = false;
            }

            if (request != null && reply != null) {
                // XXX insert the number of bytes read into the
                // reply content-length for logging.
                if (reply != null && currentLength > 0) {
                    reply.setHeaderField("Content-length", currentLength);
                }
            }
        }
        while (keepAlive);


        if (reason != null && reason.getMessage().indexOf("Broken pipe") == -1) {
        }

        close();
    }

    boolean processRequest() throws IOException {
        boolean keepAlive = false;

        while (reply == null) {
            boolean secure = false;
            boolean uncompress = false;


            if (request.getCommand().equals("CONNECT")) {
                secure = true;
            } else if (request.getURL().startsWith("https://")) {
                System.out.println("Netscape keep-alive bug: " + request.getURL());
                return false;
            } else if (!request.getURL().startsWith("http://")) {
                System.out.println("Unknown URL: " + request.getURL());
                return false;
            }

            keepAlive = (request.containsHeaderField("Proxy-Connection")
                    && request.getHeaderField("Proxy-Connection").equals("Keep-Alive"));


            /* First look for any HttpFilters */


            /* None found.  Use http or https relay. */
            if (secure) {
                http = createHttpsRelay();
            } else {
                http = createHttpRelay();
            }


            try {
                http.sendRequest(request);
                if (http instanceof Http) {
                    ((Http) http).setTimeout(MAXTIMEOUT);
                }
                reply = http.recvReply(request);
            }
            catch (RetryRequestException e) {
                http.close();
                http = null;
                continue; /* XXX */
            }

            /* Guess content-type if there aren't any headers.
              Probably an upgraded HTTP/0.9 reply. */
            if (reply.headerCount() == 0) {
                String url = request.getURL();
                if (url.endsWith("/")
                        || url.endsWith(".html") || url.endsWith(".htm")) {
                    reply.setHeaderField("Content-type", "text/html");
                }
            }


            String encoding = reply.getHeaderField("Content-Encoding");
            if (encoding != null && encoding.indexOf("gzip") != -1) {
                reply.removeHeaderField("Content-Encoding");
                reply.removeHeaderField("Content-length");
                uncompress = true;
            }
            /* update reply */


            reply.removeHeaderField("Proxy-Connection");
            if (keepAlive && reply.containsHeaderField("Content-length")) {
                reply.setHeaderField("Proxy-Connection", "Keep-Alive");
            } else {
                keepAlive = false;
            }

            currentLength = -1;
            contentLength = -1;
            try {
                contentLength = Integer.parseInt(reply.getHeaderField("Content-length"));
            }
            catch (NumberFormatException e) {
            }

            /* update content-length reply */


            if (secure) {
                Https https = (Https) http;
                int timeout = MAXTIMEOUT;

                client.write(reply);

                try {
                    https.setTimeout(timeout);

                    Copy cp = new Copy(client.getInputStream(), https.getOutputStream());
                    WorkerThread.assignThread(cp, "");
                    flushCopy(https.getInputStream(), client.getOutputStream(), -1, true);
                    client.close();
                }
                catch (InterruptedIOException iioe) {
                    // ignore socket timeout exceptions
                }
            } else if (reply.hasContent()) {
                try {
                    processContent(uncompress);
                }
                catch (IOException e) {
                    if (http instanceof Http) {
                        ((Http) http).reallyClose();
                    } else {
                        http.close();
                    }
                    http = null;
                    client.close();
                    client = null;
                    throw e;
                    //return false; /* XXX */
                }

                /* Document contains no data. */
                if (contentLength == 0) {
                    client.close();
                }
            } else {
                client.write(reply);
            }

            http.close();
        }

        return keepAlive;
    }

    HttpRelay createHttpsRelay() throws IOException {
        HttpRelay http;

        http = new Https(request.getHost(), request.getPort());

        return http;
    }

    HttpRelay createHttpRelay() throws IOException {
        HttpRelay http;


        http = Http.open(request.getHost(), request.getPort());


        return http;
    }


    InputStream readChunkedTransfer(InputStream in) throws IOException {
        ByteArrayOutputStream chunks = new ByteArrayOutputStream(8192);
        int size = 0;

        contentLength = 0;
        while ((size = reply.getChunkSize(in)) > 0) {
            contentLength += size;
            copy(in, chunks, size, true);
            reply.readLine(in);
        }
        reply.getChunkedFooter(in);

        reply.removeHeaderField("Transfer-Encoding");
        reply.setHeaderField("Content-length", contentLength);

        return new ByteArrayInputStream(chunks.toByteArray());
    }


    private static DateFormat httpDateFormat() {
        DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return httpDateFormat;
    }

    void disableReplyCaching() {
        reply.removeHeaderField("Cache-Control");
        reply.removeHeaderField("Last-Modified");
        reply.removeHeaderField("Expires");
        reply.removeHeaderField("Date");
        reply.removeHeaderField("ETag");
        reply.removeHeaderField("Pragma");

        reply.setHeaderField("Pragma", "no-cache");
        reply.setHeaderField("Cache-Control", "no-cache, must-revalidate");
        Calendar now = Calendar.getInstance();
        reply.setHeaderField("Expires", httpDateFormat().format(now.getTime()));//"Sat, 26 Jul 1997 05:00:00 GMT");
        reply.setHeaderField("Last-Modified", "Sat, 26 Jul 1997 05:00:00 GMT");
    }


    void processContent(boolean uncompress) throws IOException {
        InputStream in;
        boolean chunked = false;

        if (reply.containsHeaderField("Transfer-Encoding")
                && reply.getTransferEncoding().equals("chunked")) {
            in = readChunkedTransfer(reply.getContent());
            chunked = true;
        } else {
            in = reply.getContent();
        }

        if (in == null) {
            System.out.println("No inputstream");
            return;
        } else if (uncompress) {
            in = new GZIPInputStream(in);
        }

        String url = request.getURL();
        boolean replaced = false;
        for (Replacement r : replacements) {
            if (r.matches(url)) {
                r.lastAccess = Calendar.getInstance();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                try {
                    FileInputStream fis = new FileInputStream(r.targetFile);
                    byte buf[] = new byte[2048];
                    int pos = 0;
                    while ((pos = fis.read(buf)) > 0) {
                        buffer.write(buf, 0, pos);
                    }
                    fis.close();
                    buffer.close();
                } catch (IOException ex) {
                    if (Main.DEBUG_MODE) {
                        System.err.println("ERROR:Cannot read file: " + r.targetFile);
                    }
                }
                byte bytes[] = buffer.toByteArray();
                contentLength = bytes.length;
                reply.setHeaderField("Content-length", contentLength);
                disableReplyCaching();
                client.write(reply);
                copy(new ByteArrayInputStream(bytes),
                        client.getOutputStream(), contentLength, false);
                replaced = true;
                if (replacedListener != null) {
                    replacedListener.replaced(r, request.getURL(), reply.getContentType());
                }
                break;
            }
        }

        if (!replaced) {

            String contentType = reply.getHeaderField("Content-type");
            if (reply.getStatusCode() == 200) {
                if (contentType != null) {
                    for (String ct : catchedContentTypes) {
                        String convContentType = contentType;
                        if (convContentType.contains(";"))
                            convContentType = convContentType.substring(0, convContentType.indexOf(";"));

                        if (ct.toLowerCase().equals(convContentType.toLowerCase())) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            copy(in, baos, contentLength, true);
                            if (catchedListener != null) {
                                catchedListener.catched(ct, request.getURL(), new ByteArrayInputStream(baos.toByteArray()));
                            }
                            in = new ByteArrayInputStream(baos.toByteArray());
                            disableReplyCaching();
                            break;
                        }
                    }
                }
            }
            client.write(reply);
            copy(in, client.getOutputStream(), contentLength, true);
        }
    }


    /**
     * Return the content length.
     */
    int getTotalBytes() {
        return contentLength > 0 ? contentLength : 0;
    }

    /**
     * Return the number of bytes read so far.
     */
    int getCurrentBytes() {
        return currentLength > 0 ? currentLength : 0;
    }


    /**
     * Copy in to out.
     *
     * @param in        InputStream
     * @param out       OutputStream
     * @param monitored Update the Monitor
     */
    void copy(InputStream in, OutputStream out, int length, boolean monitored)
            throws IOException {
        if (length == 0) {
            return;
        }

        int n;
        byte buffer[] = new byte[8192];
        long start = System.currentTimeMillis();
        long now = 0, then = start;

        bytesPerSecond = 0;

        if (monitored) {
            currentLength = 0;
        }

        for (; ;) {
            n = (length > 0) ? Math.min(length, buffer.length) : buffer.length;
            n = in.read(buffer, 0, n);
            if (n < 0) {
                break;
            }

            out.write(buffer, 0, n);

            if (monitored) {
                currentLength += n;

            }

            now = System.currentTimeMillis();
            bytesPerSecond = currentLength / ((now - start) / 1000.0);

            // flush after 1 second
            if (now - then > 1000) {
                out.flush();
            }

            if (length != -1) {
                length -= n;
                if (length == 0) {
                    break;
                }
            }

            then = now;
        }

        out.flush();

        if (DEBUG) {
            System.out.println(currentLength + " bytes processed in "
                    + ((System.currentTimeMillis() - start)
                    / 1000.0) + " seconds "
                    + ((int) bytesPerSecond / 1024) + " kB/s");
        }
    }

    /**
     * Copy in to out.
     *
     * @param in        InputStream
     * @param out       OutputStream
     * @param monitored Update the Monitor
     */
    void flushCopy(InputStream in, OutputStream out, int length, boolean monitored)
            throws IOException {
        if (length == 0) {
            return;
        }

        int n;
        byte buffer[] = new byte[8192];
        long start = System.currentTimeMillis();
        bytesPerSecond = 0;

        if (monitored) {
            currentLength = 0;
        }

        for (; ;) {
            n = (length > 0) ? Math.min(length, buffer.length) : buffer.length;
            n = in.read(buffer, 0, n);
            if (n < 0) {
                break;
            }

            out.write(buffer, 0, n);
            out.flush();
            if (monitored) {
                currentLength += n;

            }
            bytesPerSecond = currentLength / ((System.currentTimeMillis() - start) / 1000.0);
            if (length != -1) {
                length -= n;
                if (length == 0) {
                    break;
                }
            }
        }
        out.flush();

        if (DEBUG) {
            System.out.println(currentLength + " bytes processed in "
                    + ((System.currentTimeMillis() - start) / 1000.0) + " seconds "
                    + ((int) bytesPerSecond / 1024) + " kB/s");
        }
    }


    /**
     * Return a string represenation of the hander's state.
     */
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("CLIENT ");
        str.append(socket.getInetAddress().getHostAddress());
        str.append(":");
        str.append(socket.getPort());
        str.append(" - ");
        if (request == null) {
            str.append("idle " + ((System.currentTimeMillis() - idle) / 1000.0) + " sec");
        } else {
            if (reply != null && currentLength > 0) {
                str.append("(");
                str.append(currentLength);
                if (contentLength > 0) {
                    str.append("/");
                    str.append(contentLength);
                }
                str.append(" ");
                str.append(((int) bytesPerSecond / 1024) + " kB/s");
                str.append(") ");
            }
            str.append(request.getURL());
        }
        return str.toString();
    }
}