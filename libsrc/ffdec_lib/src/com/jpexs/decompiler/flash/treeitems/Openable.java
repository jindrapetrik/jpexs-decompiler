/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.treeitems;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An item that can be loaded(opened) in FFDec. For example SWF or ABC.
 *
 * @author JPEXS
 */
public interface Openable extends TreeItem {

    /**
     * Gets file title.
     *
     * @return File title
     */
    public String getFileTitle();

    /**
     * Gets title with path from root (like in nested SWFs)
     *
     * @return Short path title
     */
    public String getShortPathTitle();

    /**
     * Gets short file name based on file title and file.
     *
     * @return Short filename
     */
    public String getShortFileName();

    /**
     * Gets opened file.
     *
     * @return File or null
     */
    public String getFile();

    /**
     * Gets title or short filename.
     * @return File title or short filename
     */
    public String getTitleOrShortFileName();

    /**
     * Gets title of the file or short filename.
     *
     * @return file title or base file name when file title is null or "_" when
     * file is null too
     */
    public String getFullPathTitle();

    /**
     * Sets OpenableList which this Openable resides.
     *
     * @param openableList OpenableList
     */
    public void setOpenableList(OpenableList openableList);

    /**
     * Gets OpenableList which this Openable resides.
     *
     * @return OpenableList
     */
    public OpenableList getOpenableList();

    /**
     * Saves Openable to stream.
     *
     * @param os Output stream
     * @throws IOException On I/O error
     */
    public void saveTo(OutputStream os) throws IOException;

    /**
     * Sets file.
     *
     * @param file File
     */
    public void setFile(String file);

    /**
     * Clears modified flag from this item and all subitems.
     */
    public void clearModified();
}
