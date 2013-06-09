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
package com.jpexs.decompiler.flash.graph;

import com.jpexs.decompiler.flash.abc.avm2.treemodel.CommentTreeItem;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public Graph(GraphSource code, List<Integer> alternateEntries) {
        this.code = code;
        heads = makeGraph(code, new ArrayList<GraphPart>(), alternateEntries);
        for (GraphPart head : heads) {
            fixGraph(head);
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

    private void fixGraph(GraphPart part) {
        while (fixGraphOnce(part, new ArrayList<GraphPart>(), false)) {
        }
    }

    private boolean fixGraphOnce(GraphPart part, List<GraphPart> visited, boolean doChildren) {
        if (visited.contains(part)) {
            return false;
        }
        visited.add(part);
        boolean fixed = false;
        int i = 0;
        GraphPath lastpref = null;
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
                    if (part.leadsTo(code, r, new ArrayList<GraphPart>())) {
                        //modify=false;
                        //continue;
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
                        fixGraphOnce(part, new ArrayList<GraphPart>(), true);
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
            if (part.nextParts.get(1).leadsTo(code, part.nextParts.get(0), visited)) {
                fixGraphOnce(part.nextParts.get(1), visited, doChildren);
                fixGraphOnce(part.nextParts.get(0), visited, doChildren);
            } else {
                fixGraphOnce(part.nextParts.get(0), visited, doChildren);
                fixGraphOnce(part.nextParts.get(1), visited, doChildren);
            }
        } else {
            for (int j = part.nextParts.size() - 1; j >= 0; j--) {
                GraphPart p = part.nextParts.get(j);
                fixGraphOnce(p, visited, doChildren);
            }
        }
        return fixed;
    }

    private void makeMulti(GraphPart part, List<GraphPart> visited) {
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

    public GraphPart getCommonPart(List<GraphPart> parts) {
        GraphPart head = new GraphPart(0, 0);
        head.nextParts.addAll(parts);
        List<GraphPart> allVisited = new ArrayList<>();
        head = deepCopy(head, allVisited, null);
        for (int g = 0; g < head.nextParts.size(); g++) {
            GraphPart gr = head.nextParts.get(g);
            for (GraphPart r : gr.refs) {
                r.nextParts.remove(gr);
            }
            gr.refs.clear();
            gr.refs.add(head);
        }
        head.path = new GraphPath();
        resetGraph(head, new ArrayList<GraphPart>());
        fixGraph(head);

        /*Graph gr=new Graph();
         gr.heads=new ArrayList<GraphPart>();
         gr.heads.add(head);
         GraphFrame gf=new GraphFrame(gr, "");
         gf.setVisible(true);
         */

        GraphPart next = head.getNextPartPath(new ArrayList<GraphPart>());
        if (next == null) {
            return null;
        }
        for (GraphPart g : allVisited) {
            if (g.start == next.start) {
                return g;
            }
        }
        return null;
    }

    public GraphPart getNextNoJump(GraphPart part) {
        while (code.get(part.start).isJump()) {
            part = part.getSubParts().get(0).nextParts.get(0);
        }
        return part;
    }

    public static List<GraphTargetItem> translateViaGraph(List<Object> localData, String path, GraphSource code, List<Integer> alternateEntries) {
        Graph g = new Graph(code, alternateEntries);
        return g.translate(localData);
    }

    public List<GraphTargetItem> translate(List<Object> localData) {
        List<GraphPart> allParts = new ArrayList<>();
        for (GraphPart head : heads) {
            populateParts(head, allParts);
        }
        Stack<GraphTargetItem> stack = new Stack<>();
        List<Loop> loops = new ArrayList<>();
        getLoops(heads.get(0), loops, null);
        /*System.out.println("<loops>");
         for (Loop el : loops) {
         System.out.println(el);
         }
         System.out.println("</loops>");*/
        getPrecontinues(null, heads.get(0), loops, null);
        /*System.out.println("<loopspre>");
         for (Loop el : loops) {
         System.out.println(el);
         }
         System.out.println("</loopspre>");*/
        List<GraphTargetItem> ret = printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, null, heads.get(0), null, loops);
        processIfs(ret);
        finalProcessStack(stack, ret);
        finalProcessAll(ret, 0);
        return ret;
    }

    public void finalProcessStack(Stack<GraphTargetItem> stack, List<GraphTargetItem> output) {
    }

    private void finalProcessAll(List<GraphTargetItem> list, int level) {
        finalProcess(list, level);
        for (GraphTargetItem item : list) {
            if (item instanceof Block) {
                List<List<GraphTargetItem>> subs = ((Block) item).getSubs();
                for (List<GraphTargetItem> sub : subs) {
                    finalProcessAll(sub, level + 1);
                }
            }
        }
    }

    protected void finalProcess(List<GraphTargetItem> list, int level) {
    }

    private void processIfs(List<GraphTargetItem> list) {
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

    protected GraphTargetItem checkLoop(GraphPart part, GraphPart stopPart, List<Loop> loops) {
        if (part == stopPart) {
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

    protected List<GraphTargetItem> check(GraphSource code, List<Object> localData, List<GraphPart> allParts, Stack<GraphTargetItem> stack, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops, List<GraphTargetItem> output) {
        return null;
    }

    protected GraphPart checkPart(List<Object> localData, GraphPart part) {
        return part;
    }

    @SuppressWarnings("unchecked")
    protected GraphTargetItem translatePartGetStack(List<Object> localData, GraphPart part, Stack<GraphTargetItem> stack) {
        stack = (Stack<GraphTargetItem>) stack.clone();
        translatePart(localData, part, stack);
        return stack.pop();
    }

    protected List<GraphTargetItem> translatePart(List<Object> localData, GraphPart part, Stack<GraphTargetItem> stack) {
        List<GraphPart> sub = part.getSubParts();
        List<GraphTargetItem> ret = new ArrayList<>();
        int end = 0;
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
            ret.addAll(code.translatePart(part, localData, stack, start, end));
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

    protected List<GraphTargetItem> printGraph(List<GraphPart> visited, List<Object> localData, Stack<GraphTargetItem> stack, List<GraphPart> allParts, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops) {
        return printGraph(visited, localData, stack, allParts, parent, part, stopPart, loops, null);
    }

    protected GraphTargetItem checkLoop(LoopItem loopItem, List<Object> localData, List<Loop> loops) {
        return loopItem;
    }

    private void getPrecontinues(GraphPart parent, GraphPart part, List<Loop> loops, List<GraphPart> stopParts) {
        if (stopParts == null) {
            stopParts = new ArrayList<>();
        }




        for (Loop el : loops) {
            if (el.precoPhase != 1) {
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

        if ((parent != null) && (part.path.length() < parent.path.length())) {
            if (part.refs.size() > 1) {
                List<GraphPart> nextList = new ArrayList<>();
                populateParts(part, nextList);
                Loop nearestLoop = null;
                loopn:
                for (GraphPart n : nextList) {
                    if ((!stopParts.isEmpty()) && stopParts.get(stopParts.size() - 1) == n) {
                        break;
                    }
                    for (Loop l : loops) {
                        if (l.loopContinue == n) {
                            nearestLoop = l;
                            break loopn;
                        }
                    }
                }

                if ((nearestLoop != null) && (nearestLoop.loopContinue != part)) {// && (nearestLoop.loopBreak != null)) {
                    if (nearestLoop.precoPhase == 1) {
                        if ((nearestLoop.loopPreContinue == null) || (nearestLoop.loopPreContinue.leadsTo(code, part, getLoopsContinues(loops)))) {
                            nearestLoop.loopPreContinue = part;
                        }
                    }
                    return;
                }
            }

        }

        if (stopParts.contains(part)) {
            return;
        }

        List<GraphPart> loopContinues = getLoopsContinues(loops);
        boolean isLoop = false;
        Loop currentLoop = null;
        for (Loop el : loops) {
            if ((el.precoPhase == 0) && (el.loopContinue == part)) {
                isLoop = true;
                currentLoop = el;
                break;
            }
        }
        if (isLoop) {
            currentLoop.precoPhase = 1;
        }
        if (part.nextParts.size() == 2) {
            GraphPart next = part.getNextPartPath(new ArrayList<GraphPart>());
            List<GraphPart> stopParts2 = new ArrayList<>(/*stopParts*/);
            if (next != null) {
                stopParts2.add(next);
            }
            getPrecontinues(part, part.nextParts.get(0), loops, next == null ? stopParts : stopParts2);
            getPrecontinues(part, part.nextParts.get(1), loops, next == null ? stopParts : stopParts2);
            if (next != null) {
                getPrecontinues(part, next, loops, stopParts);
            }
        }

        if (part.nextParts.size() > 2) {
            GraphPart next = getCommonPart(part.nextParts);
            List<GraphPart> stopParts2 = new ArrayList<>(/*stopParts*/);
            if (next != null) {
                stopParts2.add(next);
            }

            for (GraphPart p : part.nextParts) {
                getPrecontinues(part, p, loops, next == null ? stopParts : stopParts2);
            }
            if (next != null) {
                getPrecontinues(part, next, loops, stopParts);
            }
        }

        if (part.nextParts.size() == 1) {
            getPrecontinues(part, part.nextParts.get(0), loops, stopParts);
        }
        if (isLoop) {
            if (currentLoop.loopBreak != null) {
                currentLoop.precoPhase = 2;
                getPrecontinues(null, currentLoop.loopBreak, loops, stopParts);
            }
        }
    }

    private void getLoops(GraphPart part, List<Loop> loops, GraphPart stopPart) {

        if (part == null) {
            return;
        }

        List<GraphPart> loopContinues = getLoopsContinues(loops);

        for (Loop el : loops) {
            if (el.loopBreak == null) { //break not found yet
                if (el.loopContinue != part) {
                    if (el.breakCandidates.contains(part)) {
                        el.breakCandidates.add(part);
                        return;
                    } else {
                        List<GraphPart> loopContinues2 = new ArrayList<>(loopContinues);
                        loopContinues2.remove(el.loopContinue);
                        if (!part.leadsTo(code, el.loopContinue, loopContinues2)) {
                            el.breakCandidates.add(part);
                            return;
                        }
                    }
                }

            }
        }

        for (Loop el : loops) {
            if (el.loopContinue == part) {
                return;
            }
        }

        if (part == stopPart) {
            return;
        }


        boolean isLoop = part.leadsTo(code, part, loopContinues);
        Loop currentLoop = null;
        if (isLoop) {
            currentLoop = new Loop(loops.size(), part, null);
            loops.add(currentLoop);
            loopContinues.add(part);
        }

        if (part.nextParts.size() == 2) {
            GraphPart next = part.getNextPartPath(loopContinues);
            getLoops(part.nextParts.get(0), loops, next == null ? stopPart : next);
            getLoops(part.nextParts.get(1), loops, next == null ? stopPart : next);
            if (next != null) {
                getLoops(next, loops, stopPart);
            }
        }
        if (part.nextParts.size() > 2) {
            //GraphPart next=getCommonPart(part.nextParts);
            for (GraphPart p : part.nextParts) {
                getLoops(p, loops, stopPart);
            }
        }
        if (part.nextParts.size() == 1) {
            getLoops(part.nextParts.get(0), loops, stopPart);
        }


        if (isLoop) {
            GraphPart found;
            do {
                found = null;
                loopcand:
                for (GraphPart cand : currentLoop.breakCandidates) {
                    for (GraphPart cand2 : currentLoop.breakCandidates) {
                        if (cand.leadsTo(code, cand2, loopContinues)) {
                            if (cand.path.equals(cand2.path)) {
                                found = cand2;
                            } else {
                                found = cand;
                            }
                            break loopcand;
                        }
                    }
                }
                if (found != null) {
                    currentLoop.breakCandidates.remove(found);
                }
            } while (found != null);

            Map<GraphPart, Integer> count = new HashMap<>();
            GraphPart winner = null;
            int winnerCount = 0;
            for (GraphPart cand : currentLoop.breakCandidates) {
                if (!count.containsKey(cand)) {
                    count.put(cand, 0);
                }
                count.put(cand, count.get(cand) + 1);
                if (count.get(cand) > winnerCount) {
                    winnerCount = count.get(cand);
                    winner = cand;
                } else if (count.get(cand) == winnerCount) {
                    if (cand.path.length() < winner.path.length()) {
                        winner = cand;
                    }
                }
            }
            currentLoop.loopBreak = winner;

            getLoops(currentLoop.loopBreak, loops, stopPart);
        }
    }

    protected List<GraphTargetItem> printGraph(List<GraphPart> visited, List<Object> localData, Stack<GraphTargetItem> stack, List<GraphPart> allParts, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops, List<GraphTargetItem> ret) {
        if (visited.contains(part)) {
            //return new ArrayList<GraphTargetItem>();
        } else {
            visited.add(part);
        }
        if (ret == null) {
            ret = new ArrayList<>();
        }
        try {
            boolean debugMode = false;

            if (debugMode) {
                System.err.println("PART " + part);
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
            part = checkPart(localData, part);
            if (part == null) {
                return ret;
            }

            if (part.ignored) {
                return ret;
            }



            //System.out.println("part:" + part);


            /*    if ((parent != null) && (part.path.length() < parent.path.length())) {
             boolean can = true;
             for (Loop el : loops) {
             if (el.loopContinue == part) {
             can = false;
             break;
             }
             if (el.loopBreak == part) {
             can = false;
             break;
             }
             if (el.breakCandidates.containsKey(part)) {
             can = false;
             break;
             }
             }
             if (can) {
             if ((part != stopPart) && (part.refs.size() > 1)) {
             List<GraphPart> nextList = new ArrayList<>();
             populateParts(part, nextList);
             Loop nearestLoop = null;
             loopn:
             for (GraphPart n : nextList) {
             for (Loop l : loops) {
             if (l.loopContinue == n) {
             nearestLoop = l;
             break loopn;
             }
             }
             }

             if ((nearestLoop != null)) {// && (nearestLoop.loopBreak != null)) {

             List<GraphTargetItem> finalCommands = printGraph(visited, localData, stack, allParts, null, part, nearestLoop.loopContinue, loops);
             nearestLoop.loopContinue = part;
             forFinalCommands.put(nearestLoop, finalCommands);
             ContinueItem cti = new ContinueItem(null, nearestLoop.id);
             ret.add(cti);
             //ret.add(new CommentItem("CONTTEST"));
             return ret;
             }
             }
             }
             }
             */
            List<GraphPart> loopContinues = getLoopsContinues(loops);
            boolean isLoop = false; //part.leadsTo(code, part, loopContinues);
            Loop currentLoop = null;
            for (Loop el : loops) {
                if ((el.loopContinue == part) && (!el.used)) {
                    currentLoop = el;
                    isLoop = true;
                    break;
                }
            }
            /*Loop currentLoop = null;
             if (isLoop) {
             currentLoop = new Loop(loops.size(), part, null);
             loops.add(currentLoop);
             loopContinues.add(part);
             }*/


            for (int l = loops.size() - 1; l >= 0; l--) {
                Loop el = loops.get(l);
                if (!el.used) {
                    continue;
                }
                if ((el.loopBreak == part) && (!el.finished)) {
                    ret.add(new BreakItem(null, el.id));
                    return ret;
                }
                if (el.loopPreContinue == part) {
                    ret.add(new ContinueItem(null, el.id));
                    return ret;
                }
                if (el.loopContinue == part) {
                    ret.add(new ContinueItem(null, el.id));
                    return ret;
                }
            }

            if ((part != null) && (code.size() <= part.start)) {
                ret.add(new ScriptEndItem());
                return ret;
            }

            if (part == stopPart) {
                return ret;
            }

            if (currentLoop != null) {
                currentLoop.used = true;
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
            int end = part.end;
            for (GraphPart p : parts) {
                end = p.end;
                int start = p.start;

                try {
                    output.addAll(code.translatePart(p, localData, stack, start, end));
                    if ((end >= code.size() - 1) && p.nextParts.isEmpty()) {
                        output.add(new ScriptEndItem());
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, "error during printgraph", ex);
                    return ret;
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

            /**
             * AND / OR detection
             */
            if (part.nextParts.size() == 2) {
                if ((stack.size() >= 2) && (stack.get(stack.size() - 1) instanceof NotItem) && (((NotItem) (stack.get(stack.size() - 1))).getOriginal().getNotCoerced() == stack.get(stack.size() - 2).getNotCoerced())) {
                    currentRet.addAll(output);
                    GraphPart sp0 = getNextNoJump(part.nextParts.get(0));
                    GraphPart sp1 = getNextNoJump(part.nextParts.get(1));
                    boolean reversed = false;
                    loopContinues = getLoopsContinues(loops);
                    loopContinues.add(part);
                    if (sp1.leadsTo(code, sp0, loopContinues)) {
                    } else if (sp0.leadsTo(code, sp1, loopContinues)) {
                        reversed = true;
                    }
                    GraphPart next = reversed ? sp0 : sp1;
                    GraphTargetItem ti;
                    if ((ti = checkLoop(next, stopPart, loops)) != null) {
                        currentRet.add(ti);
                    } else {

                        printGraph(visited, localData, stack, allParts, parent, next, reversed ? sp1 : sp0, new ArrayList<Loop>()/*ignore loops*/);
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
                            currentRet.addAll(printGraph(visited, localData, stack, allParts, parent, next, stopPart, loops));
                        }
                    }
                    parseNext = false;
                    //return ret;
                } else if ((stack.size() >= 2) && (stack.get(stack.size() - 1).getNotCoerced() == stack.get(stack.size() - 2).getNotCoerced())) {
                    currentRet.addAll(output);
                    GraphPart sp0 = getNextNoJump(part.nextParts.get(0));
                    GraphPart sp1 = getNextNoJump(part.nextParts.get(1));
                    boolean reversed = false;
                    loopContinues = getLoopsContinues(loops);
                    loopContinues.add(part);
                    if (sp1.leadsTo(code, sp0, loopContinues)) {
                    } else if (sp0.leadsTo(code, sp1, loopContinues)) {
                        reversed = true;
                    }
                    GraphPart next = reversed ? sp0 : sp1;
                    GraphTargetItem ti;
                    if ((ti = checkLoop(next, stopPart, loops)) != null) {
                        currentRet.add(ti);
                    } else {
                        printGraph(visited, localData, stack, allParts, parent, next, reversed ? sp1 : sp0, new ArrayList<Loop>()/*ignore loops*/);
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
                            currentRet.addAll(printGraph(visited, localData, stack, allParts, parent, next, stopPart, loops));
                        }
                    }
                    parseNext = false;
                    //return ret;
                }
            }
//********************************END PART DECOMPILING

            if (parseNext) {
                List<GraphTargetItem> retCheck = check(code, localData, allParts, stack, parent, part, stopPart, loops, output);
                if (retCheck != null) {
                    if (!retCheck.isEmpty()) {
                        currentRet.addAll(retCheck);
                    }
                    return ret;
                } else {
                    currentRet.addAll(output);
                }

                if (part.nextParts.size() == 2) {
                    //List<GraphPart> ignore = new ArrayList<>();
                    //ignore.addAll(loopContinues);
                    GraphPart next = part.getNextPartPath(loopContinues); //loopContinues);

                    /*for (Loop el : loops) {
                     if (el.loopContinue == next) {
                     next = null;
                     break;
                     }
                     if (el.loopBreak == next) {
                     next = null;
                     break;
                     }
                     }*/

                    GraphTargetItem expr = stack.pop();
                    if (expr instanceof LogicalOpItem) {
                        expr = ((LogicalOpItem) expr).invert();
                    } else {
                        expr = new NotItem(null, expr);
                    }
                    @SuppressWarnings("unchecked")
                    Stack<GraphTargetItem> trueStack = (Stack<GraphTargetItem>) stack.clone();
                    @SuppressWarnings("unchecked")
                    Stack<GraphTargetItem> falseStack = (Stack<GraphTargetItem>) stack.clone();
                    int trueStackSizeBefore = trueStack.size();
                    int falseStackSizeBefore = falseStack.size();
                    List<GraphTargetItem> onTrue = new ArrayList<>();
                    boolean isEmpty = part.nextParts.get(0) == part.nextParts.get(1);

                    if (isEmpty) {
                        next = part.nextParts.get(0);
                    }

                    if (!isEmpty) {
                        onTrue = printGraph(visited, localData, trueStack, allParts, part, part.nextParts.get(1), next == null ? stopPart : next, loops);
                    }
                    List<GraphTargetItem> onFalse = new ArrayList<>();
                    if (!isEmpty) {
                        onFalse = printGraph(visited, localData, falseStack, allParts, part, part.nextParts.get(0), next == null ? stopPart : next, loops);
                    }
                    if (isEmpty(onTrue) && isEmpty(onFalse) && (trueStack.size() > trueStackSizeBefore) && (falseStack.size() > falseStackSizeBefore)) {
                        stack.push(new TernarOpItem(null, expr, trueStack.pop(), falseStack.pop()));
                    } else {
                        currentRet.add(new IfItem(null, expr, onTrue, onFalse));
                    }
                    if (next != null) {
                        printGraph(visited, localData, stack, allParts, part, next, stopPart, loops, currentRet);
                        //currentRet.addAll();
                    }
                } else if (part.nextParts.size() == 1) {

                    printGraph(visited, localData, stack, allParts, part, part.nextParts.get(0), stopPart, loops, currentRet);
                }

            }
            if (isLoop) {
                LoopItem li = loopItem;
                boolean loopTypeFound = false;


                checkContinueAtTheEnd(loopItem.commands, currentLoop);

                //Loop with condition at the beginning (While)
                if (!loopTypeFound && (!loopItem.commands.isEmpty())) {
                    if (loopItem.commands.get(0) instanceof IfItem) {
                        IfItem ifi = (IfItem) loopItem.commands.get(0);
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
                            commands.addAll(loopItem.commands);
                            checkContinueAtTheEnd(commands, currentLoop);
                            List<GraphTargetItem> finalComm = new ArrayList<>();
                            if (currentLoop.loopPreContinue != null) {
                                GraphPart backup = currentLoop.loopPreContinue;
                                currentLoop.loopPreContinue = null;
                                finalComm = printGraph(visited, localData, new Stack<GraphTargetItem>(), allParts, null, backup, currentLoop.loopContinue, loops);
                                currentLoop.loopPreContinue = backup;
                                checkContinueAtTheEnd(finalComm, currentLoop);
                            }
                            if (!finalComm.isEmpty()) {
                                ret.add(index, li = new ForTreeItem(null, currentLoop, new ArrayList<GraphTargetItem>(), exprList.get(exprList.size() - 1), finalComm, commands));
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
                            loopItem.commands.remove(loopItem.commands.size() - 1);
                            if (!bodyBranch.isEmpty()) {
                                exprList.addAll(loopItem.commands);
                                commands.addAll(bodyBranch);
                                exprList.add(expr);
                                checkContinueAtTheEnd(commands, currentLoop);
                                ret.add(index, li = new WhileItem(null, currentLoop, exprList, commands));
                            } else {
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
                        List<GraphTargetItem> finalComm = printGraph(visited, localData, new Stack<GraphTargetItem>(), allParts, null, backup, currentLoop.loopContinue, loops);
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

                GraphTargetItem replaced = checkLoop(li, localData, loops);
                if (replaced != li) {
                    int index = ret.indexOf(li);
                    ret.remove(index);
                    if (replaced != null) {
                        ret.add(index, replaced);
                    }
                }

                //loops.remove(currentLoop);
                if (currentLoop.loopBreak != null) {
                    currentLoop.finished = true;
                    ret.addAll(printGraph(visited, localData, stack, allParts, part, currentLoop.loopBreak, stopPart, loops));
                }
            }

            return ret;
        } catch (StackOverflowError soe) {
            ret.add(new CommentTreeItem(null, "StackOverflowError"));
            Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, "error during printGraph", soe);
            return ret;
        }
    }

    private List<GraphPart> makeGraph(GraphSource code, List<GraphPart> allBlocks, List<Integer> alternateEntries) {
        HashMap<Integer, List<Integer>> refs = code.visitCode(alternateEntries);
        List<GraphPart> ret = new ArrayList<>();
        boolean visited[] = new boolean[code.size()];
        ret.add(makeGraph(null, new GraphPath(), code, 0, 0, allBlocks, refs, visited));
        for (int pos : alternateEntries) {
            GraphPart e1 = new GraphPart(-1, -1);
            e1.path = new GraphPath("e");
            ret.add(makeGraph(e1, new GraphPath("e"), code, pos, pos, allBlocks, refs, visited));
        }
        return ret;
    }

    protected int checkIp(int ip) {
        return ip;
    }

    private GraphPart makeGraph(GraphPart parent, GraphPath path, GraphSource code, int startip, int lastIp, List<GraphPart> allBlocks, HashMap<Integer, List<Integer>> refs, boolean visited2[]) {

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
    private static final String INDENT_STRING = "   ";

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
     * @return String
     */
    public static String graphToString(List<GraphTargetItem> tree, Object... localData) {
        StringBuilder ret = new StringBuilder();
        List<Object> localDataList = new ArrayList<>();
        for (Object o : localData) {
            localDataList.add(o);
        }
        for (GraphTargetItem ti : tree) {
            if (!ti.isEmpty()) {
                ret.append(ti.toStringSemicoloned(localDataList));
                ret.append("\r\n");
            }
        }
        String parts[] = ret.toString().split("\r\n");
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
            if (Highlighting.stripHilights(parts[p]).equals(INDENTOPEN)) {
                level++;
                continue;
            }
            if (Highlighting.stripHilights(parts[p]).equals(INDENTCLOSE)) {
                level--;
                continue;
            }
            if (Highlighting.stripHilights(parts[p]).equals("}")) {
                level--;
            }
            if (Highlighting.stripHilights(parts[p]).equals("};")) {
                level--;
            }
            ret.append(tabString(level));
            ret.append(parts[p]);
            ret.append("\r\n");
            if (Highlighting.stripHilights(parts[p]).equals("{")) {
                level++;
            }
        }
        return ret.toString();
    }

    public List<Object> prepareBranchLocalData(List<Object> localData) {
        return localData;
    }
}
