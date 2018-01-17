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
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.plugin.CharSequenceJavaFileObject;
import com.jpexs.helpers.plugin.ClassFileManager;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
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

    private static final Logger logger = Logger.getLogger(SWFDecompilerPlugin.class.getName());

    private static final List<SWFDecompilerListener> listeners = new ArrayList<>();

    public static String[] customParameters = new String[0];

    public static File getPluginsDir() {
        File pluginPath = null;

        try {
            String pluginPathConfig = Configuration.pluginPath.get();
            if (pluginPathConfig != null && !pluginPathConfig.isEmpty()) {
                pluginPath = new File(pluginPathConfig);
            }

            if (pluginPath == null || !pluginPath.exists()) {
                File f = new File(SWFDecompilerPlugin.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                File dir = f.getAbsoluteFile().getParentFile().getParentFile();
                pluginPath = new File(Path.combine(dir.getPath(), "plugins")).getCanonicalFile();
            }
        } catch (IOException | URISyntaxException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return pluginPath;
    }

    public static void loadPlugins() {
        File pluginPath = getPluginsDir();
        if (pluginPath != null && pluginPath.exists()) {
            System.out.println("Loading plugins from " + pluginPath.getPath());
            File[] files = pluginPath.listFiles();
            if (files != null) {
                for (File file : files) {
                    System.out.println("Loading plugin: " + file.getPath());
                    loadPlugin(file.getPath());
                }
            }
        }

    }

    public static void loadPlugin(String path) {
        if (".class".equals(Path.getExtension(path))) {
            loadPluginCompiled(path);
        } else {
            loadPluginSource(path);
        }
    }

    private static void loadPluginCompiled(String path) {
        File pluginFile = new File(path);
        File file = pluginFile.getParentFile();

        try {
            // Convert File to a URL
            URL url = file.toURI().toURL();
            URL[] urls = new URL[]{url};

            // Create a new class loader with the directory
            ClassLoader cl = new URLClassLoader(urls);

            String pluginName = Path.getFileNameWithoutExtension(pluginFile);
            Class<?> cls = cl.loadClass(pluginName);
            if (SWFDecompilerListener.class.isAssignableFrom(cls)) {
                SWFDecompilerListener listener = (SWFDecompilerListener) cls.newInstance();
                listeners.add(listener);
            }

            System.out.println("Plugin loaded: " + pluginName);
        } catch (MalformedURLException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private static void loadPluginSource(String path) {

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
        if (compiler == null) {
            logger.log(Level.SEVERE, "Compiler is null");
            return;
        }

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
            listeners.add((SWFDecompilerListener) fileManager.getClassLoader(null).loadClass(fullName).newInstance());
            System.out.println("Plugin loaded: " + fullName);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public static byte[] fireProxyFileCatched(byte[] data) {
        byte[] result = null;
        for (SWFDecompilerListener listener : listeners) {
            try {
                byte[] newResult = listener.proxyFileCatched(data);
                if (newResult != null) {
                    result = newResult;
                    data = newResult;
                }
            } catch (ThreadDeath ex) {
                throw ex;
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Failed to call plugin method proxyFileCatched.", e);
            }
        }
        return result;
    }

    public static boolean fireSwfParsed(SWF swf) {
        for (SWFDecompilerListener listener : listeners) {
            try {
                listener.swfParsed(swf);
            } catch (ThreadDeath ex) {
                throw ex;
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Failed to call plugin method swfParsed.", e);
            }
        }
        return !listeners.isEmpty();
    }

    public static boolean fireActionListParsed(ActionList actions, SWF swf) throws InterruptedException {
        for (SWFDecompilerListener listener : listeners) {
            try {
                listener.actionListParsed(actions, swf);
            } catch (ThreadDeath | InterruptedException ex) {
                throw ex;
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Failed to call plugin method actionListParsed.", e);
            }
        }
        return !listeners.isEmpty();
    }

    public static boolean fireActionTreeCreated(List<GraphTargetItem> tree, SWF swf) throws InterruptedException {
        for (SWFDecompilerListener listener : listeners) {
            try {
                listener.actionTreeCreated(tree, swf);
            } catch (ThreadDeath | InterruptedException ex) {
                throw ex;
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Failed to call plugin method actionTreeCreated.", e);
            }
        }
        return !listeners.isEmpty();
    }

    public static boolean fireAbcParsed(ABC abc, SWF swf) {
        for (SWFDecompilerListener listener : listeners) {
            try {
                listener.abcParsed(abc, swf);
            } catch (ThreadDeath ex) {
                throw ex;
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Failed to call plugin method abcParsed.", e);
            }
        }
        return !listeners.isEmpty();
    }

    public static boolean fireMethodBodyParsed(ABC abc, MethodBody body, SWF swf) {
        for (SWFDecompilerListener listener : listeners) {
            try {
                listener.methodBodyParsed(abc, body, swf);
            } catch (ThreadDeath ex) {
                throw ex;
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Failed to call plugin method methodBodyParsed.", e);
            }
        }
        return !listeners.isEmpty();
    }

    public static boolean fireAvm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
        for (SWFDecompilerListener listener : listeners) {
            try {
                listener.avm2CodeRemoveTraps(path, classIndex, isStatic, scriptIndex, abc, trait, methodInfo, body);
            } catch (ThreadDeath | InterruptedException ex) {
                throw ex;
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Failed to call plugin method abcParsed.", e);
            }
        }
        return !listeners.isEmpty();
    }
}
