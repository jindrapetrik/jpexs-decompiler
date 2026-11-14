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
package com.jpexs.decompiler.flash.ftr;

import com.jpexs.decompiler.flash.Bundle;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.streams.SeekableInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A DirectUI FTR resource exposed as a single editable SWF.
 */
public final class FtrSwfBundle implements Bundle {

    private final File file;
    private final String key;
    private byte[] originalFtr;
    private byte[] originalSwf;

    public FtrSwfBundle(File file) {
        this.file = file;
        key = Path.getFileNameWithoutExtension(file) + ".swf";
    }

    public static SeekableInputStream openSwf(InputStream input) throws IOException {
        return new MemoryInputStream(convertToSwf(input));
    }

    private static byte[] convertToSwf(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FtrConverter.read(input).saveTo(output);
        return output.toByteArray();
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public Set<String> getKeys() {
        return Collections.singleton(key);
    }

    @Override
    public synchronized SeekableInputStream getOpenable(String key) throws IOException {
        checkKey(key);
        if (originalSwf == null) {
            byte[] data = Files.readAllBytes(file.toPath());
            originalSwf = convertToSwf(new ByteArrayInputStream(data));
            originalFtr = data;
        }
        return new MemoryInputStream(originalSwf);
    }

    @Override
    public Map<String, SeekableInputStream> getAll() throws IOException {
        return Collections.singletonMap(key, getOpenable(key));
    }

    @Override
    public String getExtension() {
        return "ftr";
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public synchronized boolean putOpenable(String key, InputStream input) throws IOException {
        checkKey(key);
        byte[] swf = Helper.readStream(input);
        byte[] ftr = encode(swf);
        replaceFile(file, ftr);
        originalSwf = swf;
        originalFtr = ftr;
        return true;
    }

    public synchronized void saveCopy(SWF swf, File destination) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        swf.saveTo(output);
        replaceFile(destination, encode(output.toByteArray()));
    }

    public static void save(SWF swf, File destination) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        FtrConverter.write(swf, output);
        replaceFile(destination, output.toByteArray());
    }

    private byte[] encode(byte[] swf) throws IOException {
        if (Arrays.equals(swf, originalSwf)) {
            return originalFtr;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            FtrConverter.write(new SWF(new ByteArrayInputStream(swf), false, "UTF-8"), output);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while converting SWF to FTR", ex);
        }
        return output.toByteArray();
    }

    private static void replaceFile(File file, byte[] data) throws IOException {
        java.nio.file.Path target = file.toPath().toAbsolutePath();
        java.nio.file.Path temporary = Files.createTempFile(target.getParent(), ".ffdec-ftr-", ".tmp");
        try {
            try (OutputStream stream = Files.newOutputStream(temporary)) {
                stream.write(data);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void checkKey(String key) throws IOException {
        if (!this.key.equals(key)) {
            throw new IOException("Unknown FTR movie " + key);
        }
    }
}

