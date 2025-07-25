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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.fastactionlist.ActionItem;
import com.jpexs.decompiler.flash.action.fastactionlist.FastActionList;
import com.jpexs.decompiler.flash.action.fastactionlist.FastActionListIterator;
import com.jpexs.decompiler.flash.action.flashlite.ActionStrictMode;
import com.jpexs.decompiler.flash.action.swf4.ActionGetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal;
import com.jpexs.decompiler.flash.action.swf5.ActionGetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionNewObject;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerAdapter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Incorrect class header remover.
 * This neutralizes ActionIfs from the beginning of the class.
 * Without this, the ifs can contain long and incorrect values.
 * The AS2 class detector handles this properly.
 * @author JPEXS
 */
public class ActionIncorrectClassHeaderRemover extends SWFDecompilerAdapter {

    @Override
    public void actionListParsed(ActionList actions, SWF swf) throws InterruptedException {
        FastActionList list = new FastActionList(actions);
        int ip = 0;
        BaseLocalData ld = new ActionLocalData(null, true, new HashMap<>(), new LinkedHashSet<>());
        TranslateStack stack = new TranslateStack("");
        List<GraphTargetItem> output = new ArrayList<>();
        FastActionListIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            ActionItem ai = iterator.next();
            Action a = ai.action;
            if (
                    (a instanceof ActionPush)
                    || (a instanceof ActionGetVariable)
                    || (a instanceof ActionGetMember)
                    || (a instanceof ActionNot)
                    || (a instanceof ActionNewObject)
                    || (a instanceof ActionSetMember)
                    || (a instanceof ActionPop)
                    || (a instanceof ActionConstantPool)
                    || (a instanceof ActionDefineLocal) // obfuscated variable assignments
                    || (a instanceof ActionStrictMode) // seen in obfuscated code
                ) {
                a.translate(ld, stack, output, ip, "");
            } else if (a instanceof ActionJump) {
                ActionJump jump = (ActionJump) a;
                iterator.setCurrent(ai.getJumpTarget());;
            } else if (a instanceof ActionIf) {
                iterator.add(new ActionPop());
                iterator.prev();
                iterator.remove();
                iterator.next();
            } else {
                break;
            }
        }
        actions.setActions(list.toActionList());
    }    
}
