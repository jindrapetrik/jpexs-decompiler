/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.swf5.ActionWith;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.action.swf7.ActionThrow;
import com.jpexs.decompiler.flash.action.swf7.ActionTry;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.helpers.CancellableWorker;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads reached by individual AVM1 register writes, before the next write on
 * each control-flow path. Nested functions have separate register lifetimes.
 * With and try sections share the analysis of the enclosing function.
 *
 * @author JPEXS
 */
public class ActionRegisterUsage {

    private final Map<GraphSourceItem, List<GraphSourceItemPos>> reads = new IdentityHashMap<>();

    private enum SectionKind {
        TRY, CATCH, FINALLY
    }

    private static class Section {

        final TryRegion region;
        final SectionKind kind;

        Section(TryRegion region, SectionKind kind) {
            this.region = region;
            this.kind = kind;
        }
    }

    private static class Node {

        final int ip;
        GraphSourceItem action;
        int writtenRegister = -1;
        boolean barrier;
        List<Section> sections = Collections.emptyList();
        Node normalNext;
        final Set<Node> next = new HashSet<>();

        Node(int ip) {
            this.ip = ip;
        }
    }

    private static class TryRegion {

        final int start;
        final int end;
        Node catchEntry;
        Node finallyEntry;
        Node finallyEnd;
        int tryEnd;
        int catchEnd;
        Node bodyExit;

        TryRegion(int start, int end) {
            this.start = start;
            this.end = end;
        }

        boolean contains(Node target) {
            return target == catchEntry || target == finallyEntry || (target.ip > start && target.ip < end);
        }
    }

    private static class Flow {

        final ActionGraphSource code;
        final int startIp;
        final Map<Integer, Node> nodes = new HashMap<>();
        final Node exit;
        final Node unknown;

        Flow(ActionGraphSource code, int startIp) {
            this.code = code;
            this.startIp = startIp;
            exit = new Node(code.size());
            unknown = new Node(-1);
            unknown.barrier = true;
        }

        Node node(int ip) {
            return nodes.computeIfAbsent(ip, Node::new);
        }

        Node target(int ip) {
            if (ip == code.size()) {
                return exit;
            }
            Node result = nodes.get(ip);
            return result == null || result.action == null ? unknown : result;
        }

        List<Section> inSection(List<Section> enclosing, TryRegion region, SectionKind kind) {
            List<Section> result = new ArrayList<>(enclosing);
            result.add(new Section(region, kind));
            return result;
        }

        Node block(int start, int end, List<Section> sections, Node continuation) throws InterruptedException {
            for (int ip = start; ip < end;) {
                checkInterrupted();
                Node current = node(ip);
                current.action = code.get(ip);
                current.sections = sections;
                int nextIp = ip + 1;
                if (!current.action.isIgnored() && current.action instanceof GraphSourceItemContainer) {
                    GraphSourceItemContainer container = (GraphSourceItemContainer) current.action;
                    long address = ((Action) current.action).getAddress() + container.getHeaderSize();
                    List<Integer> boundaries = new ArrayList<>();
                    boundaries.add(code.adr2pos(address));
                    for (long size : container.getContainerSizes()) {
                        address += size;
                        boundaries.add(code.adr2pos(address));
                    }
                    nextIp = boundaries.get(boundaries.size() - 1);
                    if (nextIp <= ip || nextIp > end) {
                        current.barrier = true;
                        current.normalNext = continuation;
                        break;
                    }
                    Node after = nextIp == end ? continuation : node(nextIp);
                    if (current.action instanceof ActionWith) {
                        current.normalNext = block(boundaries.get(0), nextIp, sections, after);
                    } else if (current.action instanceof ActionTry) {
                        ActionTry actionTry = (ActionTry) current.action;
                        TryRegion region = new TryRegion(ip, nextIp);
                        region.tryEnd = boundaries.get(1);
                        region.catchEnd = actionTry.catchBlockFlag ? boundaries.get(2) : region.tryEnd;
                        Node bodyExit = after;
                        if (actionTry.finallyBlockFlag) {
                            int finalSection = actionTry.catchBlockFlag ? 2 : 1;
                            region.finallyEntry = new Node(boundaries.get(finalSection));
                            region.finallyEnd = new Node(nextIp);
                            region.finallyEnd.next.add(after);
                            Node finalBody = block(boundaries.get(finalSection), nextIp,
                                    inSection(sections, region, SectionKind.FINALLY), region.finallyEnd);
                            region.finallyEntry.next.add(finalBody);
                            bodyExit = region.finallyEntry;
                        }
                        region.bodyExit = bodyExit;
                        if (actionTry.catchBlockFlag) {
                            region.catchEntry = new Node(boundaries.get(1));
                            if (actionTry.catchInRegisterFlag) {
                                region.catchEntry.action = actionTry;
                                region.catchEntry.writtenRegister = actionTry.catchRegister;
                            }
                            region.catchEntry.next.add(block(boundaries.get(1), boundaries.get(2),
                                    inSection(sections, region, SectionKind.CATCH), bodyExit));
                        }
                        current.normalNext = block(boundaries.get(0), boundaries.get(1),
                                inSection(sections, region, SectionKind.TRY), bodyExit);
                    } else {
                        current.normalNext = after;
                        current.barrier = !(current.action instanceof ActionDefineFunction)
                                && !(current.action instanceof ActionDefineFunction2);
                    }
                } else {
                    current.normalNext = nextIp == end ? continuation : node(nextIp);
                    if (!current.action.isIgnored() && current.action instanceof ActionStoreRegister) {
                        current.writtenRegister = ((ActionStoreRegister) current.action).registerNumber;
                    }
                }
                ip = nextIp;
            }
            return start == end ? continuation : node(start);
        }

