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
package com.jpexs.decompiler.flash.tags.base;

import java.io.IOException;

/**
 * Has separate alpha channel interface.
 * @author JPEXS
 */
public interface HasSeparateAlphaChannel {

    /**
     * Checks if this has an alpha channel.
     * @return True if this has an alpha channel, false otherwise.
     */
    public boolean hasAlphaChannel();

    /**
     * Gets the image alpha channel.
     * @return Image alpha channel
     * @throws IOException On I/O error
     */
    public byte[] getImageAlpha() throws IOException;

    /**
     * Sets the image alpha channel.
     * @param data Image alpha channel
     * @throws IOException On I/O error
     */
    public void setImageAlpha(byte[] data) throws IOException;
}
