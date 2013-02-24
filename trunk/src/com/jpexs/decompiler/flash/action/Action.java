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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.parser.FlasmLexer;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.ParsedSymbol;
import com.jpexs.decompiler.flash.action.special.ActionNop;
import com.jpexs.decompiler.flash.action.swf4.*;
import com.jpexs.decompiler.flash.action.swf5.*;
import com.jpexs.decompiler.flash.action.swf6.ActionEnumerate2;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.action.swf7.ActionTry;
import com.jpexs.decompiler.flash.action.treemodel.*;
import com.jpexs.decompiler.flash.action.treemodel.clauses.*;
import com.jpexs.decompiler.flash.action.treemodel.operations.NotTreeItem;
import com.jpexs.decompiler.flash.graph.GraphSource;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.IfItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents one ACTIONRECORD, also has some static method to work with Actions
 */
public class Action implements GraphSourceItem {

   public Action beforeInsert;
   public boolean ignored = false;
   /**
    * Action type identifier
    */
   public int actionCode;
   /**
    * Length of action data
    */
   public int actionLength;
   private long address;
   /**
    * String used to indent line when converting to string
    */
   public static final String INDENTOPEN = "INDENTOPEN";
   /**
    * String used to unindent line when converting to string
    */
   public static final String INDENTCLOSE = "INDENTCLOSE";
   /**
    * Names of ActionScript properties
    */
   public static final String[] propertyNames = new String[]{
      "_x",
      "_y",
      "_xscale",
      "_yscale",
      "_currentframe",
      "_totalframes",
      "_alpha",
      "_visible",
      "_width",
      "_height",
      "_rotation",
      "_target",
      "_framesloaded",
      "_name",
      "_droptarget",
      "_url",
      "_highquality",
      "_focusrect",
      "_soundbuftime",
      "_quality",
      "_xmouse",
      "_ymouse"
   };
   private static Logger logger = Logger.getLogger(Action.class.getName());

   /**
    * Constructor
    *
    * @param actionCode Action type identifier
    * @param actionLength Length of action data
    */
   public Action(int actionCode, int actionLength) {
      this.actionCode = actionCode;
      this.actionLength = actionLength;
   }

   /**
    * Returns address of this action
    *
    * @return
    */
   public long getAddress() {
      return address;
   }

   /**
    * Gets all addresses which are referenced from this action and/or subactions
    *
    * @param version SWF version
    * @return List of addresses
    */
   public List<Long> getAllRefs(int version) {
      List<Long> ret = new ArrayList<Long>();
      return ret;
   }

   /**
    * Gets all ActionIf or ActionJump actions from subactions
    *
    * @return List of actions
    */
   public List<Action> getAllIfsOrJumps() {
      List<Action> ret = new ArrayList<Action>();
      return ret;
   }

   /**
    * Gets all ActionIf or ActionJump actions from list of actions
    *
    * @param list List of actions
    * @return List of actions
    */
   public static List<Action> getActionsAllIfsOrJumps(List<Action> list) {
      List<Action> ret = new ArrayList<Action>();
      for (Action a : list) {
         List<Action> part = a.getAllIfsOrJumps();
         ret.addAll(part);
      }
      return ret;
   }

   /**
    * Gets all addresses which are referenced from the list of actions
    *
    * @param list List of actions
    * @param version SWF version
    * @return List of addresses
    */
   public static List<Long> getActionsAllRefs(List<Action> list, int version) {
      List<Long> ret = new ArrayList<Long>();
      for (Action a : list) {
         List<Long> part = a.getAllRefs(version);
         ret.addAll(part);
      }
      return ret;
   }

   /**
    * Sets address of this instruction
    *
    * @param address Address
    * @param version SWF version
    */
   public void setAddress(long address, int version) {
      this.address = address;
   }

   /**
    * Returns a string representation of the object
    *
    * @return a string representation of the object.
    */
   @Override
   public String toString() {
      return "Action" + actionCode;
   }

   /**
    * Reads String from FlasmLexer
    *
    * @param lex FlasmLexer
    * @return String value
    * @throws IOException
    * @throws ParseException When read object is not String
    */
   protected String lexString(FlasmLexer lex) throws IOException, ParseException {
      ParsedSymbol symb = lex.yylex();
      if (symb.type != ParsedSymbol.TYPE_STRING) {
         throw new ParseException("String expected", lex.yyline());
      }
      return (String) symb.value;
   }

