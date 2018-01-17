/*
 *  Copyright (C) 2010-2018 JPEXS, Miron Sadziak, All rights reserved.
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
 * License along with this library. */
package com.jpexs.helpers.plugin;

import java.net.URI;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;

/**
 *
 * @author JPEXS
 */
public class CharSequenceJavaFileObject extends SimpleJavaFileObject {

    /**
     * CharSequence representing the source code to be compiled
     */
    private final CharSequence content;

    /**
     * This constructor will store the source code in the internal "content"
     * variable and register it as a source code, using a URI containing the
     * class full name
     *
     * @param className name of the public class in the source code
     * @param content source code to compile
     */
    public CharSequenceJavaFileObject(String className, CharSequence content) {
        super(URI.create("string:///" + className.replace('.', '/')
                + Kind.SOURCE.extension), Kind.SOURCE);
        this.content = content;
    }

    /**
     * Answers the CharSequence to be compiled. It will give the source code
     * stored in variable "content"
     *
     * @param ignoreEncodingErrors
     * @return
     */
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }
}
