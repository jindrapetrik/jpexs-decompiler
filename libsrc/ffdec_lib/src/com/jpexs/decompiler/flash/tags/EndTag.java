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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;

/**
 * End tag - Marks the end of the file or sprite.
 *
 * @author JPEXS
 */
@SWFVersion(from = 1)
public class EndTag extends Tag {

    public static final int ID = 0;

    public static final String NAME = "End";

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public EndTag(SWF swf) {
        super(swf, ID, NAME, null);
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public EndTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     */
    @Override
    public void getData(SWFOutputStream sos) {
    }
}
