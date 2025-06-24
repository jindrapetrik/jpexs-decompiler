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
import com.jpexs.decompiler.flash.packers.Packer;
import com.jpexs.helpers.ByteArrayRange;

/**
 * Interface for binary data objects. It is a DefineBinaryData tag and its
 * subdata.
 *
 * @author JPEXS
 */
public interface BinaryDataInterface extends Exportable {

    /**
     * Gets sub binary data.
     * @return Sub binary data
     */
    public PackedBinaryData getSub();

    /**
     * Checks if the data is SWF data.
     * @return True if the data is SWF data
     */
    public boolean isSwfData();

    /**
     * Unpacks the data.
     * @param packer Packer
     * @param key Key
     * @return True if the data was unpacked
     */
    public boolean unpack(Packer packer, String key);

    /**
     * Detects the packer.
     */
    public void detectPacker();

    /**
     * Gets the used packer.
     * @return Used packer
     */
    public Packer getUsedPacker();
    
    /**
     * Gets the used packer key.
     * @return Packer key
     */
    public String getPackerKey();

    /**
     * Sets the data bytes.
     * @param data Data bytes
     */
    public void setDataBytes(ByteArrayRange data);

    /**
     * Gets the data bytes.
     * @return Data bytes
     */
    public ByteArrayRange getDataBytes();

    /**
     * Sets the modified flag.
     * @param value Modified flag
     */
    public void setModified(boolean value);

    /**
     * Packs the data.
     * @return True if the data was packed
     */
    public boolean pack();

    /**
     * Gets the SWF.
     * @return SWF
     */
    public SWF getSwf();

    /**
     * Sets inner SWF.
     * @param swf SWF
     */
    public void setInnerSwf(SWF swf);

    /**
     * Gets inner SWF.
     * @return Inner SWF
     */
    public SWF getInnerSwf();

    /**
     * Gets the path identifier.
     * @return Path identifier
     */
    public String getPathIdentifier();

    /**
     * Gets the storages path identifier.
     * @return Storages path identifier
     */
    public String getStoragesPathIdentifier();

    /**
     * Gets the top level binary data.
     * @return Top level binary data
     */
    public BinaryDataInterface getTopLevelBinaryData();

    /**
     * Gets the character export file name.
     * @return Character export file name
     */
    public String getCharacterExportFileName();

    /**
     * Gets the name.
     * @return Name
     */
    public String getName();

    /**
     * Gets the class export file name.
     * @param className Class name
     * @return Class export file
     */
    public String getClassExportFileName(String className);

}
