/*
 *  Copyright (C) 2010 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.jpexs.asdec.abc.avm2.flowgraph;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConvertException;
import com.jpexs.asdec.abc.avm2.instructions.IfTypeIns;
import com.jpexs.asdec.abc.avm2.instructions.jumps.IfFalseIns;
import com.jpexs.asdec.abc.avm2.instructions.jumps.IfTrueIns;
import com.jpexs.asdec.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.asdec.abc.avm2.instructions.localregs.GetLocalTypeIns;
import com.jpexs.asdec.abc.avm2.instructions.localregs.SetLocalTypeIns;
import com.jpexs.asdec.abc.avm2.instructions.other.ReturnValueIns;
import com.jpexs.asdec.abc.avm2.instructions.other.ReturnVoidIns;
import com.jpexs.asdec.abc.avm2.instructions.stack.DupIns;
import com.jpexs.asdec.abc.avm2.instructions.stack.PopIns;
import com.jpexs.asdec.abc.avm2.instructions.stack.PushFalseIns;
import com.jpexs.asdec.abc.avm2.instructions.stack.PushTrueIns;
import com.jpexs.asdec.abc.avm2.instructions.stack.SwapIns;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class Graph {

    public List<GraphPart> parts;
    public List<Integer> ignored = new ArrayList<Integer>();
    private int trueReg = -1;
    private int falseReg = -1;

    public Graph(AVM2Code code) {
        parts = new ArrayList<GraphPart>();
        int start = checkSWFSecureStart(code);
        makeGraph(new Stack<Boolean>(),code, start, parts, new ArrayList<GraphBlock>());
        do {
        } while (optimizeDecisions(parts) > 0);
    }

    private int checkSWFSecureStart(AVM2Code code) {
        if (code.code.size() < 2) {
            return 0;
        }
        if (!((code.code.get(0).definition instanceof PushFalseIns) || (code.code.get(0).definition instanceof PushTrueIns))) {
            return 0;
        }
        if (!((code.code.get(1).definition instanceof PushFalseIns) || (code.code.get(1).definition instanceof PushTrueIns))) {
            return 0;
        }
        System.out.println("A");
        int pos = 2;
        Stack<Boolean> myStack = new Stack<Boolean>();
        int ip = 0;
        int setCount = 0;
        while (ip < code.code.size()) {
            if (code.code.get(ip).definition instanceof PushFalseIns) {
                myStack.push(Boolean.FALSE);
            } else if (code.code.get(ip).definition instanceof PushTrueIns) {
                myStack.push(Boolean.TRUE);
            } else if (code.code.get(ip).definition instanceof SwapIns) {
                Boolean b1 = myStack.pop();
                Boolean b2 = myStack.pop();
                myStack.push(b1);
                myStack.push(b2);
            } else if (code.code.get(ip).definition instanceof JumpIns) {
                try {
                    ip = code.adr2pos(code.pos2adr(ip + 1) + code.code.get(ip).operands[0]);
                } catch (ConvertException ex) {
                    Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
                }
                continue;
            } else if (code.code.get(ip).definition instanceof SetLocalTypeIns) {
                Boolean val = myStack.pop();
                if (val == true) {
                    trueReg = ((SetLocalTypeIns) code.code.get(ip).definition).getRegisterId(code.code.get(ip));
                } else {
                    falseReg = ((SetLocalTypeIns) code.code.get(ip).definition).getRegisterId(code.code.get(ip));
                }
                setCount++;
                if (setCount == 2) {
                    return ip + 1;
                }
            }
            ip++;
        }
        return 0;
    }

    private int optimizeDecisions(List<GraphPart> parts) {
        for (int p = 0; p < parts.size(); p++) {
            GraphPart part = parts.get(p);
            if (part instanceof GraphDecision) {
                /**
                 * if
                 *  onTrue:  nop
                 *           nop
                 *           link A
                 *  onFalse: nop
                 *           A
                 *           B
                 *
                 *  ==>
                 *
                 * if onTrue:  nop
                 *             nop
                 *    onFalse: nop
                 * A
                 * B
                 */
                if (((GraphDecision) part).onTrue.size() > 0) {
                    GraphPart lastTruePart = ((GraphDecision) part).onTrue.get(((GraphDecision) part).onTrue.size() - 1);
                    if (lastTruePart instanceof GraphLink) {
                        for (int f = 0; f < ((GraphDecision) part).onFalse.size(); f++) {
                            if (((GraphDecision) part).onFalse.get(f).start == ((GraphLink) lastTruePart).ip) {
                                ((GraphDecision) part).onFalse.get(f).linkCount--;
                                ((GraphDecision) part).onTrue.remove(((GraphDecision) part).onTrue.size() - 1);
                                for (int k = f; k < ((GraphDecision) part).onFalse.size(); k++) {
                                    parts.add(p + 1, ((GraphDecision) part).onFalse.remove(k));
                                }
                                return optimizeDecisions(parts) + 1;
                            }
                        }
                    }
                }

                /**
                 * if
                 *  onTrue:  nop
                 *           nop
                 *           A
                 *           B
                 *  onFalse: nop
                 *           link A
                 *
                 *  ==>
                 *
                 * if onTrue:  nop
                 *             nop
                 *    onFalse: nop
                 * A
                 * B
                 */
                if (((GraphDecision) part).onFalse.size() > 0) {
                    GraphPart lastFalsePart = ((GraphDecision) part).onFalse.get(((GraphDecision) part).onFalse.size() - 1);
                    if (lastFalsePart instanceof GraphLink) {
                        for (int t = 0; t < ((GraphDecision) part).onTrue.size(); t++) {
                            if (((GraphDecision) part).onTrue.get(t).start == ((GraphLink) lastFalsePart).ip) {
                                ((GraphDecision) part).onTrue.get(t).linkCount--;
                                ((GraphDecision) part).onFalse.remove(((GraphDecision) part).onFalse.size() - 1);
                                for (int k = t; k < ((GraphDecision) part).onTrue.size(); k++) {
                                    parts.add(p + 1, ((GraphDecision) part).onTrue.remove(k));
                                }
                                return optimizeDecisions(parts) + 1;
                            }
                        }
                    }
                }

                /*
                 * if
                 *  onTrue:  nop
                 *           link A
                 *  onFalse: nop
                 *           link A
                 *
                 * ==>
                 *  if
                 *   onTrue: nop
                 *   onFalse: nop
                 *  link A
                 *
                 */
                if ((((GraphDecision) part).onTrue.size() > 0)&&(((GraphDecision) part).onFalse.size() > 0)) {
                    GraphPart lastFalsePart = ((GraphDecision) part).onFalse.get(((GraphDecision) part).onFalse.size() - 1);
                    GraphPart lastTruePart = ((GraphDecision) part).onTrue.get(((GraphDecision) part).onTrue.size() - 1);
                    if((lastFalsePart instanceof GraphLink)&&(lastTruePart instanceof GraphLink)){
                        if(((GraphLink)lastFalsePart).ip==((GraphLink)lastTruePart).ip){
                            ((GraphDecision) part).onFalse.remove(((GraphDecision) part).onFalse.size()-1);
                            ((GraphDecision) part).onTrue.remove(((GraphDecision) part).onTrue.size()-1);
                            parts.add(p+1,lastTruePart);
                            return optimizeDecisions(parts) + 1;
                        }
                    }
                }
            }
        }

        int optcount = 0;
        for (int p = 0; p < parts.size(); p++) {
            GraphPart part = parts.get(p);
            if (part instanceof GraphDecision) {
                optcount += optimizeDecisions(((GraphDecision) part).onTrue);
                optcount += optimizeDecisions(((GraphDecision) part).onFalse);
            }
        }
        return optcount;
    }

    private GraphBlock splitBlock(GraphBlock block, int ip, List<GraphBlock> allBlocks) {
        return processSplit(block, parts, ip, allBlocks);
    }

    private GraphBlock processSplit(GraphBlock block, List<GraphPart> parts, int ip, List<GraphBlock> allBlocks) {
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i) == block) {
                parts.remove(i);
                GraphBlock gr1 = new GraphBlock(block.start, ip - 1);
                parts.add(i, gr1);
                gr1.linkCount=block.linkCount;
                GraphBlock gr2 = new GraphBlock(ip, block.end);
                parts.add(i + 1, gr2);
                allBlocks.remove(block);
                allBlocks.add(gr1);
                allBlocks.add(gr2);
                return (GraphBlock) parts.get(i + 1);
            } else if (parts.get(i) instanceof GraphDecision) {
                GraphBlock gr = processSplit(block, ((GraphDecision) parts.get(i)).onTrue, ip, allBlocks);
                if (gr != null) {
                    return gr;
                }
                gr = processSplit(block, ((GraphDecision) parts.get(i)).onFalse, ip, allBlocks);
                if (gr != null) {
                    return gr;
                }
            }
        }
        return null;
    }

    private void makeGraph(Stack<Boolean> myStack,AVM2Code code, int start, List<GraphPart> parts, List<GraphBlock> allBlocks) {
        try {
            int ip = start;
            while (ip < code.code.size()) {
                for (GraphBlock block : allBlocks) {
                    if (block.contains(ip)) {
                        if (block.start < ip) {
                            block = splitBlock(block, ip, allBlocks);
                        }
                        if (ip - 1 >= start) {
                            GraphBlock bl = new GraphBlock(start, ip - 1);
                            parts.add(bl);
                            allBlocks.add(bl);
                        }
                        parts.add(new GraphLink(block));
                        return;
                    }
                }
                boolean forceJump = false;
                boolean forceSkip = false;
                if (code.code.get(ip).definition instanceof IfTrueIns) {
                    if (!myStack.empty()) {
                        if (myStack.pop() == true) {
                            forceJump = true;
                        } else {
                            forceSkip = true;
                        }
                    }
                }
                if (code.code.get(ip).definition instanceof IfFalseIns) {
                    if (!myStack.empty()) {
                        if (myStack.pop() == false) {
                            forceJump = true;
                        } else {
                            forceSkip = true;
                        }
                    }
                }
                if (code.code.get(ip).definition instanceof GetLocalTypeIns) {
                    int locreg = ((GetLocalTypeIns) code.code.get(ip).definition).getRegisterId(code.code.get(ip));
                    if (locreg == trueReg) {
                        myStack.push(Boolean.TRUE);
                        ignored.add(ip);
                    }
                    if (locreg == falseReg) {
                        myStack.push(Boolean.FALSE);
                        ignored.add(ip);
                    }
                } else if (code.code.get(ip).definition instanceof PopIns) {
                    if (!myStack.empty()) {
                        myStack.pop();
                        ignored.add(ip);
                    }
                } else if (code.code.get(ip).definition instanceof SwapIns) {
                    if (myStack.size() >= 2) {
                        Boolean b1 = myStack.pop();
                        Boolean b2 = myStack.pop();
                        myStack.push(b1);
                        myStack.push(b2);
                        ignored.add(ip);
                    }
                } else if (code.code.get(ip).definition instanceof DupIns) {
                    if (!myStack.empty()) {
                        Boolean b = myStack.pop();
                        myStack.push(b);
                        myStack.push(b);
                        ignored.add(ip);
                    }
                } else if ((code.code.get(ip).definition instanceof JumpIns) || forceJump) {
                    if (ip - 1 >= start) {
                        GraphBlock bl = new GraphBlock(start, ip - 1);
                        parts.add(bl);
                        allBlocks.add(bl);
                    }
                    int jumpIp = code.adr2pos(code.pos2adr(ip + 1) + code.code.get(ip).operands[0]);
                    makeGraph(myStack,code, jumpIp, parts, allBlocks);
                    return;
                } else if (code.code.get(ip).definition instanceof IfTypeIns) {
                    if (forceSkip) {
                        ip++;
                        continue;
                    }

                    if (ip - 1 >= start) {
                        GraphBlock bl = new GraphBlock(start, ip - 1);
                        parts.add(bl);
                        allBlocks.add(bl);
                    }
                    int jumpIp = code.adr2pos(code.pos2adr(ip + 1) + code.code.get(ip).operands[0]);
                    GraphDecision dec = new GraphDecision();
                    parts.add(dec);
                    dec.start = ip;
                    makeGraph(myStack,code, jumpIp, dec.onTrue, allBlocks);
                    makeGraph(myStack,code, ip + 1, dec.onFalse, allBlocks);
                    return;
                } else if ((code.code.get(ip).definition instanceof ReturnValueIns) || (code.code.get(ip).definition instanceof ReturnVoidIns)) {
                    ip++;
                    break;
                }
                ip++;
            }
            GraphFinalBlock bl = new GraphFinalBlock(start, ip - 1);
            parts.add(bl);
            allBlocks.add(bl);
        } catch (ConvertException ex) {
            Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
