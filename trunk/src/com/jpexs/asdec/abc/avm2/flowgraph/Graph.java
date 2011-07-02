/*
 *  Copyright (C) 2010-2011 JPEXS
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

    public GraphPart head;
    public List<Integer> ignored = new ArrayList<Integer>();
    private int trueReg = -1;
    private int falseReg = -1;

    public Graph(AVM2Code code) {
        int start = checkSWFSecureStart(code);
        head=makeGraph(new Stack<Boolean>(), code, start, new ArrayList<GraphPart>());
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
   

    private GraphPart makeGraph(Stack<Boolean> myStack,AVM2Code code, int start, List<GraphPart> allBlocks) {
        GraphPart ret=new GraphPart(start,-1);
        ret.instanceCount=1;
        allBlocks.add(ret);
        GraphPart actual=ret;
        try {
            int ip = start;
            while (ip < code.code.size()) {
                for (GraphPart block : allBlocks) {
                    if (block.containsIP(ip)) {
                        if(block.start<ip){
                            int oldEnd=block.end;
                            block.end=ip-1;
                            GraphPart newBlock=new GraphPart(ip,oldEnd);
                            newBlock.nextParts.addAll(block.nextParts);
                            newBlock.instanceCount=1;
                            block.nextParts.clear();
                            block.nextParts.add(newBlock);
                            allBlocks.add(newBlock);
                            block=newBlock;                            
                        }
                        block.instanceCount++;
                        if(start<ip){
                          actual.end=ip-1;
                          actual.nextParts.add(block);
                          return ret;
                        }else{
                            return block;
                        }
                        
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

                    int jumpIp = code.adr2pos(code.pos2adr(ip + 1) + code.code.get(ip).operands[0]);
                    actual.end=ip;
                    GraphPart newActual=makeGraph(myStack,code,jumpIp,allBlocks);
                    actual.nextParts.add(newActual);
                    return ret;
                } else if (code.code.get(ip).definition instanceof IfTypeIns) {
                    if (forceSkip) {
                        ip++;
                        continue;
                    }

                    actual.end=ip;
                    int jumpIp = code.adr2pos(code.pos2adr(ip + 1) + code.code.get(ip).operands[0]);
                    GraphPart onTrue=makeGraph(myStack,code, jumpIp, allBlocks);
                    actual.nextParts.add(onTrue);
                    GraphPart onFalse=makeGraph(myStack,code, ip + 1, allBlocks);                    
                    actual.nextParts.add(onFalse);                    
                    return ret;
                } else if ((code.code.get(ip).definition instanceof ReturnValueIns) || (code.code.get(ip).definition instanceof ReturnVoidIns)) {
                    ip++;
                    break;
                }
                ip++;
            }
            actual.end=ip-1;
            return ret;
        } catch (ConvertException ex) {
            Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