   /**
    * Reads Block startServer from FlasmLexer
    *
    * @param lex FlasmLexer
    * @throws IOException
    * @throws ParseException When read object is not Block startServer
    */
   protected void lexBlockOpen(FlasmLexer lex) throws IOException, ParseException {
      ParsedSymbol symb = lex.yylex();
      if (symb.type != ParsedSymbol.TYPE_BLOCK_START) {
         throw new ParseException("Block startServer ", lex.yyline());
      }
   }

   /**
    * Reads Identifier from FlasmLexer
    *
    * @param lex FlasmLexer
    * @return Identifier name
    * @throws IOException
    * @throws ParseException When read object is not Identifier
    */
   protected String lexIdentifier(FlasmLexer lex) throws IOException, ParseException {
      ParsedSymbol symb = lex.yylex();
      if (symb.type != ParsedSymbol.TYPE_IDENTIFIER) {
         throw new ParseException("Identifier expected", lex.yyline());
      }
      return (String) symb.value;
   }

   /**
    * Reads long value from FlasmLexer
    *
    * @param lex FlasmLexer
    * @return long value
    * @throws IOException
    * @throws ParseException When read object is not long value
    */
   protected long lexLong(FlasmLexer lex) throws IOException, ParseException {
      ParsedSymbol symb = lex.yylex();
      if (symb.type != ParsedSymbol.TYPE_INTEGER) {
         throw new ParseException("Integer expected", lex.yyline());
      }
      return (Long) symb.value;
   }

   /**
    * Reads boolean value from FlasmLexer
    *
    * @param lex FlasmLexer
    * @return boolean value
    * @throws IOException
    * @throws ParseException When read object is not boolean value
    */
   protected boolean lexBoolean(FlasmLexer lex) throws IOException, ParseException {
      ParsedSymbol symb = lex.yylex();
      if (symb.type != ParsedSymbol.TYPE_BOOLEAN) {
         throw new ParseException("Boolean expected", lex.yyline());
      }
      return (Boolean) symb.value;
   }

   /**
    * Gets action converted to bytes
    *
    * @param version SWF version
    * @return Array of bytes
    */
   public byte[] getBytes(int version) {
      byte ret[] = new byte[1];
      ret[0] = (byte) actionCode;
      return ret;
   }

   /**
    * Surrounds byte array with Action header
    *
    * @param data Byte array
    * @param version SWF version
    * @return Byte array
    */
   protected byte[] surroundWithAction(byte[] data, int version) {
      ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
      SWFOutputStream sos2 = new SWFOutputStream(baos2, version);
      try {
         sos2.writeUI8(actionCode);
         sos2.writeUI16(data.length);
         sos2.write(data);
         sos2.close();
      } catch (IOException e) {
      }
      return baos2.toByteArray();
   }

