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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.packers.Packer;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.helpers.ByteArrayRange;

/**
 *
 * @author JPEXS
 */
public interface BinaryDataInterface extends Exportable {

    public PackedBinaryData getSub();

    public boolean isSwfData();

    public boolean unpack(Packer packer);

    public void detectPacker();

    public Packer getUsedPacker();       

    public void setDataBytes(ByteArrayRange data);

    public ByteArrayRange getDataBytes();

    public void setModified(boolean value);

    public boolean pack();

    public SWF getSwf();

    public void setInnerSwf(SWF swf);

    public SWF getInnerSwf();

    public String getPathIdentifier();

    public String getStoragesPathIdentifier();

    public BinaryDataInterface getTopLevelBinaryData();

    public String getCharacterExportFileName();

    public String getName();

    public String getClassExportFileName(String className);

}
