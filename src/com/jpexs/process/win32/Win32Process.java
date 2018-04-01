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
package com.jpexs.process.win32;

import com.jpexs.helpers.ProgressListener;
import com.jpexs.process.Process;
import com.sun.jna.platform.win32.WinDef.DWORD;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class Win32Process implements Process {

    public String filePath;

    public String fileName;

    public BufferedImage icon;

    public DWORD th32ProcessID;

    @Override
    public long getPid() {
        return th32ProcessID.longValue();
    }

    public Win32Process(String filePath, String fileName, BufferedImage icon, DWORD th32ProcessID) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.icon = icon;
        this.th32ProcessID = th32ProcessID;
    }

    @Override
    public String toString() {
        return new File(filePath).getName() + " (pid " + th32ProcessID.longValue() + ")";
    }

    @Override
    public int compareTo(Process o) {
        if (!(o instanceof Win32Process)) {
            return -1;
        }
        Win32Process p = (Win32Process) o;
        int ret = fileName.toLowerCase().compareTo(p.fileName.toLowerCase());
        if (ret == 0) {
            ret = th32ProcessID.intValue() - p.th32ProcessID.intValue();
        }
        return ret;
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public BufferedImage getIcon() {
        return icon;
    }

    @Override
    public Map<Long, InputStream> search(byte[]... data) {
        return search(null, data);
    }

    @Override
    public Map<Long, InputStream> search(ProgressListener progListener, byte[]... data) {
        return Win32ProcessTools.findBytesInProcessMemory(progListener, th32ProcessID, data);
    }
}
