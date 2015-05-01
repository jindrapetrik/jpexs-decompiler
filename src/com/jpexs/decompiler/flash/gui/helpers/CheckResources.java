/*
 *  Copyright (C) 2010-2015 JPEXS
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
package com.jpexs.decompiler.flash.gui.helpers;

import com.jpexs.decompiler.flash.gui.AboutDialog;
import com.jpexs.decompiler.flash.gui.AdvancedSettingsDialog;
import com.jpexs.decompiler.flash.gui.DebugLogDialog;
import com.jpexs.decompiler.flash.gui.ErrorLogFrame;
import com.jpexs.decompiler.flash.gui.ExportDialog;
import com.jpexs.decompiler.flash.gui.FontEmbedDialog;
import com.jpexs.decompiler.flash.gui.FontPreviewDialog;
import com.jpexs.decompiler.flash.gui.GraphDialog;
import com.jpexs.decompiler.flash.gui.LoadFromCacheFrame;
import com.jpexs.decompiler.flash.gui.LoadFromMemoryFrame;
import com.jpexs.decompiler.flash.gui.LoadingDialog;
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.gui.ModeFrame;
import com.jpexs.decompiler.flash.gui.NewVersionDialog;
import com.jpexs.decompiler.flash.gui.RenameDialog;
import com.jpexs.decompiler.flash.gui.ReplaceCharacterDialog;
import com.jpexs.decompiler.flash.gui.ReplaceTraceDialog;
import com.jpexs.decompiler.flash.gui.SearchDialog;
import com.jpexs.decompiler.flash.gui.SearchResultsDialog;
import com.jpexs.decompiler.flash.gui.SelectLanguageDialog;
import com.jpexs.decompiler.flash.gui.abc.DeobfuscationDialog;
import com.jpexs.decompiler.flash.gui.abc.NewTraitDialog;
import com.jpexs.decompiler.flash.gui.abc.UsageFrame;
import com.jpexs.decompiler.flash.gui.proxy.ProxyFrame;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class CheckResources {

    public static void checkResources(PrintStream stream) {
        Class[] classes = new Class[]{
            AboutDialog.class,
            AdvancedSettingsDialog.class,
            DebugLogDialog.class,
            ErrorLogFrame.class,
            ExportDialog.class,
            FontEmbedDialog.class,
            FontPreviewDialog.class,
            GraphDialog.class,
            // GraphTreeFrame.class, // empty
            LoadFromCacheFrame.class,
            LoadFromMemoryFrame.class,
            LoadingDialog.class,
            MainFrame.class,
            ModeFrame.class,
            NewVersionDialog.class,
            RenameDialog.class,
            ReplaceCharacterDialog.class,
            ReplaceTraceDialog.class,
            SearchDialog.class,
            SearchResultsDialog.class,
            SelectLanguageDialog.class,
            // ABC
            DeobfuscationDialog.class,
            NewTraitDialog.class,
            UsageFrame.class,
            // Proxy
            ProxyFrame.class,};
        checkResources(classes, stream);
    }

    private static void checkResources(Class[] classes, PrintStream stream) {
        try {
            String[] languages = SelectLanguageDialog.getAvailableLanguages();
            Map<Class, Properties> properties = new HashMap<>();
            for (Class clazz : classes) {
                String resourcePath = getResourcePath(clazz, null);
                InputStream is = CheckResources.class.getResourceAsStream(resourcePath);
                if (is == null) {
                    stream.println("Resource file not found: " + resourcePath);
                    return;
                }
                Properties prop = new Properties();
                prop.load(is);
                properties.put(clazz, prop);
            }

            for (String lang : languages) {
                if (lang.equals("en")) {
                    continue;
                }

                boolean firstMissing = true;
                for (Class clazz : classes) {
                    Properties prop = properties.get(clazz);
                    Properties prop2 = new Properties();
                    String resourcePath = getResourcePath(clazz, lang);
                    InputStream is = CheckResources.class.getResourceAsStream(resourcePath);
                    if (is == null) {
                        stream.println(lang + ": Resource file not found: " + resourcePath);
                        continue;
                    }
                    try {
                        prop2.load(is);
                    } catch (Exception ex) {
                        Logger.getLogger(CheckResources.class.getName()).log(Level.SEVERE, "Cannot load resource:" + clazz.getSimpleName() + " " + lang, ex);
                    }

                    for (Object key : prop.keySet()) {
                        String keyStr = (String) key;
                        String value = prop2.getProperty(keyStr);
                        if (value == null) {
                            if (firstMissing) {
                                stream.println(lang);
                                stream.println("-----------------------------");
                                firstMissing = false;
                            }

                            stream.println(clazz.getSimpleName() + ", property: " + key + "=" + prop.getProperty(keyStr));
                        }
                    }
                }

                if (!firstMissing) {
                    stream.println();
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CheckResources.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CheckResources.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String getResourcePath(Class cls, String lang) {
        String name = cls.getName();
        if (name.startsWith("com.jpexs.decompiler.flash.gui.")) {
            name = name.substring("com.jpexs.decompiler.flash.gui.".length());
            name = "/com/jpexs/decompiler/flash/gui/locales/" + name.replace(".", "/");
            if (lang != null) {
                name += "_" + lang.replace("-", "_");
            }
            name += ".properties";
        }
        return name;
    }
}
