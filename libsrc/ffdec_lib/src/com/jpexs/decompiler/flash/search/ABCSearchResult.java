/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.search;

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ABCSearchResult implements Serializable, ScriptSearchResult {

    public static String STR_INSTANCE_INITIALIZER = AppResources.translate("trait.instanceinitializer");

    public static String STR_CLASS_INITIALIZER = AppResources.translate("trait.classinitializer");

    public static String STR_SCRIPT_INITIALIZER = AppResources.translate("trait.scriptinitializer");

    private ScriptPack scriptPack;

    private final boolean pcode;

    private final int classIndex;

    private final int traitId;

    private static final int SERIAL_VERSION_MAJOR = 1;
    private static final int SERIAL_VERSION_MINOR = 0;

    @SuppressWarnings("unchecked")
    public ABCSearchResult(SWF swf, InputStream is) throws IOException, ScriptNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(is);
        int versionMajor = ois.read();
        ois.read(); //minor
        if (versionMajor != SERIAL_VERSION_MAJOR) {
            throw new IOException("Unknown search result version: " + versionMajor);
        }

        ClassPath cp;
        List<Integer> traitIndices;
        try {
            cp = (ClassPath) ois.readObject();
            traitIndices = (List<Integer>) ois.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ABCSearchResult.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException();
        }

        this.pcode = ois.readBoolean();
        this.classIndex = ois.readInt();
        this.traitId = ois.readInt();
        boolean packFound = false;
        for (ScriptPack pack : swf.getAS3Packs()) {
            if (cp.equals(pack.getClassPath()) && traitIndices.equals(pack.traitIndices)) {
                this.scriptPack = pack;
                packFound = true;
                break;
            }
        }
        if (!packFound) {
            throw new ScriptNotFoundException();
        }
    }

    public void save(OutputStream os) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.write(SERIAL_VERSION_MAJOR);
        oos.write(SERIAL_VERSION_MINOR);
        oos.writeObject(scriptPack.getClassPath());
        oos.writeObject(scriptPack.traitIndices);
        oos.writeBoolean(pcode);
        oos.writeInt(classIndex);
        oos.writeInt(traitId);
        oos.flush();
        oos.close();
    }

    public ABCSearchResult(ScriptPack scriptPack) {
        this.scriptPack = scriptPack;
        pcode = false;
        classIndex = 0;
        traitId = GraphTextWriter.TRAIT_UNKNOWN;
    }

    public ABCSearchResult(ScriptPack scriptPack, int classIndex, int traitId) {
        this.scriptPack = scriptPack;
        pcode = true;
        this.classIndex = classIndex;
        this.traitId = traitId;
    }

    public ScriptPack getScriptPack() {
        return scriptPack;
    }

    public boolean isPcode() {
        return pcode;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public int getTraitId() {
        return traitId;
    }

    private String getTraitName() {
        if (traitId == GraphTextWriter.TRAIT_SCRIPT_INITIALIZER) {
            return STR_SCRIPT_INITIALIZER;
        }

        if (classIndex == -1) {
            return null;
        }

        if (traitId == GraphTextWriter.TRAIT_CLASS_INITIALIZER) {
            return STR_CLASS_INITIALIZER;
        }

        if (traitId == GraphTextWriter.TRAIT_INSTANCE_INITIALIZER) {
            return STR_INSTANCE_INITIALIZER;
        }

        ABC abc = scriptPack.abc;

        int staticTraitCount = abc.class_info.get(classIndex).static_traits.traits.size();
        boolean isStatic = traitId < staticTraitCount;

        if (isStatic) {
            return abc.class_info.get(classIndex).static_traits.traits.get(traitId).getName(abc).getName(abc.constants, null, false, true);
        } else {
            int index = traitId - staticTraitCount;
            return abc.instance_info.get(classIndex).instance_traits.traits.get(index).getName(abc).getName(abc.constants, null, false, true);
        }
    }

    @Override
    public String toString() {
        String result = scriptPack.getClassPath().toString();

        if (pcode) {
            result += "/";
            result += getTraitName();
        }

        return result;
    }

    @Override
    public SWF getSWF() {
        return scriptPack.getSwf();
    }
}
