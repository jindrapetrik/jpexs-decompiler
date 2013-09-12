package com.jpexs.browsers.cache;

import com.jpexs.helpers.LimitedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 *
 * @author JPEXS
 */
public abstract class CacheEntry {

    public abstract String getRequestURL();

    public abstract Map<String, String> getResponseHeaders();

    public abstract String getStatusLine();

    public abstract String getRequestMethod();

    public abstract InputStream getResponseRawDataStream();

    public InputStream getResponseDataStream() {
        String contentLengthStr = getHeader("Content-Length");
        int contentLength = -1;
        if (contentLengthStr != null) {
            try {
                contentLength = Integer.parseInt(contentLengthStr);
            } catch (NumberFormatException nex) {
            }
        }
        final InputStream rawIs = getResponseRawDataStream();
        InputStream is = rawIs;
        if (contentLength > -1) {
            is = new LimitedInputStream(is, contentLength);
        }

        String encoding = getHeader("Content-Encoding");
        if (encoding != null) {
            switch (encoding) {
                case "gzip":
                    try {
                        is = new GZIPInputStream(is);
                    } catch (IOException ex) {
                        is = null;
                        //ignore
                    }
                    break;
                case "deflate":
                    is = new InflaterInputStream(is);
                    break;
                default: //unknown
                    return null;
            }
        }
        if ("chunked".equals(getHeader("Transfer-Encoding"))) {
            is = new ChunkedInputStream(is);
        }
        return is;
    }

    @Override
    public String toString() {
        return getRequestURL();
    }

    public int getStatusCode() {
        String st = getStatusLine();
        if (st == null) {
            return 0;
        }
        String parts[] = st.split(" ");
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public String getHeader(String header) {
        Map<String, String> m = getResponseHeaders();
        if(m == null){
           return null;
        }
        for (String k : m.keySet()) {
            if (k.toLowerCase().equals(header.toLowerCase())) {
                return m.get(k);
            }
        }
        return null;
    }
}
