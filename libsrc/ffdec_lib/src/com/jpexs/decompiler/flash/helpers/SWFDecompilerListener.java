/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface SWFDecompilerListener {

    byte[] proxyFileCatched(byte[] data);

    void swfParsed(SWF swf);

    void actionListParsed(ActionList actions, SWF swf) throws InterruptedException;

    void actionTreeCreated(List<GraphTargetItem> tree, SWF swf) throws InterruptedException;

    void abcParsed(ABC abc, SWF swf);

    void methodBodyParsed(ABC abc, MethodBody body, SWF swf);

    /**
     * this method is only called when deobfuscation is enabled and new
     * deobfuscation mode is selected
     *
     * @param path
     * @param classIndex
     * @param isStatic
     * @param scriptIndex
     * @param abc
     * @param trait
     * @param methodInfo
     * @param body
     * @throws InterruptedException
     */
    void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException;
}
