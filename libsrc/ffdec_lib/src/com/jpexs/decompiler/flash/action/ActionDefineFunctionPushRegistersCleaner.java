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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionReturn;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerAdapter;
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
public class ActionDefineFunctionPushRegistersCleaner extends SWFDecompilerAdapter {

    @Override
    public void actionListParsed(ActionList actions, SWF swf) throws InterruptedException {
        cleanActionDefineFunctions(actions);
    }

    private void cleanActionDefineFunctions(ActionList actions) {
        for (int i = actions.size() - 1; i >= 0; i--) {
            Action action = actions.get(i);
            if (action instanceof ActionDefineFunction) {
                ActionDefineFunction def = (ActionDefineFunction) action;

                List<Long> sizes = def.getContainerSizes();
                long endAddress = action.getAddress() + def.getHeaderSize() + sizes.get(0);
                int lastIndex = actions.getIndexByAddress(endAddress);
                int startIndex = i + 1;
                int count = lastIndex - startIndex;
                cleanPushRegisters(actions, startIndex, count);
            }
        }
    }

    /**
     *
     * @param code
     * @param startIndex Index of first Action in DefineFunction body
     * @param count Count of actions in DefineFunction
     * @return
     */
    private boolean cleanPushRegisters(ActionList code, int startIndex, int count) {
        if (count == 0) {
            return false;
        }

        /*
        ON BEGINNING:
        Push register1 register2 normalval
         */
        List<Integer> pushedRegisters = new ArrayList<>();
        int pos = startIndex;
        loopregs:
        while (code.get(pos) instanceof ActionPush) {
            ActionPush ap = (ActionPush) code.get(pos);
            for (int i = 0; i < ap.values.size(); i++) {
                if (ap.values.get(i) instanceof RegisterNumber) {
                    RegisterNumber rn = (RegisterNumber) ap.values.get(i);
                    pushedRegisters.add(rn.number);
                } else {
                    break loopregs;
                }
            }
            pos++;
            if (pos >= code.size()) {
                return false;
            }
        }
        if (pushedRegisters.isEmpty()) {
            return false;
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
        
        when original function has some returns, but no return at the end of function:
        Push undefined
        locjump: StoreRegister 0
        Pop
        ...
        
         */
        int returnReg = -1;
        pos = startIndex + count - 1;
        if (code.get(pos) instanceof ActionReturn) {
            pos--;
            if (pos < startIndex) {
                return false;
            }
            if (!(code.get(pos) instanceof ActionPush)) {
                return false;
            }
            ActionPush pu = (ActionPush) code.get(pos);
            if (pu.values.size() != 1) {
                return false;
            }
            if (!(pu.values.get(0) instanceof RegisterNumber)) {
                return false;
            }
            RegisterNumber rn = (RegisterNumber) pu.values.get(0);
            returnReg = rn.number;
            pos--;
            if (pos < startIndex) {
                return false;
            }

        }
        for (int i = 0; i < pushedRegisters.size(); i++) {
            if (!(code.get(pos) instanceof ActionPop)) {
                return false;
            }
            pos--;
            if (pos < startIndex) {
                return false;
            }
            if (!(code.get(pos) instanceof ActionStoreRegister)) {
                return false;
            }
            ActionStoreRegister asr = (ActionStoreRegister) code.get(pos);
            int expectedReg = pushedRegisters.get(i);
            if (asr.registerNumber != expectedReg) {
                return false;
            }
            pos--;
            if (pos < startIndex) {
                return false;
            }
        }

        Set<Integer> jumpsToReturnPositions = new TreeSet<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1; //biggest first
            }
        });

        int posBeforeFinishPart;
        Action actionBeforeFinishPart = null;
        if (returnReg > -1) {
            if (!(code.get(pos) instanceof ActionPop)) {
                return false;
            }
            pos--;
            if (pos < startIndex) {
                return false;
            }
            if (!(code.get(pos) instanceof ActionStoreRegister)) {
                return false;
            }
            ActionStoreRegister asr = (ActionStoreRegister) code.get(pos);
            int expectedReg = returnReg;
            if (asr.registerNumber != expectedReg) {
                return false;
            }

            Iterator<Action> ait = code.getReferencesFor(asr);
            while (ait.hasNext()) {
                Action a = ait.next();
                if (!(a instanceof ActionJump)) {
                    return false;
                }
                jumpsToReturnPositions.add(code.indexOf(a));
            }
            pos--;
            if (!(code.get(pos) instanceof ActionJump)) {
                actionBeforeFinishPart = code.get(pos);
            }
            posBeforeFinishPart = pos;
        } else {
            posBeforeFinishPart = pos;
        }

        //process code...
        //replace jumps to return with returns
        for (Integer jp : jumpsToReturnPositions) {
            int index = jp;
            code.removeAction(index);
            code.addAction(index, new ActionReturn());
        }

        //previous action (not jump) also leads to finishpart, we might add return there aswell
        if (returnReg > -1 && actionBeforeFinishPart != null) {
            if ((actionBeforeFinishPart instanceof ActionPush) && ((ActionPush) actionBeforeFinishPart).values.size() == 1 && ((ActionPush) actionBeforeFinishPart).values.get(0) == Undefined.INSTANCE) {
                //its return undefined, which is same as no return
                code.removeAction(posBeforeFinishPart);
                posBeforeFinishPart--;
                count--;
            } else if (actionBeforeFinishPart instanceof ActionReturn) {
                //it was a jump that was replaced with Return
            } else if (actionBeforeFinishPart instanceof ActionJump) {
                //its jump to another location, we will not add return there
            } else { //might be another returned value
                posBeforeFinishPart++;
                code.addAction(posBeforeFinishPart, new ActionReturn());
                count++; //action added, but not removed, we increase total count
            }
        }

        //remove finishPart
        int removeStartIndex = posBeforeFinishPart + 1;
        int removeCount = startIndex + count - removeStartIndex;
        code.removeAction(removeStartIndex, removeCount);

        //remove pushes from beginning part
        pos = startIndex;
        int registersLeft = pushedRegisters.size();
        loopregs2:
        while (code.get(pos) instanceof ActionPush) {
            ActionPush currentPush = (ActionPush) code.get(pos);

            List<Object> currentPushedValues = currentPush.values;
            List<Object> newPushedValues = new ArrayList<>();
            for (int i = 0; i < currentPushedValues.size(); i++) {
                if (registersLeft > 0 && currentPushedValues.get(i) instanceof RegisterNumber) {
                    registersLeft--;
                } else {
                    newPushedValues.add(currentPushedValues.get(i));
                }
            }
            if (newPushedValues.size() != currentPushedValues.size()) {
                code.removeAction(pos); //remove that push
                if (!newPushedValues.isEmpty()) {
                    ActionPush newPush = new ActionPush(newPushedValues.toArray());
                    newPush.constantPool = currentPush.constantPool;
                    code.addAction(pos, newPush); //replace with different push
                } else {
                    pos--; //action removed, but not added
                }
            }
            if (registersLeft == 0) { //we removed all unwanted registers
                break;
            }
            pos++;
        }
        return true;
    }
}
