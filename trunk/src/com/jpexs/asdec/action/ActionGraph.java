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
package com.jpexs.asdec.action;

import com.jpexs.asdec.action.swf4.ActionIf;
import com.jpexs.asdec.action.swf4.ActionJump;
import com.jpexs.asdec.action.swf4.Null;
import com.jpexs.asdec.action.swf5.ActionReturn;
import com.jpexs.asdec.action.swf7.ActionThrow;
import com.jpexs.asdec.action.treemodel.BreakTreeItem;
import com.jpexs.asdec.action.treemodel.ContinueTreeItem;
import com.jpexs.asdec.action.treemodel.DirectValueTreeItem;
import com.jpexs.asdec.action.treemodel.StoreRegisterTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.clauses.DoWhileTreeItem;
import com.jpexs.asdec.action.treemodel.clauses.ForTreeItem;
import com.jpexs.asdec.action.treemodel.clauses.IfTreeItem;
import com.jpexs.asdec.action.treemodel.clauses.SwitchTreeItem;
import com.jpexs.asdec.action.treemodel.clauses.TernarOpTreeItem;
import com.jpexs.asdec.action.treemodel.clauses.WhileTreeItem;
import com.jpexs.asdec.action.treemodel.operations.AndTreeItem;
import com.jpexs.asdec.action.treemodel.operations.LogicalOp;
import com.jpexs.asdec.action.treemodel.operations.NotTreeItem;
import com.jpexs.asdec.action.treemodel.operations.OrTreeItem;
import com.jpexs.asdec.action.treemodel.operations.StrictEqTreeItem;
import com.jpexs.asdec.graph.Graph;
import com.jpexs.asdec.graph.GraphPart;
import com.jpexs.asdec.graph.GraphPartMulti;
import com.jpexs.asdec.graph.Loop;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class ActionGraph extends Graph {

   private List<Action> code;
   private int version;

   public ActionGraph(List<Action> code, int version) {
      this.version = version;
      this.code = code;
      heads = makeGraph(code, new ArrayList<GraphPart>());
      for (GraphPart head : heads) {
         fixGraph(head);
         makeMulti(head, new ArrayList<GraphPart>());
      }

   }

   public static List<TreeItem> translateViaGraph(HashMap<Integer, String> registerNames, List<Action> code, int version) {
      ActionGraph g = new ActionGraph(code, version);
      List<GraphPart> allParts = new ArrayList<GraphPart>();
      for (GraphPart head : g.heads) {
         populateParts(head, allParts);
      }
      return Action.checkClass(g.printGraph(registerNames, new Stack<TreeItem>(), allParts, null, g.heads.get(0), null, new ArrayList<Loop>(), new HashMap<Loop, List<TreeItem>>()));
   }

   private TreeItem translatePartGetStack(GraphPart part, Stack<TreeItem> stack, HashMap<Integer, String> registerNames) {
      stack = (Stack<TreeItem>) stack.clone();
      translatePart(part, stack, registerNames);
      return stack.pop();
   }

   private List<TreeItem> translatePart(GraphPart part, Stack<TreeItem> stack, HashMap<Integer, String> registerNames) {
      List<GraphPart> sub = part.getSubParts();
      List<TreeItem> ret = new ArrayList<TreeItem>();
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
         if (code.get(end) instanceof ActionJump) {
            end--;
         } else if (code.get(end) instanceof ActionIf) {
            end--;
         }
         ret.addAll(Action.actionsPartToTree(registerNames, stack, code, start, end, version));
      }
      return ret;
   }

   private TreeItem checkLoop(GraphPart part, GraphPart stopPart, List<Loop> loops) {
      if (part == stopPart) {
         return null;
      }
      for (Loop l : loops) {
         if (l.loopContinue == part) {
            return (new ContinueTreeItem(null, l.loopBreak == null ? -1 : l.loopBreak.start));
         }
         if (l.loopBreak == part) {
            return (new BreakTreeItem(null, part.start));
         }
      }
      return null;
   }
   private boolean doDecompile = true;

   private List<TreeItem> printGraph(HashMap<Integer, String> registerNames, Stack<TreeItem> stack, List<GraphPart> allParts, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops, HashMap<Loop, List<TreeItem>> forFinalCommands) {
      List<TreeItem> ret = new ArrayList<TreeItem>();
      boolean debugMode = false;
      if (part.start >= code.size()) {
         return ret;
      }

      if (!doDecompile) {
         // ret.add(new CommentTreeItem(null, "not decompiled"));
         return ret;
      }

      if (debugMode) {
         System.err.println("PART " + part);
      }

      if (part == stopPart) {
         return ret;
      }
      if (part.ignored) {
         return ret;
      }
      List<String> fqn = new ArrayList<String>();
      HashMap<Integer, String> lrn = new HashMap<Integer, String>();
      List<TreeItem> output = new ArrayList<TreeItem>();
      boolean isSwitch = false;
      List<GraphPart> parts = new ArrayList<GraphPart>();
      if (part instanceof GraphPartMulti) {
         parts = ((GraphPartMulti) part).parts;
      } else {
         parts.add(part);
      }
      boolean isIf = false;
      int end = part.end;
      if (end == -1) {
         end = code.size() - 1;
      }
      if (end == code.size()) {
         end--;
      }
      output.addAll(translatePart(part, stack, registerNames));
      if (end > -1) {
         if (code.get(end) instanceof ActionJump) {
            end--;
         } else if (code.get(end) instanceof ActionIf) {
            end--;
            isIf = true;
         }
      }
      if (isIf) {
         Action ins = code.get(end + 1);
         if ((stack.size() >= 2) && (stack.get(stack.size() - 1) instanceof NotTreeItem) && (((NotTreeItem) stack.get(stack.size() - 1)).value == stack.get(stack.size() - 2))) {
            ret.addAll(output);
            printGraph(registerNames, stack, allParts, parent, part.nextParts.get(1), part.nextParts.get(0), loops, forFinalCommands);
            TreeItem second = stack.pop();
            TreeItem first = stack.pop();
            stack.push(new AndTreeItem(ins, first, second));
            ret.addAll(printGraph(registerNames, stack, allParts, parent, part.nextParts.get(0), stopPart, loops, forFinalCommands));
            return ret;
         } else if ((stack.size() >= 2) && (stack.get(stack.size() - 1) == stack.get(stack.size() - 2))) {
            ret.addAll(output);
            printGraph(registerNames, stack, allParts, parent, part.nextParts.get(1), part.nextParts.get(0), loops, forFinalCommands);
            TreeItem second = stack.pop();
            TreeItem first = stack.pop();
            stack.push(new OrTreeItem(ins, first, second));
            ret.addAll(printGraph(registerNames, stack, allParts, parent, part.nextParts.get(0), stopPart, loops, forFinalCommands));
            return ret;
         } else if (stack.peek() instanceof StrictEqTreeItem) {

            TreeItem switchedObject = null;
            if (!output.isEmpty()) {
               if (output.get(output.size() - 1) instanceof StoreRegisterTreeItem) {
                  switchedObject = ((StoreRegisterTreeItem) output.get(output.size() - 1)).value;
               }
            }
            if (switchedObject == null) {
               switchedObject = new DirectValueTreeItem(null, -1, new Null(), null);
            }
            HashMap<Integer, TreeItem> caseValuesMap = new HashMap<Integer, TreeItem>();

            int pos = 0;
            StrictEqTreeItem set = (StrictEqTreeItem) stack.pop();
            caseValuesMap.put(pos, set.rightSide);

            //GraphPart switchLoc = part.nextParts.get(1).nextParts.get(0);
            List<GraphPart> caseBodyParts = new ArrayList<GraphPart>();
            caseBodyParts.add(part.nextParts.get(0));
            TreeItem top = null;
            int cnt = 1;
            while (part.nextParts.size() > 1
                    && part.nextParts.get(1).getHeight() > 1
                    && code.get(part.nextParts.get(1).end) instanceof ActionIf
                    && ((top = translatePartGetStack(part.nextParts.get(1), stack, registerNames)) instanceof StrictEqTreeItem)) {
               cnt++;
               part = part.nextParts.get(1);
               pos++;
               caseBodyParts.add(part.nextParts.get(0));
               /*List<GraphPart> subparts=part.getSubParts();
                for(GraphPart p:subparts){
                Action.actionsPartToTree(registerNames, stack, code, p.start, p.end - 1, version);
                }*/
               set = (StrictEqTreeItem) top;
               caseValuesMap.put(pos, set.rightSide);
            }
            if (cnt == 1) {
               stack.push(set);
            } else {
               part = part.nextParts.get(1);

               GraphPart defaultPart = part;
               //caseBodyParts.add(defaultPart);



               List<GraphPart> defaultAndLastPart = new ArrayList<GraphPart>();
               defaultAndLastPart.add(defaultPart);
               defaultAndLastPart.add(caseBodyParts.get(caseBodyParts.size() - 1));

               GraphPart defaultPart2 = getCommonPart(defaultAndLastPart);

               List<TreeItem> defaultCommands = new ArrayList<TreeItem>();

               defaultCommands = printGraph(registerNames, stack, allParts, null, defaultPart, defaultPart2, loops, forFinalCommands);


               List<GraphPart> loopContinues = new ArrayList<GraphPart>();
               for (Loop l : loops) {
                  if (l.loopContinue != null) {
                     loopContinues.add(l.loopContinue);
                  }
               }

               List<GraphPart> breakParts = new ArrayList<GraphPart>();
               for (int g = 0; g < caseBodyParts.size(); g++) {
                  if (g < caseBodyParts.size() - 1) {
                     if (caseBodyParts.get(g).leadsTo(caseBodyParts.get(g + 1), loopContinues)) {
                        continue;
                     }
                  }
                  GraphPart nsp = caseBodyParts.get(g).getNextSuperPartPath(loopContinues);
                  if (nsp != null) {
                     breakParts.add(nsp);
                  }
               }
               /*GraphPart nspd=defaultPart.getNextSuperPartPath(loopContinues);
                if(nspd!=null){
                breakParts.add(nspd);
                }*/

               Collections.sort(breakParts, new Comparator<GraphPart>() {
                  @Override
                  public int compare(GraphPart o1, GraphPart o2) {
                     return o2.path.length() - o1.path.length();
                  }
               });

               GraphPart breakPart = breakParts.isEmpty() ? null : breakParts.get(0);
               if ((defaultPart2 != breakPart) && (defaultCommands.isEmpty())) {
                  defaultPart = defaultPart2;
               }


               List<TreeItem> caseValues = new ArrayList<TreeItem>();
               for (int i = 0; i < caseBodyParts.size(); i++) {
                  if (caseValuesMap.containsKey(i)) {
                     caseValues.add(caseValuesMap.get(i));
                  } else {
                     continue;
                  }
               }

               List<List<TreeItem>> caseCommands = new ArrayList<List<TreeItem>>();
               GraphPart next = null;



               next = breakPart;

               TreeItem ti = checkLoop(next, stopPart, loops);
               loops.add(new Loop(null, next));
               //switchLoc.getNextPartPath(new ArrayList<GraphPart>());
               List<Integer> valuesMapping = new ArrayList<Integer>();
               List<GraphPart> caseBodies = new ArrayList<GraphPart>();
               for (int i = 0; i < caseValues.size(); i++) {
                  GraphPart cur = caseBodyParts.get(i);
                  if (!caseBodies.contains(cur)) {
                     caseBodies.add(cur);
                  }
                  valuesMapping.add(caseBodies.indexOf(cur));
               }




               if (defaultPart == breakPart) {
                  defaultPart = null;
               }
               if ((defaultPart != null) && (defaultCommands.isEmpty())) {
                  defaultCommands = printGraph(registerNames, stack, allParts, null, defaultPart, next, loops, forFinalCommands);
               }

               List<GraphPart> ignored = new ArrayList<GraphPart>();
               for (Loop l : loops) {
                  ignored.add(l.loopContinue);
               }

               for (int i = 0; i < caseBodies.size(); i++) {
                  List<TreeItem> cc = new ArrayList<TreeItem>();
                  GraphPart nextCase = null;
                  nextCase = next;
                  if (next != null) {
                     if (i < caseBodies.size() - 1) {
                        if (!caseBodies.get(i).leadsTo(caseBodies.get(i + 1), ignored)) {
                           cc.add(new BreakTreeItem(null, next.start));
                        } else {
                           nextCase = caseBodies.get(i + 1);
                        }
                     } else if (!defaultCommands.isEmpty()) {
                        if (!caseBodies.get(i).leadsTo(defaultPart, ignored)) {
                           cc.add(new BreakTreeItem(null, next.start));
                        } else {
                           nextCase = defaultPart;
                        }
                     }
                  }
                  cc.addAll(0, printGraph(registerNames, stack, allParts, null, caseBodies.get(i), nextCase, loops, forFinalCommands));
                  if (cc.size() >= 2) {
                     if (cc.get(cc.size() - 1) instanceof BreakTreeItem) {
                        if ((cc.get(cc.size() - 2) instanceof ContinueTreeItem) || (cc.get(cc.size() - 2) instanceof BreakTreeItem)) {
                           cc.remove(cc.size() - 1);
                        }
                     }
                  }
                  caseCommands.add(cc);
               }

               SwitchTreeItem sti = new SwitchTreeItem(null, next == null ? -1 : next.start, switchedObject, caseValues, caseCommands, defaultCommands, valuesMapping);
               ret.add(sti);

               if (next != null) {
                  if (ti != null) {
                     ret.add(ti);
                  } else {
                     ret.addAll(printGraph(registerNames, stack, allParts, null, next, stopPart, loops, forFinalCommands));
                  }
               }
               return ret;
            }
         } else {
            /*ins.translate*/
         }
         //((IfTypeIns)ins.definition).translateInverted(new HashMap<Integer,TreeItem>(), co.stack, ins);
      }

      int ip = part.start;
      List<GraphPart> loopContinues = new ArrayList<GraphPart>();
      for (Loop l : loops) {
         if (l.loopContinue != null) {
            loopContinues.add(l.loopContinue);
         }
      }
      boolean loop = false;
      boolean reversed = false;
      if ((!part.nextParts.isEmpty()) && part.nextParts.get(0).leadsTo(part, loopContinues)) {
         loop = true;
      } else if ((part.nextParts.size() > 1) && part.nextParts.get(1).leadsTo(part, loopContinues)) {
         loop = true;
         reversed = true;
      }
      if (((part.nextParts.size() == 2) || ((part.nextParts.size() == 1) && loop)) && (!isSwitch)) {

         boolean doWhile = loop;
         if (loop && output.isEmpty()) {
            doWhile = false;
         }
         Loop currentLoop = new Loop(part, null);
         if (loop) {
            loops.add(currentLoop);
         }



         loopContinues = new ArrayList<GraphPart>();
         for (Loop l : loops) {
            if (l.loopContinue != null) {
               loopContinues.add(l.loopContinue);
            }
         }

         if (part.nextParts.size() > 1) {
            currentLoop.loopBreak = part.nextParts.get(reversed ? 0 : 1);
         }

         forFinalCommands.put(currentLoop, new ArrayList<TreeItem>());

         int breakIp = -1;
         if (currentLoop.loopBreak != null) {
            breakIp = currentLoop.loopBreak.start;
         }
         TreeItem expr = null;
         if ((code.get(part.end) instanceof ActionJump) || (!(code.get(part.end) instanceof ActionIf))) {
            expr = new DirectValueTreeItem(null, -1, (Boolean) true, new ArrayList<String>());
         } else {
            if (stack.isEmpty()) {
            }
            expr = stack.pop();
         }
         if (doWhile) {
            ret.add(new DoWhileTreeItem(null, breakIp, part.start, output, expr));
         } else {
            ret.addAll(output);
         }

         GraphPart next = part.getNextPartPath(loopContinues);

         if (loop && (!doWhile)) {

            if (reversed && (expr instanceof LogicalOp)) {
               expr = ((LogicalOp) expr).invert();
            }
            List<TreeItem> loopBody = null;
            List<TreeItem> finalCommands = null;
            GraphPart finalPart = null;
            loopBody = printGraph(registerNames, stack, allParts, part, part.nextParts.get(reversed ? 1 : 0), stopPart, loops, forFinalCommands);

            finalCommands = forFinalCommands.get(currentLoop);
            if (!finalCommands.isEmpty()) {
               ret.add(new ForTreeItem(null, breakIp, currentLoop.loopContinue.start, new ArrayList<TreeItem>(), expr, finalCommands, loopBody));
            } /*else if ((expr instanceof HasNextTreeItem) && (!loopBody.isEmpty()) && (loopBody.get(0) instanceof SetTypeTreeItem) && (((SetTypeTreeItem) loopBody.get(0)).getValue().getNotCoerced() instanceof NextValueTreeItem)) {
             TreeItem obj = ((SetTypeTreeItem) loopBody.get(0)).getObject();
             loopBody.remove(0);
             ret.add(new ForEachInTreeItem(null, breakIp, part.start, new InTreeItem(expr.instruction, obj, ((HasNextTreeItem) expr).collection), loopBody));
             } else if ((expr instanceof HasNextTreeItem) && (!loopBody.isEmpty()) && (loopBody.get(0) instanceof SetTypeTreeItem) && (((SetTypeTreeItem) loopBody.get(0)).getValue().getNotCoerced() instanceof NextNameTreeItem)) {
             TreeItem obj = ((SetTypeTreeItem) loopBody.get(0)).getObject();
             loopBody.remove(0);
             ret.add(new ForInTreeItem(null, breakIp, part.start, new InTreeItem(expr.instruction, obj, ((HasNextTreeItem) expr).collection), loopBody));
             }*/ else {
               ret.add(new WhileTreeItem(null, breakIp, part.start, expr, loopBody));
            }
         } else if (!loop) {
            if (expr instanceof LogicalOp) {
               expr = ((LogicalOp) expr).invert();
            } else {
               expr = new NotTreeItem(null, expr);
            }

            int stackSizeBefore = stack.size();
            Stack<TreeItem> trueStack = (Stack<TreeItem>) stack.clone();
            Stack<TreeItem> falseStack = (Stack<TreeItem>) stack.clone();
            TreeItem lopTrue = checkLoop(part.nextParts.get(1), stopPart, loops);
            TreeItem lopFalse = null;
            if (next != part.nextParts.get(0)) {
               lopFalse = checkLoop(part.nextParts.get(0), stopPart, loops);
            }
            List<TreeItem> onTrue = new ArrayList<TreeItem>();
            if (lopTrue != null) {
               onTrue.add(lopTrue);
            } else {
               if (debugMode) {
                  System.err.println("ONTRUE: (inside " + part + ")");
               }
               onTrue = printGraph(registerNames, trueStack, allParts, part, part.nextParts.get(1), next == null ? stopPart : next, loops, forFinalCommands);
               if (debugMode) {
                  System.err.println("/ONTRUE (inside " + part + ")");
               }
            }
            List<TreeItem> onFalse = new ArrayList<TreeItem>();
            if (lopFalse != null) {
               onFalse.add(lopFalse);
            } else {
               if (debugMode) {
                  System.err.println("ONFALSE: (inside " + part + ")");
               }
               onFalse = (((next == part.nextParts.get(0)) || (part.nextParts.get(0).path.equals(part.path) || part.nextParts.get(0).path.length() < part.path.length())) ? new ArrayList<TreeItem>() : printGraph(registerNames, falseStack, allParts, part, part.nextParts.get(0), next == null ? stopPart : next, loops, forFinalCommands));
               if (debugMode) {
                  System.err.println("/ONFALSE (inside " + part + ")");
               }
            }

            if (onTrue.isEmpty() && onFalse.isEmpty() && (trueStack.size() > stackSizeBefore) && (falseStack.size() > stackSizeBefore)) {
               stack.push(new TernarOpTreeItem(null, expr, trueStack.pop(), falseStack.pop()));
            } else {
               ret.add(new IfTreeItem(null, expr, onTrue, onFalse));

               //Same continues in onTrue and onFalse gets continue on parent level
               if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
                  if (onTrue.get(onTrue.size() - 1) instanceof ContinueTreeItem) {
                     if (onFalse.get(onFalse.size() - 1) instanceof ContinueTreeItem) {
                        if (((ContinueTreeItem) onTrue.get(onTrue.size() - 1)).loopPos == ((ContinueTreeItem) onFalse.get(onFalse.size() - 1)).loopPos) {
                           onTrue.remove(onTrue.size() - 1);
                           ret.add(onFalse.remove(onFalse.size() - 1));
                        }
                     }
                  }
               }
            }
         }
         if (loop && (part.nextParts.size() > 1)) {
            loops.remove(currentLoop); //remove loop so no break shows up
            ret.addAll(printGraph(registerNames, stack, allParts, part, part.nextParts.get(reversed ? 0 : 1), stopPart, loops, forFinalCommands));
         }

         if (next != null) {
            TreeItem ti = checkLoop(next, stopPart, loops);
            if (ti != null) {
               ret.add(ti);
            } else {
               if (debugMode) {
                  System.err.println("NEXT: (inside " + part + ")");
               }
               ret.addAll(printGraph(registerNames, stack, allParts, part, next, stopPart, loops, forFinalCommands));
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


         GraphPart p = part.nextParts.get(0);
         TreeItem lop = checkLoop(p, stopPart, loops);
         if (lop == null) {
            if (p.path.length() == part.path.length()) {
               ret.addAll(printGraph(registerNames, stack, allParts, part, p, stopPart, loops, forFinalCommands));
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
                  if ((nearestLoop != null) && (nearestLoop.loopBreak != null)) {
                     List<TreeItem> finalCommands = printGraph(registerNames, stack, allParts, part, p, nearestLoop.loopContinue, loops, forFinalCommands);
                     nearestLoop.loopContinue = p;
                     forFinalCommands.put(nearestLoop, finalCommands);
                     ContinueTreeItem cti = new ContinueTreeItem(null, nearestLoop.loopBreak.start);
                     ret.add(cti);
                  }
               }
            }
         } else {
            ret.add(lop);
         }
      }
      return ret;
   }
   private List<Integer> posCache;

   private void buildCache() {
      posCache = new ArrayList<Integer>();
      int pos = 0;
      for (int i = 0; i < code.size(); i++) {
         posCache.add(pos);
         pos += code.get(i).getBytes(version).length;
      }
      posCache.add(pos);
   }

   private int pos2adr(int pos) {
      if (posCache == null) {
         buildCache();
      }
      return posCache.get(pos);
   }

   private int adr2pos(int adr) {
      if (posCache == null) {
         buildCache();
      }
      return posCache.indexOf(adr);
   }

   private void visitCode(int ip, int lastIp, HashMap<Integer, List<Integer>> refs) {
      while (ip < code.size()) {
         refs.get(ip).add(lastIp);
         lastIp = ip;
         if (refs.get(ip).size() > 1) {
            break;
         }
         Action ins = code.get(ip);
         if (ins instanceof ActionThrow) {
            break;
         }
         if (ins instanceof ActionReturn) {
            break;
         }

         /*if (ins.definition instanceof LookupSwitchIns) {
          try {
          for (int i = 2; i < ins.operands.length; i++) {
          visitCode(adr2pos(pos2adr(ip) + ins.operands[i]), ip, refs);
          }
          ip = adr2pos(pos2adr(ip) + ins.operands[0]);
          continue;
          } catch (ConvertException ex) {
          }
          }*/
         if (ins instanceof ActionJump) {
            ip = adr2pos(pos2adr(ip) + ins.getBytes(version).length + ((ActionJump) ins).offset);
            continue;
         } else if (ins instanceof ActionIf) {
            visitCode(adr2pos(pos2adr(ip) + ins.getBytes(version).length + ((ActionIf) ins).offset), ip, refs);
         }
         ip++;
      };
   }

   public HashMap<Integer, List<Integer>> visitCode() {
      HashMap<Integer, List<Integer>> refs = new HashMap<Integer, List<Integer>>();
      for (int i = 0; i < code.size(); i++) {
         refs.put(i, new ArrayList<Integer>());
      }
      visitCode(0, 0, refs);
      return refs;
   }

   private List<GraphPart> makeGraph(List<Action> actions, List<GraphPart> allBlocks) {
      HashMap<Integer, List<Integer>> refs = visitCode();
      List<GraphPart> ret = new ArrayList<GraphPart>();
      boolean visited[] = new boolean[code.size()];
      ret.add(makeGraph(null, "0", code, 0, 0, allBlocks, refs, visited));
      return ret;
   }

   private GraphPart makeGraph(GraphPart parent, String path, List<Action> code, int startip, int lastIp, List<GraphPart> allBlocks, HashMap<Integer, List<Integer>> refs, boolean visited2[]) {

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
         lastIp = ip;
         Action ins = code.get(ip);
         if ((ins instanceof ActionThrow) || (ins instanceof ActionReturn)) {
            part.end = ip;
            allBlocks.add(part);
            break;
         }
         /*if (ins.definition instanceof LookupSwitchIns) {
          part.end = ip;
          allBlocks.add(part);
          try {
          part.nextParts.add(g = makeGraph(part, path + "0", code, code.adr2pos(code.pos2adr(ip) + ins.operands[0]), ip, allBlocks, refs, visited2));
          g.refs.add(part);
          for (int i = 2; i < ins.operands.length; i++) {
          part.nextParts.add(g = makeGraph(part, path + (i - 1), code, code.adr2pos(code.pos2adr(ip) + ins.operands[i]), ip, allBlocks, refs, visited2));
          g.refs.add(part);
          }
          break;
          } catch (ConvertException ex) {
          }
          }*/
         if (ins instanceof ActionJump) {
            part.end = ip;
            allBlocks.add(part);
            int newip = adr2pos(pos2adr(ip) + ins.getBytes(version).length + ((ActionJump) ins).offset);
            part.nextParts.add(g = makeGraph(part, path, code, newip, lastIp, allBlocks, refs, visited2));
            g.refs.add(part);
            break;
         } else if (ins instanceof ActionIf) {
            part.end = ip;
            allBlocks.add(part);
            part.nextParts.add(g = makeGraph(part, path + "0", code, adr2pos(pos2adr(ip) + ins.getBytes(version).length + ((ActionIf) ins).offset), ip, allBlocks, refs, visited2));
            g.refs.add(part);
            part.nextParts.add(g = makeGraph(part, path + "1", code, ip + 1, ip, allBlocks, refs, visited2));
            g.refs.add(part);

            break;
         }
         ip++;
      };
      if (ip == code.size()) {
         allBlocks.add(part);
         if (part.start == ip) {
            part.end = ip;
         } else {
            part.end = code.size() - 1;
            part.nextParts.add(makeGraph(part, path, code, ip, ip, allBlocks, refs, visited2));
         }

      }
      return ret;
   }
}
