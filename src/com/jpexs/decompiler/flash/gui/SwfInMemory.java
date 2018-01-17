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

import com.jpexs.helpers.ReReadableInputStream;
import java.util.ResourceBundle;

/**
 *
 * @author JPEXS
 */
public class SwfInMemory {

    public ReReadableInputStream is;

    public int version;

    public long address;

    public long fileSize;

    public com.jpexs.process.Process process;

    public SwfInMemory(ReReadableInputStream is, long address, int version, long fileSize, com.jpexs.process.Process process) {
        this.is = is;
        this.address = address;
        this.version = version;
        this.fileSize = fileSize;
        this.process = process;
    }

    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle(AppStrings.getResourcePath(LoadFromMemoryFrame.class));

    public String translate(String key) {
        return resourceBundle.getString(key);
    }

    @Override
    public String toString() {
        String p = translate("swfitem").replace("%version%", Integer.toString(version)).replace("%size%", Long.toString(fileSize));
        return p;
    }
}
