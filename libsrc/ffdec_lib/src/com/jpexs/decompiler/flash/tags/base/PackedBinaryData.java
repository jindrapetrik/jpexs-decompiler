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
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Binary data packed with a packer.
 *
 * @author JPEXS
 */
public class PackedBinaryData implements TreeItem, BinaryDataInterface {

    private boolean modified = false;
    private final SWF swf;
    private final BinaryDataInterface parent;
    private ByteArrayRange data;
    private PackedBinaryData sub;
    private Packer usedPacker;
    private SWF innerSwf;
    private String packerKey;

    /**
     * Constructor.
     * @param swf SWF
     * @param parent Parent binary data
     * @param data Data
     */
    public PackedBinaryData(SWF swf, BinaryDataInterface parent, ByteArrayRange data) {
        this.swf = swf;
        this.parent = parent;
        this.data = data;
    }

    @Override
    public boolean unpack(Packer packer, String key) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (!packer.decrypt(new ByteArrayInputStream(data.getRangeData()), baos, key)) {
                return false;
            }
        } catch (IOException ex) {
            return false;
        }
        sub = new PackedBinaryData(swf, this, new ByteArrayRange(baos.toByteArray()));
        usedPacker = packer;
        packerKey = key;
        return true;
    }

    @Override
    public PackedBinaryData getSub() {
        return sub;
    }

    /**
     * Gets parent binary data.
     * @return Parent binary data
     */
    public BinaryDataInterface getParent() {
        return parent;
    }

    @Override
    public Openable getOpenable() {
        return swf;
    }

    @Override
    public SWF getSwf() {
        return swf;
    }

    @Override
    public void setModified(boolean value) {
        modified = value;
        if (value) {
            parent.setModified(value);
        } else {
            if (sub != null) {
                sub.setModified(false);
            }
        }
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public boolean isSwfData() {
        try {
            if (data.getLength() > 8) {
                String signature = new String(data.getRangeData(0, 3), Utf8Helper.charset);
                if (SWF.swfSignatures.contains(signature)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            //ignored
        }
        return false;
    }

    @Override
    public Packer getUsedPacker() {
        return usedPacker;
    }

    @Override
    public void detectPacker() {
        for (Packer packer : DefineBinaryDataTag.getAvailablePackers()) {
            if (packer.suitableForData(data.getRangeData()) == Boolean.TRUE) {
                usedPacker = packer;
                break;
            }
        }
    }

    @Override
    public ByteArrayRange getDataBytes() {
        return data;
    }

    @Override
    public void setDataBytes(ByteArrayRange data) {
        this.data = data;
        setModified(true);
    }

    @Override
    public boolean pack() {
        if (sub == null) {
            return false;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (!usedPacker.encrypt(new ByteArrayInputStream(sub.getDataBytes().getRangeData()), baos, packerKey)) {
                return false;
            }
        } catch (IOException ex) {
            return false;
        }
        setDataBytes(new ByteArrayRange(baos.toByteArray()));
        return true;
    }

    @Override
    public String toString() {
        return "(Data - " + parent.getUsedPacker().getName() + ")";
    }

    @Override
    public void setInnerSwf(SWF swf) {
        this.innerSwf = swf;
    }

    @Override
    public SWF getInnerSwf() {
        return this.innerSwf;
    }

    @Override
    public String getPathIdentifier() {
        return "(Data - " + parent.getUsedPacker().getName() + ")";
    }

    @Override
    public String getStoragesPathIdentifier() {
        return "data-" + parent.getUsedPacker().getIdentifier();
    }

    @Override
    public BinaryDataInterface getTopLevelBinaryData() {
        PackedBinaryData packed = this;
        while (packed.parent instanceof PackedBinaryData) {
            packed = (PackedBinaryData) packed.parent;
        }
        return packed.parent;
    }

    @Override
    public String getExportFileName() {
        List<String> parts = new ArrayList<>();
        BinaryDataInterface binaryData = this;
        while (binaryData instanceof PackedBinaryData) {
            parts.add(0, ((PackedBinaryData) binaryData).parent.getUsedPacker().getIdentifier());
            binaryData = ((PackedBinaryData) binaryData).parent;
        }

        parts.add(0, binaryData.getExportFileName());
        return String.join("_", parts);
    }

    @Override
    public String getCharacterExportFileName() {
        List<String> parts = new ArrayList<>();
        BinaryDataInterface binaryData = this;
        while (binaryData instanceof PackedBinaryData) {
            parts.add(0, ((PackedBinaryData) binaryData).parent.getUsedPacker().getIdentifier());
            binaryData = ((PackedBinaryData) binaryData).parent;
        }

        parts.add(0, binaryData.getCharacterExportFileName());
        return String.join("_", parts);
    }

    @Override
    public String getName() {
        List<String> parts = new ArrayList<>();
        BinaryDataInterface binaryData = this;
        while (binaryData instanceof PackedBinaryData) {
            parts.add(0, ((PackedBinaryData) binaryData).parent.getUsedPacker().getName());
            binaryData = ((PackedBinaryData) binaryData).parent;
        }

        parts.add(0, binaryData.getName());
        return String.join(" / ", parts);
    }

    @Override
    public String getClassExportFileName(String className) {
        List<String> parts = new ArrayList<>();
        BinaryDataInterface binaryData = this;
        while (binaryData instanceof PackedBinaryData) {
            parts.add(0, binaryData.getStoragesPathIdentifier());
            binaryData = ((PackedBinaryData) binaryData).parent;
        }

        parts.add(0, binaryData.getClassExportFileName(className));
        return String.join("_", parts);
    }
    
    @Override
    public String getPackerKey() {
        return packerKey;
    }
}
