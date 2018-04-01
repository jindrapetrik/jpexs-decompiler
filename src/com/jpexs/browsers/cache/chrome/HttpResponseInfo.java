/*
 *  Copyright (C) 2010-2018 JPEXS
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
package com.jpexs.browsers.cache.chrome;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author JPEXS
 */
public class HttpResponseInfo {

    public long flags;

    public int version;

    public long request_time;

    public long response_time;

    public long payload_size;

    public List<String> headers;

    // The version of the response info used when persisting response info.
    public static final int RESPONSE_INFO_VERSION = 3;

    // The minimum version supported for deserializing response info.
    public static final int RESPONSE_INFO_MINIMUM_VERSION = 1;

    // We reserve up to 8 bits for the version number.
    public static final int RESPONSE_INFO_VERSION_MASK = 0xFF;

    // This bit is set if the response info has a cert at the end.
    // Version 1 serialized only the end-entity certificate, while subsequent
    // versions include the available certificate chain.
    public static final int RESPONSE_INFO_HAS_CERT = 1 << 8;

    // This bit is set if the response info has a security-bits field (security
    // strength, in bits, of the SSL connection) at the end.
    public static final int RESPONSE_INFO_HAS_SECURITY_BITS = 1 << 9;

    // This bit is set if the response info has a cert status at the end.
    public static final int RESPONSE_INFO_HAS_CERT_STATUS = 1 << 10;

    // This bit is set if the response info has vary header data.
    public static final int RESPONSE_INFO_HAS_VARY_DATA = 1 << 11;

    // This bit is set if the request was cancelled before completion.
    public static final int RESPONSE_INFO_TRUNCATED = 1 << 12;

    // This bit is set if the response was received via SPDY.
    public static final int RESPONSE_INFO_WAS_SPDY = 1 << 13;

    // This bit is set if the request has NPN negotiated.
    public static final int RESPONSE_INFO_WAS_NPN = 1 << 14;

    // This bit is set if the request was fetched via an explicit proxy.
    public static final int RESPONSE_INFO_WAS_PROXY = 1 << 15;

    // This bit is set if the response info has an SSL connection status field.
    // This contains the ciphersuite used to fetch the resource as well as the
    // protocol version, compression method and whether SSLv3 fallback was used.
    public static final int RESPONSE_INFO_HAS_SSL_CONNECTION_STATUS = 1 << 16;

    // This bit is set if the response info has protocol version.
    public static final int RESPONSE_INFO_HAS_NPN_NEGOTIATED_PROTOCOL = 1 << 17;

    // This bit is set if the response info has connection info.
    public static final int RESPONSE_INFO_HAS_CONNECTION_INFO = 1 << 18;

    // This bit is set if the request has http authentication.
    public static final int RESPONSE_INFO_USE_HTTP_AUTHENTICATION = 1 << 19;

    public String getHeaderValue(String header) {
        for (String h : headers) {
            if (h.contains(":")) {
                String[] keyval = h.split(":");
                String key = keyval[0].trim().toLowerCase(Locale.ENGLISH);
                String val = keyval[1].trim();
                if (header.toLowerCase(Locale.ENGLISH).equals(key)) {
                    return val;
                }
            }
        }
        return null;
    }

    public HttpResponseInfo(InputStream is) throws IOException {
        IndexInputStream iis = new IndexInputStream(is);
        payload_size = iis.readUInt32();
        flags = iis.readInt();
        version = (int) (flags & RESPONSE_INFO_VERSION_MASK);
        if (version < RESPONSE_INFO_MINIMUM_VERSION || version > RESPONSE_INFO_VERSION) {
            throw new RuntimeException("unexpected response info version: " + version);
        }
        request_time = iis.readInt64();
        response_time = iis.readInt64();
        String headersStr = iis.readString();
        headers = new ArrayList<>();
        int nulpos;
        while ((nulpos = headersStr.indexOf(0)) > 0) {
            String h = headersStr.substring(0, nulpos);
            headersStr = headersStr.substring(nulpos + 1);
            headers.add(h);
        }

        //TODO: Read SSL info
    }
}
