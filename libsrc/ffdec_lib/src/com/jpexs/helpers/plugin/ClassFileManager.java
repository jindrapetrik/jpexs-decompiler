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

import java.io.IOException;
import java.security.SecureClassLoader;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

/**
 *
 * @author JPEXS
 */
public class ClassFileManager extends
        ForwardingJavaFileManager<JavaFileManager> {

    /**
     * Instance of JavaClassObject that will store the compiled bytecode of our
     * class
     */
    private JavaClassObject jclassObject;

    /**
     * Will initialize the manager with the specified standard java file manager
     *
     * @param standardManager
     */
    public ClassFileManager(StandardJavaFileManager standardManager) {
        super(standardManager);
    }

    /**
     * Will be used by us to get the class loader for our compiled class. It
     * creates an anonymous class extending the SecureClassLoader which uses the
     * byte code created by the compiler and stored in the JavaClassObject, and
     * returns the Class for it
     *
     * @param location
     * @return
     */
    @Override
    public ClassLoader getClassLoader(Location location) {
        return new SecureClassLoader() {
            @Override
            protected Class<?> findClass(String name)
                    throws ClassNotFoundException {
                byte[] b = jclassObject.getBytes();
                return super.defineClass(name, jclassObject
                        .getBytes(), 0, b.length);
            }
        };
    }

    /**
     * Gives the compiler an instance of the JavaClassObject so that the
     * compiler can write the byte code into it.
     *
     * @param location
     * @param className
     * @param kind
     * @param sibling
     * @return
     * @throws java.io.IOException
     */
    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
            String className, Kind kind, FileObject sibling)
            throws IOException {
        jclassObject = new JavaClassObject(className, kind);
        return jclassObject;
    }
}
