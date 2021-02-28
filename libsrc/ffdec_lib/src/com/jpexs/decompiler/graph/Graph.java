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
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.FinalProcessLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BranchStackResistant;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DefaultItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.ExitItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.GotoItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.IntegerValueItem;
import com.jpexs.decompiler.graph.model.IntegerValueTypeItem;
import com.jpexs.decompiler.graph.model.LabelItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.LogicalOpItem;
import com.jpexs.decompiler.graph.model.LoopItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.ScriptEndItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.decompiler.graph.model.UniversalLoopItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import com.jpexs.decompiler.graph.precontinues.GraphPrecontinueDetector;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class Graph {

    public List<GraphPart> heads;

    protected GraphSource code;

    private final List<GraphException> exceptions;

    public static final int SOP_USE_STATIC = 0;

    public static final int SOP_SKIP_STATIC = 1;

    public static final int SOP_REMOVE_STATIC = 2;

    private boolean debugPrintAllParts = false;
    private boolean debugPrintLoopList = false;
    private boolean debugGetLoops = false;
    private boolean debugPrintGraph = false;

    private static final Logger logger = Logger.getLogger(Graph.class.getName());

    public GraphSource getGraphCode() {
        return code;
    }

    public LinkedHashMap<String, Graph> getSubGraphs() {
        return new LinkedHashMap<>();
    }

    public Graph(GraphSource code, List<GraphException> exceptions) {
        this.code = code;
        this.exceptions = exceptions;

    }

    public void init(BaseLocalData localData) throws InterruptedException {
        if (heads != null) {
            return;
        }
        heads = makeGraph(code, new ArrayList<>(), exceptions);
        int time = 1;
        List<GraphPart> ordered = new ArrayList<>();
        List<GraphPart> visited = new ArrayList<>();
        for (GraphPart head : heads) {
            time = head.setTime(time, ordered, visited);
        }
    }

    public List<GraphException> getExceptions() {
        return exceptions;
    }

    protected static void populateParts(GraphPart part, Set<GraphPart> allParts) {
        if (allParts.contains(part)) {
            return;
        }
        allParts.add(part);
        for (GraphPart p : part.nextParts) {
            populateParts(p, allParts);
        }
    }

    public GraphPart deepCopy(GraphPart part) {
        return deepCopy(part, new HashMap<>());
    }

    private GraphPart deepCopy(GraphPart part, Map<GraphPart, GraphPart> copies) {
        GraphPart copy = copies.get(part);
        if (copy != null) {
            return copy;
        }

        copy = new GraphPart(part.start, part.end);
        copy.path = part.path;
        copies.put(part, copy);
        copy.nextParts = new ArrayList<>();
        for (int i = 0; i < part.nextParts.size(); i++) {
            copy.nextParts.add(deepCopy(part.nextParts.get(i), copies));
        }

        for (int i = 0; i < part.refs.size(); i++) {
            copy.refs.add(deepCopy(part.refs.get(i), copies));
        }

        return copy;
    }

    public void resetGraph(GraphPart part, Set<GraphPart> visited) {
        if (visited.contains(part)) {
            return;
        }

        visited.add(part);
        int pos = 0;
        for (GraphPart p : part.nextParts) {
            if (!visited.contains(p)) {
                p.path = part.path.sub(pos, p.end);
            }

            resetGraph(p, visited);
            pos++;
        }
    }

    protected void getReachableParts(BaseLocalData localData, GraphPart part, LinkedHashSet<GraphPart> ret, List<Loop> loops, List<ThrowState> throwStates) {
        // use LinkedHashSet to preserve order
        getReachableParts(localData, part, ret, loops, throwStates, true);
    }

    private void getReachableParts(BaseLocalData localData, GraphPart part, LinkedHashSet<GraphPart> ret, List<Loop> loops, List<ThrowState> throwStates, boolean first) {
        // todo: honfika: why call with first = true parameter always?
        Stack<GraphPartQueue> stack = new Stack<>();
        GraphPartQueue queue = new GraphPartQueue();
        queue.add(part);
        stack.add(queue);
        stacknext:
        while (!stack.isEmpty()) {

            queue = stack.peek();
            if (!queue.isEmpty()) {
                part = queue.remove();
            } else if (queue.currentLoop != null) {
                Loop cLoop = queue.currentLoop;
                part = cLoop.loopBreak;
                queue.currentLoop = null;
                if (ret.contains(part)) {
                    continue;
                }

                ret.add(part);
                cLoop.reachableMark = 2;
            } else {
                stack.pop();
                continue;
            }

            for (Loop l : loops) {
                l.reachableMark = 0;
            }

            Loop currentLoop = null;
            for (Loop l : loops) {
                if ((l.phase == 1) || (l.reachableMark == 1)) {
                    if (l.loopContinue == part) {
                        continue stacknext;
                    }
                    if (l.loopBreak == part) {
                        continue stacknext;
                    }
                    if (l.loopPreContinue == part) {
                        continue stacknext;
                    }
                }
                if (l.reachableMark == 0) {
                    if (l.loopContinue == part) {
                        l.reachableMark = 1;
                        currentLoop = l;
                    }
                }
            }

            GraphPartQueue newParts = new GraphPartQueue();
            List<GraphPart> nextParts = new ArrayList<>(getNextParts(localData, part));

            for (ThrowState ts : throwStates) {
                if (ts.state != 1) {
                    if (ts.throwingParts.contains(part)) {
                        newParts.add(ts.targetPart);
                    }
                }
            }
            loopnext:
            for (GraphPart nextRaw : nextParts) {

                GraphPart next = checkPart(null, localData, part, nextRaw, null);
                if (next == null) {
                    continue;
                }

                for (Loop l : loops) {
                    if ((l.phase == 1) || (l.reachableMark == 1)) {
                        if (l.loopContinue == next) {
                            continue loopnext;
                        }
                        if (l.loopBreak == next) {
                            continue loopnext;
                        }
                        if (l.loopPreContinue == next) {
                            continue loopnext;
                        }
                    }

                }
                if (!ret.contains(next)) {
                    newParts.add(next);
                }
            }

            ret.addAll(newParts);
            if (currentLoop != null && currentLoop.loopBreak != null) {
                newParts.currentLoop = currentLoop;
            }

            if (!newParts.isEmpty() || newParts.currentLoop != null) {
                stack.add(newParts);
            }
        }
    }

    public GraphPart getNextCommonPart(BaseLocalData localData, GraphPart part, List<Loop> loops, List<ThrowState> throwStates) throws InterruptedException {
        return getCommonPart(localData, part, getNextParts(localData, part), loops, throwStates);
    }

    //TODO: Make this faster!
    public GraphPart getCommonPart(BaseLocalData localData, GraphPart prev, List<GraphPart> parts, List<Loop> loops, List<ThrowState> throwStates) throws InterruptedException {
        if (parts.isEmpty()) {
            return null;
        }

        List<GraphPart> loopContinues = new ArrayList<>();//getLoopsContinues(loops);
        for (Loop l : loops) {
            if (l.phase == 1) {
                loopContinues.add(l.loopContinue);
                if (l.loopPreContinue != null) {
                    loopContinues.add(l.loopPreContinue);
                }
            }
        }

        for (GraphPart p : parts) {
            if (loopContinues.contains(p)) {
                break;
            }
            boolean common = true;
            for (GraphPart q : parts) {
                if (q == p) {
                    continue;
                }
                if (!q.leadsTo(localData, this, code, p, loops, throwStates, false /*!!THROW*/)) {
                    common = false;
                    break;
                }
            }
            if (common) {
                return p;
            }
        }
        List<Set<GraphPart>> reachable = new ArrayList<>();
        for (GraphPart p : parts) {
            LinkedHashSet<GraphPart> r1 = new LinkedHashSet<>();
            getReachableParts(localData, p, r1, loops, throwStates);
            r1.add(p);
            reachable.add(r1);
        }
        Set<GraphPart> first = reachable.get(0);
        for (GraphPart p : first) {
            boolean common = true;
            for (Set<GraphPart> r : reachable) {
                if (!r.contains(p)) {
                    common = false;
                    break;
                }
            }
            if (common) {
                if (loopContinues.contains(p)) {
                    return null;
                }
                return p;
            }
        }
        return null;
    }

    public GraphPart getMostCommonPart(BaseLocalData localData, List<GraphPart> parts, List<Loop> loops, List<ThrowState> throwStates) throws InterruptedException {
        if (parts.isEmpty()) {
            return null;
        }

        Set<GraphPart> s = new HashSet<>(parts); //unique
        parts = new ArrayList<>(s); //make local copy

        List<GraphPart> loopContinues = new ArrayList<>();//getLoopsContinues(loops);
        for (Loop l : loops) {
            if (l.phase == 1) {
                loopContinues.add(l.loopContinue);
                loopContinues.add(l.loopPreContinue);
            }
        }

        for (GraphPart p : parts) {
            if (loopContinues.contains(p)) {
                break;
            }
            boolean common = true;
            for (GraphPart q : parts) {
                if (q == p) {
                    continue;
                }
                if (!q.leadsTo(localData, this, code, p, loops, throwStates, false /*!!THROW*/)) {
                    common = false;
                    break;
                }
            }
            if (common) {
                return p;
            }
        }

        /*loopi:
        for (int i = 0; i < parts.size(); i++) {
            for (int j = 0; j < parts.size(); j++) {
                if (j == i) {
                    continue;
                }
                if (parts.get(i).leadsTo(localData, this, code, parts.get(j), loops)) {
                    parts.remove(i);
                    i--;
                    continue loopi;
                }
            }
        }*/
        List<Set<GraphPart>> reachable = new ArrayList<>();
        Set<GraphPart> allReachable = new LinkedHashSet<>();
        for (GraphPart p : parts) {
            LinkedHashSet<GraphPart> r1 = new LinkedHashSet<>();
            getReachableParts(localData, p, r1, loops, throwStates);
            Set<GraphPart> r2 = new LinkedHashSet<>();
            r2.add(p);
            r2.addAll(r1);
            reachable.add(r2);
            allReachable.add(p);
            allReachable.addAll(r1);
        }
        int maxCommonLevel = 0;
        GraphPart maxCommonLevelPart = null;
        Set<GraphPart> visited = new HashSet<>();
        for (GraphPart p : allReachable) {
            if (loopContinues.contains(p)) {
                break;
            }
            if (visited.contains(p)) {
                continue;
            }
            visited.add(p);
            int commonLevel = 0;
            for (Set<GraphPart> r : reachable) {
                if (r.contains(p)) {
                    commonLevel++;
                }
            }
            //System.err.println("commonlevel of " + p + " is " + commonLevel);
            if (commonLevel <= maxCommonLevel) {
                continue;
            }
            maxCommonLevel = commonLevel;
            maxCommonLevelPart = p;
        }
        //System.err.println("maxclevel = " + maxCommonLevel);
        //System.err.println("maxclevelpart = " + maxCommonLevelPart);

        if (maxCommonLevel <= 1) {
            return null;
        }
        return maxCommonLevelPart;
    }

    public GraphPart getNextNoJump(GraphPart part, BaseLocalData localData) {
        while (code.get(part.start).isJump()) {
            part = part.getSubParts().get(0).nextParts.get(0);
        }
        /*localData = prepareBranchLocalData(localData);
         TranslateStack st = new TranslateStack();
         List<GraphTargetItem> output=new ArrayList<>();
         GraphPart startPart = part;
         for (int i = part.start; i <= part.end; i++) {
         GraphSourceItem src = code.get(i);
         if (src.isJump()) {
         part = getNextParts(localData, part).get(0);
         if(st.isEmpty()){
         startPart = part;
         }
         i = part.start - 1;
         continue;
         }
         try{
         src.translate(localData, st, output, SOP_USE_STATIC, "");
         }catch(Exception ex){
         return startPart;
         }
         if(!output.isEmpty()){
         return startPart;
         }
         }*/
        return part;
    }

    public static List<GraphTargetItem> translateViaGraph(BaseLocalData localData, String path, GraphSource code, List<GraphException> exceptions, int staticOperation) throws InterruptedException {
        Graph g = new Graph(code, exceptions);
        g.init(localData);
        return g.translate(localData, staticOperation, path);
    }

    protected void afterPopupateAllParts(Set<GraphPart> allParts) {

    }

    protected List<ThrowState> getThrowStates(BaseLocalData localData, Set<GraphPart> allParts) {
        return new ArrayList<>();
    }

    public List<GraphTargetItem> translate(BaseLocalData localData, int staticOperation, String path) throws InterruptedException {

        Set<GraphPart> allParts = new HashSet<>();
        for (GraphPart head : heads) {
            populateParts(head, allParts);
        }
        afterPopupateAllParts(allParts);
        if (debugPrintAllParts) {
            System.err.println("parts:");
            for (GraphPart p : allParts) {
                System.err.print(p);
                if (!getNextParts(localData, p).isEmpty()) {
                    System.err.print(", next: ");
                }
                boolean first = true;
                for (GraphPart n : getNextParts(localData, p)) {
                    if (!first) {
                        System.err.print(",");
                    }
                    System.err.print(n);
                    first = false;
                }
                System.err.println("");
            }
            System.err.println("/parts");
        }
        TranslateStack stack = new TranslateStack(path);
        List<ThrowState> throwStates = getThrowStates(localData, allParts);
        beforeGetLoops(localData, path, allParts, throwStates);
        List<Loop> loops = new ArrayList<>();

        getLoops(localData, heads.get(0), loops, throwStates, null);

        afterGetLoops(localData, path, allParts);
        if (debugPrintLoopList) {
            System.err.println("<loops>");
            for (Loop el : loops) {
                System.err.println(el);
            }
            System.err.println("</loops>");
        }

        //TODO: Make getPrecontinues faster
        getBackEdges(localData, loops, throwStates);

        new GraphPrecontinueDetector().detectPrecontinues(heads, allParts, loops, throwStates);

        /*System.err.println("<loopspre>");
         for (Loop el : loops) {
         System.err.println(el);
         }
         System.err.println("</loopspre>");//*/
        List<GotoItem> gotos = new ArrayList<>();

        List<GraphTargetItem> ret = printGraph(gotos, new HashMap<>(), new HashMap<>(), new HashSet<>(), localData, stack, allParts, null, heads.get(0), null, null, loops, throwStates, staticOperation, path);

        processIfGotos2(new ArrayList<>(), gotos, ret, ret);
        processIfGotos(gotos, ret, ret);

        Map<String, Integer> usages = new HashMap<>();
        Map<String, GotoItem> lastUsage = new HashMap<>();
        for (GotoItem gi : gotos) {
            if (!usages.containsKey(gi.labelName)) {
                usages.put(gi.labelName, 0);
            }
            usages.put(gi.labelName, usages.get(gi.labelName) + 1);
            lastUsage.put(gi.labelName, gi);
        }
        for (String labelName : usages.keySet()) {
            logger.log(Level.FINE, "usage - {0}: {1}", new Object[]{labelName, usages.get(labelName)});
            if (usages.get(labelName) == 1) {
                lastUsage.get(labelName).labelName = null;
            }
        }
        expandGotos(ret);
        processIfs(ret);
        finalProcessStack(stack, ret, path);
        makeAllCommands(ret, stack);
        finalProcessAll(ret, 0, getFinalData(localData, loops, throwStates), path);
        return ret;
    }

    protected FinalProcessLocalData getFinalData(BaseLocalData localData, List<Loop> loops, List<ThrowState> throwStates) {
        return new FinalProcessLocalData(loops);
    }

    protected void beforeGetLoops(BaseLocalData localData, String path, Set<GraphPart> allParts, List<ThrowState> throwStates) throws InterruptedException {

    }

    protected void afterGetLoops(BaseLocalData localData, String path, Set<GraphPart> allParts) throws InterruptedException {

    }

    protected boolean isPartEmpty(GraphPart part) {
        return false;
    }

    private String pathToString(Collection<? extends Object> list) {
        List<String> strs = new ArrayList<>();
        for (Object p : list) {
            strs.add(p.toString());
        }
        return "[" + String.join(", ", strs) + "]";
    }

    private List<GraphPart> getUnicatePartList(List<GraphPart> list) {
        List<GraphPart> result = new ArrayList<>();
        for (GraphPart p : list) {
            if (!result.contains(p)) {
                result.add(p);
            }
        }
        return result;
    }

    private List<GraphPart> getUnicateRefsNoThrow(GraphPart part, Set<GraphPartEdge> throwEdges) {
        List<GraphPart> result = new ArrayList<>();
        for (GraphPart r : part.refs) {
            GraphPartEdge edge = new GraphPartEdge(r, part);
            if (!throwEdges.contains(edge)) {
                result.add(r);
            }
        }

        return getUnicatePartList(result);
    }

    private List<GraphPart> getUsableRefs(GraphPart g, List<Loop> loops, List<ThrowState> throwStates) {
        List<GraphPart> ret = getUnicatePartList(g.refs);
        for (Loop el : loops) {
            for (GraphPart be : el.backEdges) {
                if (ret.contains(be)) {
                    ret.remove(be);
                }
            }
        }
        return ret;
    }


    /**/
    //if (getNextParts(localData, ref))
    private void getBackEdges(BaseLocalData localData, List<Loop> loops, List<ThrowState> throwStates) throws InterruptedException {
        clearLoops(loops);
        for (Loop el : loops) {
            el.backEdges.clear();
            Set<GraphPart> uniqueRefs = new HashSet<>(el.loopContinue.refs);
            for (GraphPart r : uniqueRefs) {
                if (el.loopContinue.leadsTo(localData, this, code, r, loops, throwStates, true)) {
                    el.backEdges.add(r);
                }
            }
            el.phase = 1;
        }
        clearLoops(loops);
    }

    public void finalProcessStack(TranslateStack stack, List<GraphTargetItem> output, String path) {
    }

    private void finalProcessAll(List<GraphTargetItem> list, int level, FinalProcessLocalData localData, String path) throws InterruptedException {
        finalProcess(list, level, localData, path);
        for (GraphTargetItem item : list) {
            if (item instanceof Block) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    finalProcessAll(sub, level + 1, localData, path);
                }
            }
        }
        finalProcessAfter(list, level, localData, path);
    }

    private boolean processSubBlk(Block b, GraphTargetItem replacement) {
        boolean allSubPush = true;
        boolean atleastOne = false;
        for (List<GraphTargetItem> sub : b.getSubs()) {
            if (!sub.isEmpty()) {
                int lastPos = sub.size() - 1;

                GraphTargetItem last = sub.get(sub.size() - 1);
                GraphTargetItem br = null;

                if ((last instanceof BreakItem) && (sub.size() >= 2)) {
                    br = last;
                    lastPos--;
                    last = sub.get(lastPos);
                }
                if (last instanceof Block) {
                    if (!processSubBlk((Block) last, replacement)) {
                        allSubPush = false;
                    } else {
                        atleastOne = true;
                    }
                } else if (last instanceof PushItem) {
                    if (replacement != null) {
                        GraphTargetItem e2 = (((GraphTargetItem) replacement).clone());
                        e2.value = last.value;
                        sub.set(lastPos, e2);
                        if (br != null) {
                            sub.remove(sub.size() - 1);
                        }
                    }
                    atleastOne = true;
                } else if (!(last instanceof ExitItem)) {
                    allSubPush = false;
                }
            }
        }
        return allSubPush && atleastOne;
    }

    protected void finalProcessAfter(List<GraphTargetItem> list, int level, FinalProcessLocalData localData, String path) {
        if (list.size() >= 2) {
            if (list.get(list.size() - 1) instanceof ExitItem) {
                ExitItem e = (ExitItem) list.get(list.size() - 1);
                if (list.get(list.size() - 1).value instanceof PopItem) {
                    if (list.get(list.size() - 2) instanceof Block) {
                        Block b = (Block) list.get(list.size() - 2);
                        if (processSubBlk(b, (GraphTargetItem) e)) {
                            list.remove(list.size() - 1);
                        }
                    }
                }
            }
        }
    }

    protected void finalProcess(List<GraphTargetItem> list, int level, FinalProcessLocalData localData, String path) throws InterruptedException {

        //For detection based on debug line information
        boolean[] toDelete = new boolean[list.size()];
        for (int i = 0; i < list.size(); i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            GraphTargetItem itemI = list.get(i);
            if (itemI instanceof ForItem) {
                ForItem fori = (ForItem) itemI;
                int exprLine = fori.getLine();
                if (exprLine > 0) {
                    List<GraphTargetItem> forFirstCommands = new ArrayList<>();
                    for (int j = i - 1; j >= 0; j--) {
                        if (list.get(j).getLine() == exprLine && !(list.get(j) instanceof LoopItem /*to avoid recursion and StackOverflow*/)) {
                            forFirstCommands.add(0, list.get(j));
                            toDelete[j] = true;
                        } else {
                            break;
                        }
                    }
                    fori.firstCommands.addAll(0, forFirstCommands);
                }
            }

            if (itemI instanceof WhileItem) {
                WhileItem whi = (WhileItem) itemI;
                int whileExprLine = whi.getLine();
                if (whileExprLine > 0) {
                    List<GraphTargetItem> forFirstCommands = new ArrayList<>();
                    List<GraphTargetItem> forFinalCommands = new ArrayList<>();

                    for (int j = i - 1; j >= 0; j--) {
                        GraphTargetItem itemJ = list.get(j);
                        if (itemJ.getLine() == whileExprLine && !(itemJ instanceof LoopItem /*to avoid recursion and StackOverflow*/)) {
                            forFirstCommands.add(0, itemJ);
                            toDelete[j] = true;
                        } else {
                            break;
                        }
                    }
                    for (int j = whi.commands.size() - 1; j >= 0; j--) {
                        if (whi.commands.get(j).getLine() == whileExprLine && !(whi.commands.get(j) instanceof LoopItem /*to avoid recursion and StackOverflow*/)) {
                            forFinalCommands.add(0, whi.commands.remove(j));
                        } else {
                            break;
                        }
                    }
                    if (!forFirstCommands.isEmpty() || !forFinalCommands.isEmpty()) {
                        //Do not allow more than 2 first/final commands, since it can be obfuscated
                        if (forFirstCommands.size() > 2 || forFinalCommands.size() > 2) {
                            //put it back
                            for (int k = 0; k < forFirstCommands.size(); k++) {
                                toDelete[i - 1 - k] = false;
                            }
                            whi.commands.addAll(forFinalCommands); //put it back
                        } else if (whi.commands.isEmpty() && forFirstCommands.isEmpty()) {
                            //it would be for(;expr;commands) {}  which looks better as while(expr){commands}
                            whi.commands.addAll(forFinalCommands); //put it back
                        } else {
                            GraphTargetItem lastExpr = whi.expression.remove(whi.expression.size() - 1);
                            forFirstCommands.addAll(whi.expression);
                            list.set(i, new ForItem(whi.getSrc(), whi.getLineStartItem(), whi.loop, forFirstCommands, lastExpr, forFinalCommands, whi.commands));
                        }
                    }
                }
            }
        }

        for (int i = toDelete.length - 1; i >= 0; i--) {
            if (toDelete[i]) {
                list.remove(i);
            }
        }
    }

    private void expandGotos(List<GraphTargetItem> list) {
        if (!list.isEmpty() && (list.get(list.size() - 1) instanceof GotoItem)) {
            GotoItem gi = (GotoItem) list.get(list.size() - 1);
            if (gi.targetCommands != null) {
                list.remove(gi);
                if (gi.labelName != null) {
                    list.add(new LabelItem(null, gi.lineStartItem, gi.labelName));
                }
                list.addAll(gi.targetCommands);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem item = list.get(i);
            if (item instanceof Block) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    expandGotos(sub);
                }
            }
        }
    }

    private void processIfGotos2(List<List<GraphTargetItem>> alreadyProcessedBlocks, List<GotoItem> allGotos, List<GraphTargetItem> list, List<GraphTargetItem> rootList) {
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem item = list.get(i);
            if (item instanceof Block) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    processIfGotos2(alreadyProcessedBlocks, allGotos, sub, rootList);
                }
            }
            if (item instanceof GotoItem) {
                GotoItem gi = (GotoItem) item;
                loopblk:
                for (List<GraphTargetItem> blk : alreadyProcessedBlocks) {
                    for (int j = 0; j < blk.size(); j++) {
                        GraphTargetItem ti = blk.get(j);
                        if (ti instanceof LabelItem) {
                            LabelItem label = (LabelItem) ti;
                            if (label.labelName.equals(gi.labelName)) {
                                if (blk.get(blk.size() - 1) instanceof ExitItem) {
                                    int siz = blk.size();
                                    for (int k = 0; k < siz - j; k++) {
                                        list.add(i + 1 + k, blk.remove(j));
                                    }
                                    blk.add(j, list.remove(i));
                                }
                                break loopblk;
                            }
                        }
                    }
                }
            }
        }
        alreadyProcessedBlocks.add(list);
    }
    /**
     * if (xxx) { y ; goto a } else { z ; goto a }
     *
     * =>
     *
     * if (xxx) { y } else { z } goto a
     *
     */
    private void processIfGotos(List<GotoItem> allGotos, List<GraphTargetItem> list, List<GraphTargetItem> rootList) {
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem item = list.get(i);
            if (item instanceof Block) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    processIfGotos(allGotos, sub, rootList);
                }
            }
            if (item instanceof IfItem) {
                IfItem ii = (IfItem) item;
                if (!ii.onTrue.isEmpty() && !ii.onFalse.isEmpty()) {
                    if (ii.onTrue.get(ii.onTrue.size() - 1) instanceof GotoItem) {
                        if (ii.onFalse.get(ii.onFalse.size() - 1) instanceof GotoItem) {
                            for (int j = i + 1; j < list.size(); j++) {
                                list.remove(i + 1);
                            }
                        }
                    }
                }
                if (!ii.onTrue.isEmpty() && !ii.onFalse.isEmpty()) {
                    if (ii.onTrue.get(ii.onTrue.size() - 1) instanceof GotoItem) {
                        if (ii.onFalse.get(ii.onFalse.size() - 1) instanceof GotoItem) {
                            GotoItem gotoOnTrue = (GotoItem) ii.onTrue.get(ii.onTrue.size() - 1);
                            GotoItem gotoOnFalse = (GotoItem) ii.onFalse.get(ii.onFalse.size() - 1);
                            if (gotoOnTrue.labelName.equals(gotoOnFalse.labelName)) {
                                String labelOnTrue = gotoOnTrue.labelName;
                                String labelOnFalse = gotoOnFalse.labelName;
                                if (labelOnTrue != null && labelOnFalse != null) {
                                    if (labelOnTrue.equals(labelOnFalse)) {
                                        GotoItem gotoMerged;
                                        GotoItem gotoRemoved;
                                        if (gotoOnTrue.targetCommands != null) {
                                            gotoMerged = gotoOnTrue;
                                            gotoRemoved = gotoOnFalse;
                                        } else {
                                            gotoMerged = gotoOnFalse;
                                            gotoRemoved = gotoOnTrue;
                                        }
                                        ii.onTrue.remove(ii.onTrue.size() - 1);
                                        ii.onFalse.remove(ii.onFalse.size() - 1);
                                        list.add(i + 1, gotoMerged);
                                        allGotos.remove(gotoRemoved);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!ii.onTrue.isEmpty() && ii.onFalse.isEmpty()) {
                    if (ii.onTrue.get(ii.onTrue.size() - 1) instanceof GotoItem) {
                        GotoItem g1 = (GotoItem) ii.onTrue.get(ii.onTrue.size() - 1);
                        if (i + 1 < list.size()) {
                            if (list.get(i + 1) instanceof GotoItem) {

                                GotoItem g2 = (GotoItem) list.get(i + 1);
                                if (g1.labelName.equals(g2.labelName)) {
                                    ii.onTrue.remove(ii.onTrue.size() - 1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void processIfs(List<GraphTargetItem> list) {
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem item = list.get(i);
            if ((item instanceof LoopItem) && (item instanceof Block)) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    processIfs(sub);
                    checkContinueAtTheEnd(sub, ((LoopItem) item).loop);
                }
            } else if (item instanceof Block) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    processIfs(sub);
                }
            }
            if (item instanceof IfItem) {
                IfItem ifi = (IfItem) item;
                List<GraphTargetItem> onTrue = ifi.onTrue;
                List<GraphTargetItem> onFalse = ifi.onFalse;
                if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
                    if (onTrue.get(onTrue.size() - 1) instanceof ContinueItem) {
                        if (onFalse.get(onFalse.size() - 1) instanceof ContinueItem) {
                            if (((ContinueItem) onTrue.get(onTrue.size() - 1)).loopId == ((ContinueItem) onFalse.get(onFalse.size() - 1)).loopId) {
                                onTrue.remove(onTrue.size() - 1);
                                list.add(i + 1, onFalse.remove(onFalse.size() - 1));
                            }
                        }
                    }
                }

                if (i < list.size() - 1) {
                    if (list.get(i + 1) instanceof ContinueItem) {
                        if ((!onTrue.isEmpty()) && (onFalse.isEmpty())) {
                            if (onTrue.get(onTrue.size() - 1) instanceof ContinueItem) {
                                if (((ContinueItem) onTrue.get(onTrue.size() - 1)).loopId == ((ContinueItem) list.get(i + 1)).loopId) {
                                    onTrue.remove(onTrue.size() - 1);
                                }
                            }
                        }
                    }

                }

                if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
                    GraphTargetItem last = onTrue.get(onTrue.size() - 1);
                    if ((last instanceof ExitItem) || (last instanceof ContinueItem) || (last instanceof BreakItem)) {
                        list.addAll(i + 1, onFalse);
                        onFalse.clear();
                    }
                }

                if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
                    if ((onFalse.get(onFalse.size() - 1) instanceof ExitItem) || (onFalse.get(onFalse.size() - 1) instanceof BreakItem)) {
                        if (onTrue.get(onTrue.size() - 1) instanceof ContinueItem) {
                            list.add(i + 1, onTrue.remove(onTrue.size() - 1));
                        }
                    }
                }

                //Prefer continue/return/throw/break in onTrue rather than onFalse
                if (!onFalse.isEmpty()
                        && ((onFalse.get(onFalse.size() - 1) instanceof BreakItem)
                        || (onFalse.get(onFalse.size() - 1) instanceof ExitItem)
                        || (onFalse.get(onFalse.size() - 1) instanceof ContinueItem))
                        && !(onFalse.get(onFalse.size() - 1) instanceof ScriptEndItem)
                        && (onTrue.isEmpty() || !((onTrue.get(onTrue.size() - 1) instanceof BreakItem)
                        || (onTrue.get(onTrue.size() - 1) instanceof ExitItem)
                        || (onTrue.get(onTrue.size() - 1) instanceof ContinueItem)))) {
                    ifi.expression = ifi.expression.invert(null);
                    ifi.onTrue = onFalse;
                    ifi.onFalse = new ArrayList<>();
                    list.addAll(i + 1, onTrue);
                    onFalse = ifi.onFalse;
                    onTrue = ifi.onTrue;
                }

                if (i < list.size() - 1) {
                    if ((list.get(i + 1) instanceof BreakItem) && onFalse.isEmpty()) {
                        if (!onTrue.isEmpty() && (onTrue.get(onTrue.size() - 1) instanceof ContinueItem)) {
                            ifi.expression = ifi.expression.invert(null);
                            list.addAll(i + 2, ifi.onTrue);
                            ifi.onTrue.clear();
                            ifi.onTrue.add(list.remove(i + 1));
                        }
                    }
                }
            }
        }

        //Same continues in onTrue and onFalse gets continue on parent level
    }

    protected List<GraphPart> getLoopsContinuesPreAndBreaks(List<Loop> loops) {
        List<GraphPart> ret = new ArrayList<>();
        for (Loop l : loops) {
            if (l.loopContinue != null) {
                ret.add(l.loopContinue);
            }
            if (l.loopPreContinue != null) {
                ret.add(l.loopPreContinue);
            }
            if (l.loopBreak != null) {
                ret.add(l.loopBreak);
            }
        }
        return ret;
    }

    protected List<GraphPart> getLoopsContinuesAndPre(List<Loop> loops) {
        List<GraphPart> ret = new ArrayList<>();
        for (Loop l : loops) {
            if (l.loopContinue != null) {
                ret.add(l.loopContinue);
            }
            if (l.loopPreContinue != null) {
                ret.add(l.loopPreContinue);
            }
        }
        return ret;
    }

    protected List<GraphPart> getLoopsContinues(List<Loop> loops) {
        List<GraphPart> ret = new ArrayList<>();
        for (Loop l : loops) {
            if (l.loopContinue != null) {
                ret.add(l.loopContinue);
            }
            /*if (l.loopPreContinue != null) {
             ret.add(l.loopPreContinue);
             }*/
        }
        return ret;
    }

    protected GraphTargetItem checkLoop(List<GraphTargetItem> output, GraphPart part, List<GraphPart> stopPart, List<Loop> loops, List<ThrowState> throwStates) {
        if (stopPart.contains(part)) {
            return null;
        }

        GraphSourceItem firstIns = null;
        if (part != null) {
            if (part.start >= 0 && part.start < code.size()) {
                firstIns = code.get(part.start);
            }
        }

        for (Loop l : loops) {
            if (l.phase == 2) {
                continue;
            }
            if (l.loopContinue == part) {
                return (new ContinueItem(null, firstIns, l.id));
            }
            if (l.loopBreak == part) {
                return (new BreakItem(null, firstIns, l.id));
            }
        }
        return null;
    }

    private void checkContinueAtTheEnd(List<GraphTargetItem> commands, Loop loop) {
        if (!commands.isEmpty()) {
            int i = commands.size() - 1;
            for (; i >= 0; i--) {
                if (commands.get(i) instanceof ContinueItem) {
                    continue;
                }
                if (commands.get(i) instanceof BreakItem) {
                    continue;
                }
                break;
            }
            if (i < commands.size() - 1) {
                for (int k = i + 2; k < commands.size(); k++) {
                    commands.remove(k);
                }
            }
            if (commands.get(commands.size() - 1) instanceof ContinueItem) {
                if (((ContinueItem) commands.get(commands.size() - 1)).loopId == loop.id) {
                    commands.remove(commands.size() - 1);
                }
            }
        }
    }

    protected boolean isEmpty(List<GraphTargetItem> output) {
        if (output.isEmpty()) {
            return true;
        }
        if (output.size() == 1) {
            if (output.get(0) instanceof MarkItem) {
                return true;
            }
        }
        return false;
    }

    protected List<GraphTargetItem> check(List<GraphTargetItem> currentRet, List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, GraphSource code, BaseLocalData localData, Set<GraphPart> allParts, TranslateStack stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, List<GraphTargetItem> output, Loop currentLoop, int staticOperation, String path) throws InterruptedException {
        return null;
    }

    protected GraphPart checkPartWithOutput(List<GraphTargetItem> output, TranslateStack stack, BaseLocalData localData, GraphPart prev, GraphPart part, Set<GraphPart> allParts) {
        return checkPart(stack, localData, prev, part, allParts);
    }

    protected GraphPart checkPart(TranslateStack stack, BaseLocalData localData, GraphPart prev, GraphPart part, Set<GraphPart> allParts) {
        return part;
    }

    //@SuppressWarnings("unchecked")
    protected GraphTargetItem translatePartGetStack(BaseLocalData localData, GraphPart part, TranslateStack stack, int staticOperation) throws InterruptedException {
        stack = (TranslateStack) stack.clone();
        translatePart(localData, part, stack, staticOperation, null);
        return stack.pop();
    }

    protected List<GraphTargetItem> translatePart(BaseLocalData localData, GraphPart part, TranslateStack stack, int staticOperation, String path) throws InterruptedException {
        List<GraphPart> sub = part.getSubParts();
        List<GraphTargetItem> ret = new ArrayList<>();
        int end;
        for (GraphPart p : sub) {
            if (p.end == -1) {
                p.end = code.size() - 1;
            }
            if (p.start == code.size()) {
                continue;
            } else if (p.end == code.size()) {
                p.end--;
            }
            end = p.end;
            int start = p.start;
            ret.addAll(code.translatePart(part, localData, stack, start, end, staticOperation, path));
        }
        return ret;
    }

    private void markBranchEnd(List<GraphTargetItem> items) {
        if (!items.isEmpty()) {
            if (items.get(items.size() - 1) instanceof BreakItem) {
                return;
            }
            if (items.get(items.size() - 1) instanceof ContinueItem) {
                return;
            }
            if (items.get(items.size() - 1) instanceof ExitItem) {
                return;
            }
        }
        items.add(new MarkItem("finish"));
    }

    private static GraphTargetItem getLastNoEnd(List<GraphTargetItem> list) {
        if (list.isEmpty()) {
            return null;
        }
        if (list.get(list.size() - 1) instanceof ScriptEndItem) {
            if (list.size() >= 2) {
                return list.get(list.size() - 2);
            }
            return list.get(list.size() - 1);
        }
        return list.get(list.size() - 1);
    }

    private static void removeLastNoEnd(List<GraphTargetItem> list) {
        if (list.isEmpty()) {
            return;
        }
        if (list.get(list.size() - 1) instanceof ScriptEndItem) {
            if (list.size() >= 2) {
                list.remove(list.size() - 2);
            }
            return;
        }
        list.remove(list.size() - 1);
    }

    protected List<GraphTargetItem> printGraph(List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, BaseLocalData localData, TranslateStack stack, Set<GraphPart> allParts, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, int staticOperation, String path) throws InterruptedException {
        return printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, parent, part, stopPart, stopPartKind, loops, throwStates, null, staticOperation, path, 0);
    }

    protected GraphTargetItem checkLoop(List<GraphTargetItem> output, LoopItem loopItem, BaseLocalData localData, List<Loop> loops, List<ThrowState> throwStates) {
        return loopItem;
    }

    private void clearLoops(List<Loop> loops) {
        for (Loop l : loops) {
            l.phase = 0;
        }
    }

    private void clearThrowStates(List<ThrowState> throwStates) {
        for (ThrowState ts : throwStates) {
            ts.state = 0;
        }
    }

    private void getLoops(BaseLocalData localData, GraphPart part, List<Loop> loops, List<ThrowState> throwStates, List<GraphPart> stopPart) throws InterruptedException {
        clearLoops(loops);
        clearThrowStates(throwStates);
        getLoopsWalk(localData, part, loops, throwStates, stopPart, true, new ArrayList<>(), 0);
        clearLoops(loops);
        clearThrowStates(throwStates);
    }

    protected boolean canBeBreakCandidate(BaseLocalData localData, GraphPart part, List<ThrowState> throwStates) {
        return true;
    }

    protected void checkGetLoopsPart(GraphPart part) {

    }

    private void findPartsOutsideTry(ThrowState ts, GraphPart part, List<GraphPart> ret, Set<GraphPart> visited) {
        if (visited.contains(part)) {
            return;
        }
        visited.add(part);
        if (!ts.throwingParts.contains(part)) {
            ret.add(part);
            return;
        }
        for (GraphPart n : part.nextParts) {
            findPartsOutsideTry(ts, n, ret, visited);
        }
    }

    private void getLoopsWalk(BaseLocalData localData, GraphPart part, List<Loop> loops, List<ThrowState> throwStates, List<GraphPart> stopPart, boolean first, List<GraphPart> visited, int level) throws InterruptedException {

        if (part == null) {
            return;
        }

        part = checkPart(null, localData, null, part, null);
        if (part == null) {
            return;
        }
        if (!visited.contains(part)) {
            visited.add(part);
        }
        checkGetLoopsPart(part);

        for (ThrowState ts : throwStates) {
            if (ts.throwingParts.contains(part)) {
                ts.state = 1;
            }
        }

        if (debugGetLoops) {
            System.err.println("getloops: " + part);
        }
        //List<GraphPart> loopContinues = getLoopsContinues(loops);
        Loop lastP1 = null;
        for (Loop el : loops) {
            if ((el.phase == 1) && el.loopBreak == null) { //break not found yet
                if (el.loopContinue != part) {
                    lastP1 = el;

                } else {
                    lastP1 = null;
                }

            }
        }

        boolean canBeCandidate = true;
        if (lastP1 != null) {
            for (ThrowState ts : throwStates) {
                if (!ts.catchParts.contains(lastP1.loopContinue) && ts.catchParts.contains(part)) {
                    canBeCandidate = false;
                    break;
                }
            }
        }
        if (lastP1 != null && canBeCandidate && canBeBreakCandidate(localData, part, throwStates)) {
            if (lastP1.breakCandidates.contains(part)) {
                lastP1.breakCandidates.add(part);
                lastP1.breakCandidatesLevels.add(level);
                return;
            } else {
                List<Loop> loops2 = new ArrayList<>(loops);
                loops2.remove(lastP1);
                if (!part.leadsTo(localData, this, code, lastP1.loopContinue, loops2, throwStates, true)) {
                    if (lastP1.breakCandidatesLocked == 0) {
                        if (debugGetLoops) {
                            System.err.println("added breakCandidate " + part + " to " + lastP1);
                        }

                        lastP1.breakCandidates.add(part);
                        lastP1.breakCandidatesLevels.add(level);
                        return;
                    }
                }
            }
        }

        for (Loop el : loops) {
            if (el.loopContinue == part) {
                return;
            }
        }

        if (stopPart != null && stopPart.contains(part)) {
            return;
        }
        part.level = level;

        boolean isLoop = part.leadsTo(localData, this, code, part, loops, throwStates, true);
        Loop currentLoop = null;
        if (isLoop) {
            currentLoop = new Loop(loops.size(), part, null);
            currentLoop.phase = 1;
            loops.add(currentLoop);
            //loopContinues.add(part);
        }

        for (ThrowState ts : throwStates) {
            if (ts.throwingParts.contains(part)) {
                GraphPart t = ts.targetPart;
                if (!visited.contains(t)) {
                    getLoopsWalk(localData, t, loops, throwStates, stopPart, false, visited, level);
                }
            }
        }

        if (part.nextParts.size() == 2 && !partIsSwitch(part)) {

            List<GraphPart> nps;/* = new ArrayList<>(part.nextParts);
             for(int i=0;i<nps.size();i++){
             nps.set(i,getNextNoJump(nps.get(i),localData));
             }
             if(nps.get(0) == nps.get(1)){
             nps = part.nextParts;
             }*/

            nps = part.nextParts;
            GraphPart next = getCommonPart(localData, part, nps, loops, throwStates);
            List<GraphPart> stopPart2 = stopPart == null ? new ArrayList<>() : new ArrayList<>(stopPart);
            if (next != null) {
                stopPart2.add(next);
            }
            if (next != nps.get(0)) {
                getLoopsWalk(localData, nps.get(0), loops, throwStates, stopPart2, false, visited, level + 1);
            }
            if (next != nps.get(1)) {
                getLoopsWalk(localData, nps.get(1), loops, throwStates, stopPart2, false, visited, level + 1);
            }
            if (next != null) {
                getLoopsWalk(localData, next, loops, throwStates, stopPart, false, visited, level);
            }
        } else if (part.nextParts.size() > 2 || partIsSwitch(part)) {
            GraphPart next = getMostCommonPart(localData, part.nextParts, loops, throwStates);

            for (GraphPart p : part.nextParts) {
                List<GraphPart> stopPart2 = stopPart == null ? new ArrayList<>() : new ArrayList<>(stopPart);
                if (next != null) {
                    stopPart2.add(next);
                }
                for (GraphPart p2 : part.nextParts) {
                    if (p2 == p) {
                        continue;
                    }
                    if (!stopPart2.contains(p2)) {
                        stopPart2.add(p2);
                    }
                }
                if (next != p) {
                    getLoopsWalk(localData, p, loops, throwStates, stopPart2, false, visited, level + 1);
                }
            }
            if (next != null) {
                getLoopsWalk(localData, next, loops, throwStates, stopPart, false, visited, level);
            }
        } else if (part.nextParts.size() == 1) {
            getLoopsWalk(localData, part.nextParts.get(0), loops, throwStates, stopPart, false, visited, level);
        }

        /*List<Loop> loops2 = new ArrayList<>(loops);
        for (Loop l : loops2) {
            l.breakCandidatesLocked++;
        }*/
 /*for (Loop l : loops2) {
            l.breakCandidatesLocked--;
        }*/
        if (isLoop && currentLoop != null) {
            GraphPart found;

            for (int i = 0; i < currentLoop.breakCandidates.size(); i++) {
                GraphPart ch = checkPart(null, localData, null, currentLoop.breakCandidates.get(i), null);
                if (ch == null) {
                    currentLoop.breakCandidates.remove(i);
                    currentLoop.breakCandidatesLevels.remove(i);
                    i--;
                }
            }
            if (debugGetLoops) {
                System.err.println("loop " + currentLoop + " break candidates:");
                for (GraphPart cand : currentLoop.breakCandidates) {
                    System.err.println("- " + cand);
                }
            }

            List<Integer> contThrowStates = new ArrayList<>();

            for (ThrowState ts : throwStates) {
                if (ts.throwingParts.contains(currentLoop.loopContinue)) {
                    contThrowStates.add(ts.exceptionId);
                }
            }

            Map<GraphPart, Integer> removed = new HashMap<>();

            loopcand:
            for (int c = 0; c < currentLoop.breakCandidates.size(); c++) {
                GraphPart cand = currentLoop.breakCandidates.get(c);
                List<Integer> candThrowStates = new ArrayList<>();
                for (ThrowState ts : throwStates) {
                    if (ts.throwingParts.contains(cand)) {
                        if (contThrowStates.equals(candThrowStates)) {
                            //adding new ts
                            //this means breakcandidate is in nested try
                            if (debugGetLoops) {
                                System.err.println("candidate " + cand + " is in inner try, getting outside parts");
                            }
                            List<GraphPart> outsideTry = new ArrayList<>();
                            findPartsOutsideTry(ts, cand, outsideTry, new HashSet<>());

                            for (int j = outsideTry.size() - 1; j >= 0; j--) {
                                if (!canBeBreakCandidate(localData, outsideTry.get(j), throwStates)) {
                                    outsideTry.remove(j);
                                }
                            }
                            if (debugGetLoops) {
                                for (GraphPart op : outsideTry) {
                                    System.err.println("- outsidepart " + op);
                                }
                            }
                            int bcLevel = currentLoop.breakCandidatesLevels.get(c);
                            currentLoop.breakCandidates.remove(c);

                            currentLoop.breakCandidates.addAll(c, outsideTry);
                            currentLoop.breakCandidatesLevels.remove(c);

                            removed.put(cand, bcLevel);
                            for (int j = 0; j < outsideTry.size(); j++) {
                                currentLoop.breakCandidatesLevels.add(c, bcLevel);
                            }

                            c--;
                            continue loopcand;
                        }
                        candThrowStates.add(ts.exceptionId);
                    }
                }
            }

            do {
                found = null;

                loopcand:
                for (int c1 = 0; c1 < currentLoop.breakCandidates.size(); c1++) {
                    GraphPart cand = currentLoop.breakCandidates.get(c1);
                    for (int c2 = 0; c2 < currentLoop.breakCandidates.size(); c2++) {
                        GraphPart cand2 = currentLoop.breakCandidates.get(c2);
                        if (cand == cand2) {
                            continue;
                        }
                        if (cand.leadsTo(localData, this, code, cand2, loops, throwStates, true)) {

                            int curLevl = currentLoop.breakCandidatesLevels.get(c1);
                            int curLev2 = currentLoop.breakCandidatesLevels.get(c2);
                            /*
                            found = cand;
                            int lev2 = currentLoop.breakCandidatesLevels.get(c2);
                            currentLoop.breakCandidates.set(c1, cand2);
                            currentLoop.breakCandidatesLevels.set(c1, lev2);*/
                            int lev1 = Integer.MAX_VALUE;
                            int lev2 = Integer.MAX_VALUE;
                            for (int i = 0; i < currentLoop.breakCandidates.size(); i++) {
                                if (currentLoop.breakCandidates.get(i) == cand) {
                                    if (currentLoop.breakCandidatesLevels.get(i) < lev1) {
                                        lev1 = currentLoop.breakCandidatesLevels.get(i);
                                    }
                                }
                                if (currentLoop.breakCandidates.get(i) == cand2) {
                                    if (currentLoop.breakCandidatesLevels.get(i) < lev2) {
                                        lev2 = currentLoop.breakCandidatesLevels.get(i);
                                    }
                                }
                            }
                            //
                            GraphPart other;
                            int curLev;
                            if (lev1 <= lev2) {
                                found = cand2;
                                other = cand;
                                curLev = curLevl;
                            } else {
                                found = cand;
                                other = cand2;
                                curLev = curLev2;
                            }

                            currentLoop.breakCandidates.add(other);
                            currentLoop.breakCandidatesLevels.add(curLev);

                            break loopcand;
                        }
                    }
                }
                if (found != null) {
                    int maxlevel = 0;
                    while (currentLoop.breakCandidates.contains(found)) {
                        int ind = currentLoop.breakCandidates.indexOf(found);
                        currentLoop.breakCandidates.remove(ind);
                        int lev = currentLoop.breakCandidatesLevels.remove(ind);
                        if (lev > maxlevel) {
                            maxlevel = lev;
                        }
                    }
                    if (removed.containsKey(found)) {
                        if (removed.get(found) > maxlevel) {
                            maxlevel = removed.get(found);
                        }
                    }
                    removed.put(found, maxlevel);
                }
            } while ((found != null) && (currentLoop.breakCandidates.size() > 1));

            Map<GraphPart, Integer> count = new HashMap<>();
            GraphPart winner = null;
            int winnerCount = 0;
            for (GraphPart cand : currentLoop.breakCandidates) {

                if (!count.containsKey(cand)) {
                    count.put(cand, 0);
                }
                count.put(cand, count.get(cand) + 1);
                boolean otherBreakCandidate = false;
                for (Loop el : loops) {
                    if (el == currentLoop) {
                        continue;
                    }
                    if (el.breakCandidates.contains(cand)) {
                        otherBreakCandidate = true;
                        break;
                    }
                }
                if (otherBreakCandidate) {
                } else if (count.get(cand) > winnerCount) {
                    winnerCount = count.get(cand);
                    winner = cand;
                } else if (count.get(cand) == winnerCount && winner != null) {
                    if (cand.path.length() < winner.path.length()) {
                        winner = cand;
                    }
                }
            }
            for (int i = 0; i < currentLoop.breakCandidates.size(); i++) {
                GraphPart cand = currentLoop.breakCandidates.get(i);
                if (cand != winner) {
                    int lev = currentLoop.breakCandidatesLevels.get(i);
                    if (removed.containsKey(cand)) {
                        if (removed.get(cand) > lev) {
                            lev = removed.get(cand);
                        }
                    }
                    removed.put(cand, lev);
                }
            }
            currentLoop.loopBreak = winner;
            currentLoop.phase = 2;
            boolean start = false;
            for (int l = 0; l < loops.size(); l++) {
                Loop el = loops.get(l);
                if (start) {
                    el.phase = 1;
                }
                if (el == currentLoop) {
                    start = true;
                }
            }
            List<GraphPart> removedVisited = new ArrayList<>();
            for (GraphPart r : removed.keySet()) {
                if (removedVisited.contains(r)) {
                    continue;
                }
                getLoopsWalk(localData, r, loops, throwStates, stopPart, false, visited, removed.get(r));
                removedVisited.add(r);
            }
            start = false;
            for (int l = 0; l < loops.size(); l++) {
                Loop el = loops.get(l);
                if (el == currentLoop) {
                    start = true;
                }
                if (start) {
                    el.phase = 2;
                }
            }
            getLoopsWalk(localData, currentLoop.loopBreak, loops, throwStates, stopPart, false, visited, level);
        }
    }

    private void getContinuesCommands(List<GraphTargetItem> commands, List<List<GraphTargetItem>> result, long loopId) {
        for (GraphTargetItem ti : commands) {
            if (ti instanceof ContinueItem) {
                ContinueItem ci = (ContinueItem) ti;
                if (ci.loopId == loopId) {
                    result.add(commands);
                }
            }
            if (ti instanceof Block) {
                Block bl = (Block) ti;
                for (List<GraphTargetItem> subCommands : bl.getSubs()) {
                    getContinuesCommands(subCommands, result, loopId);
                }
            }
        }
    }

    protected List<GraphPart> getNextParts(BaseLocalData localData, GraphPart part) {
        return part.nextParts;
    }

    protected boolean checkPartOutput(List<GraphTargetItem> currentRet, List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, GraphSource code, BaseLocalData localData, Set<GraphPart> allParts, TranslateStack stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, Loop currentLoop, int staticOperation, String path, int recursionLevel) throws InterruptedException {
        return false;
    }

    protected boolean partIsLoopContBrePre(GraphPart part, List<Loop> loops, List<ThrowState> throwStates) {
        for (Loop el : loops) {
            if (el.phase == 1) {
                if (el.loopBreak == part) {
                    return true;
                }
                if (el.loopContinue == part) {
                    return true;
                }
                if (el.loopPreContinue == part) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean canHandleLoop(BaseLocalData localData, GraphPart part, List<Loop> loops, List<ThrowState> throwStates) {
        return true;
    }

    protected boolean canHandleVisited(BaseLocalData localData, GraphPart part) {
        return true;
    }

    protected List<GraphTargetItem> printGraph(List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, BaseLocalData localData, TranslateStack stack, Set<GraphPart> allParts, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, List<GraphTargetItem> ret, int staticOperation, String path, int recursionLevel) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        if (stopPart == null) {
            stopPart = new ArrayList<>();
        }
        if (stopPartKind == null) {
            stopPartKind = new ArrayList<>();
        }
        if (recursionLevel > allParts.size() + 1) {
            throw new TranslateException("printGraph max recursion level reached.");
        }

        if (ret == null) {
            ret = new GraphPartMarkedArrayList<>();
        }

        //try {
        if (debugPrintGraph) {
            System.err.println("PART " + part + " nextsize:" + getNextParts(localData, part).size());
        }

        /*while (((part != null) && (part.getHeight() == 1)) && (code.size() > part.start) && (code.get(part.start).isJump())) {  //Parts with only jump in it gets ignored

         if (part == stopPart) {
         return ret;
         }
         GraphTargetItem lop = checkLoop(getNextParts(localData, part).get(0), stopPart, loops);
         if (lop == null) {
         part = getNextParts(localData, part).get(0);
         } else {
         break;
         }
         }*/
        if (part == null) {
            return ret;
        }
        part = checkPartWithOutput(ret, stack, localData, parent, part, allParts);
        if (part == null) {
            return ret;
        }

        if (part.ignored) {
            return ret;
        }

        //List<GraphPart> loopContinues = getLoopsContinues(loops);
        boolean isLoop = false;
        Loop currentLoop = null;

        boolean vCanHandleLoop = canHandleLoop(localData, part, loops, throwStates);
        Loop ignoredLoop = null;
        for (Loop el : loops) {
            if ((el.loopContinue == part) && (el.phase == 0)) {
                if (vCanHandleLoop) {
                    currentLoop = el;
                    currentLoop.phase = 1;
                    isLoop = true;
                } else {
                    ignoredLoop = el;
                }

                break;
            }
        }

        if (debugPrintGraph) {
            System.err.println("loopsize:" + loops.size());
        }
        for (int l = loops.size() - 1; l >= 0; l--) {
            Loop el = loops.get(l);
            if (el == ignoredLoop) {
                if (debugPrintGraph) {
                    System.err.println("ignoring to be loop " + el);
                }
                continue;
            }
            if (el == currentLoop) {
                if (debugPrintGraph) {
                    System.err.println("ignoring current loop " + el);
                }
                continue;
            }
            if (el.phase != 1) {
                if (debugPrintGraph) {
                    System.err.println("ignoring loop " + el);
                }
                continue;
            }
            if (el.loopBreak == part) {
                if (currentLoop != null) {
                    currentLoop.phase = 0;
                }
                if (debugPrintGraph) {
                    System.err.println("Adding break");
                }
                ret.add(new BreakItem(null, localData.lineStartInstruction, el.id));
                return ret;
            }
            if (el.loopPreContinue == part) {
                if (currentLoop != null) {
                    currentLoop.phase = 0;
                }
                if (debugPrintGraph) {
                    System.err.println("Adding precontinue");
                }
                ret.add(new ContinueItem(null, localData.lineStartInstruction, el.id));
                return ret;
            }
            if (el.loopContinue == part) {
                if (currentLoop != null) {
                    currentLoop.phase = 0;
                }
                if (debugPrintGraph) {
                    System.err.println("Adding continue");
                }
                ret.add(new ContinueItem(null, localData.lineStartInstruction, el.id));
                return ret;
            }
        }

        if (debugPrintGraph) {
            System.err.println("stopParts: " + pathToString(stopPart));
        }

        if (stopPart.contains(part)) {

            boolean hasBlockClosesAfter = false;
            loopi:
            for (int i = 0; i < stopPartKind.size(); i++) {
                if (stopPart.get(i) == part) {
                    for (int j = i + 1; j < stopPartKind.size(); j++) {
                        if (stopPart.get(j) != part) {
                            if (stopPartKind.get(j) == StopPartKind.BLOCK_CLOSE) {
                                hasBlockClosesAfter = true;
                                break loopi;
                            }
                        }
                    }
                }
            }

            if (!hasBlockClosesAfter) {
                if (currentLoop != null) {
                    currentLoop.phase = 0;
                }
                if (debugPrintGraph) {
                    System.err.println("Stopped on part " + part);
                }
                return ret;
            }
        }

        if (code.size() <= part.start) {
            ret.add(new ScriptEndItem());
            return ret;
        }

        boolean vCanHandleVisited = canHandleVisited(localData, part);

        if (vCanHandleVisited) {
            if (visited.contains(part)) {
                String labelName = "addr" + part.start;
                List<GraphTargetItem> firstCode = partCodes.get(part);
                int firstCodePos = partCodePos.get(part);
                if (firstCodePos > firstCode.size()) {
                    firstCodePos = firstCode.size();
                }
                if (firstCode instanceof GraphPartMarkedArrayList) {
                    GraphPartMarkedArrayList<GraphTargetItem> markedFirstCode = (GraphPartMarkedArrayList<GraphTargetItem>) firstCode;
                    firstCodePos = markedFirstCode.indexOfPart(part);
                    if (firstCodePos == -1) {
                        firstCodePos = firstCode.size();
                    }
                    ((GraphPartMarkedArrayList<GraphTargetItem>) firstCode).startPart(part);
                }

                if (firstCode.size() > firstCodePos && (firstCode.get(firstCodePos) instanceof LabelItem)) {
                    labelName = ((LabelItem) firstCode.get(firstCodePos)).labelName;
                } else {
                    firstCode.add(firstCodePos, new LabelItem(null, localData.lineStartInstruction, labelName));
                }
                ret.add(new GotoItem(null, localData.lineStartInstruction, labelName));
                return ret;
            } else {
                visited.add(part);
                partCodes.put(part, ret);
                partCodePos.put(part, ret.size());
            }
        }
        List<GraphTargetItem> currentRet = ret;
        UniversalLoopItem loopItem = null;
        TranslateStack sPreLoop = stack;
        if (isLoop) {
            //makeAllCommands(currentRet, stack);
            stack = (TranslateStack) stack.clone();

            //hack for as1/2 for..in to get enumeration through
            GraphTargetItem topBsr = !stack.isEmpty() && (stack.peek() instanceof BranchStackResistant) ? stack.peek() : null;
            stack.clear();
            if (topBsr != null) {
                stack.push(topBsr);
            }
            loopItem = new UniversalLoopItem(null, localData.lineStartInstruction, currentLoop, new ArrayList<>());
            //loopItem.commands=printGraph(visited, localData, stack, allParts, parent, part, stopPart, loops);
            currentRet.add(loopItem);
            currentRet = loopItem.commands;
            //return ret;
        }

        boolean parseNext = true;

        //****************************DECOMPILING PART*************
        GraphPartMarkedArrayList<GraphTargetItem> output = new GraphPartMarkedArrayList<>();

        output.startPart(part);
        if (currentRet instanceof GraphPartMarkedArrayList) {
            ((GraphPartMarkedArrayList) currentRet).startPart(part);
        }
        if (checkPartOutput(currentRet, foundGotos, partCodes, partCodePos, visited, code, localData, allParts, stack, parent, part, stopPart, stopPartKind, loops, throwStates, currentLoop, staticOperation, path, recursionLevel)) {
            parseNext = false;
        } else {
            output.addAll(code.translatePart(part, localData, stack, part.start, part.end, staticOperation, path));
            if ((part.end >= code.size() - 1) && getNextParts(localData, part).isEmpty()) {
                output.add(new ScriptEndItem());
            }
        }

        if (parseNext) {
            List<GraphTargetItem> retCheck = check(currentRet, foundGotos, partCodes, partCodePos, visited, code, localData, allParts, stack, parent, part, stopPart, stopPartKind, loops, throwStates, output, currentLoop, staticOperation, path);
            if (retCheck != null) {
                if (!retCheck.isEmpty()) {
                    currentRet.addAll(retCheck);
                }
                parseNext = false;
                //return ret;
            } else {
                currentRet.addAll(output);
            }
        }
//********************************END PART DECOMPILING
        if (parseNext) {

            if (getNextParts(localData, part).size() > 2 || partIsSwitch(part)) {
                GraphTargetItem originalSwitchedItem = stack.pop();
                makeAllCommands(currentRet, stack);
                GraphTargetItem switchedItem = originalSwitchedItem;
                if ((switchedItem instanceof PopItem) && !currentRet.isEmpty() && (currentRet.get(currentRet.size() - 1) instanceof IfItem)) {
                    switchedItem = currentRet.get(currentRet.size() - 1);
                }

                List<GraphTargetItem> caseValues = new ArrayList<>();
                List<List<GraphTargetItem>> caseCommands = new ArrayList<>();
                List<Integer> valueMappings = new ArrayList<>();
                boolean first = false;
                int pos;

                Map<Integer, GraphTargetItem> caseExpressions = new HashMap<>();
                Map<Integer, GraphTargetItem> caseExpressionLeftSides = new HashMap<>();
                Map<Integer, GraphTargetItem> caseExpressionRightSides = new HashMap<>();

                GraphTargetItem it = switchedItem;
                int defaultBranch = 0;
                boolean hasExpr = false;
                List<GraphTargetItem> commaCommands = new ArrayList<>();
                Map<Integer, List<GraphTargetItem>> caseCommaCommands = new HashMap<>();

                while ((it instanceof TernarOpItem) || (it instanceof IfItem)) {

                    if (it instanceof IfItem) {
                        IfItem ii = (IfItem) it;
                        if (ii.expression instanceof EqualsTypeItem) {
                            if (!ii.onFalse.isEmpty() && !ii.onTrue.isEmpty()
                                    && ii.onTrue.get(ii.onTrue.size() - 1) instanceof PushItem
                                    && ii.onTrue.get(ii.onTrue.size() - 1).value instanceof IntegerValueTypeItem) {
                                int cpos = ((IntegerValueTypeItem) ii.onTrue.get(ii.onTrue.size() - 1).value).intValue();
                                caseCommaCommands.put(cpos, commaCommands);
                                caseExpressionLeftSides.put(cpos, ((EqualsTypeItem) ii.expression).getLeftSide());
                                caseExpressionRightSides.put(cpos, ((EqualsTypeItem) ii.expression).getRightSide());
                                commaCommands = new ArrayList<>();
                                for (int f = 0; f < ii.onFalse.size() - 1; f++) {
                                    commaCommands.add(ii.onFalse.get(f));
                                }
                                it = ii.onFalse.get(ii.onFalse.size() - 1);
                                if (it instanceof PushItem) {
                                    it = it.value;
                                }
                            } else {
                                break;
                            }
                        } else if (ii.expression instanceof FalseItem && !ii.onFalse.isEmpty()) {
                            it = ii.onFalse.get(ii.onFalse.size() - 1);
                        } else if (ii.expression instanceof TrueItem && !ii.onTrue.isEmpty()) {
                            it = ii.onTrue.get(ii.onTrue.size() - 1);
                        } else {
                            break;
                        }
                    } else if (it instanceof TernarOpItem) {
                        TernarOpItem to = (TernarOpItem) it;
                        if (to.expression instanceof EqualsTypeItem) {
                            if (to.onTrue instanceof IntegerValueTypeItem) {
                                int cpos = ((IntegerValueTypeItem) to.onTrue).intValue();
                                caseExpressionLeftSides.put(cpos, ((EqualsTypeItem) to.expression).getLeftSide());
                                caseExpressionRightSides.put(cpos, ((EqualsTypeItem) to.expression).getRightSide());
                                caseCommaCommands.put(cpos, commaCommands);
                                commaCommands = new ArrayList<>();
                                it = to.onFalse;
                            } else {
                                break;
                            }
                        } else if (to.expression instanceof FalseItem) {
                            it = to.onFalse;
                        } else if (to.expression instanceof TrueItem) {
                            it = to.onTrue;
                        } else {
                            break;
                        }
                    }
                }

                if (switchedItem != originalSwitchedItem && !caseExpressionRightSides.isEmpty()) {
                    currentRet.remove(currentRet.size() - 1);
                } else {
                    switchedItem = originalSwitchedItem;
                }

                //int ignoredBranch = -1;
                if ((it instanceof IntegerValueTypeItem) && !(switchedItem instanceof IntegerValueTypeItem)) {
                    defaultBranch = ((IntegerValueTypeItem) it).intValue();
                }

                Map<Integer, GraphTargetItem> caseExpressionOtherSides = caseExpressions;
                if (!caseExpressionRightSides.isEmpty()) {
                    GraphTargetItem firstItem;
                    firstItem = (GraphTargetItem) caseExpressionRightSides.values().toArray()[0];
                    boolean sameRight = true;
                    for (GraphTargetItem cit : caseExpressionRightSides.values()) {
                        if (!cit.equals(firstItem)) {
                            sameRight = false;
                            break;
                        }
                    }

                    if (sameRight) {
                        caseExpressions = caseExpressionLeftSides;
                        caseExpressionOtherSides = caseExpressionRightSides;
                        switchedItem = firstItem;
                        hasExpr = true;
                    } else {
                        firstItem = (GraphTargetItem) caseExpressionLeftSides.values().toArray()[0];

                        boolean sameLeft = true;
                        for (GraphTargetItem cit : caseExpressionLeftSides.values()) {
                            if (!cit.equals(firstItem)) {
                                sameLeft = false;
                                break;
                            }
                        }
                        if (sameLeft) {
                            caseExpressions = caseExpressionRightSides;
                            caseExpressionOtherSides = caseExpressionLeftSides;
                            switchedItem = firstItem;
                            hasExpr = true;
                        }
                    }
                }

                first = true;
                pos = 0;
                //This is tied to AS3 switch implementation which has nextparts switched from index 1. TODO: Make more universal

                GraphPart defaultPart = hasExpr ? getNextParts(localData, part).get(1 + defaultBranch) : getNextParts(localData, part).get(0);
                List<GraphPart> caseBodyParts = new ArrayList<>();
                for (int i = 1; i < getNextParts(localData, part).size(); i++) {
                    if (!hasExpr) {
                        if (getNextParts(localData, part).get(i) == defaultPart) {
                            pos++;
                            continue;
                        }
                        caseValues.add(new IntegerValueItem(null, localData.lineStartInstruction, pos));
                    } else if (caseExpressions.containsKey(pos)) {
                        GraphTargetItem expr = caseExpressions.get(pos);
                        if (caseCommaCommands.get(pos).size() > 0) {
                            List<GraphTargetItem> exprCommaCommands = new ArrayList<>(caseCommaCommands.get(pos));
                            exprCommaCommands.add(expr);
                            expr = new CommaExpressionItem(null, expr.lineStartItem, exprCommaCommands);
                        }
                        caseValues.add(expr);
                    } else {
                        pos++;
                        continue;
                    }
                    caseBodyParts.add(getNextParts(localData, part).get(i));
                    pos++;
                }
                Reference<GraphPart> nextRef = new Reference<>(null);
                Reference<GraphTargetItem> tiRef = new Reference<>(null);
                SwitchItem sw = handleSwitch(switchedItem, originalSwitchedItem.getSrc(), foundGotos, partCodes, partCodePos, visited, allParts, stack, stopPart, stopPartKind, loops, throwStates, localData, staticOperation, path,
                        caseValues, defaultPart, caseBodyParts, nextRef, tiRef);
                GraphPart next = nextRef.getVal();
                checkSwitch(localData, sw, caseExpressionOtherSides.values(), currentRet);
                currentRet.add(sw);
                if (next != null) {
                    if (tiRef.getVal() != null) {
                        ret.add(tiRef.getVal());
                    } else {
                        printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, part, next, stopPart, stopPartKind, loops, throwStates, currentRet, staticOperation, path, recursionLevel + 1);
                    }
                }
                pos++;
            } //else
            GraphPart nextOnePart = null;
            if (getNextParts(localData, part).size() == 2 && !partIsSwitch(part)) {
                GraphTargetItem expr = stack.pop();
                /*if (expr instanceof LogicalOpItem) {
                 expr = ((LogicalOpItem) expr).invert();
                 } else {
                 expr = new NotItem(null, expr);
                 }*/
                if (nextOnePart == null) {

                    List<GraphPart> nps;
                    nps = getNextParts(localData, part);
                    boolean isEmpty = nps.get(0) == nps.get(1);

                    GraphPart next = getCommonPart(localData, part, nps, loops, throwStates);
                    //System.err.println("on part " + part + ", next: " + next);
                    TranslateStack trueStack = (TranslateStack) stack.clone();
                    TranslateStack falseStack = (TranslateStack) stack.clone();

                    //hack for as1/2 for..in to get enumeration through
                    GraphTargetItem topBsr = !stack.isEmpty() && (stack.peek() instanceof BranchStackResistant) ? stack.peek() : null;
                    trueStack.clear();
                    falseStack.clear();
                    if (topBsr != null) {
                        trueStack.add(topBsr);
                        falseStack.add(topBsr);
                    }
                    if (isEmpty) {
                        next = nps.get(0);
                    }
                    boolean hasOntrue = nps.get(1) != next;
                    boolean hasOnFalse = nps.get(0) != next;

                    List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                    List<StopPartKind> stopPartKind2 = new ArrayList<>(stopPartKind);

                    if ((!isEmpty) && (next != null)) {
                        stopPart2.add(next);
                        stopPartKind2.add(StopPartKind.BLOCK_CLOSE);
                    }

                    List<GraphTargetItem> onTrue = new ArrayList<>();
                    if (!isEmpty && hasOntrue) {
                        onTrue = printGraph(foundGotos, partCodes, partCodePos, visited, prepareBranchLocalData(localData), trueStack, allParts, part, nps.get(1), stopPart2, stopPartKind2, loops, throwStates, null, staticOperation, path, recursionLevel + 1);
                    }
                    List<GraphTargetItem> onFalse = new ArrayList<>();

                    if (!isEmpty && hasOnFalse) {
                        onFalse = printGraph(foundGotos, partCodes, partCodePos, visited, prepareBranchLocalData(localData), falseStack, allParts, part, nps.get(0), stopPart2, stopPartKind2, loops, throwStates, null, staticOperation, path, recursionLevel + 1);
                    }
                    //List<GraphTargetItem> out2 = new ArrayList<>();
                    //makeAllCommands(out2, stack);
                    makeAllCommands(onTrue, trueStack);
                    makeAllCommands(onFalse, falseStack);
                    GraphTargetItem addAfterIf = null;
                    if (!onTrue.isEmpty() && !onFalse.isEmpty()
                            && (onTrue.get(onTrue.size() - 1) instanceof ContinueItem)
                            && (onFalse.get(onFalse.size() - 1) instanceof ContinueItem)) {
                        ContinueItem contTrue = (ContinueItem) onTrue.get(onTrue.size() - 1);
                        ContinueItem contFalse = (ContinueItem) onFalse.get(onFalse.size() - 1);
                        if (contTrue.loopId == contFalse.loopId) {
                            onTrue.remove(onTrue.size() - 1);
                            onFalse.remove(onFalse.size() - 1);
                            addAfterIf = contTrue;
                        }
                    }

                    List<GraphTargetItem> filteredOnTrue = onTrue;
                    List<GraphTargetItem> filteredOnFalse = onFalse;

                    if (!isEmpty(filteredOnTrue) && !isEmpty(filteredOnFalse) && filteredOnTrue.size() == 1 && filteredOnFalse.size() == 1 && (filteredOnTrue.get(0) instanceof PushItem) && (filteredOnFalse.get(0) instanceof PushItem)) {
                        stack.push(new TernarOpItem(null, localData.lineStartInstruction, expr.invert(null), ((PushItem) filteredOnTrue.get(0)).value, ((PushItem) filteredOnFalse.get(0)).value));
                    } else {
                        boolean isIf = true;
                        //If the ontrue is empty, switch ontrue and onfalse
                        if (filteredOnTrue.isEmpty() && !filteredOnFalse.isEmpty()) {
                            expr = expr.invert(null);
                            List<GraphTargetItem> tmp = onTrue;
                            onTrue = onFalse;
                            onFalse = tmp;
                            //tmp = filteredOnTrue;
                            filteredOnTrue = filteredOnFalse;
                            //filteredOnFalse = tmp;
                        }
                        if (!stack.isEmpty() && ((filteredOnTrue.size() == 1 && (filteredOnTrue.get(0) instanceof PopItem)) || ((filteredOnTrue.size() >= 2) && (filteredOnTrue.get(0) instanceof PopItem) && (filteredOnTrue.get(filteredOnTrue.size() - 1) instanceof PushItem)))) {
                            if (filteredOnTrue.size() > 1) {
                                GraphTargetItem rightSide = ((PushItem) filteredOnTrue.get(filteredOnTrue.size() - 1)).value;
                                GraphTargetItem prevExpr = stack.pop();
                                GraphTargetItem leftSide = expr.getNotCoercedNoDup();

                                boolean hideEmptyTrueFalse = true;

                                if (leftSide instanceof DuplicateItem) {
                                    isIf = false;
                                    if (hideEmptyTrueFalse && prevExpr.getNotCoercedNoDup() instanceof FalseItem) {
                                        stack.push(rightSide);
                                    } else if (hideEmptyTrueFalse && rightSide.getNotCoercedNoDup() instanceof FalseItem) {
                                        stack.push(prevExpr);
                                    } else {
                                        stack.push(new OrItem(null, localData.lineStartInstruction, prevExpr, rightSide));
                                    }
                                } else if (leftSide.invert(null).getNotCoercedNoDup() instanceof DuplicateItem) {
                                    isIf = false;
                                    if (hideEmptyTrueFalse && prevExpr.getNotCoercedNoDup() instanceof TrueItem) {
                                        stack.push(rightSide);
                                    } else if (hideEmptyTrueFalse && rightSide.getNotCoercedNoDup() instanceof TrueItem) {
                                        stack.push(prevExpr);
                                    } else {
                                        stack.push(new AndItem(null, localData.lineStartInstruction, prevExpr, rightSide));
                                    }
                                } else if (prevExpr instanceof FalseItem) {
                                    isIf = false;
                                    leftSide = leftSide.invert(null);

                                    if (hideEmptyTrueFalse && leftSide.getNotCoercedNoDup() instanceof TrueItem) {
                                        stack.push(rightSide);
                                    } else if (hideEmptyTrueFalse && rightSide.getNotCoercedNoDup() instanceof TrueItem) {
                                        stack.push(leftSide);
                                    } else {
                                        stack.push(new AndItem(null, localData.lineStartInstruction, leftSide, rightSide));
                                    }
                                } else if (prevExpr instanceof TrueItem) {
                                    isIf = false;
                                    if (hideEmptyTrueFalse && leftSide.getNotCoercedNoDup() instanceof FalseItem) {
                                        stack.push(rightSide);
                                    } else if (hideEmptyTrueFalse && rightSide.getNotCoercedNoDup() instanceof FalseItem) {
                                        stack.push(leftSide);
                                    } else {
                                        stack.push(new OrItem(null, localData.lineStartInstruction, leftSide, rightSide));
                                    }
                                } else {
                                    stack.push(prevExpr); //push it back
                                    //Still unstructured
                                }
                            } else {
                                isIf = false;
                            }
                        }

                        if (isIf) {
                            makeAllCommands(currentRet, stack);
                            IfItem b = new IfItem(null, localData.lineStartInstruction, expr.invert(null), onTrue, onFalse);
                            currentRet.add(b);
                            if (processSubBlk(b, null)) {
                                stack.push(new PopItem(null, localData.lineStartInstruction));
                            }
                        }
                    }
                    if (addAfterIf != null) {
                        currentRet.add(addAfterIf);
                    }
                    //currentRet.addAll(out2);
                    if (next != null) {
                        printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, part, next, stopPart, stopPartKind, loops, throwStates, currentRet, staticOperation, path, recursionLevel + 1);
                        //currentRet.addAll();
                    }
                }
            }  //else
            if (getNextParts(localData, part).size() == 1) {
                nextOnePart = getNextParts(localData, part).get(0);
            }

            if (getNextParts(localData, part).isEmpty()) {
                makeAllCommands(currentRet, stack);
            }

            if (nextOnePart != null) {
                printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, part, getNextParts(localData, part).get(0), stopPart, stopPartKind, loops, throwStates, currentRet, staticOperation, path, recursionLevel + 1);
            }

        }
        if (isLoop && loopItem != null && currentLoop != null) {

            LoopItem li = loopItem;
            boolean loopTypeFound = false;

            boolean hasContinue = false;
            processIfs(loopItem.commands);
            checkContinueAtTheEnd(loopItem.commands, currentLoop);
            List<ContinueItem> continues = loopItem.getContinues();
            for (ContinueItem c : continues) {
                if (c.loopId == currentLoop.id) {
                    hasContinue = true;
                    break;
                }
            }
            if (!hasContinue) {
                if (currentLoop.loopPreContinue != null) {
                    List<GraphPart> stopContPart = new ArrayList<>();
                    stopContPart.add(currentLoop.loopContinue);
                    List<StopPartKind> stopContPartKind = new ArrayList<>();
                    stopContPartKind.add(StopPartKind.OTHER);
                    GraphPart precoBackup = currentLoop.loopPreContinue;
                    currentLoop.loopPreContinue = null;
                    printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, null, precoBackup, stopContPart, stopContPartKind, loops, throwStates, loopItem.commands, staticOperation, path, recursionLevel + 1);
                    checkContinueAtTheEnd(loopItem.commands, currentLoop);
                }
            }

            //Loop with condition at the beginning (While)
            if (!loopTypeFound && (!loopItem.commands.isEmpty())) {
                if (loopItem.commands.get(0) instanceof IfItem) {
                    IfItem ifi = (IfItem) loopItem.commands.get(0);

                    List<GraphTargetItem> bodyBranch = null;
                    boolean inverted = false;
                    boolean breakpos2 = false;
                    BreakItem addBreakItem = null;
                    if ((ifi.onTrue.size() == 1) && (ifi.onTrue.get(0) instanceof BreakItem)) {
                        BreakItem bi = (BreakItem) ifi.onTrue.get(0);
                        if (bi.loopId == currentLoop.id) {
                            bodyBranch = ifi.onFalse;
                            inverted = true;
                        }
                    } else if ((ifi.onFalse.size() == 1) && (ifi.onFalse.get(0) instanceof BreakItem)) {
                        BreakItem bi = (BreakItem) ifi.onFalse.get(0);
                        if (bi.loopId == currentLoop.id) {
                            bodyBranch = ifi.onTrue;
                        }
                    } else if (loopItem.commands.size() == 2 && (loopItem.commands.get(1) instanceof BreakItem)) {
                        BreakItem bi = (BreakItem) loopItem.commands.get(1);
                        if (ifi.onTrue.isEmpty()) {
                            inverted = true;
                        }
                        bodyBranch = inverted ? ifi.onFalse : ifi.onTrue;
                        breakpos2 = true;
                        if (bi.loopId != currentLoop.id) { //it's break of another parent loop
                            addBreakItem = bi; //we must add it after the loop
                        }
                    }
                    if (bodyBranch != null) {
                        int index = ret.indexOf(loopItem);
                        ret.remove(index);
                        List<GraphTargetItem> exprList = new ArrayList<>();
                        GraphTargetItem expr = ifi.expression;
                        if (inverted) {
                            if (expr instanceof LogicalOpItem) {
                                expr = ((LogicalOpItem) expr).invert(null);
                            } else {
                                expr = new NotItem(null, expr.getLineStartItem(), expr);
                            }
                        }
                        exprList.add(expr);
                        List<GraphTargetItem> commands = new ArrayList<>();
                        commands.addAll(bodyBranch);
                        loopItem.commands.remove(0);
                        if (breakpos2) {
                            loopItem.commands.remove(0); //remove that break too
                        }
                        commands.addAll(loopItem.commands);
                        checkContinueAtTheEnd(commands, currentLoop);
                        List<GraphTargetItem> finalComm = new ArrayList<>();

                        //findGotoTargets - comment this out:
                        if (currentLoop.loopPreContinue != null) {
                            GraphPart backup = currentLoop.loopPreContinue;
                            currentLoop.loopPreContinue = null;
                            List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                            stopPart2.add(currentLoop.loopContinue);
                            List<StopPartKind> stopPartKind2 = new ArrayList<>(stopPartKind);
                            stopPartKind2.add(StopPartKind.OTHER);
                            List<GraphTargetItem> precoCommands = printGraph(foundGotos, partCodes, partCodePos, visited, localData, new TranslateStack(path), allParts, null, backup, stopPart2, stopPartKind2, loops, throwStates, null, staticOperation, path, recursionLevel + 1);
                            currentLoop.loopPreContinue = backup;
                            checkContinueAtTheEnd(precoCommands, currentLoop);

                            List<List<GraphTargetItem>> continueCommands = new ArrayList<>();
                            getContinuesCommands(commands, continueCommands, currentLoop.id);

                            if (continueCommands.isEmpty()) {
                                commands.addAll(precoCommands);
                                precoCommands = new ArrayList<>();
                            } //Single continue and there is break/continue/return/throw at end of the commands
                            else if (!commands.isEmpty() && continueCommands.size() == 1) {
                                GraphTargetItem lastItem = commands.get(commands.size() - 1);
                                if ((lastItem instanceof BreakItem) || (lastItem instanceof ContinueItem) || (lastItem instanceof ExitItem)) {
                                    continueCommands.get(0).addAll(continueCommands.get(0).size() - 1, precoCommands);
                                    precoCommands = new ArrayList<>();
                                }
                            }

                            finalComm.addAll(precoCommands);
                        }
                        if (currentLoop.precontinueCommands != null) {
                            finalComm.addAll(currentLoop.precontinueCommands);
                        }
                        if (!finalComm.isEmpty()) {
                            ret.add(index, li = new ForItem(expr.getSrc(), expr.getLineStartItem(), currentLoop, new ArrayList<>(), exprList.get(exprList.size() - 1), finalComm, commands));
                        } else {
                            ret.add(index, li = new WhileItem(expr.getSrc(), expr.getLineStartItem(), currentLoop, exprList, commands));
                        }
                        if (addBreakItem != null) {
                            ret.add(index + 1, addBreakItem);
                        }

                        loopTypeFound = true;
                    }
                }
            }

            //Loop with condition at the end (Do..While)
            if (!loopTypeFound && (!loopItem.commands.isEmpty())) {
                if (loopItem.commands.get(loopItem.commands.size() - 1) instanceof IfItem) {
                    IfItem ifi = (IfItem) loopItem.commands.get(loopItem.commands.size() - 1);
                    List<GraphTargetItem> bodyBranch = null;
                    boolean inverted = false;
                    if ((ifi.onTrue.size() == 1) && (ifi.onTrue.get(0) instanceof BreakItem)) {
                        BreakItem bi = (BreakItem) ifi.onTrue.get(0);
                        if (bi.loopId == currentLoop.id) {
                            bodyBranch = ifi.onFalse;
                            inverted = true;
                        }
                    } else if ((ifi.onFalse.size() == 1) && (ifi.onFalse.get(0) instanceof BreakItem)) {
                        BreakItem bi = (BreakItem) ifi.onFalse.get(0);
                        if (bi.loopId == currentLoop.id) {
                            bodyBranch = ifi.onTrue;
                        }
                    }
                    if (bodyBranch != null) {
                        //Condition at the beginning
                        int index = ret.indexOf(loopItem);
                        ret.remove(index);
                        List<GraphTargetItem> exprList = new ArrayList<>();
                        GraphTargetItem expr = ifi.expression;
                        if (inverted) {
                            expr = expr.invert(null);
                        }

                        checkContinueAtTheEnd(bodyBranch, currentLoop);

                        List<GraphTargetItem> commands = new ArrayList<>();

                        if (!bodyBranch.isEmpty()) {
                            ret.add(index, loopItem);
                        } else {
                            loopItem.commands.remove(loopItem.commands.size() - 1);
                            commands.addAll(loopItem.commands);
                            commands.addAll(bodyBranch);
                            exprList.add(expr);
                            checkContinueAtTheEnd(commands, currentLoop);
                            ret.add(index, li = new DoWhileItem(null, exprList.get(0).getLineStartItem(), currentLoop, commands, exprList));
                        }

                        loopTypeFound = true;
                    }
                }
            }

            if (!loopTypeFound) {
                if (currentLoop.loopPreContinue != null) {
                    loopTypeFound = true;
                    GraphPart backup = currentLoop.loopPreContinue;
                    currentLoop.loopPreContinue = null;
                    List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                    stopPart2.add(currentLoop.loopContinue);
                    List<StopPartKind> stopPartKind2 = new ArrayList<>(stopPartKind);
                    stopPartKind2.add(StopPartKind.OTHER);
                    List<GraphTargetItem> finalComm = printGraph(foundGotos, partCodes, partCodePos, visited, localData, new TranslateStack(path), allParts, null, backup, stopPart2, stopPartKind2, loops, throwStates, null, staticOperation, path, recursionLevel + 1);
                    currentLoop.loopPreContinue = backup;
                    checkContinueAtTheEnd(finalComm, currentLoop);

                    if (!finalComm.isEmpty()) {
                        if (finalComm.get(finalComm.size() - 1) instanceof IfItem) {
                            IfItem ifi = (IfItem) finalComm.get(finalComm.size() - 1);
                            boolean ok = false;
                            boolean invert = false;
                            if (((ifi.onTrue.size() == 1) && (ifi.onTrue.get(0) instanceof BreakItem) && (((BreakItem) ifi.onTrue.get(0)).loopId == currentLoop.id))
                                    && ((ifi.onFalse.size() == 1) && (ifi.onFalse.get(0) instanceof ContinueItem) && (((ContinueItem) ifi.onFalse.get(0)).loopId == currentLoop.id))) {
                                ok = true;
                                invert = true;
                            }
                            if (((ifi.onTrue.size() == 1) && (ifi.onTrue.get(0) instanceof ContinueItem) && (((ContinueItem) ifi.onTrue.get(0)).loopId == currentLoop.id))
                                    && ((ifi.onFalse.size() == 1) && (ifi.onFalse.get(0) instanceof BreakItem) && (((BreakItem) ifi.onFalse.get(0)).loopId == currentLoop.id))) {
                                ok = true;
                            }
                            if (ok) {
                                finalComm.remove(finalComm.size() - 1);
                                int index = ret.indexOf(loopItem);
                                ret.remove(index);
                                List<GraphTargetItem> exprList = new ArrayList<>(finalComm);
                                GraphTargetItem expr = ifi.expression;
                                if (invert) {
                                    expr = expr.invert(null);
                                }
                                exprList.add(expr);
                                ret.add(index, li = new DoWhileItem(null, expr.getLineStartItem(), currentLoop, loopItem.commands, exprList));
                            }
                        }
                    }
                }
            }

            if (!loopTypeFound) {
                checkContinueAtTheEnd(loopItem.commands, currentLoop);
            }
            currentLoop.phase = 2;

            GraphTargetItem replaced = checkLoop(ret, li, localData, loops, throwStates);
            if (replaced != li) {
                int index = ret.indexOf(li);
                ret.remove(index);
                if (replaced != null) {
                    ret.add(index, replaced);
                }
            }

            if (currentLoop.loopBreak != null) {
                printGraph(foundGotos, partCodes, partCodePos, visited, localData, sPreLoop, allParts, part, currentLoop.loopBreak, stopPart, stopPartKind, loops, throwStates, ret, staticOperation, path, recursionLevel + 1);
            }
        }

        return ret;
    }

    protected void checkSwitch(BaseLocalData localData, SwitchItem switchItem, Collection<? extends GraphTargetItem> otherSides, List<GraphTargetItem> output) {

    }

    protected void checkGraph(List<GraphPart> allBlocks) {
    }

    public List<GraphPart> makeGraph(GraphSource code, List<GraphPart> allBlocks, List<GraphException> exceptions) throws InterruptedException {
        List<Integer> alternateEntries = new ArrayList<>();
        for (GraphException ex : exceptions) {
            alternateEntries.add(ex.start);
            //alternateEntries.add(ex.end);
            alternateEntries.add(ex.target);
        }
        HashMap<Integer, List<Integer>> refs = code.visitCode(alternateEntries);
        List<GraphPart> ret = new ArrayList<>();
        boolean[] visited = new boolean[code.size()];
        ret.add(makeGraph(null, new GraphPath(), code, 0, 0, allBlocks, refs, visited));
        for (int pos : alternateEntries) {
            GraphPart e1 = new GraphPart(-1, -1);
            e1.path = new GraphPath("e");
            ret.add(makeGraph(e1, new GraphPath("e"), code, pos, pos, allBlocks, refs, visited));
        }
        checkGraph(allBlocks);
        return ret;
    }

    protected int checkIp(int ip) {
        return ip;
    }

    public GraphPart searchPart(int ip, Collection<? extends GraphPart> allParts) {
        if (ip < 0) {
            return null;
        }
        for (GraphPart p : allParts) {
            if (ip >= p.start && ip <= p.end) {
                return p;
            }
        }
        return null;
    }

    private GraphPart makeGraph(GraphPart parent, GraphPath path, GraphSource code, int startip, int lastIp, List<GraphPart> allBlocks, HashMap<Integer, List<Integer>> refs, boolean[] visited) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        int ip = startip;
        GraphPart existingPart = searchPart(ip, allBlocks);
        if (existingPart != null) {
            if (parent != null) {
                existingPart.refs.add(parent);
                parent.nextParts.add(existingPart);
            }
            return existingPart;
        }
        GraphPart ret = new GraphPart(ip, -1);
        ret.path = path;
        GraphPart part = ret;
        if (parent != null) {
            ret.refs.add(parent);
            parent.nextParts.add(ret);
        }
        while (ip < code.size()) {
            ip = checkIp(ip);
            if (ip >= code.size()) {
                break;
            }
            if (visited[ip] || ((ip != startip) && (refs.get(ip).size() > 1))) {
                part.end = lastIp;
                GraphPart found = searchPart(ip, allBlocks);

                allBlocks.add(part);

                if (found != null) {
                    part.nextParts.add(found);
                    found.refs.add(part);
                    break;
                } else {
                    GraphPart nextPart = new GraphPart(ip, -1);
                    nextPart.path = path;
                    part.nextParts.add(nextPart);
                    nextPart.refs.add(part);
                    part = nextPart;
                }
            }
            visited[ip] = true;
            lastIp = ip;
            GraphSourceItem ins = code.get(ip);
            if (ins.isIgnored()) {
                ip++;
                continue;
            }
            if (ins instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer cnt = (GraphSourceItemContainer) ins;
                if (ins instanceof Action) { //TODO: Remove dependency of AVM1
                    long endAddr = ((Action) ins).getAddress() + cnt.getHeaderSize();
                    for (long size : cnt.getContainerSizes()) {
                        endAddr += size;
                    }
                    ip = code.adr2pos(endAddr);

                    if ((ins instanceof ActionDefineFunction) || (ins instanceof ActionDefineFunction2)) {
                        part.end = lastIp;
                        allBlocks.add(part);
                        GraphPart nextGraphPart = new GraphPart(ip, -1);
                        nextGraphPart.path = path;
                        part.nextParts.add(nextGraphPart);
                        nextGraphPart.refs.add(part);
                        part = nextGraphPart;
                    }
                }

                continue;
            } else if (ins.isExit()) {
                part.end = ip;
                allBlocks.add(part);
                break;
            } else if (ins.isJump()) {
                part.end = ip;
                allBlocks.add(part);
                ip = ins.getBranches(code).get(0);
                makeGraph(part, path, code, ip, lastIp, allBlocks, refs, visited);
                break;
            } else if (ins.isBranch()) {
                part.end = ip;

                allBlocks.add(part);
                List<Integer> branches = ins.getBranches(code);
                for (int i = 0; i < branches.size(); i++) {
                    makeGraph(part, path.sub(i, ip), code, branches.get(i), ip, allBlocks, refs, visited);
                }
                break;
            }
            ip++;
        }
        if ((part.end == -1) && (ip >= code.size())) {
            if (part.start == code.size()) {
                part.end = code.size();
                allBlocks.add(part);
            } else {
                part.end = ip - 1;
                for (GraphPart p : allBlocks) {
                    if (p.start == ip) {
                        p.refs.add(part);
                        part.nextParts.add(p);
                        allBlocks.add(part);
                        return ret;
                    }
                }
                GraphPart gp = new GraphPart(ip, ip);
                allBlocks.add(gp);
                gp.refs.add(part);
                part.nextParts.add(gp);
                allBlocks.add(part);
            }
        }
        return ret;
    }

    /**
     * String used to indent line when converting to string
     */
    public static final String INDENTOPEN = "INDENTOPEN";

    /**
     * String used to unindent line when converting to string
     */
    public static final String INDENTCLOSE = "INDENTCLOSE";

    /**
     * Converts list of TreeItems to string
     *
     * @param tree List of TreeItem
     * @param writer
     * @param localData
     * @return String
     * @throws java.lang.InterruptedException
     */
    public static GraphTextWriter graphToString(List<GraphTargetItem> tree, GraphTextWriter writer, LocalData localData) throws InterruptedException {
        for (GraphTargetItem ti : tree) {
            if (!ti.isEmpty()) {
                ti.toStringSemicoloned(writer, localData);
                if (!ti.handlesNewLine()) {
                    writer.newLine();
                }
            }
        }
        return writer;
    }

    public BaseLocalData prepareBranchLocalData(BaseLocalData localData) {
        return localData;
    }

    protected List<GraphSourceItem> getPartItems(GraphPart part) {
        List<GraphSourceItem> ret = new ArrayList<>();
        do {
            for (int i = 0; i < part.getHeight(); i++) {
                if (part.getPosAt(i) < code.size()) {
                    if (part.getPosAt(i) < 0) {
                        continue;
                    }
                    GraphSourceItem s = code.get(part.getPosAt(i));
                    if (!s.isJump()) {
                        ret.add(s);
                    }
                }
            }
            if (part.nextParts.size() == 1 && part.nextParts.get(0).refs.size() == 1) {
                part = part.nextParts.get(0);
            } else {
                part = null;
            }
        } while (part != null);
        return ret;
    }

    protected static void makeAllStack(List<GraphTargetItem> commands, TranslateStack stack) {
        int pcnt = 0;
        for (int i = commands.size() - 1; i >= 0; i--) {
            if (commands.get(i) instanceof PushItem) {
                pcnt++;
            } else {
                break;
            }
        }
        for (int i = commands.size() - pcnt; i < commands.size(); i++) {
            stack.push(commands.remove(i).value);
            i--;
        }
    }

    protected void makeAllCommands(List<GraphTargetItem> commands, TranslateStack stack) {
        int clen = commands.size();
        boolean isExit = false;
        if (clen > 0) {
            if (commands.get(clen - 1) instanceof ScriptEndItem) {
                clen--;
                isExit = true;
            }
        }
        if (clen > 0) {
            if (commands.get(clen - 1) instanceof ExitItem) {
                isExit = true;
                clen--;
            }
        }
        if (clen > 0) {
            if (commands.get(clen - 1) instanceof BreakItem) {
                clen--;
            }
        }
        if (clen > 0) {
            if (commands.get(clen - 1) instanceof ContinueItem) {
                clen--;
            }
        }
        while (stack.size() > 0) {
            GraphTargetItem p = stack.pop();
            if (p instanceof BranchStackResistant) {
                continue;
            }
            if (!(p instanceof PopItem)) {
                if (p instanceof FunctionActionItem) {
                    commands.add(clen, p);
                } else {
                    if (isExit) {
                        //ASC2 leaves some function calls unpopped on stack before returning from a method
                        commands.add(clen, p);
                    } else {
                        commands.add(clen, new PushItem(p));
                    }
                }
            }
        }
    }

    protected SwitchItem handleSwitch(GraphTargetItem switchedObject,
            GraphSourceItem switchStartItem, List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, Set<GraphPart> allParts, TranslateStack stack, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, BaseLocalData localData, int staticOperation, String path,
            List<GraphTargetItem> caseValuesMap, GraphPart defaultPart, List<GraphPart> caseBodyParts, Reference<GraphPart> nextRef, Reference<GraphTargetItem> tiRef) throws InterruptedException {
        boolean hasDefault = false;
        /*
                case 4:
                case 5:
                default: 
                    trace("5 & def");
                    ...
                case 6:
                
         */
        //must go backwards to hit case 5, not case 4
        for (int i = caseBodyParts.size() - 1; i >= 0; i--) {
            if (caseBodyParts.get(i) == defaultPart) {
                DefaultItem di = new DefaultItem();
                caseValuesMap.add(i + 1, di);
                caseBodyParts.add(i + 1, defaultPart);
                hasDefault = true;
                break;
            }
        }

        if (!hasDefault) {
            /*
                    case 1:
                        trace("1");
                    case 2:
                        trace("2"); //no break
                    default:
                        trace("def");
                        ...
                    case 3:  
             */
            //must go backwards to hit case 2, not case 1
            for (int i = caseBodyParts.size() - 1; i >= 0; i--) {
                if (caseBodyParts.get(i).leadsTo(localData, this, code, defaultPart, loops, throwStates, false)) {
                    DefaultItem di = new DefaultItem();
                    caseValuesMap.add(i + 1, di);
                    caseBodyParts.add(i + 1, defaultPart);
                    hasDefault = true;
                    break;
                }
            }
        }

        if (!hasDefault) {
            /*
                    case 1:
                        trace("1");
                        break;
                    default:
                        trace("def"); //no break
                    case 2:
                        trace("2");                    
             */
            for (int i = 0; i < caseBodyParts.size(); i++) {
                if (defaultPart.leadsTo(localData, this, code, caseBodyParts.get(i), loops, throwStates, false)) {
                    DefaultItem di = new DefaultItem();
                    caseValuesMap.add(i, di);
                    caseBodyParts.add(i, defaultPart);
                    hasDefault = true;
                    break;
                }
            }
        }

        if (!hasDefault) {
            /*
                        case 1:
                        ...
                        case 2:
                        ...
                        default:
                            trace("def");                        
             */
            caseValuesMap.add(new DefaultItem());
            caseBodyParts.add(defaultPart);
        }

        //gotoTargets
        GraphPart breakPart = getMostCommonPart(localData, caseBodyParts, loops, throwStates);
        //removeEdgeToFromList(gotoTargets, breakPart);

        List<List<GraphTargetItem>> caseCommands = new ArrayList<>();
        GraphPart next = breakPart;

        //create switch as new loop break command detection to work
        Loop currentLoop = new Loop(loops.size(), null, next);
        currentLoop.phase = 1;
        loops.add(currentLoop);
        List<Integer> valuesMapping = new ArrayList<>();
        List<GraphPart> caseBodies = new ArrayList<>();

        for (int i = 0; i < caseValuesMap.size(); i++) {
            GraphPart cur = caseBodyParts.get(i);
            if (!caseBodies.contains(cur)) {
                caseBodies.add(cur);
            }
        }

        //Sort bodies by leadsto to proper handle clauses without a break statement
        loopi:
        for (int i = 0; i < caseBodies.size(); i++) {
            GraphPart b = caseBodies.get(i);
            for (int j = i + 1; j < caseBodies.size(); j++) {
                GraphPart b2 = caseBodies.get(j);
                if (b2.leadsTo(localData, this, code, b, loops, throwStates, false)) {
                    caseBodies.remove(j);
                    caseBodies.add(i, b2);
                    i--;
                    continue loopi;
                } else if (j > i + 1) {
                    if (b.leadsTo(localData, this, code, b2, loops, throwStates, false)) {
                        caseBodies.remove(j);
                        caseBodies.add(i + 1, b2);
                        continue loopi;
                    }
                }
            }
        }

        for (int i = 0; i < caseValuesMap.size(); i++) {
            GraphPart cur = caseBodyParts.get(i);
            valuesMapping.add(caseBodies.indexOf(cur));
        }

        for (int i = 0; i < caseBodies.size(); i++) {
            List<GraphTargetItem> currentCaseCommands = new ArrayList<>();
            boolean willHaveBreak = false;
            if (i < caseBodies.size() - 1) {
                if (!caseBodies.get(i).leadsTo(localData, this, code, caseBodies.get(i + 1), loops, throwStates, false)) {
                    willHaveBreak = true;
                }
            }

            List<GraphPart> stopPart2x = new ArrayList<>(stopPart);
            List<StopPartKind> stopPartKind2x = new ArrayList<>(stopPartKind);
            for (GraphPart b : caseBodies) {
                if (b != caseBodies.get(i)) {
                    stopPart2x.add(b);
                    stopPartKind2x.add(StopPartKind.OTHER);
                }
            }
            if (breakPart != null) {
                stopPart2x.add(breakPart);
                stopPartKind2x.add(StopPartKind.OTHER);
            }
            currentCaseCommands = printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, null, caseBodies.get(i), stopPart2x, stopPartKind2x, loops, throwStates, staticOperation, path);
            if (willHaveBreak) {
                if (!currentCaseCommands.isEmpty()) {
                    GraphTargetItem last = currentCaseCommands.get(currentCaseCommands.size() - 1);
                    if (!(last instanceof ContinueItem) && !(last instanceof BreakItem) && !(last instanceof GotoItem) && !(last instanceof ExitItem) && !(last instanceof ScriptEndItem)) {
                        currentCaseCommands.add(new BreakItem(null, localData.lineStartInstruction, currentLoop.id));
                    }
                }
            }
            caseCommands.add(currentCaseCommands);
        }

        //If the lastone is default empty and alone, remove it
        if (!caseCommands.isEmpty()) {
            List<GraphTargetItem> lastc = caseCommands.get(caseCommands.size() - 1);
            if (!lastc.isEmpty() && (lastc.get(lastc.size() - 1) instanceof BreakItem)) {
                BreakItem bi = (BreakItem) lastc.get(lastc.size() - 1);
                lastc.remove(lastc.size() - 1);
            }
            if (lastc.isEmpty()) {
                int cnt2 = 0;
                if (caseValuesMap.get(caseValuesMap.size() - 1) instanceof DefaultItem) {
                    for (int i = valuesMapping.size() - 1; i >= 0; i--) {
                        if (valuesMapping.get(i) == caseCommands.size() - 1) {
                            cnt2++;
                        }
                    }

                    caseValuesMap.remove(caseValuesMap.size() - 1);
                    valuesMapping.remove(valuesMapping.size() - 1);
                    if (cnt2 == 1) {
                        caseCommands.remove(lastc);
                    }
                }
            }
        }
        //remove last break from last section                
        if (!caseCommands.isEmpty()) {
            List<GraphTargetItem> lastc = caseCommands.get(caseCommands.size() - 1);
            if (!lastc.isEmpty() && (lastc.get(lastc.size() - 1) instanceof BreakItem)) {
                BreakItem bi = (BreakItem) lastc.get(lastc.size() - 1);
                lastc.remove(lastc.size() - 1);
            }
        }
        nextRef.setVal(next);

        currentLoop.phase = 2;
        GraphTargetItem ti = checkLoop(new ArrayList<>() /*??*/, next, stopPart, loops, throwStates);
        tiRef.setVal(ti);

        return new SwitchItem(null, switchStartItem, currentLoop, switchedObject, caseValuesMap, caseCommands, valuesMapping);

    }

    protected boolean partIsSwitch(GraphPart part) {
        return false;
    }
}
