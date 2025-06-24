/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.amf.amf3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Interface for serialization objects of a given type.
 */
public interface ObjectTypeSerializeHandler {

    /**
     * Reads an object from the input stream.
     *
     * @param className Class name of the object.
     * @param is Input stream.
     * @return Map of object members.
     * @throws IOException On I/O error
     */
    public Map<String, Object> readObject(String className, InputStream is) throws IOException;

    /**
     * Writes an object to the output stream.
     *
     * @param members Map of object members.
     * @param os Output stream.
     * @throws IOException On I/O error
     */
    public void writeObject(Map<String, Object> members, OutputStream os) throws IOException;
}
