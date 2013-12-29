/*
 *  Copyright (C) 2010-2013 JPEXS
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

import com.sun.jna.Platform;
import java.awt.GraphicsEnvironment;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class FontHelper {

    public static String[] getInstalledFontFamilyNames() {
        if (Platform.isWindows()) {
            try {
                Class<?> clW32Fm = Class.forName("sun.awt.Win32FontManager");
                Class<?> clSunFm = Class.forName("sun.font.SunFontManager");
                Object fm = clW32Fm.newInstance();
                return (String[]) clSunFm.getDeclaredMethod("getInstalledFontFamilyNames", Locale.class).invoke(fm, Locale.getDefault());
            } catch (Throwable ex) {
                // catch everything to avoid class not found problems, because Win32FontManager is an internal proprietary API
                Logger.getLogger(FontHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }
}
