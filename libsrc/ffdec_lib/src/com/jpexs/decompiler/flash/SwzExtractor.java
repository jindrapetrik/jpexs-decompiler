/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Extracts the embedded SWF from a PKCS#7 SWZ using only Java 8 APIs. Does NOT
 * verify signatures, certificates or an expected content digest. Supports
 * definite lengths and constructed BER indefinite lengths/OCTET STRINGs. This
 * is a content extractor, not a general ASN.1 or PKCS#7 validator.
 */
public final class SwzExtractor {

    private static final int MAX_DEPTH = 64;
    private static final byte[] SIGNED_DATA = {42, -122, 72, -122, -9, 13, 1, 7, 2};
    private static final byte[] DATA = {42, -122, 72, -122, -9, 13, 1, 7, 1};

    private SwzExtractor() {
    }

    public static byte[] extract(byte[] swz) throws IOException {
        if (swz == null) {
            throw new IOException("Missing SWZ bytes");
        }
        Reader file = new Reader(swz, 0, swz.length);
        Reader outer = file.read(0x30).children(swz);
        file.requireEnd();
        requireOid(swz, outer.read(0x06), SIGNED_DATA);
        Reader wrapper = outer.read(0xA0).children(swz);
        outer.requireEnd();
        Reader signed = wrapper.read(0x30).children(swz);
        wrapper.requireEnd();
        signed.read(0x02); // version
        signed.read(0x31); // digestAlgorithms
        Reader contentInfo = signed.read(0x30).children(swz);
        requireOid(swz, contentInfo.read(0x06), DATA);
        Reader content = contentInfo.read(0xA0).children(swz);
        contentInfo.requireEnd();
        Element octets = content.read();
        content.requireEnd();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        appendOctets(swz, octets, out, 0);
        byte[] swf = out.toByteArray();
        if (swf.length < 8 || swf[1] != 'W' || swf[2] != 'S'
                || (swf[0] != 'F' && swf[0] != 'C' && swf[0] != 'Z')) {
            throw new IOException("Embedded content has no FWS/CWS/ZWS header");
        }
        return swf;
    }

    private static void requireOid(byte[] bytes, Element e, byte[] expected) throws IOException {
        if (!Arrays.equals(Arrays.copyOfRange(bytes, e.start, e.end), expected)) {
            throw new IOException("Unexpected PKCS#7 content type");
        }
    }

    private static void appendOctets(byte[] bytes, Element e,
            ByteArrayOutputStream out, int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new IOException("ASN.1 nesting too deep");
        }
        if (e.tag == 0x04) {
            out.write(bytes, e.start, e.end - e.start);
        } else if (e.tag == 0x24) {
            Reader parts = e.children(bytes);
            while (parts.pos < parts.end) {
                appendOctets(bytes, parts.read(), out, depth + 1);
            }
        } else {
            throw new IOException("Expected OCTET STRING");
        }
    }

    private static final class Element {

        final int tag, start, end, next;

        Element(int tag, int start, int end, int next) {
            this.tag = tag;
            this.start = start;
            this.end = end;
            this.next = next;
        }

        Reader children(byte[] bytes) {
            return new Reader(bytes, start, end);
        }
    }

    private static final class Reader {

        final byte[] bytes;
        final int end;
        int pos;

        Reader(byte[] bytes, int start, int end) {
            this.bytes = bytes;
            this.pos = start;
            this.end = end;
        }

        Element read() throws IOException {
            Element e = element(bytes, pos, end, 0);
            pos = e.next;
            return e;
        }

        Element read(int tag) throws IOException {
            Element e = read();
            if (e.tag != tag) {
                throw new IOException("Unexpected ASN.1 tag: " + e.tag);
            }
            return e;
        }

        void requireEnd() throws IOException {
            if (pos != end) {
                throw new IOException("Unexpected trailing ASN.1 data");
            }
        }
    }

    private static Element element(byte[] bytes, int pos, int limit, int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new IOException("ASN.1 nesting too deep");
        }
        if (limit - pos < 2) {
            throw new IOException("Truncated ASN.1 header");
        }
        int tag = bytes[pos++] & 255;
        if (tag == 0 || (tag & 31) == 31) {
            throw new IOException("Unsupported ASN.1 tag");
        }
        int first = bytes[pos++] & 255;
        if (first == 0x80) {
            if ((tag & 0x20) == 0) {
                throw new IOException("Primitive indefinite-length ASN.1");
            }
            int start = pos;
            while (true) {
                if (limit - pos < 2) {
                    throw new IOException("Missing ASN.1 end-of-contents");
                }
                if (bytes[pos] == 0 && bytes[pos + 1] == 0) {
                    return new Element(tag, start, pos, pos + 2);
                }
                pos = element(bytes, pos, limit, depth + 1).next;
            }
        }
        long length = first;
        if ((first & 0x80) != 0) {
            int count = first & 127;
            if (count > 4 || count > limit - pos) {
                throw new IOException("Invalid ASN.1 length");
            }
            length = 0;
            for (int i = 0; i < count; i++) {
                length = (length << 8) | (bytes[pos++] & 255);
            }
        }
        if (length > limit - pos) {
            throw new IOException("ASN.1 element extends beyond its container");
        }
        int end = pos + (int) length;
        return new Element(tag, pos, end, end);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java ExtractSwz input.swz output.swf");
            System.exit(2);
        }
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        if (input.toAbsolutePath().normalize().equals(output.toAbsolutePath().normalize())
                || (Files.exists(output) && Files.isSameFile(input, output))) {
            throw new IOException("Input and output must be different files");
        }
        byte[] swf = extract(Files.readAllBytes(input));
        Files.write(output, swf);
        System.out.println("Extracted " + swf.length + " bytes to " + output);
    }
}
