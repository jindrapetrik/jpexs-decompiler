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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.Map;

/**
 * FrameLabel tag - defines a label for a frame.
 *
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class FrameLabelTag extends Tag {

    public static final int ID = 43;

    public static final String NAME = "FrameLabel";

    public String name;

    @Conditional(minSwfVersion = 6)
    public boolean namedAnchor = false;

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public FrameLabelTag(SWF swf) {
        super(swf, ID, NAME, null);
        name = "New frame label";
    }

    public FrameLabelTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        name = sis.readString("name");
        if (sis.available() > 0) {
            if (sis.readUI8("namedAnchor") == 1) {
                namedAnchor = true;
            }
        }
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeString(name);
        if (namedAnchor) {
            sos.writeUI8(1);
        }
    }

    public String getLabelName() {
        return name;
    }

    public boolean isNamedAnchor() {
        return namedAnchor;
    }

    @Override
    public Map<String, String> getNameProperties() {
        Map<String, String> ret = super.getNameProperties();
        if (!name.isEmpty()) {
            ret.put("name", name);
        }
        return ret;
    }

}