        Node transfer(Node source, Node target) {
            // Each exit from a protected region executes its finally first.
            // Finally continuations are joined conservatively: a normal end,
            // jump, return or uncaught exception may resume after that body.
            Node result = target;
            for (Section section : source.sections) {
                TryRegion region = section.region;
                if (section.kind != SectionKind.FINALLY && region.finallyEntry != null && !region.contains(target)) {
                    region.finallyEnd.next.add(result);
                    result = region.finallyEntry;
                }
            }
            return result;
        }

        Node branchTarget(Node source, int ip) {
            for (int i = source.sections.size() - 1; i >= 0; i--) {
                Section section = source.sections.get(i);
                TryRegion region = section.region;
                if ((section.kind == SectionKind.TRY && ip == region.tryEnd)
                        || (section.kind == SectionKind.CATCH && ip == region.catchEnd)) {
                    return transfer(source, region.bodyExit);
                }
                if (section.kind == SectionKind.FINALLY && ip == region.end) {
                    return transfer(source, region.finallyEnd);
                }
            }
            return transfer(source, target(ip));
        }

        Node exceptionTarget(Node source) {
            for (int i = source.sections.size() - 1; i >= 0; i--) {
                Section section = source.sections.get(i);
                if (section.kind == SectionKind.TRY && section.region.catchEntry != null) {
                    return transfer(source, section.region.catchEntry);
                }
            }
            return transfer(source, exit);
        }

        Node build() throws InterruptedException {
            Node entry = block(startIp, code.size(), Collections.emptyList(), exit);
            for (Node current : new ArrayList<>(nodes.values())) {
                checkInterrupted();
                GraphSourceItem action = current.action;
                if (action == null) {
                    current.barrier = true;
                    continue;
                }
                if (action.isIgnored()) {
                    current.next.add(current.normalNext);
                } else if (action instanceof ActionThrow) {
                    current.next.add(exceptionTarget(current));
                } else if (action.isExit()) {
                    current.next.add(transfer(current, exit));
                } else {
                    if (action.isJump() || action.isBranch()) {
                        for (int targetIp : action.getBranches(code)) {
                            current.next.add(branchTarget(current, targetIp));
                        }
                    } else {
                        current.next.add(current.normalNext);
                    }
                    // Overapproximate throwing instructions. Register writes
                    // themselves cannot throw before assigning their value.
                    if (!(action instanceof ActionStoreRegister) && !(action instanceof ActionPush)
                            && !(action instanceof ActionPop) && !action.isJump() && !action.isBranch()
                            && !current.sections.isEmpty()) {
                        current.next.add(exceptionTarget(current));
                    }
                }
            }
            return entry;
        }
    }

    /**
     * Calculates register usage in a complete script or function. A partial
     * with/try graph must reuse its enclosing analysis instead.
     *
     * @param code Code
     * @param startIp Start of the script or function
     * @throws InterruptedException On interruption
     */
    public ActionRegisterUsage(ActionGraphSource code, int startIp) throws InterruptedException {
        if (startIp > 0 && !(code.get(startIp - 1) instanceof ActionDefineFunction)
                && !(code.get(startIp - 1) instanceof ActionDefineFunction2)) {
            return;
        }
        Node entry = new Flow(code, startIp).build();
        Set<Node> reachable = new HashSet<>();
        ArrayDeque<Node> pending = new ArrayDeque<>();
        pending.add(entry);
        while (!pending.isEmpty()) {
            checkInterrupted();
            Node node = pending.removeFirst();
            if (reachable.add(node)) {
                pending.addAll(node.next);
            }
        }
        for (Node store : reachable) {
            if (store.writtenRegister < 0) {
                continue;
            }
            List<GraphSourceItemPos> usages = new ArrayList<>();
            Set<Node> visited = new HashSet<>();
            pending.addAll(store.next);
            boolean complete = true;
            while (!pending.isEmpty()) {
                checkInterrupted();
                Node node = pending.removeFirst();
                if (!visited.add(node)) {
                    continue;
                }
                complete &= !node.barrier;
                if (node.writtenRegister == store.writtenRegister) {
                    continue;
                }
                if (node.action instanceof ActionPush && !node.action.isIgnored()) {
                    List<Object> values = ((ActionPush) node.action).values;
                    for (int pos = 0; pos < values.size(); pos++) {
                        Object value = values.get(pos);
                        if (value instanceof RegisterNumber && ((RegisterNumber) value).number == store.writtenRegister) {
                            usages.add(new GraphSourceItemPos(node.action, pos));
                        }
                    }
                }
                pending.addAll(node.next);
            }
            if (complete) {
                reads.put(store.action, usages);
            }
        }
    }

    private static void checkInterrupted() throws InterruptedException {
        if (CancellableWorker.isInterrupted()) {
            throw new InterruptedException();
        }
    }

    /**
     * Gets distinct read sites for a register write. Multiple register operands
     * in one Push count as separate reads. An ActionTry denotes its implicit
     * catch-register write.
     *
     * @param store Register write
     * @return Reads, or null if the write is unreachable or crosses an unknown boundary
     */
    public List<GraphSourceItemPos> getReads(GraphSourceItem store) {
        List<GraphSourceItemPos> usages = reads.get(store);
        return usages == null ? null : Collections.unmodifiableList(usages);
    }
}