   /**
    * Converts list of Actions to bytes
    *
    * @param list List of actions
    * @param addZero Whether or not to add 0 UI8 value to the end
    * @param version SWF version
    * @return Array of bytes
    */
   public static byte[] actionsToBytes(List<Action> list, boolean addZero, int version) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      for (Action a : list) {
         try {
            baos.write(a.getBytes(version));
         } catch (IOException e) {
         }
      }
      if (addZero) {
         baos.write(0);
      }
      return baos.toByteArray();
   }

   /**
    * Set addresses of actions in the list
    *
    * @param list List of actions
    * @param baseAddress Address of first action in the list
    * @param version SWF version
    */
   public static void setActionsAddresses(List<Action> list, long baseAddress, int version) {
      long offset = baseAddress;
      for (Action a : list) {
         a.setAddress(offset, version);
         offset += a.getBytes(version).length;
      }
   }

   /**
    * Converts list of actions to ASM source
    *
    * @param list List of actions
    * @param importantOffsets List of important offsets to mark as labels
    * @param version SWF version
    * @return ASM source as String
    */
   public static String actionsToString(List<Action> list, List<Long> importantOffsets, int version) {
      return actionsToString(list, importantOffsets, new ArrayList<String>(), version);
   }

   /**
    * Converts list of actions to ASM source
    *
    * @param list List of actions
    * @param importantOffsets List of important offsets to mark as labels
    * @param constantPool Constant pool
    * @param version SWF version
    * @return ASM source as String
    */
   public static String actionsToString(List<Action> list, List<Long> importantOffsets, List<String> constantPool, int version) {
      String ret = "";
      long offset;
      if (importantOffsets == null) {
         setActionsAddresses(list, 0, version);
         importantOffsets = getActionsAllRefs(list, version);
      }

      offset = 0;
      for (Action a : list) {
         offset = a.getAddress();
         if (importantOffsets.contains(offset)) {
            ret += "loc" + Helper.formatAddress(offset) + ":";
         }

         if (a.ignored) {
            int len = a.getBytes(version).length;
            for (int i = 0; i < len; i++) {
               ret += "Nop\r\n";
            }
         } else {
            if (a.beforeInsert != null) {
               ret += a.beforeInsert.getASMSource(importantOffsets, constantPool, version) + "\r\n";
            }
            if (!(a instanceof ActionNop)) {
               ret += Highlighting.hilighOffset("", offset) + a.getASMSource(importantOffsets, constantPool, version) + "\r\n";
            }
         }
         offset += a.getBytes(version).length;
      }
      if (importantOffsets.contains(offset)) {
         ret += "loc" + Helper.formatAddress(offset) + ":\r\n";
      }
      return ret;
   }

   /**
    * Convert action to ASM source
    *
    * @param knownAddreses List of important offsets to mark as labels
    * @param constantPool Constant pool
    * @param version SWF version
    * @return
    */
   public String getASMSource(List<Long> knownAddreses, List<String> constantPool, int version) {
      return toString();
   }

   /**
    * Translates this function to stack and output.
    *
    * @param stack Stack
    * @param constants Constant pool
    * @param output Output
    * @param regNames Register names
    */
   public void translate(Stack<com.jpexs.decompiler.flash.graph.GraphTargetItem> stack, List<com.jpexs.decompiler.flash.graph.GraphTargetItem> output, java.util.HashMap<Integer, String> regNames) {
   }

   /**
    * Pops long value off the stack
    *
    * @param stack Stack
    * @return long value
    */
   protected long popLong(Stack<GraphTargetItem> stack) {
      GraphTargetItem item = stack.pop();
      if (item instanceof DirectValueTreeItem) {
         if (((DirectValueTreeItem) item).value instanceof Long) {
            return (long) (Long) ((DirectValueTreeItem) item).value;
         }
      }
      return 0;
   }

   /**
    * Converts action index to address in the specified list of actions
    *
    * @param actions List of actions
    * @param ip Action index
    * @param version SWF version
    * @return address
    */
   public static long ip2adr(List<Action> actions, int ip, int version) {
      if (ip >= actions.size()) {
         if (actions.isEmpty()) {
            return 0;
         }
         return actions.get(actions.size() - 1).getAddress() + actions.get(actions.size() - 1).getBytes(version).length;
      }
      if (ip == -1) {
         return 0;
      }
      return actions.get(ip).getAddress();
   }

   /**
    * Converts address to action index in the specified list of actions
    *
    * @param actions List of actions
    * @param addr Address
    * @param version SWF version
    * @return action index
    */
   public static int adr2ip(List<Action> actions, long addr, int version) {
      for (int ip = 0; ip < actions.size(); ip++) {
         if (actions.get(ip).getAddress() == addr) {
            return ip;
         }
      }
      if (actions.size() > 0) {
         long outpos = actions.get(actions.size() - 1).getAddress() + actions.get(actions.size() - 1).getBytes(version).length;
         if (addr == outpos) {
            return actions.size();
         }
      }
      return -1;
   }

   /**
    * Converts list of TreeItems to string
    *
    * @param tree List of TreeItem
    * @return String
    */
   public static String treeToString(List<GraphTargetItem> tree) {
      String ret = "";
      List localData = new ArrayList();
      for (GraphTargetItem ti : tree) {
         ret += ti.toStringSemicoloned(localData) + "\r\n";
      }
      String parts[] = ret.split("\r\n");
      ret = "";


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
         ret += tabString(level) + parts[p] + "\r\n";
         if (Highlighting.stripHilights(parts[p]).equals("{")) {
            level++;
         }
      }
      return ret;
   }
   private static final String INDENT_STRING = "   ";

   private static String tabString(int len) {
      String ret = "";
      for (int i = 0; i < len; i++) {
         ret += INDENT_STRING;
      }
      return ret;
   }

   /**
    * Converts list of actions to ActionScript source code
    *
    * @param actions List of actions
    * @param version SWF version
    * @return String with Source code
    */
   public static String actionsToSource(List<Action> actions, int version) {
      try {
         //List<TreeItem> tree = actionsToTree(new HashMap<Integer, String>(), actions, version);
         List<GraphTargetItem> tree = actionsToTree(new HashMap<Integer, String>(), actions, version);


         return treeToString(tree);
      } catch (Exception ex) {
         Logger.getLogger(Action.class.getName()).log(Level.SEVERE, null, ex);
         return "//Decompilation error :" + ex.getLocalizedMessage();
      }
   }

   /**
    * Converts list of actions to List of treeItems
    *
    * @param regNames Register names
    * @param actions List of actions
    * @param version SWF version
    * @return List of treeItems
    */
   public static List<GraphTargetItem> actionsToTree(HashMap<Integer, String> regNames, List<Action> actions, int version) {
      //Stack<TreeItem> stack = new Stack<TreeItem>();
      return ActionGraph.translateViaGraph(regNames, actions, version);
      //return actionsToTree(regNames,   stack, actions, 0, actions.size() - 1, version);
   }

   @Override
   public void translate(List localData, Stack<GraphTargetItem> stack, List<GraphTargetItem> output) {
      translate(stack, output, (HashMap<Integer, String>) localData.get(0));
   }

   @Override
   public boolean isJump() {
      return false;
   }

   @Override
   public boolean isBranch() {
      return false;
   }

   @Override
   public boolean isExit() {
      return false;
   }

   @Override
   public long getOffset() {
      return getAddress();
   }

   @Override
   public List<Integer> getBranches(GraphSource code) {
      return new ArrayList<Integer>();
   }

   private static class Loop {

      public long loopContinue;
      public long loopBreak;
      public int continueCount = 0;
      public int breakCount = 0;

      public Loop(long loopContinue, long loopBreak) {
         this.loopContinue = loopContinue;
         this.loopBreak = loopBreak;
      }

      @Override
      public String toString() {
         return "[Loop continue:" + loopContinue + ", break:" + loopBreak + "]";
      }
   }

   private static void log(String s) {
      logger.fine(s);
   }

   public static List<GraphTargetItem> actionsPartToTree(HashMap<Integer, String> registerNames, Stack<GraphTargetItem> stack, List<Action> actions, int start, int end, int version) {
      if (start < actions.size() && (end > 0) && (start > 0)) {
         log("Entering " + start + "-" + end + (actions.size() > 0 ? (" (" + actions.get(start).toString() + " - " + actions.get(end == actions.size() ? end - 1 : end) + ")") : ""));
      }
      List localData = new ArrayList();
      localData.add(registerNames);
      List<GraphTargetItem> output = new ArrayList<GraphTargetItem>();
      int ip = start;
      boolean isWhile = false;
      boolean isForIn = false;
      GraphTargetItem inItem = null;
      int loopStart = 0;
      loopip:
      while (ip <= end + 1) {

         long addr = ip2adr(actions, ip, version);
         /*if (unknownJumps.contains(addr)) {
          unknownJumps.remove(new Long(addr));
          boolean switchFound = false;
          for (int i = output.size() - 1; i >= 0; i--) {
          if (output.get(i) instanceof SwitchTreeItem) {
          if (((SwitchTreeItem) output.get(i)).defaultCommands == null) {
          List<ContinueTreeItem> continues = ((SwitchTreeItem) output.get(i)).getContinues();
          boolean breakFound = false;
          for (ContinueTreeItem cti : continues) {
          if (cti.loopPos == addr) {
          cti.isKnown = true;
          cti.isBreak = true;
          ((SwitchTreeItem) output.get(i)).loopBreak = addr;
          breakFound = true;
          }
          }
          if (breakFound) {
          switchFound = true;
          ((SwitchTreeItem) output.get(i)).defaultCommands = new ArrayList<TreeItem>();
          for (int k = i + 1; k < output.size(); k++) {
          ((SwitchTreeItem) output.get(i)).defaultCommands.add(output.remove(i + 1));
          }
          }
          }
          break;
          }
          }
          if (!switchFound) {
          throw new UnknownJumpException(stack, addr, output);
          }
          }*/
         if (ip > end) {
            break;
         }
         if (ip >= actions.size()) {
            break;
         }
         Action action = actions.get(ip);
         /*ActionJump && ActionIf removed*/
         if ((action instanceof ActionEnumerate2) || (action instanceof ActionEnumerate)) {
            loopStart = ip + 1;
            isForIn = true;
            ip += 4;
            action.translate(localData, stack, output);
            EnumerateTreeItem en = (EnumerateTreeItem) stack.peek();
            inItem = en.object;
            continue;
         } else if (action instanceof ActionTry) {
            ActionTry atry = (ActionTry) action;
            List<GraphTargetItem> tryCommands = ActionGraph.translateViaGraph(registerNames, atry.tryBody, version);
            TreeItem catchName;
            if (atry.catchInRegisterFlag) {
               catchName = new DirectValueTreeItem(atry, -1, new RegisterNumber(atry.catchRegister), new ArrayList<String>());
            } else {
               catchName = new DirectValueTreeItem(atry, -1, atry.catchName, new ArrayList<String>());
            }
            List<GraphTargetItem> catchExceptions = new ArrayList<GraphTargetItem>();
            catchExceptions.add(catchName);
            List<List<GraphTargetItem>> catchCommands = new ArrayList<List<GraphTargetItem>>();
            catchCommands.add(ActionGraph.translateViaGraph(registerNames, atry.catchBody, version));
            List<GraphTargetItem> finallyCommands = ActionGraph.translateViaGraph(registerNames, atry.finallyBody, version);
            output.add(new TryTreeItem(tryCommands, catchExceptions, catchCommands, finallyCommands));
         } else if (action instanceof ActionWith) {
            ActionWith awith = (ActionWith) action;
            List<GraphTargetItem> withCommands = ActionGraph.translateViaGraph(registerNames, awith.actions, version);
            output.add(new WithTreeItem(action, stack.pop(), withCommands));
         } else if (action instanceof ActionDefineFunction) {
            FunctionTreeItem fti = new FunctionTreeItem(action, ((ActionDefineFunction) action).functionName, ((ActionDefineFunction) action).paramNames, ActionGraph.translateViaGraph(registerNames, ((ActionDefineFunction) action).code, version), ((ActionDefineFunction) action).constantPool, 1);
            stack.push(fti);
         } else if (action instanceof ActionDefineFunction2) {
            HashMap<Integer, String> funcRegNames = (HashMap<Integer, String>) registerNames.clone();
            for (int f = 0; f < ((ActionDefineFunction2) action).paramNames.size(); f++) {
               int reg = ((ActionDefineFunction2) action).paramRegisters.get(f);
               if (reg != 0) {
                  funcRegNames.put(reg, ((ActionDefineFunction2) action).paramNames.get(f));
               }
            }
            int pos = 1;
            if (((ActionDefineFunction2) action).preloadThisFlag) {
               funcRegNames.put(pos, "this");
               pos++;
            }
            if (((ActionDefineFunction2) action).preloadArgumentsFlag) {
               funcRegNames.put(pos, "arguments");
               pos++;
            }
            if (((ActionDefineFunction2) action).preloadSuperFlag) {
               funcRegNames.put(pos, "super");
               pos++;
            }
            if (((ActionDefineFunction2) action).preloadRootFlag) {
               funcRegNames.put(pos, "_root");
               pos++;
            }
            if (((ActionDefineFunction2) action).preloadParentFlag) {
               funcRegNames.put(pos, "_parent");
               pos++;
            }
            if (((ActionDefineFunction2) action).preloadGlobalFlag) {
               funcRegNames.put(pos, "_global");
               pos++;
            }

            FunctionTreeItem fti = new FunctionTreeItem(action, ((ActionDefineFunction2) action).functionName, ((ActionDefineFunction2) action).paramNames, ActionGraph.translateViaGraph(funcRegNames, ((ActionDefineFunction2) action).code, version), ((ActionDefineFunction2) action).constantPool, ((ActionDefineFunction2) action).getFirstRegister());
            stack.push(fti);
         } /*else if (action instanceof ActionPushDuplicate) {
          do {
          if (actions.get(ip + 1) instanceof ActionNot) {
          if (actions.get(ip + 2) instanceof ActionIf) {
          int nextPos = adr2ip(actions, ((ActionIf) actions.get(ip + 2)).getRef(version), version);
          stack.push(new AndTreeItem(action, stack.pop(), actionsToStackTree(registerNames, actions, constants, ip + 4, nextPos - 1, version).pop()));
          ip = nextPos;
          } else {
          output.add(new UnsupportedTreeItem(action, "ActionPushDuplicate with Not"));
          break;
          }
          } else if (actions.get(ip + 1) instanceof ActionIf) {
          int nextPos = adr2ip(actions, ((ActionIf) actions.get(ip + 1)).getRef(version), version);
          stack.push(new OrTreeItem(action, stack.pop(), actionsToStackTree(registerNames,  actions, constants, ip + 3, nextPos - 1, version).pop()));
          ip = nextPos;
          } else {
          output.add(new UnsupportedTreeItem(action, "ActionPushDuplicate with no If"));
          break loopip;
          }
          action = actions.get(ip);
          } while (action instanceof ActionPushDuplicate);
          continue;
          }*/ else if (action instanceof ActionStoreRegister) {
            if ((ip + 1 <= end) && (actions.get(ip + 1) instanceof ActionPop)) {
               action.translate(localData, stack, output);
               stack.pop();
               ip++;
            } else {
               action.translate(localData, stack, output);
            }
         } /*else if (action instanceof ActionStrictEquals) {
          if ((ip + 1 < actions.size()) && (actions.get(ip + 1) instanceof ActionIf)) {
          List<TreeItem> caseValues = new ArrayList<TreeItem>();
          List<List<TreeItem>> caseCommands = new ArrayList<List<TreeItem>>();
          caseValues.add(stack.pop());
          TreeItem switchedObject = stack.pop();
          if (output.size() > 0) {
          if (output.get(output.size() - 1) instanceof StoreRegisterTreeItem) {
          output.remove(output.size() - 1);
          }
          }
          int caseStart = ip + 2;
          List<Integer> caseBodyIps = new ArrayList<Integer>();
          long defaultAddr = 0;
          caseBodyIps.add(adr2ip(actions, ((ActionIf) actions.get(ip + 1)).getRef(version), version));
          ip++;
          do {
          ip++;
          if ((actions.get(ip - 1) instanceof ActionStrictEquals) && (actions.get(ip) instanceof ActionIf)) {
          caseValues.add(actionsToStackTree(registerNames, jumpsOrIfs, actions, constants, caseStart, ip - 2, version).pop());
          caseStart = ip + 1;
          caseBodyIps.add(adr2ip(actions, ((ActionIf) actions.get(ip)).getRef(version), version));
          if (actions.get(ip + 1) instanceof ActionJump) {
          defaultAddr = ((ActionJump) actions.get(ip + 1)).getRef(version);
          ip = adr2ip(actions, defaultAddr, version);
          break;
          }
          }
          } while (ip < end);
               
          for (int i = 0; i < caseBodyIps.size(); i++) {
          int caseEnd = ip - 1;
          if (i < caseBodyIps.size() - 1) {
          caseEnd = caseBodyIps.get(i + 1) - 1;
          }
          caseCommands.add(actionsToTree(registerNames, unknownJumps, loopList, jumpsOrIfs, stack, constants, actions, caseBodyIps.get(i), caseEnd, version));
          }
          output.add(new SwitchTreeItem(action, defaultAddr, switchedObject, caseValues, caseCommands, null));
          continue;
          } else {
          action.translate(stack, constants, output, registerNames);
          }
          } */ else {
            try {
               action.translate(localData, stack, output);
            } catch (EmptyStackException ese) {
               System.err.println("EMPTYSTACK===========================");
               Logger.getLogger(Action.class.getName()).log(Level.SEVERE, null, ese);
               output.add(new UnsupportedTreeItem(action, "Empty stack"));
            }

         }

         ip++;
      }
      if (stack.size() > 0) {
         for (int i = stack.size() - 1; i >= 0; i--) {
            if (stack.get(i) instanceof FunctionTreeItem) {
               output.add(0, stack.get(i));
               stack.remove(i);
            }
         }
      }
      //output = checkClass(output);
      log("Leaving " + start + "-" + end);
      return output;
   }

   public static GraphTargetItem getWithoutGlobal(GraphTargetItem ti) {
      GraphTargetItem t = ti;
      if (!(t instanceof GetMemberTreeItem)) {
         return ti;
      }
      GetMemberTreeItem lastMember = null;
      while (((GetMemberTreeItem) t).object instanceof GetMemberTreeItem) {
         lastMember = (GetMemberTreeItem) t;
         t = ((GetMemberTreeItem) t).object;
      }
      if (((GetMemberTreeItem) t).object instanceof GetVariableTreeItem) {
         GetVariableTreeItem v = (GetVariableTreeItem) ((GetMemberTreeItem) t).object;
         if (v.value instanceof DirectValueTreeItem) {
            if (((DirectValueTreeItem) v.value).value instanceof String) {
               if (((DirectValueTreeItem) v.value).value.equals("_global")) {
                  GetVariableTreeItem gvt = new GetVariableTreeItem(null, ((GetMemberTreeItem) t).memberName);
                  if (lastMember == null) {
                     return gvt;
                  } else {
                     lastMember.object = gvt;
                  }
               }
            }
         }
      }
      return ti;
   }

   public static List<GraphTargetItem> checkClass(List<GraphTargetItem> output) {
      List<GraphTargetItem> ret = new ArrayList<GraphTargetItem>();
      List<FunctionTreeItem> functions = new ArrayList<FunctionTreeItem>();
      List<FunctionTreeItem> staticFunctions = new ArrayList<FunctionTreeItem>();
      HashMap<GraphTargetItem, GraphTargetItem> vars = new HashMap<GraphTargetItem, GraphTargetItem>();
      HashMap<GraphTargetItem, GraphTargetItem> staticVars = new HashMap<GraphTargetItem, GraphTargetItem>();
      GraphTargetItem className;
      GraphTargetItem extendsOp = null;
      List<GraphTargetItem> implementsOp = new ArrayList<GraphTargetItem>();
      boolean ok = true;
      for (GraphTargetItem t : output) {
         if (t instanceof IfItem) {
            IfItem it = (IfItem) t;
            if (it.expression instanceof NotTreeItem) {
               NotTreeItem nti = (NotTreeItem) it.expression;
               if (nti.value instanceof GetMemberTreeItem) {
                  if (true) { //it.onFalse.isEmpty()){ //||(it.onFalse.get(0) instanceof UnsupportedTreeItem)) {
                     if ((it.onTrue.size() == 1) && (it.onTrue.get(0) instanceof SetMemberTreeItem) && (((SetMemberTreeItem) it.onTrue.get(0)).value instanceof NewObjectTreeItem)) {
                        //ignore
                     } else {
                        List<GraphTargetItem> parts = it.onTrue;
                        className = getWithoutGlobal((GetMemberTreeItem) nti.value);
                        if (parts.size() >= 1) {
                           if (parts.get(0) instanceof StoreRegisterTreeItem) {
                              int classReg = ((StoreRegisterTreeItem) parts.get(0)).register.number;
                              if ((parts.size() >= 2) && (parts.get(1) instanceof SetMemberTreeItem)) {
                                 GraphTargetItem ti1 = ((SetMemberTreeItem) parts.get(1)).value;
                                 GraphTargetItem ti2 = ((StoreRegisterTreeItem) parts.get(0)).value;
                                 if (ti1 == ti2) {
                                    if (((SetMemberTreeItem) parts.get(1)).value instanceof FunctionTreeItem) {
                                       ((FunctionTreeItem) ((SetMemberTreeItem) parts.get(1)).value).calculatedFunctionName = (className instanceof GetMemberTreeItem) ? ((GetMemberTreeItem) className).memberName : className;
                                       functions.add((FunctionTreeItem) ((SetMemberTreeItem) parts.get(1)).value);
                                       int pos = 2;
                                       if (parts.size() <= pos) {
                                          ok = false;
                                          break;
                                       }

                                       if (parts.get(pos) instanceof ExtendsTreeItem) {
                                          ExtendsTreeItem et = (ExtendsTreeItem) parts.get(pos);
                                          extendsOp = getWithoutGlobal(et.superclass);
                                          pos++;
                                       }

                                       if (parts.get(pos) instanceof StoreRegisterTreeItem) {
                                          int instanceReg = -1;
                                          if (((StoreRegisterTreeItem) parts.get(pos)).value instanceof GetMemberTreeItem) {
                                             GraphTargetItem obj = ((GetMemberTreeItem) ((StoreRegisterTreeItem) parts.get(pos)).value).object;
                                             if (obj instanceof DirectValueTreeItem) {
                                                if (((DirectValueTreeItem) obj).value instanceof RegisterNumber) {
                                                   if (((RegisterNumber) ((DirectValueTreeItem) obj).value).number == classReg) {
                                                      instanceReg = ((StoreRegisterTreeItem) parts.get(pos)).register.number;
                                                   }
                                                }
                                             }
                                          } else if (((StoreRegisterTreeItem) parts.get(pos)).value instanceof NewMethodTreeItem) {

                                             if (parts.get(pos + 1) instanceof SetMemberTreeItem) {
                                                if (((SetMemberTreeItem) parts.get(pos + 1)).value == ((StoreRegisterTreeItem) parts.get(pos)).value) {
                                                   instanceReg = ((StoreRegisterTreeItem) parts.get(pos)).register.number;
                                                   NewMethodTreeItem nm = (NewMethodTreeItem) ((StoreRegisterTreeItem) parts.get(pos)).value;
                                                   GetMemberTreeItem gm = new GetMemberTreeItem(null, nm.scriptObject, nm.methodName);
                                                   extendsOp = gm;
                                                } else {
                                                   ok = false;
                                                   break;
                                                }
                                             } else {
                                                ok = false;
                                                break;
                                             }
                                             pos++;
                                          } else {
                                             ok = false;
                                             break;
                                          }
                                          if (instanceReg == -1) {
                                             ok = false;
                                             break;
                                          }
                                          pos++;
                                          if (parts.size() <= pos) {
                                             ok = false;
                                             break;
                                          }
                                          if (parts.get(pos) instanceof ImplementsOpTreeItem) {
                                             ImplementsOpTreeItem io = (ImplementsOpTreeItem) parts.get(pos);
                                             implementsOp = io.superclasses;
                                             pos++;
                                          }
                                          while ((parts.size() > pos) && ok) {
                                             if (parts.get(pos) instanceof SetMemberTreeItem) {
                                                SetMemberTreeItem smt = (SetMemberTreeItem) parts.get(pos);
                                                if (smt.object instanceof DirectValueTreeItem) {
                                                   if (((DirectValueTreeItem) smt.object).value instanceof RegisterNumber) {
                                                      if (((RegisterNumber) ((DirectValueTreeItem) smt.object).value).number == instanceReg) {
                                                         if (smt.value instanceof FunctionTreeItem) {
                                                            ((FunctionTreeItem) smt.value).calculatedFunctionName = smt.objectName;
                                                            functions.add((FunctionTreeItem) smt.value);
                                                         } else {
                                                            vars.put(smt.objectName, smt.value);
                                                         }
                                                      } else if (((RegisterNumber) ((DirectValueTreeItem) smt.object).value).number == classReg) {
                                                         if (smt.value instanceof FunctionTreeItem) {
                                                            ((FunctionTreeItem) smt.value).calculatedFunctionName = smt.objectName;
                                                            staticFunctions.add((FunctionTreeItem) smt.value);
                                                         } else {
                                                            staticVars.put(smt.objectName, smt.value);
                                                         }
                                                      } else {
                                                         ok = false;
                                                      }
                                                   }
                                                } else {
                                                   ok = false;
                                                }
                                             } else if (parts.get(pos) instanceof CallFunctionTreeItem) {
                                                //if(((CallFunctionTreeItem)parts.get(pos)).functionName){
                                                if (((CallFunctionTreeItem) parts.get(pos)).functionName instanceof DirectValueTreeItem) {
                                                   if (((DirectValueTreeItem) ((CallFunctionTreeItem) parts.get(pos)).functionName).value.equals("ASSetPropFlags")) {
                                                   } else {
                                                      ok = false;
                                                   }
                                                } else {
                                                   ok = false;
                                                }

                                             } else {
                                                ok = false;
                                                break;
                                             }
                                             pos++;
                                          }
                                          if (ok) {
                                             List<GraphTargetItem> output2 = new ArrayList<GraphTargetItem>();
                                             output2.add(new ClassTreeItem(className, extendsOp, implementsOp, functions, vars, staticFunctions, staticVars));
                                             return output2;
                                          }
                                       } else {
                                          ok = false;
                                       }
                                    } else {
                                       ok = false;
                                    }
                                 } else {
                                    ok = false;
                                 }
                              } else {
                                 ok = false;
                              }
                           } else if (parts.get(0) instanceof SetMemberTreeItem) {
                              SetMemberTreeItem sm = (SetMemberTreeItem) parts.get(0);
                              if (sm.value instanceof FunctionTreeItem) {
                                 FunctionTreeItem f = (FunctionTreeItem) sm.value;
                                 if (f.actions.isEmpty()) {

                                    if (parts.size() == 2) {
                                       if (parts.get(1) instanceof ImplementsOpTreeItem) {
                                          ImplementsOpTreeItem iot = (ImplementsOpTreeItem) parts.get(1);
                                          implementsOp = iot.superclasses;
                                       } else {
                                          ok = false;
                                          break;
                                       }
                                    }
                                    List<GraphTargetItem> output2 = new ArrayList<GraphTargetItem>();
                                    output2.add(new InterfaceTreeItem(sm.objectName, implementsOp));
                                    return output2;
                                 } else {
                                    ok = false;
                                 }
                              } else {
                                 ok = false;
                              }
                           } else {
                              ok = false;
                           }
                        } else {
                           ok = false;
                        }
                     }
                  } else {
                     ok = false;
                  }
               } else {
                  ok = false;
               }
            } else {
               ok = false;
            }
         } else {
            ok = false;
         }
         if (!ok) {
            break;
         }
      }
      if (!ok) {
         return output;
      }
      return ret;
   }
}
