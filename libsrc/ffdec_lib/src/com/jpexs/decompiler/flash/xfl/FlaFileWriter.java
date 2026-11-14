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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.helpers.CancellableWorker;
import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Keeps the destination intact until a complete FLA file is ready.
 */
final class FlaFileWriter implements AutoCloseable {

    private final Path destination;

    private final Path temporary;

    FlaFileWriter(File destination) throws IOException {
        this.destination = destination.toPath().toAbsolutePath();
        temporary = Files.createTempFile(this.destination.getParent(), ".ffdec-fla-", ".tmp");
    }

    File getFile() {
        return temporary.toFile();
    }

    void commit() throws IOException, InterruptedException {
        if (CancellableWorker.isInterrupted()) {
            throw new InterruptedException();
        }
        try {
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public void close() throws IOException {
        Files.deleteIfExists(temporary);
    }
}
