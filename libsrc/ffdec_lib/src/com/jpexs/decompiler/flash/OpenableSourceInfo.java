/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.iggy.conversion.IggySwfBundle;
import com.jpexs.helpers.Path;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class OpenableSourceInfo {

    private final InputStream inputStream;

    private String file;

    private String fileTitle;

    private final boolean detectBundle;

    private boolean empty = false;

    private OpenableSourceKind kind;

    public OpenableSourceInfo(String fileTitle) {
        this(null, null, fileTitle, false);
        empty = true;
    }

    public boolean isEmpty() {
        return empty;
    }

    public OpenableSourceInfo(InputStream inputStream, String file, String fileTitle) {
        this(inputStream, file, fileTitle, true);
    }

    public OpenableSourceInfo(InputStream inputStream, String file, String fileTitle, boolean detectBundle) {
        this.inputStream = inputStream;
        this.file = file;
        this.fileTitle = fileTitle;
        this.detectBundle = detectBundle;
        detectKind();
    }

    public OpenableSourceKind getKind() {
        return kind;
    }

    private void detectKind() {
        if (isBundle()) {
            kind = OpenableSourceKind.BUNDLE;
        } else if (this.file != null && this.file.endsWith(".abc")) {
            kind = OpenableSourceKind.ABC;
        } else {
            kind = OpenableSourceKind.SWF;
        }
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
        detectKind();
        empty = false;
    }

    public void setFileTitle(String fileTitle) {
        this.fileTitle = fileTitle;
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
        if (inputStream == null && file != null) {
            File fileObj = new File(file);
            String fileName = fileObj.getName();
            if (fileName.startsWith("asdec_") && fileName.endsWith(".tmp")) {
                return false;
            }
            String extension = Path.getExtension(fileObj);
            return (detectBundle) && (extension == null || !(extension.equals(".swf") || extension.equals(".gfx") || extension.equals(".abc")));
        }
        return false;
    }

    public Bundle getBundle(boolean noCheck, SearchMode searchMode) throws IOException {
        if (!isBundle()) {
            return null;
        }

        String extension = Path.getExtension(new File(file));
        if (extension != null) {
            switch (extension) {
                case ".swc":
                    return new SWC(new File(file));
                case ".zip":
                    return new ZippedBundle(new File(file));
                case ".iggy":
                    return new IggySwfBundle(new File(file));
            }
        }

        return new BinarySWFBundle(new BufferedInputStream(new FileInputStream(file)), noCheck, searchMode);
    }
}
