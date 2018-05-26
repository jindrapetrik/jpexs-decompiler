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
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public abstract class GraphSource implements Serializable {

    public abstract int size();

    public abstract GraphSourceItem get(int pos);

    public abstract boolean isEmpty();

    public abstract List<GraphTargetItem> translatePart(GraphPart part, BaseLocalData localData, TranslateStack stack, int start, int end, int staticOperation, String path) throws InterruptedException;

    public abstract Set<Long> getImportantAddresses();

    public abstract String insToString(int pos);

    private void visitCode(int ip, int lastIp, HashMap<Integer, List<Integer>> refs, int endIp) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        boolean debugMode = false;
        while (((endIp == -1) || (ip < endIp)) && (ip < size())) {
            refs.get(ip).add(lastIp);
            lastIp = ip;
            if (refs.get(ip).size() > 1) {
                break;
            }
            GraphSourceItem ins = get(ip);

            if (ins.isIgnored()) {
                ip++;
                continue;
            }
            if (debugMode) {
                System.err.println("visit ip " + ip + " action:" + ins);
            }
            if (ins.isExit()) {
                break;
            }

            if (ins instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer cnt = (GraphSourceItemContainer) ins;
                if (ins instanceof Action) { //TODO: Remove dependency of AVM1
                    long endAddr = ((Action) ins).getAddress() + cnt.getHeaderSize();
                    for (long size : cnt.getContainerSizes()) {
                        if (size != 0) {
                            visitCode(adr2pos(endAddr), ip, refs, adr2pos(endAddr + size));
                        }
                        endAddr += size;
                    }
                    ip = adr2pos(endAddr);
                    continue;
                }

            }

            if (ins.isBranch() || ins.isJump()) {
                List<Integer> branches = ins.getBranches(this);
                for (int b : branches) {
                    if (b >= 0) {
                        visitCode(b, ip, refs, endIp);
                    }
                }
                break;
            }
            ip++;
        };
    }

    public HashMap<Integer, List<Integer>> visitCode(List<Integer> alternateEntries) throws InterruptedException {
        HashMap<Integer, List<Integer>> refs = new HashMap<>();
        int siz = size();
        for (int i = 0; i < siz; i++) {
            refs.put(i, new ArrayList<>());
        }
        visitCode(0, 0, refs, -1);
        int pos = 0;
        for (int e : alternateEntries) {
            pos++;
            visitCode(e, -pos, refs, -1);
        }
        return refs;
    }

    public abstract int adr2pos(long adr);

    public abstract long pos2adr(int pos);

    public long getAddressAfterCode() {
        if (isEmpty()) {
            return 0;
        }
        long lastAddr = pos2adr(size() - 1);
        return lastAddr + get(size() - 1).getBytesLength();
    }

    public final long pos2adr(int pos, boolean allowPosAfterCode) {
        if (pos == size() && allowPosAfterCode) {
            return getAddressAfterCode();
        }
        return pos2adr(pos);
    }
;
}
