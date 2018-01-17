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
package com.jpexs.process;

import com.jpexs.process.win32.Win32ProcessTools;
import com.sun.jna.Platform;
import java.util.List;

/**
 * Tools for processes. You can add support for other platforms here, if you
 * want
 *
 * @author JPEXS
 */
public class ProcessTools {

    public static List<Process> listProcesses() {
        if (Platform.isWindows()) {
            return Win32ProcessTools.listProcesses();
        }
        return null;
    }

    public static boolean toolsAvailable() {
        return Platform.isWindows();
    }
}
