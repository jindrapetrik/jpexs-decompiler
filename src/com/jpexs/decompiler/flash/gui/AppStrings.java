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
package com.jpexs.decompiler.flash.gui;

import java.util.ResourceBundle;

/**
 *
 * @author JPEXS
 */
public class AppStrings {

    private static Class resourceClass;

    private static ResourceBundle resourceBundle;

    public static void setResourceClass(Class cls) {
        resourceClass = cls;
        updateLanguage();
    }

    public static String getResourcePath(Class cls) {
        String name = cls.getName();
        if (name.startsWith("com.jpexs.decompiler.flash.gui.")) {
            name = name.substring("com.jpexs.decompiler.flash.gui.".length());
            name = "com.jpexs.decompiler.flash.gui.locales." + name;
        }
        return name;
    }

    public static String translate(String key) {
        return resourceBundle.getString(key);
    }

    public static String translate(String bundle, String key) {
        ResourceBundle b = ResourceBundle.getBundle(bundle);
        return b.getString(key);
    }

    public static void updateLanguage() {
        resourceBundle = ResourceBundle.getBundle(getResourcePath(resourceClass));
    }
}
