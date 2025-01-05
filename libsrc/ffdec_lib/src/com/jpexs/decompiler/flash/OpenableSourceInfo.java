/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
 * Information about openable source.
 *
 * @author JPEXS
 */
public class OpenableSourceInfo {

    /**
     * Input stream of the source
     */
    private final InputStream inputStream;

    /**
     * File path of the source
     */
    private String file;

    /**
     * Title of the file
     */
    private String fileTitle;

    /**
     * Whether to auto-detect bundle
     */
    private final boolean detectBundle;

    /**
     * Whether the source is empty
     */
    private boolean empty = false;

    /**
     * Kind of the source
     */
    private OpenableSourceKind kind;

    /**
     * Constructs OpenableSourceInfo with empty source
     *
     * @param fileTitle Title of the file
     */
    public OpenableSourceInfo(String fileTitle) {
        this(null, null, fileTitle, false);
        empty = true;
    }

    /**
     * Check if the source is empty
     *
     * @return true if the source is empty
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Constructs OpenableSourceInfo with input stream
     *
     * @param inputStream Input stream of the source
     * @param file File path of the source
     * @param fileTitle Title of the file
     */
    public OpenableSourceInfo(InputStream inputStream, String file, String fileTitle) {
        this(inputStream, file, fileTitle, true);
    }

    /**
     * Constructs OpenableSourceInfo with input stream
     *
     * @param inputStream Input stream of the source
     * @param file File path of the source
     * @param fileTitle Title of the file
     * @param detectBundle Whether to auto-detect bundle
     */
    public OpenableSourceInfo(InputStream inputStream, String file, String fileTitle, boolean detectBundle) {
        this.inputStream = inputStream;
        this.file = file;
        this.fileTitle = fileTitle;
        this.detectBundle = detectBundle;
        detectKind();
    }

    /**
     * Gets kind of the source.
     *
     * @return Kind of the source
     */
    public OpenableSourceKind getKind() {
        return kind;
    }

    /**
     * Detects kind of the source.
     */
    private void detectKind() {
        if (isBundle()) {
            kind = OpenableSourceKind.BUNDLE;
        } else if (this.file != null && this.file.endsWith(".abc")) {
            kind = OpenableSourceKind.ABC;
        } else {
            kind = OpenableSourceKind.SWF;
        }
    }

    /**
     * Gets input stream of the source.
     *
     * @return Input stream of the source
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Gets file path of the source.
     *
     * @return File path of the source
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets file path of the source.
     *
     * @param file File path of the source
     */
    public void setFile(String file) {
        this.file = file;
        detectKind();
        empty = false;
    }

    /**
     * Sets title of the file.
     *
     * @param fileTitle File title
     */
    public void setFileTitle(String fileTitle) {
        this.fileTitle = fileTitle;
    }

    /**
     * Gets title of the file.
     *
     * @return File title
     */
    public String getFileTitle() {
        return fileTitle;
    }

    /**
     * Gets title of the file.
     *
     * @return File title
     */
    public String getFileTitleOrName() {
        if (fileTitle != null) {
            return fileTitle;
        }
        return file;
    }

    /**
     * Checks if the source is a bundle.
     *
     * @return True if the source is a bundle
     */
    public boolean isBundle() {
        if (inputStream == null && file != null) {
            File fileObj = new File(file);
            String fileName = fileObj.getName();
            if (fileName.startsWith("asdec_") && fileName.endsWith(".tmp")) { //FIXME: is this still needed?
                return false;
            }
            String extension = Path.getExtension(fileObj);
            return detectBundle 
                    && (
                        extension == null 
                        || !(
                            extension.equals(".swf")
                            || extension.equals(".spl")
                            || extension.equals(".gfx") 
                            || extension.equals(".abc")
                            )
                    );
        }
        return false;
    }

    /**
     * Gets bundle from the source.
     *
     * @param noCheck Whether to check the bundle
     * @param searchMode Search mode
     * @return Bundle or null
     * @throws IOException On I/O error
     */
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
