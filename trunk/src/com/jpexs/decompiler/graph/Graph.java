/*
 *  Copyright (C) 2010-2013 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.ExitItem;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.IntegerValueItem;
import com.jpexs.decompiler.graph.model.LogicalOpItem;
import com.jpexs.decompiler.graph.model.LoopItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.ScriptEndItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.UniversalLoopItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class Graph {

    public List<GraphPart> heads;
    protected GraphSource code;
    private List<Integer> alternateEntries;
    public static final int SOP_USE_STATIC = 0;
    public static final int SOP_SKIP_STATIC = 1;
    public static final int SOP_REMOVE_STATIC = 2;

    public Graph(GraphSource code, List<Integer> alternateEntries) {
        this.code = code;
        this.alternateEntries = alternateEntries;

    }

    public void init(List<Object> localData) {
        if (heads != null) {
            return;
        }
        heads = makeGraph(code, new ArrayList<GraphPart>(), alternateEntries);
        int time = 1;
        List<GraphPart> ordered = new ArrayList<>();
        List<GraphPart> visited = new ArrayList<>();
        for (GraphPart head : heads) {
            time = head.setTime(time, ordered, visited);
            fixGraph(localData, head);
            makeMulti(head, new ArrayList<GraphPart>());
        }
    }

    protected static void populateParts(GraphPart part, List<GraphPart> allParts) {
        if (allParts.contains(part)) {
            return;
        }
        allParts.add(part);
        for (GraphPart p : part.nextParts) {
            populateParts(p, allParts);
        }
    }

    private void fixGraph(List<Object> localData, GraphPart part) {
        //if(true) return;
        try {
            while (fixGraphOnce(localData, part, new ArrayList<GraphPart>(), false)) {
            }
        } catch (Exception | Error ex) {
            //ignore
        }
    }

    private boolean fixGraphOnce(List<Object> localData, GraphPart part, List<GraphPart> visited, boolean doChildren) {
        if (visited.contains(part)) {
            return false;
        }
        visited.add(part);
        boolean fixed = false;
        int i = 0;
        GraphPath lastpref;
        boolean modify = true;
        int prvni = -1;

        if (!doChildren) {

            List<GraphPart> uniqueRefs = new ArrayList<>();
            for (GraphPart r : part.refs) {
                if (!uniqueRefs.contains(r)) {
                    uniqueRefs.add(r);
                }
            }
            loopi:
            for (; i <= part.path.length(); i++) {
                lastpref = null;
                int pos = -1;
                for (GraphPart r : uniqueRefs) {
                    pos++;
                    if (r.path.rootName.equals("e") && !part.path.rootName.equals("e")) {
                        continue;
                    }
                    if (part.leadsTo(localData, this, code, r, new ArrayList<Loop>())) {
                        modify = false;
                        continue;
                    }

                    prvni = pos;
                    if (i > r.path.length()) {
                        i--;
                        break loopi;
                    }
                    if (lastpref == null) {
                        lastpref = r.path.parent(i);
                    } else {
                        if (!r.path.startsWith(lastpref)) {
                            i--;
                            break loopi;
                        }
                    }
                }
            }
            if (i > part.path.length()) {
                i = part.path.length();
            }
            if (modify && ((uniqueRefs.size() > 1) && (prvni >= 0))) {
                GraphPath prvniUniq = uniqueRefs.get(prvni).path;
                GraphPath newpath = prvniUniq.parent(i);
                if (!part.path.equals(newpath)) {
                    if (part.path.startsWith(newpath) && ((newpath.length() == prvniUniq.length()) || (prvniUniq.getKey(newpath.length()) == part.path.getKey(newpath.length())))) {
                        GraphPath origPath = part.path;
                        GraphPart p = part;
                        part.path = newpath;
                        while (p.nextParts.size() == 1) {
                            p = p.nextParts.get(0);
                            if (!p.path.equals(origPath)) {
                                break;
                            }

                            p.path = newpath;
                        }
                        fixGraphOnce(localData, part, new ArrayList<GraphPart>(), true);
                        fixed = true;
                    }
                }
            }
        } else {

            if (!fixed) {
                if (part.nextParts.size() == 1) {
                    if (!(part.path.rootName.equals("e") && (!part.nextParts.get(0).path.rootName.equals("e")))) {
                        if (part.nextParts.get(0).path.length() > part.path.length()) {
                            part.nextParts.get(0).path = part.path;
                            fixed = true;
                        }
                    }
                }
                if (part.nextParts.size() > 1) {
                    for (int j = 0; j < part.nextParts.size(); j++) {
                        GraphPart npart = part.nextParts.get(j);

                        if (npart.path.length() > part.path.length() + 1) {
                            npart.path = part.path.sub(j, part.end);
                            fixed = true;
                        }
                    }
                }
            }

        }
        if (part.nextParts.size() == 2) {
            if (part.nextParts.get(1).leadsTo(localData, this, code, part.nextParts.get(0), new ArrayList<Loop>() /*visited*/)) {
                fixGraphOnce(localData, part.nextParts.get(1), visited, doChildren);
                fixGraphOnce(localData, part.nextParts.get(0), visited, doChildren);
            } else {
                fixGraphOnce(localData, part.nextParts.get(0), visited, doChildren);
                fixGraphOnce(localData, part.nextParts.get(1), visited, doChildren);
            }
        } else {
            for (int j = part.nextParts.size() - 1; j >= 0; j--) {
                GraphPart p = part.nextParts.get(j);
                fixGraphOnce(localData, p, visited, doChildren);
            }
        }
        return fixed;
    }

    private void makeMulti(GraphPart part, List<GraphPart> visited) {
        if (true) {
            return;
        }
        if (visited.contains(part)) {
            return;
        }
        visited.add(part);
        GraphPart p = part;
        List<GraphPart> multiList = new ArrayList<>();
        multiList.add(p);
        while ((p.nextParts.size() == 1) && (p.nextParts.get(0).refs.size() == 1)) {
            p = p.nextParts.get(0);
            multiList.add(p);
        }
        if (multiList.size() > 1) {
            GraphPartMulti gpm = new GraphPartMulti(multiList);
            gpm.refs = part.refs;
            GraphPart lastPart = multiList.get(multiList.size() - 1);
            gpm.nextParts = lastPart.nextParts;
            for (GraphPart next : gpm.nextParts) {
                int index = next.refs.indexOf(lastPart);
                if (index == -1) {

                    continue;
                }
                next.refs.remove(lastPart);
                next.refs.add(index, gpm);
            }
            for (GraphPart parent : part.refs) {
                if (parent.start == -1) {
                    continue;
                }
                int index = parent.nextParts.indexOf(part);
                if (index == -1) {
                    continue;
                }
                parent.nextParts.remove(part);
                parent.nextParts.add(index, gpm);
            }
        }
        for (int i = 0; i < part.nextParts.size(); i++) {
            makeMulti(part.nextParts.get(i), visited);
        }
    }

    public GraphPart deepCopy(GraphPart part, List<GraphPart> visited, List<GraphPart> copies) {
        if (visited == null) {
            visited = new ArrayList<>();
        }
        if (copies == null) {
            copies = new ArrayList<>();
        }
        if (visited.contains(part)) {
            return copies.get(visited.indexOf(part));
        }
        visited.add(part);
        GraphPart copy = new GraphPart(part.start, part.end);
        copy.path = part.path;
        copies.add(copy);
        copy.nextParts = new ArrayList<>();
        for (int i = 0; i < part.nextParts.size(); i++) {
            copy.nextParts.add(deepCopy(part.nextParts.get(i), visited, copies));
        }
        for (int i = 0; i < part.refs.size(); i++) {
            copy.refs.add(deepCopy(part.refs.get(i), visited, copies));
        }
        return copy;
    }

    public void resetGraph(GraphPart part, List<GraphPart> visited) {
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

    private void getReachableParts(GraphPart part, List<GraphPart> ret, List<Loop> loops) {
        getReachableParts(part, ret, loops, true);
    }

    private void getReachableParts(GraphPart part, List<GraphPart> ret, List<Loop> loops, boolean first) {

        if (first) {
            for (Loop l : loops) {
                l.reachableMark = 0;
            }
        }
        Loop currentLoop = null;

        for (Loop l : loops) {
            if ((l.phase == 1) || (l.reachableMark == 1)) {
                if (l.loopContinue == part) {
                    return;
                }
                if (l.loopBreak == part) {
                    return;
                }
                if (l.loopPreContinue == part) {
                    return;
                }
            }
            if (l.reachableMark == 0) {
                if (l.loopContinue == part) {
                    l.reachableMark = 1;
                    currentLoop = l;
                }
            }
        }

        List<GraphPart> newparts = new ArrayList<>();
        loopnext:
        for (GraphPart next : part.nextParts) {
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
                newparts.add(next);
            }
        }

        ret.addAll(newparts);
        for (GraphPart next : newparts) {
            getReachableParts(next, ret, loops);
        }

        if (currentLoop != null) {
            if (currentLoop.loopBreak != null) {
                if (!ret.contains(currentLoop.loopBreak)) {
                    ret.add(currentLoop.loopBreak);
                    currentLoop.reachableMark = 2;
                    getReachableParts(currentLoop.loopBreak, ret, loops);
                }
            }
        }
    }

    /* public GraphPart getNextCommonPart(GraphPart part, List<Loop> loops) {
     return getNextCommonPart(part, new ArrayList<GraphPart>(),loops);
     }*/
    public GraphPart getNextCommonPart(List<Object> localData, GraphPart part, List<Loop> loops) {
        return getCommonPart(localData, part.nextParts, loops);
    }

    public GraphPart getCommonPart(List<Object> localData, List<GraphPart> parts, List<Loop> loops) {
        if (parts.isEmpty()) {
            return null;
        }

        List<GraphPart> loopContinues = new ArrayList<>();//getLoopsContinues(loops);
        for (Loop l : loops) {
            if (l.phase == 1) {
                loopContinues.add(l.loopContinue);
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
                if (!q.leadsTo(localData, this, code, p, loops)) {
                    common = false;
                    break;
                }
            }
            if (common) {
                return p;
            }
        }
        List<List<GraphPart>> reachable = new ArrayList<>();
        for (GraphPart p : parts) {
            List<GraphPart> r1 = new ArrayList<>();
            getReachableParts(p, r1, loops);
            r1.add(p);
            reachable.add(r1);
        }
        List<GraphPart> first = reachable.get(0);
        for (GraphPart p : first) {
            /*if (ignored.contains(p)) {
             continue;
             }*/
            p = checkPart(null, localData, p, null);
            if (p == null) {
                continue;
            }
            boolean common = true;
            for (List<GraphPart> r : reachable) {
                if (!r.contains(p)) {
                    common = false;
                    break;
                }
            }
            if (common) {
                return p;
            }
        }
        return null;
    }

    public GraphPart getMostCommonPart(List<Object> localData, List<GraphPart> parts, List<Loop> loops) {
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
                if (!q.leadsTo(localData, this, code, p, loops)) {
                    common = false;
                    break;
                }
            }
            if (common) {
                return p;
            }
        }

        loopi:
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
        }
        List<List<GraphPart>> reachable = new ArrayList<>();
        for (GraphPart p : parts) {
            List<GraphPart> r1 = new ArrayList<>();
            getReachableParts(p, r1, loops);
            r1.add(0, p);
            reachable.add(r1);
        }
        ///List<GraphPart> first = reachable.get(0);
        int commonLevel;
        Map<GraphPart, Integer> levelMap = new HashMap<>();
        for (List<GraphPart> first : reachable) {
            int maxclevel = 0;
            Set<GraphPart> visited = new HashSet<>();
            for (GraphPart p : first) {
                if (loopContinues.contains(p)) {
                    break;
                }
                if (visited.contains(p)) {
                    continue;
                }
                visited.add(p);
                boolean common = true;
                commonLevel = 1;
                for (List<GraphPart> r : reachable) {
                    if (r == first) {
                        continue;
                    }
                    if (r.contains(p)) {
                        commonLevel++;
                    }
                }
                if (commonLevel <= maxclevel) {
                    continue;
                }
                maxclevel = commonLevel;
                if (levelMap.containsKey(p)) {
                    if (levelMap.get(p) > commonLevel) {
                        commonLevel = levelMap.get(p);
                    }
                }
                levelMap.put(p, commonLevel);
                if (common) {
                    //return p;
                }
            }
        }
        for (int i = reachable.size() - 1; i >= 2; i--) {
            for (GraphPart p : levelMap.keySet()) {
                if (levelMap.get(p) == i) {
                    return p;
                }
            }
        }
        for (GraphPart p : levelMap.keySet()) {
            if (levelMap.get(p) == parts.size()) {
                return p;
            }
        }
        return null;
    }

    public GraphPart getNextNoJump(GraphPart part, List<Object> localData) {
        while (code.get(part.start).isJump()) {
            part = part.getSubParts().get(0).nextParts.get(0);
        }
        /*localData = prepareBranchLocalData(localData);
         Stack<GraphTargetItem> st = new Stack<>();
         List<GraphTargetItem> output=new ArrayList<>();
         GraphPart startPart = part;
         for (int i = part.start; i <= part.end; i++) {
         GraphSourceItem src = code.get(i);
         if (src.isJump()) {
         part = part.nextParts.get(0);
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

    public static List<GraphTargetItem> translateViaGraph(List<Object> localData, String path, GraphSource code, List<Integer> alternateEntries, int staticOperation) {
        Graph g = new Graph(code, alternateEntries);
        g.init(localData);
        return g.translate(localData, staticOperation, path);
    }

    public List<GraphTargetItem> translate(List<Object> localData, int staticOperation, String path) {
        List<GraphPart> allParts = new ArrayList<>();
        for (GraphPart head : heads) {
            populateParts(head, allParts);
        }
        Stack<GraphTargetItem> stack = new Stack<>();
        List<Loop> loops = new ArrayList<>();
        getLoops(localData, heads.get(0), loops, null);
        /*System.out.println("<loops>");
         for (Loop el : loops) {
         System.out.println(el);
         }
         System.out.println("</loops>");*/
        getPrecontinues(localData, null, heads.get(0), loops, null);
        /*System.err.println("<loopspre>");
         for (Loop el : loops) {
         System.err.println(el);
         }
         System.err.println("</loopspre>");//*/

        List<GraphTargetItem> ret = printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, null, heads.get(0), null, loops, staticOperation, path);
        processIfs(ret);
        finalProcessStack(stack, ret);
        finalProcessAll(ret, 0, new ArrayList<>());
        return ret;


    }

    public void finalProcessStack(Stack<GraphTargetItem> stack, List<GraphTargetItem> output) {
    }

    private void finalProcessAll(List<GraphTargetItem> list, int level, List<Object> localData) {
        finalProcess(list, level, localData);
        for (GraphTargetItem item : list) {
            if (item instanceof Block) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    finalProcessAll(sub, level + 1, localData);
                }
            }
        }
    }

    protected void finalProcess(List<GraphTargetItem> list, int level, List<Object> localData) {
    }

    private void processIfs(List<GraphTargetItem> list) {
        //if(true) return;
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem item = list.get(i);
            if (item instanceof Block) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    processIfs(sub);
                }
            }
            if ((item instanceof LoopItem) && (item instanceof Block)) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    processIfs(sub);
                    checkContinueAtTheEnd(sub, ((LoopItem) item).loop);
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

                if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
                    GraphTargetItem last = onTrue.get(onTrue.size() - 1);
                    if ((last instanceof ExitItem) || (last instanceof ContinueItem) || (last instanceof BreakItem)) {
                        list.addAll(i + 1, onFalse);
                        onFalse.clear();
                    }
                }

                if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
                    if (onFalse.get(onFalse.size() - 1) instanceof ExitItem) {
                        if (onTrue.get(onTrue.size() - 1) instanceof ContinueItem) {
                            list.add(i + 1, onTrue.remove(onTrue.size() - 1));
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

    protected GraphTargetItem checkLoop(GraphPart part, List<GraphPart> stopPart, List<Loop> loops) {
        if (stopPart.contains(part)) {
            return null;
        }
        for (Loop l : loops) {
            if (l.loopContinue == part) {
                return (new ContinueItem(null, l.id));
            }
            if (l.loopBreak == part) {
                return (new BreakItem(null, l.id));
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

    protected List<GraphTargetItem> check(GraphSource code, List<Object> localData, List<GraphPart> allParts, Stack<GraphTargetItem> stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<Loop> loops, List<GraphTargetItem> output, Loop currentLoop, int staticOperation, String path) {
        return null;
    }

    protected GraphPart checkPart(Stack<GraphTargetItem> stack, List<Object> localData, GraphPart part, List<GraphPart> allParts) {
        return part;
    }

    @SuppressWarnings("unchecked")
    protected GraphTargetItem translatePartGetStack(List<Object> localData, GraphPart part, Stack<GraphTargetItem> stack, int staticOperation) {
        stack = (Stack<GraphTargetItem>) stack.clone();
        translatePart(localData, part, stack, staticOperation, null);
        return stack.pop();
    }

    protected List<GraphTargetItem> translatePart(List<Object> localData, GraphPart part, Stack<GraphTargetItem> stack, int staticOperation, String path) {
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

    protected List<GraphTargetItem> printGraph(List<GraphPart> visited, List<Object> localData, Stack<GraphTargetItem> stack, List<GraphPart> allParts, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<Loop> loops, int staticOperation, String path) {
        return printGraph(visited, localData, stack, allParts, parent, part, stopPart, loops, null, staticOperation, path);
    }

    protected GraphTargetItem checkLoop(LoopItem loopItem, List<Object> localData, List<Loop> loops) {
        return loopItem;
    }

    private void getPrecontinues(List<Object> localData, GraphPart parent, GraphPart part, List<Loop> loops, List<GraphPart> stopPart) {
        markLevels(localData, part, loops);
        //Note: this also marks part as precontinue when there is if
        /*
         while(k<10){
         if(k==7){
         trace(a);
         }else{
         trace(b);
         }
         //precontinue
         k++;
         }

         */
        looploops:
        for (Loop l : loops) {
            if (l.loopContinue != null) {
                Set<GraphPart> uniqueRefs = new HashSet<>();
                uniqueRefs.addAll(l.loopContinue.refs);
                if (uniqueRefs.size() == 2) { //only one path - from precontinue
                    List<GraphPart> uniqueRefsList = new ArrayList<>(uniqueRefs);
                    if (uniqueRefsList.get(0).discoveredTime > uniqueRefsList.get(1).discoveredTime) { //latch node is discovered later
                        part = uniqueRefsList.get(0);
                    } else {
                        part = uniqueRefsList.get(1);
                    }
                    if (part == l.loopContinue) {
                        continue looploops;
                    }

                    while (part.refs.size() == 1) {
                        if (part.refs.get(0).nextParts.size() != 1) {
                            continue looploops;
                        }

                        part = part.refs.get(0);
                        if (part == l.loopContinue) {
                            break;
                        }
                    }
                    if (part.level == 0 && part != l.loopContinue) {
                        l.loopPreContinue = part;
                    }
                }
            }
        }
        /*clearLoops(loops);
         getPrecontinues(parent, part, loops, stopPart, 0, new ArrayList<GraphPart>());
         clearLoops(loops);*/
    }

    private void markLevels(List<Object> localData, GraphPart part, List<Loop> loops) {
        clearLoops(loops);
        markLevels(localData, part, loops, new ArrayList<GraphPart>(), 1, new ArrayList<GraphPart>());
        clearLoops(loops);
    }

    private void markLevels(List<Object> localData, GraphPart part, List<Loop> loops, List<GraphPart> stopPart, int level, List<GraphPart> visited) {
        boolean debugMode = false;
        if (stopPart == null) {
            stopPart = new ArrayList<>();
        }
        if (debugMode) {
            System.err.println("markLevels " + part);
        }
        if (stopPart.contains(part)) {
            return;
        }
        for (Loop el : loops) {
            if ((el.phase == 2) && (el.loopContinue == part)) {
                return;
            }
            if (el.phase != 1) {
                if (debugMode) {
                    //System.err.println("ignoring "+el);
                }
                continue;
            }
            if (el.loopContinue == part) {
                return;
            }
            if (el.loopPreContinue == part) {
                return;
            }
            if (el.loopBreak == part) {
                return;
            }
        }


        if (visited.contains(part)) {
            part.level = 0;
        } else {
            visited.add(part);
            part.level = level;
        }

        boolean isLoop = false;
        Loop currentLoop = null;
        for (Loop el : loops) {
            if ((el.phase == 0) && (el.loopContinue == part)) {
                isLoop = true;
                currentLoop = el;
                el.phase = 1;
                break;
            }
        }


        List<GraphPart> nextParts = checkPrecoNextParts(part);
        if (nextParts == null) {
            nextParts = part.nextParts;
        }

        if (nextParts.size() == 2) {
            GraphPart next = getCommonPart(localData, nextParts, loops);//part.getNextPartPath(new ArrayList<GraphPart>());
            List<GraphPart> stopParts2 = new ArrayList<>();  //stopPart);
            if (next != null) {
                stopParts2.add(next);
            } else if (!stopPart.isEmpty()) {
                stopParts2.add(stopPart.get(stopPart.size() - 1));
            }
            if (next != nextParts.get(0)) {
                markLevels(localData, nextParts.get(0), loops, next == null ? stopPart : stopParts2, level + 1, visited);
            }
            if (next != nextParts.get(1)) {
                markLevels(localData, nextParts.get(1), loops, next == null ? stopPart : stopParts2, level + 1, visited);
            }
            if (next != null) {
                markLevels(localData, next, loops, stopPart, level, visited);
            }
        }

        if (nextParts.size() > 2) {
            GraphPart next = getMostCommonPart(localData, nextParts, loops);
            List<GraphPart> vis = new ArrayList<>();
            for (GraphPart p : nextParts) {
                if (vis.contains(p)) {
                    continue;
                }
                List<GraphPart> stopPart2 = new ArrayList<>(); //(stopPart);
                if (next != null) {
                    stopPart2.add(next);
                } else if (!stopPart.isEmpty()) {
                    stopPart2.add(stopPart.get(stopPart.size() - 1));
                }
                for (GraphPart p2 : nextParts) {
                    if (p2 == p) {
                        continue;
                    }
                    if (!stopPart2.contains(p2)) {
                        stopPart2.add(p2);
                    }
                }
                if (next != p) {
                    markLevels(localData, p, loops, stopPart2, level + 1, visited);
                    vis.add(p);
                }
            }
            if (next != null) {
                markLevels(localData, next, loops, stopPart, level, visited);
            }
        }

        if (nextParts.size() == 1) {
            markLevels(localData, nextParts.get(0), loops, stopPart, level, visited);
        }

        for (GraphPart t : part.throwParts) {
            if (!visited.contains(t)) {
                List<GraphPart> stopPart2 = new ArrayList<>();
                List<GraphPart> cmn = new ArrayList<>();
                cmn.add(part);
                cmn.add(t);
                GraphPart next = getCommonPart(localData, cmn, loops);
                if (next != null) {
                    stopPart2.add(next);
                } else {
                    stopPart2 = stopPart;
                }

                markLevels(localData, t, loops, stopPart2, level, visited);
            }
        }

        if (isLoop) {
            if (currentLoop.loopBreak != null) {
                currentLoop.phase = 2;
                markLevels(localData, currentLoop.loopBreak, loops, stopPart, level, visited);
            }
        }
    }

    private void clearLoops(List<Loop> loops) {
        for (Loop l : loops) {
            l.phase = 0;
        }
    }

    private void getLoops(List<Object> localData, GraphPart part, List<Loop> loops, List<GraphPart> stopPart) {
        clearLoops(loops);
        getLoops(localData, part, loops, stopPart, true, 1, new ArrayList<GraphPart>());
        clearLoops(loops);
    }

    private void getLoops(List<Object> localData, GraphPart part, List<Loop> loops, List<GraphPart> stopPart, boolean first, int level, List<GraphPart> visited) {
        boolean debugMode = false;

        if (stopPart == null) {
            stopPart = new ArrayList<>();
        }
        if (part == null) {
            return;
        }

        part = checkPart(null, localData, part, null);
        if (part == null) {
            return;
        }
        if (!visited.contains(part)) {
            visited.add(part);
        }

        if (debugMode) {
            System.err.println("getloops: " + part);
        }
        List<GraphPart> loopContinues = getLoopsContinues(loops);
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
        if (lastP1 != null) {
            if (lastP1.breakCandidates.contains(part)) {
                lastP1.breakCandidates.add(part);
                lastP1.breakCandidatesLevels.add(level);
                return;
            } else {
                List<GraphPart> loopContinues2 = new ArrayList<>(loopContinues);
                loopContinues2.remove(lastP1.loopContinue);
                List<Loop> loops2 = new ArrayList<>(loops);
                loops2.remove(lastP1);
                if (!part.leadsTo(localData, this, code, lastP1.loopContinue, loops2)) {
                    if (lastP1.breakCandidatesLocked == 0) {
                        if (debugMode) {
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

        if (stopPart.contains(part)) {
            return;
        }
        part.level = level;

        boolean isLoop = part.leadsTo(localData, this, code, part, loops);
        Loop currentLoop = null;
        if (isLoop) {
            currentLoop = new Loop(loops.size(), part, null);
            currentLoop.phase = 1;
            loops.add(currentLoop);
            loopContinues.add(part);
        }

        if (part.nextParts.size() == 2) {

            List<GraphPart> nps = new ArrayList<>(part.nextParts);
            /*for(int i=0;i<nps.size();i++){
             nps.set(i,getNextNoJump(nps.get(i),localData));
             }
             if(nps.get(0) == nps.get(1)){
             nps = part.nextParts;
             }*/
            nps = part.nextParts;
            GraphPart next = getCommonPart(localData, nps, loops);//part.getNextPartPath(loopContinues);
            List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
            if (next != null) {
                stopPart2.add(next);
            }
            if (next != nps.get(0)) {
                getLoops(localData, nps.get(0), loops, stopPart2, false, level + 1, visited);
            }
            if (next != nps.get(1)) {
                getLoops(localData, nps.get(1), loops, stopPart2, false, level + 1, visited);
            }
            if (next != null) {
                getLoops(localData, next, loops, stopPart, false, level, visited);
            }
        }
        if (part.nextParts.size() > 2) {
            GraphPart next = getNextCommonPart(localData, part, loops);


            for (GraphPart p : part.nextParts) {
                List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
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
                    getLoops(localData, p, loops, stopPart2, false, level + 1, visited);
                }
            }
            if (next != null) {
                getLoops(localData, next, loops, stopPart, false, level, visited);
            }
        }
        if (part.nextParts.size() == 1) {
            getLoops(localData, part.nextParts.get(0), loops, stopPart, false, level, visited);
        }

        List<Loop> loops2 = new ArrayList<>(loops);
        for (Loop l : loops2) {
            l.breakCandidatesLocked++;
        }
        for (GraphPart t : part.throwParts) {
            if (!visited.contains(t)) {
                getLoops(localData, t, loops, stopPart, false, level, visited);
            }
        }
        for (Loop l : loops2) {
            l.breakCandidatesLocked--;
        }

        if (isLoop) {
            GraphPart found;
            Map<GraphPart, Integer> removed = new HashMap<>();
            do {
                found = null;
                for (int i = 0; i < currentLoop.breakCandidates.size(); i++) {
                    GraphPart ch = checkPart(null, localData, currentLoop.breakCandidates.get(i), null);
                    if (ch == null) {
                        currentLoop.breakCandidates.remove(i);
                        i--;
                    }
                }
                loopcand:
                for (GraphPart cand : currentLoop.breakCandidates) {
                    for (GraphPart cand2 : currentLoop.breakCandidates) {
                        if (cand == cand2) {
                            continue;
                        }
                        if (cand.leadsTo(localData, this, code, cand2, loops)) {
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
                            if (lev1 <= lev2) {
                                found = cand2;
                            } else {
                                found = cand;
                            }
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
                } else if (count.get(cand) == winnerCount) {
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
                getLoops(localData, r, loops, stopPart, false, removed.get(r), visited);
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
            getLoops(localData, currentLoop.loopBreak, loops, stopPart, false, level, visited);
        }
    }

    protected List<GraphTargetItem> printGraph(List<GraphPart> visited, List<Object> localData, Stack<GraphTargetItem> stack, List<GraphPart> allParts, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<Loop> loops, List<GraphTargetItem> ret, int staticOperation, String path) {
        if (stopPart == null) {
            stopPart = new ArrayList<>();
        }
        if (visited.contains(part)) {
            //return new ArrayList<GraphTargetItem>();
        } else {
            visited.add(part);
        }
        if (ret == null) {
            ret = new ArrayList<>();
        }
        //try {
        boolean debugMode = false;


        if (debugMode) {
            System.err.println("PART " + part + " nextsize:" + part.nextParts.size());
        }

        /*while (((part != null) && (part.getHeight() == 1)) && (code.size() > part.start) && (code.get(part.start).isJump())) {  //Parts with only jump in it gets ignored

         if (part == stopPart) {
         return ret;
         }
         GraphTargetItem lop = checkLoop(part.nextParts.get(0), stopPart, loops);
         if (lop == null) {
         part = part.nextParts.get(0);
         } else {
         break;
         }
         }*/





        if (part == null) {
            return ret;
        }
        part = checkPart(stack, localData, part, allParts);
        if (part == null) {
            return ret;
        }

        if (part.ignored) {
            return ret;
        }

        List<GraphPart> loopContinues = getLoopsContinues(loops);
        boolean isLoop = false;
        Loop currentLoop = null;
        for (Loop el : loops) {
            if ((el.loopContinue == part) && (el.phase == 0)) {
                currentLoop = el;
                currentLoop.phase = 1;
                isLoop = true;
                break;
            }
        }

        if (debugMode) {
            System.err.println("loopsize:" + loops.size());
        }
        for (int l = loops.size() - 1; l >= 0; l--) {
            Loop el = loops.get(l);
            if (el == currentLoop) {
                if (debugMode) {
                    System.err.println("ignoring current loop " + el);
                }
                continue;
            }
            if (el.phase != 1) {
                if (debugMode) {
                    //System.err.println("ignoring loop "+el);
                }
                continue;
            }
            if (el.loopBreak == part) {
                if (currentLoop != null) {
                    currentLoop.phase = 0;
                }
                if (debugMode) {
                    System.err.println("Adding break");
                }
                ret.add(new BreakItem(null, el.id));
                return ret;
            }
            if (el.loopPreContinue == part) {
                if (currentLoop != null) {
                    currentLoop.phase = 0;
                }
                if (debugMode) {
                    System.err.println("Adding precontinue");
                }
                ret.add(new ContinueItem(null, el.id));
                return ret;
            }
            if (el.loopContinue == part) {
                if (currentLoop != null) {
                    currentLoop.phase = 0;
                }
                if (debugMode) {
                    System.err.println("Adding continue");
                }
                ret.add(new ContinueItem(null, el.id));
                return ret;
            }
        }



        if (stopPart.contains(part)) {
            if (currentLoop != null) {
                currentLoop.phase = 0;
            }
            return ret;
        }

        if ((part != null) && (code.size() <= part.start)) {
            ret.add(new ScriptEndItem());
            return ret;
        }
        List<GraphTargetItem> currentRet = ret;
        UniversalLoopItem loopItem = null;
        if (isLoop) {
            loopItem = new UniversalLoopItem(null, currentLoop);
            //loopItem.commands=printGraph(visited, localData, stack, allParts, parent, part, stopPart, loops);
            currentRet.add(loopItem);
            loopItem.commands = new ArrayList<>();
            currentRet = loopItem.commands;
            //return ret;
        }

        boolean parseNext = true;

        //****************************DECOMPILING PART*************
        List<GraphTargetItem> output = new ArrayList<>();

        List<GraphPart> parts = new ArrayList<>();
        if (part instanceof GraphPartMulti) {
            parts = ((GraphPartMulti) part).parts;
        } else {
            parts.add(part);
        }
        for (GraphPart p : parts) {
            int end = p.end;
            int start = p.start;

            output.addAll(code.translatePart(p, localData, stack, start, end, staticOperation, path));
            if ((end >= code.size() - 1) && p.nextParts.isEmpty()) {
                output.add(new ScriptEndItem());
            }
        }

        //Assuming part with two nextparts is an IF

        /* //If with both branches empty
         if (part.nextParts.size() == 2) {
         if (part.nextParts.get(0) == part.nextParts.get(1)) {
         if (!stack.isEmpty()) {
         GraphTargetItem expr = stack.pop();
         if (expr instanceof LogicalOpItem) {
         expr = ((LogicalOpItem) expr).invert();
         } else {
         expr = new NotItem(null, expr);
         }
         output.add(new IfItem(null, expr, new ArrayList<GraphTargetItem>(), new ArrayList<GraphTargetItem>()));
         }
         part.nextParts.remove(0);
         }
         }*/

        if (parseNext) {
            List<GraphTargetItem> retCheck = check(code, localData, allParts, stack, parent, part, stopPart, loops, output, currentLoop, staticOperation, path);
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

        /**
         * AND / OR detection
         */
        if (part.nextParts.size() == 2) {
            if ((stack.size() >= 2) && (stack.get(stack.size() - 1) instanceof NotItem) && (((NotItem) (stack.get(stack.size() - 1))).getOriginal().getNotCoerced() == stack.get(stack.size() - 2).getNotCoerced())) {
                GraphPart sp0 = getNextNoJump(part.nextParts.get(0), localData);
                GraphPart sp1 = getNextNoJump(part.nextParts.get(1), localData);
                boolean reversed = false;
                loopContinues = getLoopsContinues(loops);
                loopContinues.add(part);//???
                if (sp1.leadsTo(localData, this, code, sp0, loops)) {
                } else if (sp0.leadsTo(localData, this, code, sp1, loops)) {
                    reversed = true;
                }
                GraphPart next = reversed ? sp0 : sp1;
                GraphTargetItem ti;
                if ((ti = checkLoop(next, stopPart, loops)) != null) {
                    currentRet.add(ti);
                } else {
                    List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                    stopPart2.add(reversed ? sp1 : sp0);
                    printGraph(visited, localData, stack, allParts, parent, next, stopPart2, loops, staticOperation, path);
                    GraphTargetItem second = stack.pop();
                    GraphTargetItem first = stack.pop();

                    if (!reversed) {
                        AndItem a = new AndItem(null, first, second);
                        stack.push(a);
                        a.firstPart = part;
                        if (second instanceof AndItem) {
                            a.firstPart = ((AndItem) second).firstPart;
                        }
                        if (second instanceof OrItem) {
                            a.firstPart = ((OrItem) second).firstPart;
                        }
                    } else {
                        OrItem o = new OrItem(null, first, second);
                        stack.push(o);
                        o.firstPart = part;
                        if (second instanceof AndItem) {
                            o.firstPart = ((AndItem) second).firstPart;
                        }
                        if (second instanceof OrItem) {
                            o.firstPart = ((OrItem) second).firstPart;
                        }
                    }
                    next = reversed ? sp1 : sp0;
                    if ((ti = checkLoop(next, stopPart, loops)) != null) {
                        currentRet.add(ti);
                    } else {
                        currentRet.addAll(printGraph(visited, localData, stack, allParts, parent, next, stopPart, loops, staticOperation, path));
                    }
                }
                parseNext = false;
                //return ret;
            } else if ((stack.size() >= 2) && (stack.get(stack.size() - 1).getNotCoerced() == stack.get(stack.size() - 2).getNotCoerced())) {
                GraphPart sp0 = getNextNoJump(part.nextParts.get(0), localData);
                GraphPart sp1 = getNextNoJump(part.nextParts.get(1), localData);
                boolean reversed = false;
                loopContinues = getLoopsContinues(loops);
                loopContinues.add(part);//???
                if (sp1.leadsTo(localData, this, code, sp0, loops)) {
                } else if (sp0.leadsTo(localData, this, code, sp1, loops)) {
                    reversed = true;
                }
                GraphPart next = reversed ? sp0 : sp1;
                GraphTargetItem ti;
                if ((ti = checkLoop(next, stopPart, loops)) != null) {
                    currentRet.add(ti);
                } else {
                    List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                    stopPart2.add(reversed ? sp1 : sp0);
                    printGraph(visited, localData, stack, allParts, parent, next, stopPart2, loops, staticOperation, path);
                    GraphTargetItem second = stack.pop();
                    GraphTargetItem first = stack.pop();

                    if (reversed) {
                        AndItem a = new AndItem(null, first, second);
                        stack.push(a);
                        a.firstPart = part;
                        if (second instanceof AndItem) {
                            a.firstPart = ((AndItem) second).firstPart;
                        }
                        if (second instanceof OrItem) {
                            a.firstPart = ((AndItem) second).firstPart;
                        }
                    } else {
                        OrItem o = new OrItem(null, first, second);
                        stack.push(o);
                        o.firstPart = part;
                        if (second instanceof OrItem) {
                            o.firstPart = ((OrItem) second).firstPart;
                        }
                        if (second instanceof OrItem) {
                            o.firstPart = ((OrItem) second).firstPart;
                        }
                    }

                    next = reversed ? sp1 : sp0;
                    if ((ti = checkLoop(next, stopPart, loops)) != null) {
                        currentRet.add(ti);
                    } else {
                        currentRet.addAll(printGraph(visited, localData, stack, allParts, parent, next, stopPart, loops, staticOperation, path));
                    }
                }
                parseNext = false;
                //return ret;
            }
        }
//********************************END PART DECOMPILING


        if (parseNext) {


            if (false && part.nextParts.size() > 2) {//alchemy direct switch
                GraphPart next = getMostCommonPart(localData, part.nextParts, loops);
                List<GraphPart> vis = new ArrayList<>();
                GraphTargetItem switchedItem = stack.pop();
                List<GraphTargetItem> caseValues = new ArrayList<>();
                List<List<GraphTargetItem>> caseCommands = new ArrayList<>();
                List<GraphTargetItem> defaultCommands = new ArrayList<>();
                List<Integer> valueMappings = new ArrayList<>();
                Loop swLoop = new Loop(loops.size(), null, next);
                swLoop.phase = 1;
                loops.add(swLoop);
                boolean first = false;
                int pos = 0;
                for (GraphPart p : part.nextParts) {

                    if (!first) {
                        caseValues.add(new IntegerValueItem(null, pos++));
                        if (vis.contains(p)) {
                            valueMappings.add(caseCommands.size() - 1);
                            continue;
                        }

                        valueMappings.add(caseCommands.size());
                    }
                    List<GraphPart> stopPart2 = new ArrayList<>();
                    if (next != null) {
                        stopPart2.add(next);
                    } else if (!stopPart.isEmpty()) {
                        stopPart2.add(stopPart.get(stopPart.size() - 1));
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
                        if (first) {
                            defaultCommands = printGraph(visited, prepareBranchLocalData(localData), stack, allParts, part, p, stopPart2, loops, staticOperation, path);
                        } else {
                            caseCommands.add(printGraph(visited, prepareBranchLocalData(localData), stack, allParts, part, p, stopPart2, loops, staticOperation, path));
                        }
                        vis.add(p);
                    }
                    first = false;
                }
                SwitchItem sw = new SwitchItem(null, swLoop, switchedItem, caseValues, caseCommands, defaultCommands, valueMappings);
                currentRet.add(sw);
                swLoop.phase = 2;
                if (next != null) {
                    currentRet.addAll(printGraph(visited, localData, stack, allParts, part, next, stopPart, loops, staticOperation, path));
                }
            } //else
            GraphPart nextOnePart = null;
            if (part.nextParts.size() == 2) {
                GraphTargetItem expr = stack.pop();
                if (expr instanceof LogicalOpItem) {
                    expr = ((LogicalOpItem) expr).invert();
                } else {
                    expr = new NotItem(null, expr);
                }
                if (staticOperation != SOP_USE_STATIC) {
                    if (expr.isCompileTime()) {
                        boolean doJump = EcmaScript.toBoolean(expr.getResult());
                        if (doJump) {
                            nextOnePart = part.nextParts.get(0);
                        } else {
                            nextOnePart = part.nextParts.get(1);
                        }
                        if (staticOperation == SOP_REMOVE_STATIC) {
                            //TODO
                        }
                    }
                }
                if (nextOnePart == null) {

                    List<GraphPart> nps = new ArrayList<>(part.nextParts);
                    /*for(int i=0;i<nps.size();i++){
                     nps.set(i,getNextNoJump(nps.get(i),localData));
                     }
                     if(nps.get(0) == nps.get(1)){
                     nps = part.nextParts;
                     }*/
                    nps = part.nextParts;
                    GraphPart next = getCommonPart(localData, nps, loops);

                    @SuppressWarnings("unchecked")
                    Stack<GraphTargetItem> trueStack = (Stack<GraphTargetItem>) stack.clone();
                    @SuppressWarnings("unchecked")
                    Stack<GraphTargetItem> falseStack = (Stack<GraphTargetItem>) stack.clone();
                    int trueStackSizeBefore = trueStack.size();
                    int falseStackSizeBefore = falseStack.size();
                    List<GraphTargetItem> onTrue = new ArrayList<>();
                    boolean isEmpty = nps.get(0) == nps.get(1);

                    if (isEmpty) {
                        next = nps.get(0);
                    }

                    List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                    if (next != null) {
                        stopPart2.add(next);
                    }
                    if (!isEmpty) {
                        onTrue = printGraph(visited, prepareBranchLocalData(localData), trueStack, allParts, part, nps.get(1), stopPart2, loops, staticOperation, path);
                    }
                    List<GraphTargetItem> onFalse = new ArrayList<>();

                    if (!isEmpty) {
                        onFalse = printGraph(visited, prepareBranchLocalData(localData), falseStack, allParts, part, nps.get(0), stopPart2, loops, staticOperation, path);
                    }
                    if (isEmpty(onTrue) && isEmpty(onFalse) && (trueStack.size() > trueStackSizeBefore) && (falseStack.size() > falseStackSizeBefore)) {
                        stack.push(new TernarOpItem(null, expr, trueStack.pop(), falseStack.pop()));
                    } else {
                        currentRet.add(new IfItem(null, expr, onTrue, onFalse));
                    }
                    if (next != null) {
                        printGraph(visited, localData, stack, allParts, part, next, stopPart, loops, currentRet, staticOperation, path);
                        //currentRet.addAll();
                    }
                }
            }  //else
            if (part.nextParts.size() == 1) {
                nextOnePart = part.nextParts.get(0);
            }
            if (nextOnePart != null) {
                printGraph(visited, localData, stack, allParts, part, part.nextParts.get(0), stopPart, loops, currentRet, staticOperation, path);
            }

        }
        if (isLoop) {

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
                    GraphPart precoBackup = currentLoop.loopPreContinue;
                    currentLoop.loopPreContinue = null;
                    loopItem.commands.addAll(printGraph(visited, localData, new Stack<GraphTargetItem>(), allParts, null, precoBackup, stopContPart, loops, staticOperation, path));
                }
            }

            //Loop with condition at the beginning (While)
            if (!loopTypeFound && (!loopItem.commands.isEmpty())) {
                if (loopItem.commands.get(0) instanceof IfItem) {
                    IfItem ifi = (IfItem) loopItem.commands.get(0);


                    List<GraphTargetItem> bodyBranch = null;
                    boolean inverted = false;
                    boolean breakpos2 = false;
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
                        if (bi.loopId == currentLoop.id) {
                            bodyBranch = ifi.onTrue;
                            breakpos2 = true;
                        }
                    }
                    if (bodyBranch != null) {
                        int index = ret.indexOf(loopItem);
                        ret.remove(index);
                        List<GraphTargetItem> exprList = new ArrayList<>();
                        GraphTargetItem expr = ifi.expression;
                        if (inverted) {
                            if (expr instanceof LogicalOpItem) {
                                expr = ((LogicalOpItem) expr).invert();
                            } else {
                                expr = new NotItem(null, expr);
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
                        if (currentLoop.loopPreContinue != null) {
                            GraphPart backup = currentLoop.loopPreContinue;
                            currentLoop.loopPreContinue = null;
                            List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                            stopPart2.add(currentLoop.loopContinue);
                            finalComm = printGraph(visited, localData, new Stack<GraphTargetItem>(), allParts, null, backup, stopPart2, loops, staticOperation, path);
                            currentLoop.loopPreContinue = backup;
                            checkContinueAtTheEnd(finalComm, currentLoop);
                        }
                        if (!finalComm.isEmpty()) {
                            ret.add(index, li = new ForItem(null, currentLoop, new ArrayList<GraphTargetItem>(), exprList.get(exprList.size() - 1), finalComm, commands));
                        } else {
                            ret.add(index, li = new WhileItem(null, currentLoop, exprList, commands));
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
                            if (expr instanceof LogicalOpItem) {
                                expr = ((LogicalOpItem) expr).invert();
                            } else {
                                expr = new NotItem(null, expr);
                            }
                        }

                        checkContinueAtTheEnd(bodyBranch, currentLoop);


                        List<GraphTargetItem> commands = new ArrayList<>();

                        if (!bodyBranch.isEmpty()) {
                            ret.add(index, loopItem);
                            /*
                             loopItem.commands.remove(loopItem.commands.size() - 1);
                             exprList.addAll(loopItem.commands);
                             commands.addAll(bodyBranch);
                             exprList.add(expr);
                             checkContinueAtTheEnd(commands, currentLoop);
                             ret.add(index, li = new WhileItem(null, currentLoop, exprList, commands));*/
                        } else {
                            loopItem.commands.remove(loopItem.commands.size() - 1);
                            commands.addAll(loopItem.commands);
                            commands.addAll(bodyBranch);
                            exprList.add(expr);
                            checkContinueAtTheEnd(commands, currentLoop);
                            ret.add(index, li = new DoWhileItem(null, currentLoop, commands, exprList));

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
                    List<GraphTargetItem> finalComm = printGraph(visited, localData, new Stack<GraphTargetItem>(), allParts, null, backup, stopPart2, loops, staticOperation, path);
                    currentLoop.loopPreContinue = backup;
                    checkContinueAtTheEnd(finalComm, currentLoop);

                    if (!finalComm.isEmpty()) {
                        if (finalComm.get(finalComm.size() - 1) instanceof IfItem) {
                            IfItem ifi = (IfItem) finalComm.get(finalComm.size() - 1);
                            boolean ok = false;
                            boolean invert = false;
                            if (((ifi.onTrue.size() == 1) && (ifi.onTrue.get(0) instanceof BreakItem) && (((BreakItem) ifi.onTrue.get(0)).loopId == currentLoop.id))
                                    && ((ifi.onTrue.size() == 1) && (ifi.onFalse.get(0) instanceof ContinueItem) && (((ContinueItem) ifi.onFalse.get(0)).loopId == currentLoop.id))) {
                                ok = true;
                                invert = true;
                            }
                            if (((ifi.onTrue.size() == 1) && (ifi.onTrue.get(0) instanceof ContinueItem) && (((ContinueItem) ifi.onTrue.get(0)).loopId == currentLoop.id))
                                    && ((ifi.onTrue.size() == 1) && (ifi.onFalse.get(0) instanceof BreakItem) && (((BreakItem) ifi.onFalse.get(0)).loopId == currentLoop.id))) {
                                ok = true;
                            }
                            if (ok) {
                                finalComm.remove(finalComm.size() - 1);
                                int index = ret.indexOf(loopItem);
                                ret.remove(index);
                                List<GraphTargetItem> exprList = new ArrayList<>(finalComm);
                                GraphTargetItem expr = ifi.expression;
                                if (invert) {
                                    if (expr instanceof LogicalOpItem) {
                                        expr = ((LogicalOpItem) expr).invert();
                                    } else {
                                        expr = new NotItem(null, expr);
                                    }
                                }
                                exprList.add(expr);
                                ret.add(index, li = new DoWhileItem(null, currentLoop, loopItem.commands, exprList));
                            }
                        }
                    }
                }
            }

            if (!loopTypeFound) {
                checkContinueAtTheEnd(loopItem.commands, currentLoop);
            }
            currentLoop.phase = 2;

            GraphTargetItem replaced = checkLoop(li, localData, loops);
            if (replaced != li) {
                int index = ret.indexOf(li);
                ret.remove(index);
                if (replaced != null) {
                    ret.add(index, replaced);
                }
            }

            if (currentLoop.loopBreak != null) {
                ret.addAll(printGraph(visited, localData, stack, allParts, part, currentLoop.loopBreak, stopPart, loops, staticOperation, path));
            }
        }

        return ret;

    }

    protected void checkGraph(List<GraphPart> allBlocks) {
    }

    private List<GraphPart> makeGraph(GraphSource code, List<GraphPart> allBlocks, List<Integer> alternateEntries) {
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

    private GraphPart makeGraph(GraphPart parent, GraphPath path, GraphSource code, int startip, int lastIp, List<GraphPart> allBlocks, HashMap<Integer, List<Integer>> refs, boolean[] visited2) {

        int ip = startip;
        for (GraphPart p : allBlocks) {
            if (p.start == ip) {
                p.refs.add(parent);
                return p;
            }
        }
        GraphPart g;
        GraphPart ret = new GraphPart(ip, -1);
        ret.path = path;
        GraphPart part = ret;
        while (ip < code.size()) {
            if (visited2[ip] || ((ip != startip) && (refs.get(ip).size() > 1))) {
                part.end = lastIp;
                GraphPart found = null;
                for (GraphPart p : allBlocks) {
                    if (p.start == ip) {
                        found = p;
                        break;
                    }
                }

                allBlocks.add(part);

                if (found != null) {
                    part.nextParts.add(found);
                    found.refs.add(part);
                    break;
                } else {
                    GraphPart gp = new GraphPart(ip, -1);
                    gp.path = path;
                    part.nextParts.add(gp);
                    gp.refs.add(part);
                    part = gp;
                }
            }

            ip = checkIp(ip);
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
                part.nextParts.add(g = makeGraph(part, path, code, ip, lastIp, allBlocks, refs, visited2));
                g.refs.add(part);
                break;
            } else if (ins.isBranch()) {
                part.end = ip;

                allBlocks.add(part);
                List<Integer> branches = ins.getBranches(code);
                for (int i = 0; i < branches.size(); i++) {
                    part.nextParts.add(g = makeGraph(part, path.sub(i, ip), code, branches.get(i), ip, allBlocks, refs, visited2));
                    g.refs.add(part);
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
    public static final String INDENT_STRING = "   ";

    private static String tabString(int len) {
        String ret = "";
        for (int i = 0; i < len; i++) {
            ret += INDENT_STRING;
        }
        return ret;
    }

    /**
     * Converts list of TreeItems to string
     *
     * @param tree List of TreeItem
     * @param localData
     * @return String
     */
    public static String graphToString(List<GraphTargetItem> tree, boolean highlight, boolean replaceIndents, Object... localData) {
        StringBuilder ret = new StringBuilder();
        List<Object> localDataList = Arrays.asList(localData);
        for (GraphTargetItem ti : tree) {
            if (!ti.isEmpty()) {
                ret.append(ti.toStringSemicoloned(highlight, localDataList));
                ret.append("\r\n");
            }
        }
        String[] parts = ret.toString().split("\r\n");
        ret = new StringBuilder();

        String labelPattern = "loop(switch)?[0-9]*:";
        try {
            Stack<String> loopStack = new Stack<>();
            for (int p = 0; p < parts.length; p++) {
                String stripped = Highlighting.stripHilights(parts[p]);
                if (stripped.matches(labelPattern)) {
                    loopStack.add(stripped.substring(0, stripped.length() - 1));
                }
                if (stripped.startsWith("break ")) {
                    if (stripped.equals("break " + loopStack.peek().replace("switch", "") + ";")) {
                        parts[p] = parts[p].replace(" " + loopStack.peek().replace("switch", ""), "");
                    }
                }
                if (stripped.startsWith("continue ")) {
                    if (loopStack.size() > 0) {
                        int pos = loopStack.size() - 1;
                        String loopname = "";
                        do {
                            loopname = loopStack.get(pos);
                            pos--;
                        } while ((pos >= 0) && (loopname.startsWith("loopswitch")));
                        if (stripped.equals("continue " + loopname + ";")) {
                            parts[p] = parts[p].replace(" " + loopname, "");
                        }
                    }
                }
                if (stripped.startsWith(":")) {
                    loopStack.pop();
                }
            }
        } catch (Exception ex) {
        }

        int level = 0;
        for (int p = 0; p < parts.length; p++) {
            String strippedP = Highlighting.stripHilights(parts[p]).trim();
            if (strippedP.matches(labelPattern)) {//endsWith(":") && (!strippedP.startsWith("case ")) && (!strippedP.equals("default:"))) {
                String loopname = strippedP.substring(0, strippedP.length() - 1);
                boolean dorefer = false;
                for (int q = p + 1; q < parts.length; q++) {
                    String strippedQ = Highlighting.stripHilights(parts[q]).trim();
                    if (strippedQ.equals("break " + loopname.replace("switch", "") + ";")) {
                        dorefer = true;
                        break;
                    }
                    if (strippedQ.equals("continue " + loopname + ";")) {
                        dorefer = true;
                        break;
                    }
                    if (strippedQ.equals(":" + loopname)) {
                        break;
                    }
                }
                if (!dorefer) {
                    continue;
                }
            }
            if (strippedP.startsWith(":")) {
                continue;
            }
            strippedP = Highlighting.stripHilights(parts[p]).trim();
            
            if (replaceIndents) {
                if (strippedP.equals(INDENTOPEN)) {
                    level++;
                    continue;
                }
                if (strippedP.equals(INDENTCLOSE)) {
                    level--;
                    continue;
                }
            }
            ret.append(tabString(level));
            ret.append(parts[p].trim());
            ret.append("\r\n");
        }
        return ret.toString();
    }

    public List<Object> prepareBranchLocalData(List<Object> localData) {
        return localData;
    }

    protected List<GraphPart> checkPrecoNextParts(GraphPart part) {
        return null;
    }

    protected GraphPart makeMultiPart(GraphPart part) {
        List<GraphPart> parts = new ArrayList<>();
        do {
            parts.add(part);
            if (part.nextParts.size() == 1 && part.nextParts.get(0).refs.size() == 1) {
                part = part.nextParts.get(0);
            } else {
                part = null;
            }
        } while (part != null);
        if (parts.size() > 1) {
            GraphPartMulti ret = new GraphPartMulti(parts);
            ret.refs.addAll(parts.get(0).refs);
            ret.nextParts.addAll(parts.get(parts.size() - 1).nextParts);
            return ret;
        } else {
            return parts.get(0);
        }
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
}
