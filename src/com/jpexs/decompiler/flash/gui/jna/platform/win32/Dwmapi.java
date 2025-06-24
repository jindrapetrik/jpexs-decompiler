/*
 *  Copyright (C) 2025 JPEXS
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
package com.jpexs.decompiler.flash.gui.jna.platform.win32;

/**
 *
 * @author JPEXS
 */
import com.jpexs.decompiler.flash.gui.jna.platform.win32.WinDef.HWND;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.win32.W32APIOptions;
import java.util.Arrays;
import java.util.List;

public interface Dwmapi extends Library {
        
    Dwmapi INSTANCE = (Dwmapi) Native.loadLibrary("dwmapi", Dwmapi.class, W32APIOptions.DEFAULT_OPTIONS);    

    class MARGINS extends Structure {

        public int cxLeftWidth;
        public int cxRightWidth;
        public int cyTopHeight;
        public int cyBottomHeight;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("cxLeftWidth", "cxRightWidth", "cyTopHeight", "cyBottomHeight");
        }
    }

    int DwmExtendFrameIntoClientArea(HWND hWnd, MARGINS pMarInset);
    WinNT.HRESULT DwmIsCompositionEnabled(WinNT.BOOLbyReference pfEnabled);
}
