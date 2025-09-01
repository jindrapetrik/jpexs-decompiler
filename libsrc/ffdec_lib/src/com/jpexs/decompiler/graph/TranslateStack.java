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
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ExceptionAVM2Item;
import com.jpexs.decompiler.graph.model.BranchStackResistant;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.DuplicateSourceItem;
import com.jpexs.decompiler.graph.model.ExitItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.ScriptEndItem;
import com.jpexs.decompiler.graph.model.SetTemporaryItem;
import com.jpexs.decompiler.graph.model.TemporaryItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stack for translation.
 *
 * @author JPEXS
 */
public class TranslateStack extends Stack<GraphTargetItem> {

    /**
     * Pop item
     */
    private PopItem pop;

    /**
     * Path
     */
    private final String path;

    private List<GraphTargetItem> connectedOutput = null;

    private int prevOutputSize = 0;

    private Map<String, GraphTargetItem> marks = new HashMap<>();

    public List<GraphTargetItem> outputQueue = new ArrayList<>();

    public BaseLocalData localData = null;

    @Override
    public synchronized Object clone() {
        TranslateStack st = (TranslateStack) super.clone();
        st.outputQueue = new ArrayList<>(outputQueue);
        return st;
    }

    @Override
    public void clear() {
        super.clear();
        outputQueue.clear();
    }

    public void setConnectedOutput(int prevOutputSize, List<GraphTargetItem> connectedOutput, BaseLocalData localData) {
        this.prevOutputSize = prevOutputSize;
        this.connectedOutput = connectedOutput;
        this.localData = localData;
    }

    @Override
    public GraphTargetItem push(GraphTargetItem item) {
        if (!outputQueue.isEmpty()) {
            if ((item instanceof FindPropertyAVM2Item) || isDupsOnly()) {
                finishBlock(connectedOutput);
            } else {
                outputQueue.add(item);
                item = new CommaExpressionItem(item.dialect, null, item.lineStartItem, outputQueue);
                outputQueue = new ArrayList<>();
            }
        }
        if (connectedOutput != null && item != null) {
            item.outputPos = prevOutputSize + connectedOutput.size();
        }
        return super.push(item);
    }

