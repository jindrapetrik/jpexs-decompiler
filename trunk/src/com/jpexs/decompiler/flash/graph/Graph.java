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
        int i = 1;
        GraphPath lastpref = null;
        boolean modify = true;
        int prvni = -1;

        if (!doChildren) {

            List<GraphPart> uniqueRefs = new ArrayList<GraphPart>();
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
                GraphPath newpath = uniqueRefs.get(prvni).path.parent(i);
                if (!part.path.equals(newpath)) {
                    if (part.path.startsWith(newpath)) {
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
                            npart.path = part.path.sub(j);
                            fixed = true;
                        }
                    }
                }
            }

        }
        for (GraphPart p : part.nextParts) {
            fixGraphOnce(p, visited, doChildren);
        }
        return fixed;
    }

    private void makeMulti(GraphPart part, List<GraphPart> visited) {
        if (visited.contains(part)) {
            return;
        }
        visited.add(part);
        GraphPart p = part;
        List<GraphPart> multiList = new ArrayList<GraphPart>();
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
            visited = new ArrayList<GraphPart>();
        }
        if (copies == null) {
            copies = new ArrayList<GraphPart>();
        }
        if (visited.contains(part)) {
            return copies.get(visited.indexOf(part));
        }
        visited.add(part);
        GraphPart copy = new GraphPart(part.start, part.end);
        copy.path = part.path;
        copies.add(copy);
        copy.nextParts = new ArrayList<GraphPart>();
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
                p.path = part.path.sub(pos);
            }
            resetGraph(p, visited);
            pos++;
        }
    }

    public GraphPart getCommonPart(List<GraphPart> parts) {
        GraphPart head = new GraphPart(0, 0);
        head.nextParts.addAll(parts);
        List<GraphPart> allVisited = new ArrayList<GraphPart>();
        head = deepCopy(head, allVisited, null);
        for (GraphPart g : head.nextParts) {
            for (GraphPart r : g.refs) {
                r.nextParts.remove(g);
            }
            g.refs.clear();
            g.refs.add(head);
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

    public static List<GraphTargetItem> translateViaGraph(List localData, String path, GraphSource code, List<Integer> alternateEntries) {
        Graph g = new Graph(code, alternateEntries);
        return g.translate(localData);
    }

    public List<GraphTargetItem> translate(List localData) {
        List<GraphPart> allParts = new ArrayList<GraphPart>();
        for (GraphPart head : heads) {
            populateParts(head, allParts);
        }
        Stack<GraphTargetItem> stack = new Stack<GraphTargetItem>();
        List<GraphTargetItem> ret = printGraph(localData, stack, allParts, null, heads.get(0), null, new ArrayList<Loop>(), new HashMap<Loop, List<GraphTargetItem>>());
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
        List<GraphPart> ret = new ArrayList<GraphPart>();
        for (Loop l : loops) {
            if (l.loopContinue != null) {
                ret.add(l.loopContinue);
            }
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
        return false;
    }

    protected List<GraphTargetItem> check(GraphSource code, List localData, List<GraphPart> allParts, Stack<GraphTargetItem> stack, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops, List<GraphTargetItem> output, HashMap<Loop, List<GraphTargetItem>> forFinalCommands) {
        return null;
    }

    protected GraphPart checkPart(List localData, GraphPart part) {
        return part;
    }

    protected GraphTargetItem translatePartGetStack(List localData, GraphPart part, Stack<GraphTargetItem> stack) {
        stack = (Stack<GraphTargetItem>) stack.clone();
        translatePart(localData, part, stack);
        return stack.pop();
    }

    protected List<GraphTargetItem> translatePart(List localData, GraphPart part, Stack<GraphTargetItem> stack) {
        List<GraphPart> sub = part.getSubParts();
        List<GraphTargetItem> ret = new ArrayList<GraphTargetItem>();
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
            ret.addAll(code.translatePart(localData, stack, start, end));
        }
        return ret;
    }

    protected List<GraphTargetItem> printGraph(List localData, Stack<GraphTargetItem> stack, List<GraphPart> allParts, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops, HashMap<Loop, List<GraphTargetItem>> forFinalCommands) {
        //String methodPath, Stack<GraphTargetItem> stack, Stack<TreeItem> scopeStack, List<GraphPart> allParts, List<ABCException> parsedExceptions, List<Integer> finallyJumps, int level, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops, HashMap<Integer, TreeItem> localRegs, MethodBody body, List<Integer> ignoredSwitches

        List<GraphTargetItem> ret = new ArrayList<GraphTargetItem>();
        try {
            boolean debugMode = false;

            if (debugMode) {
                System.err.println("PART " + part);
            }

            if (part == stopPart) {
                return ret;
            }
            if (part == null) {
                //return ret;
            }
            part = checkPart(localData, part);
            if (part == null) {
                return ret;
            }

            if (part.ignored) {
                return ret;
            }
            List<String> fqn = new ArrayList<String>();
            //HashMap<Integer, String> lrn = new HashMap<Integer, String>();
            List<GraphTargetItem> output = new ArrayList<GraphTargetItem>();
            //boolean isSwitch = false;

            // code.initToSource();
            List<GraphPart> parts = new ArrayList<GraphPart>();
            if (part instanceof GraphPartMulti) {
                parts = ((GraphPartMulti) part).parts;
            } else {
                parts.add(part);
            }
            boolean isIf = false;
            int end = part.end;
            for (GraphPart p : parts) {
                end = p.end;
                int start = p.start;
                isIf = false;
                /*if (code.get(end).isBranch()) {
                 end--;
                 }*/

                try {
                    output.addAll(code.translatePart(localData, stack, start, end));
                } catch (Exception ex) {
                    Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, "error during printgraph", ex);
                    return ret;
                }
            }
            if (part.nextParts.size() == 2) {
                if (!stack.isEmpty()) {
                    GraphTargetItem top = stack.peek();
                    if (false) { //top.isCompileTime()){
                        stack.pop();
                        if (top.toBoolean()) {
                            ret.addAll(output);
                            ret.addAll(printGraph(localData, stack, allParts, parent, part.nextParts.get(0), stopPart, loops, forFinalCommands));
                            return ret;
                        } else {
                            ret.addAll(output);
                            ret.addAll(printGraph(localData, stack, allParts, parent, part.nextParts.get(1), stopPart, loops, forFinalCommands));
                            return ret;
                        }
                    } else {
                    }
                } else {
                    //EMPTY STACK
                }
            }
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
            }

            if (part.nextParts.size() == 2) {

                if ((stack.size() >= 2) && (stack.get(stack.size() - 1) instanceof NotItem) && (((NotItem) (stack.get(stack.size() - 1))).getOriginal().getNotCoerced() == stack.get(stack.size() - 2).getNotCoerced())) {
                    ret.addAll(output);
                    GraphPart sp0 = getNextNoJump(part.nextParts.get(0));
                    GraphPart sp1 = getNextNoJump(part.nextParts.get(1));
                    boolean reversed = false;
                    List<GraphPart> loopContinues = getLoopsContinues(loops);
                    loopContinues.add(part);
                    if (sp1.leadsTo(code, sp0, loopContinues)) {
                    } else if (sp0.leadsTo(code, sp1, loopContinues)) {
                        reversed = true;
                    }
                    GraphPart next = reversed ? sp0 : sp1;
                    GraphTargetItem ti;
                    if ((ti = checkLoop(next, stopPart, loops)) != null) {
                        ret.add(ti);
                    } else {
                        printGraph(localData, stack, allParts, parent, next, reversed ? sp1 : sp0, loops, forFinalCommands);
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
                            ret.add(ti);
                        } else {
                            ret.addAll(printGraph(localData, stack, allParts, parent, next, stopPart, loops, forFinalCommands));
                        }
                    }
                    return ret;
                } else if ((stack.size() >= 2) && (stack.get(stack.size() - 1).getNotCoerced() == stack.get(stack.size() - 2).getNotCoerced())) {
                    ret.addAll(output);
                    GraphPart sp0 = getNextNoJump(part.nextParts.get(0));
                    GraphPart sp1 = getNextNoJump(part.nextParts.get(1));
                    boolean reversed = false;
                    List<GraphPart> loopContinues = getLoopsContinues(loops);
                    loopContinues.add(part);
                    if (sp1.leadsTo(code, sp0, loopContinues)) {
                    } else if (sp0.leadsTo(code, sp1, loopContinues)) {
                        reversed = true;
                    }
                    GraphPart next = reversed ? sp0 : sp1;
                    GraphTargetItem ti;
                    if ((ti = checkLoop(next, stopPart, loops)) != null) {
                        ret.add(ti);
                    } else {
                        printGraph(localData, stack, allParts, parent, next, reversed ? sp1 : sp0, loops, forFinalCommands);
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
                            ret.add(ti);
                        } else {
                            ret.addAll(printGraph(localData, stack, allParts, parent, next, stopPart, loops, forFinalCommands));
                        }
                    }

                    return ret;
                }
            }



            List<GraphPart> loopContinues = getLoopsContinues(loops);
            boolean loop = false;
            boolean reversed = false;
            boolean whileTrue = false;
            Loop whileTrueLoop = null;
            if ((!part.nextParts.isEmpty()) && part.nextParts.get(0).leadsTo(code, part, loopContinues)) {
                if ((part.nextParts.size() > 1) && part.nextParts.get(1).leadsTo(code, part, loopContinues)) {
                    if (output.isEmpty()) {
                        whileTrueLoop = new Loop(loops.size(), part, null);
                        loops.add(whileTrueLoop);
                        whileTrue = true;
                    } else {
                        loop = true;//doWhile
                    }

                } else {
                    loop = true;
                }
            } else if ((part.nextParts.size() > 1) && part.nextParts.get(1).leadsTo(code, part, loopContinues)) {
                loop = true;
                reversed = true;
            }


            Loop lc = null;
            if (part.leadsTo(code, part, loopContinues)) {
                lc = new Loop(loops.size(), part, null);
                loops.add(lc);
            }

            List<GraphTargetItem> retChecked = null;
            if ((retChecked = check(code, localData, allParts, stack, parent, part, stopPart, loops, output, forFinalCommands)) != null) {
                if (lc != null) {
                    List<GraphTargetItem> trueExp = new ArrayList<GraphTargetItem>();
                    trueExp.add(new TrueItem(null));
                    boolean chwt = true;

                    if (!retChecked.isEmpty()) { //doWhileCheck
                        checkContinueAtTheEnd(retChecked, lc);
                        List<GraphTargetItem> finalCommands = forFinalCommands.get(lc);
                        IfItem ifi = null;
                        if ((finalCommands != null) && !finalCommands.isEmpty()) {
                            if (finalCommands.get(finalCommands.size() - 1) instanceof IfItem) {
                                ifi = (IfItem) finalCommands.get(finalCommands.size() - 1);
                                finalCommands.remove(finalCommands.size() - 1);
                            }
                        } else if (retChecked.get(retChecked.size() - 1) instanceof IfItem) {
                            ifi = (IfItem) retChecked.get(retChecked.size() - 1);
                            retChecked.remove(retChecked.size() - 1);
                        }
                        if (ifi != null) {
                            if (ifi.onFalse.isEmpty()) {
                                if (!ifi.onTrue.isEmpty()) {
                                    if (ifi.onTrue.get(ifi.onTrue.size() - 1) instanceof ExitItem) {
                                        whileTrue = false;
                                        List<GraphTargetItem> tr = new ArrayList<GraphTargetItem>();
                                        GraphTargetItem ex = ifi.expression;
                                        if (ex instanceof LogicalOpItem) {
                                            ex = ((LogicalOpItem) ex).invert();
                                        } else {
                                            ex = new NotItem(null, ex);
                                        }
                                        if ((finalCommands != null) && (!finalCommands.isEmpty())) {
                                            tr.addAll(finalCommands);
                                        }
                                        tr.add(ex);
                                        ret.add(new DoWhileItem(null, lc, retChecked, tr));
                                        ret.addAll(ifi.onTrue);
                                        chwt = false;
                                    }
                                }
                            }
                        }
                    }

                    if (chwt) {
                        ret.add(new WhileItem(null, lc, trueExp, retChecked));
                    }
                } else {
                    ret.addAll(retChecked);
                }
                return ret;
            } else {
                if (lc != null) {
                    loops.remove(lc);
                }
            }

            /* if ( part.leadsTo(code, part, getLoopsContinues(loops))) {
             Loop l = new Loop(loops.size(), part, null);
             List<GraphTargetItem> trueExp = new ArrayList<GraphTargetItem>();
             trueExp.add(new TrueItem(null));
             loops.add(l);
             List<GraphTargetItem> wtbody=new ArrayList<GraphTargetItem>();
             wtbody.addAll(output);
             wtbody.addAll(printGraph(localData, stack, allParts, parent, part, stopPart, loops, forFinalCommands));
             ret.add(new WhileItem(null, l, trueExp, wtbody));
             return ret;
             }*/
            if (((part.nextParts.size() == 2) || ((part.nextParts.size() == 1) && loop)) /*&& (!isSwitch)*/) {

                boolean doWhile = loop;
                if (loop && output.isEmpty()) {
                    doWhile = false;
                }
                Loop currentLoop = null;
                if (loop) {
                    currentLoop = new Loop(loops.size(), part, null);
                    loops.add(currentLoop);
                }

                loopContinues = new ArrayList<GraphPart>();
                for (Loop l : loops) {
                    if (l.loopContinue != null) {
                        loopContinues.add(l.loopContinue);
                    }
                }

                if ((!whileTrue) && loop && (part.nextParts.size() > 1) && (!doWhile)) {
                    currentLoop.loopBreak = part.nextParts.get(reversed ? 0 : 1);
                }

                forFinalCommands.put(currentLoop, new ArrayList<GraphTargetItem>());

                GraphTargetItem expr = null;
                if (part.nextParts.size() == 1) {
                    expr = new TrueItem(null);
                } else {
                    if (!stack.isEmpty()) {
                        expr = stack.pop();
                    }
                }
                if (loop) {
                    GraphTargetItem expr2 = expr;
                    if (expr2 instanceof NotItem) {
                        expr2 = ((NotItem) expr2).getOriginal();
                    }
                    if (expr2 instanceof AndItem) {
                        currentLoop.loopContinue = ((AndItem) expr2).firstPart;
                    }
                    if (expr2 instanceof OrItem) {
                        currentLoop.loopContinue = ((OrItem) expr2).firstPart;
                    }
                }

                if (doWhile) {
                    //ret.add(new DoWhileTreeItem(null, currentLoop.id, part.start, output, expr));
                } else {
                    ret.addAll(output);
                }
                GraphPart loopBodyStart = null;
                if ((reversed == loop) || doWhile) {
                    if (expr instanceof LogicalOpItem) {
                        expr = ((LogicalOpItem) expr).invert();
                    } else {
                        expr = new NotItem(null, expr);
                    }
                }

                GraphPart next = part.getNextPartPath(loopContinues);
                List<GraphTargetItem> retx = ret;
                if ((!loop) || (doWhile && (part.nextParts.size() > 1))) {
                    if (doWhile) {
                        retx = output;

                    }
                    int stackSizeBefore = stack.size();
                    Stack<GraphTargetItem> trueStack = (Stack<GraphTargetItem>) stack.clone();
                    Stack<GraphTargetItem> falseStack = (Stack<GraphTargetItem>) stack.clone();
                    GraphTargetItem lopTrue = checkLoop(part.nextParts.get(1), stopPart, loops);
                    GraphTargetItem lopFalse = null;
                    if (next != part.nextParts.get(0)) {
                        lopFalse = checkLoop(part.nextParts.get(0), stopPart, loops);
                    }
                    List<GraphTargetItem> onTrue = new ArrayList<GraphTargetItem>();
                    if (lopTrue != null) {
                        onTrue.add(lopTrue);
                    } else {
                        if (debugMode) {
                            System.err.println("ONTRUE: (inside " + part + ")");
                        }
                        onTrue = printGraph(prepareBranchLocalData(localData), trueStack, allParts, part, part.nextParts.get(1), next == null ? stopPart : next, loops, forFinalCommands);
                        if (debugMode) {
                            System.err.println("/ONTRUE (inside " + part + ")");
                        }
                    }
                    List<GraphTargetItem> onFalse = new ArrayList<GraphTargetItem>();
                    if ((!onTrue.isEmpty()) && onTrue.get(onTrue.size() - 1) instanceof ExitItem) {
                        next = part.nextParts.get(0);
                    } else {
                        if (lopFalse != null) {
                            onFalse.add(lopFalse);
                        } else {
                            if (debugMode) {
                                System.err.println("ONFALSE: (inside " + part + ")");
                            }
                            if ((next == part.nextParts.get(0)) || (part.nextParts.get(0).path.equals(part.path) || part.nextParts.get(0).path.length() < part.path.length())) {
                                onFalse = new ArrayList<GraphTargetItem>();
                            } else {
                                onFalse = (printGraph(prepareBranchLocalData(localData), falseStack, allParts, part, part.nextParts.get(0), next == null ? stopPart : next, loops, forFinalCommands));
                            }
                            if (debugMode) {
                                System.err.println("/ONFALSE (inside " + part + ")");
                            }
                        }
                    }
                    if (isEmpty(onTrue) && isEmpty(onFalse) && (trueStack.size() > stackSizeBefore) && (falseStack.size() > stackSizeBefore)) {
                        stack.push(new TernarOpItem(null, expr, trueStack.pop(), falseStack.pop()));
                    } else {
                        List<GraphTargetItem> retw = retx;
                        if (whileTrue) {
                            retw = new ArrayList<GraphTargetItem>();
                            retw.add(new IfItem(null, expr, onTrue, onFalse));

                            List<GraphTargetItem> body = new ArrayList<GraphTargetItem>();
                            if (next != null) {
                                body = printGraph(prepareBranchLocalData(localData), stack, allParts, part, next, stopPart, loops, forFinalCommands);
                            }
                            retw.addAll(body);


                            if (!retw.isEmpty()) { //doWhileCheck
                                checkContinueAtTheEnd(retw, whileTrueLoop);
                                List<GraphTargetItem> finalCommands = forFinalCommands.get(whileTrueLoop);
                                IfItem ifi = null;
                                if ((finalCommands != null) && !finalCommands.isEmpty()) {
                                    if (finalCommands.get(finalCommands.size() - 1) instanceof IfItem) {
                                        ifi = (IfItem) finalCommands.get(finalCommands.size() - 1);
                                        finalCommands.remove(finalCommands.size() - 1);
                                    }
                                } else if (retw.get(retw.size() - 1) instanceof IfItem) {
                                    ifi = (IfItem) retw.get(retw.size() - 1);
                                    retw.remove(retw.size() - 1);
                                }
                                if (ifi != null) {
                                    if (ifi.onFalse.isEmpty()) {
                                        if (!ifi.onTrue.isEmpty()) {
                                            if (ifi.onTrue.get(ifi.onTrue.size() - 1) instanceof ExitItem) {
                                                whileTrue = false;
                                                List<GraphTargetItem> tr = new ArrayList<GraphTargetItem>();
                                                GraphTargetItem ex = ifi.expression;
                                                if (ex instanceof LogicalOpItem) {
                                                    ex = ((LogicalOpItem) ex).invert();
                                                } else {
                                                    ex = new NotItem(null, ex);
                                                }
                                                if ((finalCommands != null) && (!finalCommands.isEmpty())) {
                                                    tr.addAll(finalCommands);
                                                }
                                                tr.add(ex);
                                                retx.add(new DoWhileItem(null, whileTrueLoop, retw, tr));
                                                retx.addAll(ifi.onTrue);
                                                next = null;
                                            }
                                        }
                                    }
                                }
                            }
                            if (whileTrue) {
                                List<GraphTargetItem> tr = new ArrayList<GraphTargetItem>();
                                tr.add(new TrueItem(null));
                                retx.add(new WhileItem(null, whileTrueLoop, tr, retw));
                                next = null;
                            }
                        } else {
                            retx.add(new IfItem(null, expr, onTrue, onFalse));
                        }

                    }
                    if (doWhile) {
                        loopBodyStart = next;
                    }
                    if (whileTrue) {
                        loopBodyStart = part;
                    }
                }
                if (loop) { // && (!doWhile)) {
                    List<GraphTargetItem> loopBody = new ArrayList<GraphTargetItem>();
                    List<GraphTargetItem> finalCommands = null;
                    GraphPart finalPart = null;
                    GraphTargetItem ti;
                    if ((loopBodyStart != null) && ((ti = checkLoop(loopBodyStart, stopPart, loops)) != null)) {
                        loopBody.add(ti);
                    } else {
                        if (!(doWhile && (loopBodyStart == null))) {
                            loopBody = printGraph(prepareBranchLocalData(localData), stack, allParts, part, loopBodyStart != null ? loopBodyStart : part.nextParts.get(reversed ? 1 : 0), stopPart, loops, forFinalCommands);
                        }
                    }
                    checkContinueAtTheEnd(loopBody, currentLoop);
                    finalCommands = forFinalCommands.get(currentLoop);
                    if (!finalCommands.isEmpty()) {
                        ret.add(new ForTreeItem(null, currentLoop, new ArrayList<GraphTargetItem>(), expr, finalCommands, loopBody));
                    } else {
                        if (doWhile) {
                            if (stack.isEmpty() || (part.nextParts.size() == 1)) {
                                expr = new TrueItem(null);
                            } else {
                                expr = stack.pop();
                            }
                            loopBody.addAll(0, output);
                            if (part.nextParts.size() == 1) {
                                loopBody.addAll(printGraph(prepareBranchLocalData(localData), stack, allParts, part, part.nextParts.get(0), stopPart, loops, forFinalCommands));
                            }

                            checkContinueAtTheEnd(loopBody, currentLoop);

                            List<GraphTargetItem> newBody = new ArrayList<GraphTargetItem>();
                            List<GraphTargetItem> nextcmds = new ArrayList<GraphTargetItem>();
                            if ((!loopBody.isEmpty()) && (loopBody.get(loopBody.size() - 1) instanceof IfItem)) {
                                IfItem ift = (IfItem) loopBody.get(loopBody.size() - 1);
                                if ((!ift.onFalse.isEmpty()) && (ift.onFalse.get(ift.onFalse.size() - 1) instanceof ExitItem)) {//((ift.onFalse.size() == 1) && (ift.onFalse.get(0) instanceof ContinueItem) && (((ContinueItem) ift.onFalse.get(0)).loopId == currentLoop.id))) {
                                    if (ift.expression != null) {
                                        expr = ift.expression;
                                        if (expr instanceof LogicalOpItem) {
                                            expr = ((LogicalOpItem) expr).invert();
                                        } else {
                                            expr = new NotItem(null, expr);
                                        }
                                    }
                                    nextcmds = ift.onFalse;
                                    newBody = ift.onTrue;

                                    loopBody.remove(loopBody.size() - 1);
                                } else if ((!ift.onTrue.isEmpty()) && (ift.onTrue.get(ift.onTrue.size() - 1) instanceof ExitItem)) {//((ift.onTrue.size() == 1) && (ift.onTrue.get(0) instanceof ContinueItem) && (((ContinueItem) ift.onTrue.get(0)).loopId == currentLoop.id))) {
                                    if (ift.expression != null) {
                                        expr = ift.expression;
                                        if (expr instanceof LogicalOpItem) {
                                            expr = ((LogicalOpItem) expr).invert();
                                        } else {
                                            expr = new NotItem(null, expr);
                                        }
                                    }
                                    newBody = ift.onFalse;
                                    nextcmds = ift.onTrue;
                                    loopBody.remove(loopBody.size() - 1);
                                    if (newBody.isEmpty()) {
                                        //addIf.addAll(loopBody);
                                    }
                                }
                            }
                            checkContinueAtTheEnd(newBody, currentLoop);
                            if ((!newBody.isEmpty()) && (!(newBody.get(0) instanceof ScriptEndItem))) { // && (addIf.get(addIf.size() - 1) instanceof ContinueItem) && (((ContinueItem) addIf.get(addIf.size() - 1)).loopId == currentLoop.id)) {
                                loopBody.add(expr);
                                ret.add(new WhileItem(null, currentLoop, loopBody, newBody));
                                ret.addAll(nextcmds);
                            } else {
                                List<GraphTargetItem> ex = new ArrayList<GraphTargetItem>();
                                ex.add(expr);
                                ret.add(new DoWhileItem(null, currentLoop, loopBody, ex));
                                ret.addAll(nextcmds);
                            }

                        } else {
                            List<GraphTargetItem> ex = new ArrayList<GraphTargetItem>();
                            ex.add(expr);
                            ret.add(new WhileItem(null, currentLoop, ex, loopBody));
                        }
                    }
                }
                if ((!doWhile) && (!whileTrue) && loop && (part.nextParts.size() > 1)) {
                    loops.remove(currentLoop); //remove loop so no break shows up
                    //ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level, part, part.nextParts.get(reversed ? 0 : 1), stopPart, loops, localRegs, body, ignoredSwitches));
                    next = part.nextParts.get(reversed ? 0 : 1);
                }
                if (doWhile) {
                    next = null;
                }
                if (next != null) {
                    GraphTargetItem ti = checkLoop(next, stopPart, loops);
                    if (ti != null) {
                        ret.add(ti);
                    } else {
                        if (debugMode) {
                            System.err.println("NEXT: (inside " + part + ")");
                        }
                        ret.addAll(printGraph(localData, stack, allParts, part, next, stopPart, loops, forFinalCommands));
                        if (debugMode) {
                            System.err.println("/NEXT: (inside " + part + ")");
                        }
                    }

                }
            } else {
                ret.addAll(output);
            }
            onepart:
            if (part.nextParts.size() == 1 && (!loop)) {
                /*if (part.end - part.start > 4) {
                 if (code.code.get(part.end).definition instanceof PopIns) {
                 if (code.code.get(part.end - 1).definition instanceof LabelIns) {
                 if (code.code.get(part.end - 2).definition instanceof PushByteIns) {

                 //if (code.code.get(part.end - 3).definition instanceof SetLocalTypeIns) {
                 if (part.nextParts.size() == 1) {
                 GraphPart sec = part.nextParts.get(0);

                 if (code.code.get(sec.end).definition instanceof ReturnValueIns) {
                 if (sec.end - sec.start >= 3) {
                 if (code.code.get(sec.end - 1).definition instanceof KillIns) {
                 if (code.code.get(sec.end - 2).definition instanceof GetLocalTypeIns) {
                 if (!output.isEmpty()) {
                 if (output.get(output.size() - 1) instanceof SetLocalTreeItem) {
                 sec.ignored = true;
                 ret.add(new ReturnValueTreeItem(code.code.get(sec.end), ((SetLocalTreeItem) output.get(output.size() - 1)).value));
                 break onepart;
                 }
                 }
                 }
                 }
                 }

                 } else if (code.code.get(sec.end).definition instanceof ReturnVoidIns) {
                 ret.add(new ReturnVoidTreeItem(code.code.get(sec.end)));
                 break onepart;
                 }
                 //}
                 }
                 }
                 }
                 }
                 }

                 for (int f : finallyJumps) {
                 if (part.nextParts.get(0).start == f) {
                 if ((!output.isEmpty()) && (output.get(output.size() - 1) instanceof SetLocalTreeItem)) {
                 ret.add(new ReturnValueTreeItem(null, ((SetLocalTreeItem) output.get(output.size() - 1)).value));
                 } else {
                 ret.add(new ReturnVoidTreeItem(null));
                 }

                 break onepart;
                 }
                 }
                 */
                GraphPart p = part.nextParts.get(0);
                GraphTargetItem lop = checkLoop(p, stopPart, loops);
                if (lop == null) {
                    if (p.path.length() >= part.path.length()) {
                        ret.addAll(printGraph(localData, stack, allParts, part, p, stopPart, loops, forFinalCommands));
                    } else {
                        if ((p != stopPart) && (p.refs.size() > 1)) {
                            List<GraphPart> nextList = new ArrayList<GraphPart>();
                            populateParts(p, nextList);
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
                                List<GraphTargetItem> finalCommands = printGraph(localData, stack, allParts, part, p, nearestLoop.loopContinue, loops, forFinalCommands);
                                nearestLoop.loopContinue = p;
                                forFinalCommands.put(nearestLoop, finalCommands);
                                ContinueItem cti = new ContinueItem(null, nearestLoop.id);
                                ret.add(cti);
                            }
                        }
                    }
                } else {
                    ret.add(lop);
                }
                //}
                //ret += (strOfChars(level, TAB) + "continue;\r\n");
                //}
            }
            /*if (isSwitch && (!ignoredSwitches.contains(part.end))) {
             //ret.add(new CommentTreeItem(code.code.get(part.end), "Switch not supported"));
             TreeItem switchedObject = stack.pop();
             List<TreeItem> caseValues = new ArrayList<TreeItem>();
             List<Integer> valueMappings = new ArrayList<Integer>();
             List<List<TreeItem>> caseCommands = new ArrayList<List<TreeItem>>();

             GraphPart next = part.getNextPartPath(loopContinues);
             int breakPos = -1;
             if (next != null) {
             breakPos = next.start;
             }
             List<TreeItem> defaultCommands = printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, part, part.nextParts.get(0), stopPart, loops, localRegs, body, ignoredSwitches);

             for (int i = 0; i < part.nextParts.size() - 1; i++) {
             caseValues.add(new IntegerValueTreeItem(null, (Long) (long) i));
             valueMappings.add(i);
             GraphPart nextCase = next;
             List<TreeItem> caseBody = new ArrayList<TreeItem>();
             if (i < part.nextParts.size() - 1 - 1) {
             if (!part.nextParts.get(1 + i).leadsTo(part.nextParts.get(1 + i + 1), new ArrayList<GraphPart>())) {
             caseBody.add(new BreakTreeItem(null, breakPos));
             } else {
             nextCase = part.nextParts.get(1 + i + 1);
             }
             } else if (!part.nextParts.get(1 + i).leadsTo(part.nextParts.get(0), new ArrayList<GraphPart>())) {
             caseBody.add(new BreakTreeItem(null, breakPos));
             } else {
             nextCase = part.nextParts.get(0);
             }
             caseBody.addAll(0, printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, part, part.nextParts.get(1 + i), nextCase, loops, localRegs, body, ignoredSwitches));
             caseCommands.add(caseBody);
             }

             SwitchTreeItem swt = new SwitchTreeItem(null, breakPos, switchedObject, caseValues, caseCommands, defaultCommands, valueMappings);
             ret.add(swt);

             if (next != null) {
             TreeItem lopNext = checkLoop(next, stopPart, loops);
             if (lopNext != null) {
             ret.add(lopNext);
             } else {
             ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, part, next, stopPart, loops, localRegs, body, ignoredSwitches));
             }
             }

             }*/

            /*code.clearTemporaryRegisters(ret);
             if (!part.forContinues.isEmpty()) {
             throw new ForException(new ArrayList<TreeItem>(), ret, part);
             }*/
            return ret;
        } catch (StackOverflowError soe) {
            ret.add(new CommentTreeItem(null, "StackOverflowError"));
            Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, "error during printGraph", soe);
            return ret;
        }
    }

    private List<GraphPart> makeGraph(GraphSource code, List<GraphPart> allBlocks, List<Integer> alternateEntries) {
        HashMap<Integer, List<Integer>> refs = code.visitCode(alternateEntries);
        List<GraphPart> ret = new ArrayList<GraphPart>();
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
                    part.nextParts.add(g = makeGraph(part, path.sub(i), code, branches.get(i), ip, allBlocks, refs, visited2));
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
        List localDataList = new ArrayList();
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


        try {
            Stack<String> loopStack = new Stack<String>();
            for (int p = 0; p < parts.length; p++) {
                String stripped = Highlighting.stripHilights(parts[p]);
                if (stripped.endsWith(":") && (!stripped.startsWith("case ")) && (!stripped.equals("default:"))) {
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
            if (strippedP.endsWith(":") && (!strippedP.startsWith("case ")) && (!strippedP.equals("default:"))) {
                String loopname = strippedP.substring(0, strippedP.length() - 1);
                boolean dorefer = false;
                for (int q = p + 1; q < parts.length; q++) {
                    String strippedQ = Highlighting.stripHilights(parts[q]).trim();
                    if (strippedQ.equals("break " + loopname + ";")) {
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

    public List prepareBranchLocalData(List localData) {
        return localData;
    }
}
