/*
 *  Copyright (C) 2010-2014 JPEXS, Miron Sadziak
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
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.helpers.Helper;
import com.jpexs.helpers.plugin.CharSequenceJavaFileObject;
import com.jpexs.helpers.plugin.ClassFileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 *
 * @author JPEXS
 */
public class SWFDecompilerPlugin {

    public static SWFDecompilerListener listener;

    public static void loadPlugin(String path) {

        // Here we specify the source code of the class to be compiled
        String src = Helper.readTextFile(path);

        // Full name of the class that will be compiled.
        // If class should be in some package,
        // fullName should contain it too
        // (ex. "testpackage.DynaClass")
        int idx = src.indexOf("public class ");
        String fullName = src.substring(idx + 13);
        fullName = fullName.substring(0, fullName.indexOf(' ')).trim();

        // We get an instance of JavaCompiler. Then
        // we create a file manager
        // (our custom implementation of it)
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));

        // Dynamic compiling requires specifying
        // a list of "files" to compile. In our case
        // this is a list containing one "file" which is in our case
        // our own implementation (see details below)
        List<JavaFileObject> jfiles = new ArrayList<>();
        jfiles.add(new CharSequenceJavaFileObject(fullName, src));

        // We specify a task to the compiler. Compiler should use our file
        // manager and our list of "files".
        // Then we run the compilation with call()
        compiler.getTask(null, fileManager, null, null, null, jfiles).call();

        // Creating an instance of our compiled class and
        try {
            listener = (SWFDecompilerListener) fileManager.getClassLoader(null).loadClass(fullName).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(SWFDecompilerPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