    //for #116
    private boolean isDupsOnly() {
        for (GraphTargetItem item : this) {
            if (item instanceof DuplicateItem) {
                continue;
            }
            if (item instanceof DuplicateSourceItem) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Sets mark.
     *
     * @param name Name
     * @param value Value
     */
    public void setMark(String name, GraphTargetItem value) {
        marks.put(name, value);
    }

    /**
     * Gets mark.
     *
     * @param name Name
     * @return Value
     */
    public GraphTargetItem getMark(String name) {
        return marks.get(name);
    }

    /**
     * Simplifies all items in the stack.
     */
    public void simplify() {
        for (int i = 0; i < size(); i++) {
            set(i, get(i).simplify(""));
        }
    }

    /**
     * Constructs new TranslateStack with the specified path.
     *
     * @param path The path.
     */
    public TranslateStack(String path) {
        this.path = path;
    }

    /**
     * Gets the path.
     *
     * @return The path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the pop item.
     *
     * @return The pop item.
     */
    private PopItem getPop() {
        if (pop == null) {
            pop = new PopItem(null, null, null); //TODO: linestart?
        }

        return pop;
    }

    /**
     * Gets the item at the specified index.
     *
     * @param index index of the element to return
     * @return The element at the specified position in this list
     */
    @Override
    public synchronized GraphTargetItem get(int index) {
        if (path != null) {
            if (index >= this.size() || index < 0) {
                Logger.getLogger(TranslateStack.class.getName()).log(Level.FINE, "{0}: Attempt to Get item outside of bounds of stack", path);
                return getPop();
            }
        }
        return super.get(index);
    }

    /**
     * Gets the item at the top of the stack.
     *
     * @return The item at the top of the stack.
     */
    @Override
    public synchronized GraphTargetItem peek() {
        if (path != null) {
            if (this.isEmpty()) {
                /*if (connectedOutput != null && !connectedOutput.isEmpty() && connectedOutput.get(connectedOutput.size() - 1) instanceof PushItem) {
                   PushItem pi = (PushItem) connectedOutput.get(connectedOutput.size() - 1);
                   return pi.value;
                }*/
                Logger.getLogger(TranslateStack.class.getName()).log(Level.FINE, "{0}: Attempt to Peek empty stack", path);
                return getPop();
            }
        }
        return super.peek();
    }

    /**
     * Gets the item at the specified index from the top of the stack.
     *
     * @param index The index.
     * @return The item at the specified index from the top of the stack.
     */
    public synchronized GraphTargetItem peek(int index) {
        if (path != null) {
            if (index > this.size()) {
                /*if (connectedOutput != null && connectedOutput.size() >= index - this.size() && connectedOutput.get(connectedOutput.size() - (index - this.size())) instanceof PushItem) {
                   PushItem pi = (PushItem) connectedOutput.get(connectedOutput.size() - (index - this.size()));
                   return pi.value;
                }*/
                Logger.getLogger(TranslateStack.class.getName()).log(Level.FINE, "{0}: Attempt to Peek item from stack", path);
                return getPop();
            }
        }
        return super.get(size() - index);
    }

    /**
     * Pop the item at the top of the stack.
     *
     * @return The item at the top of the stack.
     */
    @Override
    public synchronized GraphTargetItem pop() {
        if (!outputQueue.isEmpty()) {
            List<GraphTargetItem> oldQueue = outputQueue;
            outputQueue = new ArrayList<>();
            finishBlock(connectedOutput);
            connectedOutput.addAll(oldQueue);
        }

        if (isEmpty() && connectedOutput != null) {
            moveToStack(connectedOutput);
            if (!isEmpty()) {
                return pop();
            }
            for (int i = connectedOutput.size() - 1; i >= 0; i--) {
                GraphTargetItem item = connectedOutput.get(i);
                if (item instanceof Block) {
                    break;
                }
                if (item instanceof PushItem) {
                    PushItem pi = (PushItem) item;
                    if (pi.value instanceof SetTemporaryItem) {
                        SetTemporaryItem st = (SetTemporaryItem) pi.value;
                        connectedOutput.set(i, st);
                        return new TemporaryItem(pi.dialect, pi.value.getSrc(), pi.value.getLineStartItem(), pi.value, st.tempIndex);
                    } else if (pi.value instanceof DuplicateSourceItem) {
                        DuplicateSourceItem ds = (DuplicateSourceItem) pi.value;
                        //connectedOutput.set(i, new SetTemporaryItem(pi.dialect, ds.getSrc(), ds.getLineStartItem(), ds.value, ds.tempIndex,"push"));
                        connectedOutput.remove(i);
                        return ds; //new TemporaryItem(pi.dialect, pi.value.getSrc(), pi.value.getLineStartItem(), ds.value, ds.tempIndex);
                    } else if (pi.value instanceof DuplicateItem) {
                        DuplicateItem d = (DuplicateItem) pi.value;
                        //connectedOutput.remove(i);                       
                        //connectedOutput.set(i, new SetTemporaryItem(pi.dialect, d.getSrc(), d.getLineStartItem(), d.value, d.tempIndex));
                        connectedOutput.remove(i);
                        return new TemporaryItem(pi.dialect, pi.value.getSrc(), pi.value.getLineStartItem(), pi.value, d.tempIndex);
                    } else if (pi.value instanceof TemporaryItem) {
                        connectedOutput.remove(i);
                        return pi.value;
                    } else {
                        int temp = localData.maxTempIndex.getVal() + 1;
                        localData.maxTempIndex.setVal(temp);
                        connectedOutput.set(i, new SetTemporaryItem(pi.dialect, pi.value.getSrc(), pi.value.getLineStartItem(), pi.value, temp, "push", 1));
                        return new TemporaryItem(pi.dialect, pi.value.getSrc(), pi.value.getLineStartItem(), pi.value, temp);
                    }
                }
            }
        }

        if (path != null) {
            if (this.isEmpty()) {
                /*if (connectedOutput != null && !connectedOutput.isEmpty() && connectedOutput.get(connectedOutput.size() - 1) instanceof PushItem) {
                   PushItem pi = (PushItem) connectedOutput.remove(connectedOutput.size() - 1);
                   return pi.value;
                }*/

                PopItem oldpop = getPop();
                pop = null;
                Logger.getLogger(TranslateStack.class.getName()).log(Level.FINE, "{0}: Attempt to Pop empty stack", path);
                return oldpop;
            }
        }
        return super.pop();
    }

    public void moveToStack(List<GraphTargetItem> output) {
        if (!isEmpty()) {
            return;
        }
        int i = output.size() - 1;
        for (; i >= 0; i--) {
            if (!(output.get(i) instanceof PushItem)) {
                break;
            }
        }
        i++;
        while (i < output.size()) {
            PushItem pi = (PushItem) output.remove(i);
            push(pi.value);
        }
    }

    private boolean isAllTemp() {
        for (int i = 0; i < size(); i++) {
            GraphTargetItem item = get(i);
            if (item instanceof TemporaryItem) {
                continue;
            }
            if (item instanceof DuplicateItem) {
                continue;
            }
            return false;
        }
        return true;
    }

    public void addToOutput(GraphTargetItem item) {
        if (isEmpty()
                || peek() instanceof ExceptionAVM2Item
                || peek() instanceof NewActivationAVM2Item //|| isAllTemp()
            ) {
            connectedOutput.add(item);
            return;
        }
        outputQueue.add(item);
        if (item instanceof ExitItem) {
            finishBlock(connectedOutput);
        }
    }

    public void finishBlock(List<GraphTargetItem> output) {
        if (connectedOutput == null) {
            return;
        }
        /*int pos = output.size();
        
        for (int i = size() - 1; i >= 0; i--) {
            GraphTargetItem item = get(i);
            if (item instanceof BranchStackResistant) {
                continue;
            }
            if (item instanceof NewActivationAVM2Item) {
                break;
            }
            if (item instanceof  ExceptionAVM2Item) {
                break;
            }
            remove(i);
            if (item instanceof PopItem) {
                continue;
            }
            output.add(pos, beforeExit ? item : new PushItem(item));
        }*/

        int clen = output.size();
        boolean isExit = false;

        if (!outputQueue.isEmpty() && outputQueue.get(outputQueue.size() - 1) instanceof ExitItem) {
            isExit = true;
        }
        if (clen > 0) {
            if (output.get(clen - 1) instanceof ScriptEndItem) {
                clen--;
                isExit = true;
            }
        }
        if (clen > 0) {
            if (output.get(clen - 1) instanceof ExitItem) {
                isExit = true;
                clen--;
            }
        }
        if (clen > 0) {
            if (output.get(clen - 1) instanceof BreakItem) {
                clen--;
            }
        }
        if (clen > 0) {
            if (output.get(clen - 1) instanceof ContinueItem) {
                clen--;
            }
        }
        for (int i = size() - 1; i >= 0; i--) {
            GraphTargetItem p = get(i);
            if (p instanceof BranchStackResistant) {
                continue;
            }
            remove(i);
            if (!(p instanceof PopItem)) {
                if (isExit) {
                    //ASC2 leaves some function calls unpopped on stack before returning from a method
                    output.add(clen, p);
                } else {
                    /*int pos = 0;
                    if (p.outputPos < output.size()) {
                        output.add(p.outputPos, new PushItem(p));
                    } else {
                        output.add(clen + pos, new PushItem(p));
                    }*/
                    output.add(clen, new PushItem(p));
                }
            }
        }

        output.addAll(outputQueue);
        outputQueue.clear();
    }

    public void allowSwap(List<GraphTargetItem> output) {
        /*
        if (!isEmpty()) {
            return;
        }
        if (output.isEmpty()) {
            return;
        }
        if (size() != 2) {
            return;
        }
        
        if (!(output.get(output.size() - 1) instanceof SetTemporaryItem)) {
            return;
        }
                
        if (!(peek() instanceof TemporaryItem)) {
            return;
        }
        
        TemporaryItem ti = (TemporaryItem) peek();
        
        SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 1);
        
        if (!"swap".equals(st.getSuffix())) {
            return;
        }
        
        if (st.getTempIndex() != ti.getTempIndex()) {
            return;
        }
        
        output.remove(output.size() - 1);
        pop();
        push(ti.value);
        
         */
        if (!isEmpty()) {
            return;
        }
        if (output.size() < 3) {
            return;
        }
        if (!(output.get(output.size() - 1) instanceof PushItem)) {
            return;
        }

        if (!(output.get(output.size() - 2) instanceof PushItem)) {
            return;
        }

        if (!(output.get(output.size() - 3) instanceof SetTemporaryItem)) {
            return;
        }

        PushItem pi1 = (PushItem) output.get(output.size() - 1);
        if (!(pi1.value instanceof TemporaryItem)) {
            return;
        }

        TemporaryItem ti = (TemporaryItem) pi1.value;

        SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 3);

        if (!"swap".equals(st.getSuffix())) {
            return;
        }

        if (st.getTempIndex() != ti.getTempIndex()) {
            return;
        }

        PushItem pi2 = (PushItem) output.get(output.size() - 2);

        output.remove(output.size() - 1);
        output.remove(output.size() - 1);
        output.remove(output.size() - 1);
        push(pi2.value);
        push(ti.value);
        //moveToStack(output);
    }
}
