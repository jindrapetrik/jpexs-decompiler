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
package com.jpexs.decompiler.flash;

import com.jpexs.helpers.Helper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class SWFSourceInfo {

    private final InputStream inputStream;
    private final String file;
    private final String fileTitle;

    public SWFSourceInfo(InputStream inputStream, String file, String fileTitle) {
        this.inputStream = inputStream;
        this.file = file;
        this.fileTitle = fileTitle;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getFile() {
        return file;
    }

    public String getFileTitle() {
        return fileTitle;
    }

    /**
     * Get title of the file
     *
     * @return file title
     */
    public String getFileTitleOrName() {
        if (fileTitle != null) {
            return fileTitle;
        }
        return file;
    }

    public boolean isBundle() {
        if (inputStream == null) {
            String extension = Helper.getExtension(new File(file));
            return !(extension.equals(".swf") || extension.equals(".gfx"));
        }
        return false;
    }

    public SWFBundle getBundle() throws IOException {
        if (!isBundle()) {
            return null;
        }

        String extension = Helper.getExtension(new File(file));
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        switch (extension) {
            case ".swc":
                return new SWC(is);
            case ".zip":
                return new ZippedSWFBundle(is);
            default:
                return new BinarySWFBundle(is);
        }
    }
}
