package com.jpexs.proxy;

import java.io.InputStream;

/**
 * Interface to catch contentTypes
 *
 * @author JPEXS
 */
public interface CatchedListener {

    /**
     * Method called when specified contentType is received
     *
     * @param contentType Content type
     * @param url URL of the method
     * @param data Data stream
     * @return replacement data
     */
    public byte[] catched(String contentType, String url, InputStream data);
}
