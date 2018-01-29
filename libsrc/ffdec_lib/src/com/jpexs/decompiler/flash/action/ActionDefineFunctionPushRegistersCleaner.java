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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionReturn;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.ecma.Undefined;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Cleaner for ActionDefineFunction push registers. From Flash 7 onwards,
 * functions are stored as ActionDefineFunction2 with use up to 255 local
 * registers. When exporting SWF to lower formats Flash5-Flash6 in Flash IDE
 * (for example MX 2004), all ActionDefineFunction2 are replaced with
 * ActionDefineFunction, and it also use local registers (4 of them are
 * available). The code of ActionDefineFunction is also modified that it pushes
 * all previous values of registers on the code start and pops them back on code
 * exit or when return action shows up. All returns are replaced with jump to
 * popping part. This makes code flow tangled and the decompiler cannot properly
 * handle it. The cleaner will fix this mess.
 *
 * @author JPEXS
 */
public class ActionDefineFunctionPushRegistersCleaner {

    public List<Action> cleanPushRegisters(List<Action> code) {
        ActionList actionList = new ActionList(code);

        if (actionList.isEmpty()) {
            return code;
        }

        /*
        ON BEGINNING:
        Push register1 register2 normalval
         */
        List<Integer> pushedRegisters = new ArrayList<>();
        int pos = 0;
        loopregs:
        while (actionList.get(pos) instanceof ActionPush) {
            ActionPush ap = (ActionPush) actionList.get(pos);
            for (int i = 0; i < ap.values.size(); i++) {
                if (ap.values.get(i) instanceof RegisterNumber) {
                    RegisterNumber rn = (RegisterNumber) ap.values.get(i);
                    pushedRegisters.add(rn.number);
                } else {
                    break loopregs;
                }
            }
            pos++;
        }
        if (pushedRegisters.isEmpty()) {
            return code;
        }

        /*
        ON FINISH:
        
        when function returns something:
        StoreRegister 0     ;return value
        Pop
        StoreRegister 2
        Pop
        StoreRegister 1
        Pop
        Push register0
        Return
        
        when function does not return anything:
        StoreRegister 2
        Pop
        StoreRegister 1
        Pop
        
        
         */
        int returnReg = -1;
        pos = actionList.size() - 1;
        if (actionList.get(pos) instanceof ActionReturn) {
            pos--;
            if (pos == -1) {
                return code;
            }
            if (!(actionList.get(pos) instanceof ActionPush)) {
                return code;
            }
            ActionPush pu = (ActionPush) actionList.get(pos);
            if (pu.values.size() != 1) {
                return code;
            }
            if (!(pu.values.get(0) instanceof RegisterNumber)) {
                return code;
            }
            RegisterNumber rn = (RegisterNumber) pu.values.get(0);
            returnReg = rn.number;
            pos--;
            if (pos == -1) {
                return code;
            }
        }
        for (int i = 0; i < pushedRegisters.size(); i++) {
            if (!(actionList.get(pos) instanceof ActionPop)) {
                return code;
            }
            pos--;
            if (pos == -1) {
                return code;
            }
            if (!(actionList.get(pos) instanceof ActionStoreRegister)) {
                return code;
            }
            ActionStoreRegister asr = (ActionStoreRegister) actionList.get(pos);
            int expectedReg = pushedRegisters.get(i);
            if (asr.registerNumber != expectedReg) {
                return code;
            }
            pos--;
            if (pos == -1) {
                return code;
            }
        }

        Set<Integer> refPos = new TreeSet<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1; //biggest first
            }
        });
        if (returnReg > -1) {
            if (!(actionList.get(pos) instanceof ActionPop)) {
                return code;
            }
            pos--;
            if (pos == -1) {
                return code;
            }
            if (!(actionList.get(pos) instanceof ActionStoreRegister)) {
                return code;
            }
            ActionStoreRegister asr = (ActionStoreRegister) actionList.get(pos);
            int expectedReg = returnReg;
            if (asr.registerNumber != expectedReg) {
                return code;
            }

            if (!(actionList.get(pos - 1) instanceof ActionJump)) {
                refPos.add(pos - 1);
            }
            Iterator<Action> ait = actionList.getReferencesFor(asr);
            while (ait.hasNext()) {
                Action a = ait.next();
                refPos.add(actionList.indexOf(a));
            }
            pos--;
        }

        //process code...
        //TODO: make this somehow create new list instead of modifying current one
        for (Integer jp : refPos) {
            int index = jp;
            Action a = actionList.get(index);
            if (a instanceof ActionJump) {
                actionList.remove(index);
                actionList.addAction(index, new ActionReturn());
            } else if ((a instanceof ActionPush) && ((ActionPush) a).values.size() == 1 && ((ActionPush) a).values.get(0) == Undefined.INSTANCE) {
                actionList.remove(a);
            } else {
                actionList.addAction(index + 1, new ActionReturn());
                pos++;
            }
        }

        int posFromEnd = actionList.size() - pos - 1;

        actionList.removeAction(actionList.size() - posFromEnd, posFromEnd);

        pos = 0;
        int removedCnt = pushedRegisters.size();
        loopregs2:
        while (actionList.get(pos) instanceof ActionPush) {
            ActionPush ap = (ActionPush) actionList.get(pos);
            for (int i = 0; i < ap.values.size(); i++) {
                if (ap.values.get(i) instanceof RegisterNumber) {
                    ap.values.remove(i);
                    i--;
                    removedCnt--;
                    if (ap.values.isEmpty()) {
                        actionList.removeAction(pos);
                    }
                    if (removedCnt == 0) {
                        break loopregs2;
                    }
                } else {
                    break loopregs2;
                }
            }
            pos++;
        }

        return actionList;
    }
}
