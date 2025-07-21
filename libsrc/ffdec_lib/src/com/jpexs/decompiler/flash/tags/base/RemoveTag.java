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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.util.Map;

/**
 * Base class for remove object tags.
 *
 * @author JPEXS
 */
public abstract class RemoveTag extends Tag implements DepthTag {

    /**
     * Constructor.
     * @param swf SWF
     * @param id ID
     * @param name Name
     * @param data Data
     */
    public RemoveTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    @Override
    public Map<String, String> getNameProperties() {
        String exportName = swf.getExportName(getCharacterId());

        Map<String, String> ret = super.getNameProperties();
        if (getCharacterId() != -1) {
            ret.put("chid", "" + getCharacterId());
        }
        if (exportName != null) {
            ret.put("exp", Helper.escapeExportname(exportName, true));
        }
        ret.put("dpt", "" + getDepth());

        return ret;
    }

    @Override
    public String getExportFileName() {
        String result = super.getExportFileName();
        if (getCharacterId() != -1) {
            result += "_" + getCharacterId();
        }

        String exportName = swf.getExportName(getCharacterId());
        if (exportName != null) {
            result += "_" + exportName;
        }

        result += "_" + getDepth();
        return result;
    }

    private int getCharacterId() {
        if (this instanceof CharacterIdTag) {
            return ((CharacterIdTag) this).getCharacterId();
        }

        return -1;
    }
}
