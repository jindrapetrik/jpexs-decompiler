/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.ABCInputStream;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.IfTypeIns;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.instructions.SetTypeIns;
import com.jpexs.asdec.abc.avm2.instructions.arithmetic.*;
import com.jpexs.asdec.abc.avm2.instructions.bitwise.*;
import com.jpexs.asdec.abc.avm2.instructions.comparsion.*;
import com.jpexs.asdec.abc.avm2.instructions.construction.*;
import com.jpexs.asdec.abc.avm2.instructions.debug.DebugFileIns;
import com.jpexs.asdec.abc.avm2.instructions.debug.DebugIns;
import com.jpexs.asdec.abc.avm2.instructions.debug.DebugLineIns;
import com.jpexs.asdec.abc.avm2.instructions.executing.*;
import com.jpexs.asdec.abc.avm2.instructions.jumps.*;
import com.jpexs.asdec.abc.avm2.instructions.localregs.*;
import com.jpexs.asdec.abc.avm2.instructions.other.*;
import com.jpexs.asdec.abc.avm2.instructions.stack.*;
import com.jpexs.asdec.abc.avm2.instructions.types.*;
import com.jpexs.asdec.abc.avm2.instructions.xml.*;
import com.jpexs.asdec.abc.avm2.treemodel.*;
import com.jpexs.asdec.abc.avm2.treemodel.clauses.*;
import com.jpexs.asdec.abc.avm2.treemodel.operations.AndTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.OrTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.PreDecrementTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.PreIncrementTreeItem;
import com.jpexs.asdec.abc.types.ABCException;
import com.jpexs.asdec.abc.types.MethodBody;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.helpers.Helper;
import com.jpexs.asdec.helpers.Highlighting;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AVM2Code {

    public List<AVM2Instruction> code = new LinkedList<AVM2Instruction>();
    public static final int OPT_U30 = 0x100;
    public static final int OPT_U8 = 0x200;
    public static final int OPT_S24 = 0x300;
    public static final int OPT_CASE_OFFSETS = 0x400;
    public static final int OPT_BYTE = 0x500;
    public static final int DAT_MULTINAME_INDEX = OPT_U30 + 0x01;
    public static final int DAT_ARG_COUNT = OPT_U30 + 0x02;
    public static final int DAT_METHOD_INDEX = OPT_U30 + 0x03;
    public static final int DAT_STRING_INDEX = OPT_U30 + 0x04;
    public static final int DAT_DEBUG_TYPE = OPT_U8 + 0x05;
    public static final int DAT_REGISTER_INDEX = OPT_U8 + 0x06;
    public static final int DAT_LINENUM = OPT_U30 + 0x07;
    public static final int DAT_LOCAL_REG_INDEX = OPT_U30 + 0x08;
    public static final int DAT_SLOT_INDEX = OPT_U30 + 0x09;
    public static final int DAT_SLOT_SCOPE_INDEX = OPT_U30 + 0x0A;
    public static final int DAT_OFFSET = OPT_S24 + 0x0B;
    public static final int DAT_EXCEPTION_INDEX = OPT_U30 + 0x0C;
    public static final int DAT_CLASS_INDEX = OPT_U30 + 0x0D;
    public static final int DAT_INT_INDEX = OPT_U30 + 0x0E;
    public static final int DAT_UINT_INDEX = OPT_U30 + 0x0F;
    public static final int DAT_DOUBLE_INDEX = OPT_U30 + 0x10;
    public static final int DAT_CASE_BASEOFFSET = OPT_S24 + 0x11;
    public static InstructionDefinition instructionSet[] = new InstructionDefinition[]{
            new AddIns(),
            new AddIIns(),
            new AsTypeIns(),
            new AsTypeLateIns(),
            new BitAndIns(),
            new BitNotIns(),
            new BitOrIns(),
            new BitXorIns(),
            new CallIns(),
            new CallMethodIns(),
            new CallPropertyIns(),
            new CallPropLexIns(),
            new CallPropVoidIns(),
            new CallStaticIns(),
            new CallSuperIns(),
            new CallSuperVoidIns(),
            new CheckFilterIns(),
            new CoerceIns(),
            new CoerceAIns(),
            new CoerceSIns(),
            new ConstructIns(),
            new ConstructPropIns(),
            new ConstructSuperIns(),
            new ConvertBIns(),
            new ConvertIIns(),
            new ConvertDIns(),
            new ConvertOIns(),
            new ConvertUIns(),
            new ConvertSIns(),
            new DebugIns(),
            new DebugFileIns(),
            new DebugLineIns(),
            new DecLocalIns(),
            new DecLocalIIns(),
            new DecrementIns(),
            new DecrementIIns(),
            new DeletePropertyIns(),
            new DivideIns(),
            new DupIns(),
            new DXNSIns(),
            new DXNSLateIns(),
            new EqualsIns(),
            new EscXAttrIns(),
            new EscXElemIns(),
            new FindPropertyIns(),
            new FindPropertyStrictIns(),
            new GetDescendantsIns(),
            new GetGlobalScopeIns(),
            new GetGlobalSlotIns(),
            new GetLexIns(),
            new GetLocalIns(),
            new GetLocal0Ins(),
            new GetLocal1Ins(),
            new GetLocal2Ins(),
            new GetLocal3Ins(),
            new GetPropertyIns(),
            new GetScopeObjectIns(),
            new GetSlotIns(),
            new GetSuperIns(),
            new GreaterEqualsIns(),
            new GreaterThanIns(),
            new HasNextIns(),
            new HasNext2Ins(),
            new IfEqIns(),
            new IfFalseIns(),
            new IfGeIns(),
            new IfGtIns(),
            new IfLeIns(),
            new IfLtIns(),
            new IfNGeIns(),
            new IfNGtIns(),
            new IfNLeIns(),
            new IfNLtIns(),
            new IfNeIns(),
            new IfStrictEqIns(),
            new IfStrictNeIns(),
            new IfTrueIns(),
            new InIns(),
            new IncLocalIns(),
            new IncLocalIIns(),
            new IncrementIns(),
            new IncrementIIns(),
            new InitPropertyIns(),
            new InstanceOfIns(),
            new IsTypeIns(),
            new IsTypeLateIns(),
            new JumpIns(),
            new KillIns(),
            new LabelIns(),
            new LessEqualsIns(),
            new LessThanIns(),
            new LookupSwitchIns(),
            new LShiftIns(),
            new ModuloIns(),
            new MultiplyIns(),
            new MultiplyIIns(),
            new NegateIns(),
            new NegateIIns(),
            new NewActivationIns(),
            new NewArrayIns(),
            new NewCatchIns(),
            new NewClassIns(),
            new NewFunctionIns(),
            new NewObjectIns(),
            new NextNameIns(),
            new NextValueIns(),
            new NopIns(),
            new NotIns(),
            new PopIns(),
            new PopScopeIns(),
            new PushByteIns(),
            new PushDoubleIns(),
            new PushFalseIns(),
            new PushIntIns(),
            new PushNamespaceIns(),
            new PushNanIns(),
            new PushNullIns(),
            new PushScopeIns(),
            new PushShortIns(),
            new PushStringIns(),
            new PushTrueIns(),
            new PushUIntIns(),
            new PushUndefinedIns(),
            new PushWithIns(),
            new ReturnValueIns(),
            new ReturnVoidIns(),
            new RShiftIns(),
            new SetLocalIns(),
            new SetLocal0Ins(),
            new SetLocal1Ins(),
            new SetLocal2Ins(),
            new SetLocal3Ins(),
            new SetGlobalSlotIns(),
            new SetPropertyIns(),
            new SetSlotIns(),
            new SetSuperIns(),
            new StrictEqualsIns(),
            new SubtractIns(),
            new SubtractIIns(),
            new SwapIns(),
            new ThrowIns(),
            new TypeOfIns(),
            new URShiftIns()};
    //endoflist
    public static final String IDENTOPEN = "/*IDENTOPEN*/";
    public static final String IDENTCLOSE = "/*IDENTCLOSE*/";

    private class ConvertOutput {

        public Stack<TreeItem> stack;
        public List<TreeItem> output;

        public ConvertOutput(Stack<TreeItem> stack, List<TreeItem> output) {
            this.stack = stack;
            this.output = output;
        }
    }

    public AVM2Code() {
    }

    public AVM2Code(InputStream is) throws IOException {
        ABCInputStream ais = new ABCInputStream(is);
        while (ais.available() > 0) {
            long startOffset = ais.getPosition();
            ais.startBuffer();
            int instructionCode = ais.read();
            boolean known = false;
            loopi:
            for (int i = 0; i < instructionSet.length; i++) {
                if (instructionSet[i].instructionCode == instructionCode) {
                    known = true;
                    int actualOperands[];
                    if (instructionCode == 0x1b) { //switch
                        int firstOperand = ais.readS24();
                        int case_count = ais.readU30();
                        actualOperands = new int[case_count + 3];
                        actualOperands[0] = firstOperand;
                        actualOperands[1] = case_count;
                        for (int c = 0; c < case_count + 1; c++) {
                            actualOperands[2 + c] = ais.readS24();
                        }
                    } else {
                        actualOperands = new int[instructionSet[i].operands.length];
                        for (int op = 0; op < instructionSet[i].operands.length; op++) {
                            switch (instructionSet[i].operands[op] & 0xff00) {
                                case OPT_U30:
                                    actualOperands[op] = ais.readU30();
                                    break;
                                case OPT_U8:
                                    actualOperands[op] = ais.read();
                                    break;
                                case OPT_BYTE:
                                    actualOperands[op] = (byte) ais.read();
                                    break;
                                case OPT_S24:
                                    actualOperands[op] = ais.readS24();
                                    break;
                            }
                        }
                    }

                    code.add(new AVM2Instruction(startOffset, instructionSet[i], actualOperands, ais.stopBuffer()));
                    break loopi;
                }
            }
            if (!known) {
                throw new UnknownInstructionCode(instructionCode);
            }
        }
    }

    public byte[] getBytes() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            for (AVM2Instruction instruction : code) {
                bos.write(instruction.getBytes());
            }
        } catch (IOException ex) {
        }
        return bos.toByteArray();
    }

    @Override
    public String toString() {
        String s = "";
        for (AVM2Instruction instruction : code) {
            s += instruction.toString() + "\r\n";
        }
        return s;
    }

    public String toString(ConstantPool constants) {
        String s = "";
        int i = 0;
        for (AVM2Instruction instruction : code) {
            s += Helper.formatAddress(i) + " " + instruction.toString(constants) + "\r\n";
            i++;
        }
        return s;
    }

    private static String popStack(Stack<String> stack) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            String s = stack.get(i);
            if (!s.startsWith("//")) {
                stack.remove(i);
                return s;
            }
        }
        return null;
    }

    public String toASMSource(ConstantPool constants) {
        String ret = "";
        List<Long> offsets = new ArrayList<Long>();
        for (AVM2Instruction ins : code) {
            offsets.addAll(ins.getOffsets());
        }
        long ofs = 0;
        for (AVM2Instruction ins : code) {
            if (offsets.contains(ofs)) {
                ret += "ofs" + Helper.formatAddress(ofs) + ":";
            }
            ret += ins.toStringNoAddress(constants) + "\n";
            ofs += ins.getBytes().length;
        }

        return ret;
    }

    public int adr2pos(long address) throws ConvertException {
        int a = 0;
        for (int i = 0; i < code.size(); i++) {
            if (a == address) {
                return i;
            }
            a += code.get(i).getBytes().length;
        }
        if (a == address) {
            return code.size();
        }
        throw new ConvertException("Bad jump", -1);
    }

    public int pos2adr(int pos) {
        int a = 0;
        for (int i = 0; i < pos; i++) {
            a += code.get(i).getBytes().length;
        }

        return a;
    }

    private static String listToString(List<TreeItem> stack, ConstantPool constants) {
        String ret = "";
        for (int d = 0; d < stack.size(); d++) {
            TreeItem o = stack.get(d);
            ret += o.toString(constants) + "\r\n";
        }
        return ret;
    }

    private static String innerStackToString(List stack) {
        String ret = "";
        for (int d = 0; d < stack.size(); d++) {
            Object o = stack.get(d);
            ret += o.toString();
            if (d < stack.size() - 1) {
                if (!ret.endsWith("\r\n")) {
                    ret += "\r\n";
                }
            }
        }
        return ret;
    }

    private class Loop {

        public int loopContinue;
        public int loopBreak;
        public int continueCount = 0;
        public int breakCount = 0;

        public Loop(int loopContinue, int loopBreak) {
            this.loopContinue = loopContinue;
            this.loopBreak = loopBreak;
        }
    }

    private List<Loop> loopList;
    private List<Integer> unknownJumps;
    private List<Integer> finallyJumps;
    private List<ABCException> parsedExceptions;

    private String stripBrackets(String s) {
        if (s.startsWith("(") && (s.endsWith(")"))) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    private int checkCatches(ABC abc, ConstantPool constants, MethodInfo method_info[], Stack<TreeItem> stack, Stack<TreeItem> scopeStack, List<TreeItem> output, MethodBody body, int ip) throws ConvertException {
        /*int newip = ip;
        loope:
        for (int e = 0; e < body.exceptions.length; e++) {
        if (pos2adr(ip) == body.exceptions[e].end) {
        for (int f = 0; f < e; f++) {
        if (body.exceptions[e].startServer == body.exceptions[f].startServer) {
        if (body.exceptions[e].end == body.exceptions[f].end) {
        continue loope;
        }
        }
        }
        output.add("}");
        if (!(code.get(ip).definition instanceof JumpIns)) {
        throw new ConvertException("No jump to skip catches");
        }
        int addrAfterCatches = pos2adr(ip + 1) + code.get(ip).operands[0];
        int posAfterCatches = adr2pos(addrAfterCatches);
        for (int g = 0; g < body.exceptions.length; g++) {
        if (body.exceptions[e].startServer == body.exceptions[g].startServer) {
        if (body.exceptions[e].end == body.exceptions[g].end) {
        if (body.exceptions[g].isFinally()) {
        output.add("finally");
        } else {
        output.add("catch(" + body.exceptions[g].getVarName(constants) + ":" + body.exceptions[g].getTypeName(constants) + ")");
        }
        output.add("{");

        if (body.exceptions[g].isFinally()) {
        int jumppos = adr2pos(body.exceptions[g].target) - 1;
        AVM2Instruction jumpIns = code.get(jumppos);
        if (!(jumpIns.definition instanceof JumpIns)) {
        throw new ConvertException("No jump in finally block");
        }
        int nextAddr = pos2adr(jumppos + 1) + jumpIns.operands[0];
        int nextins = adr2pos(nextAddr);
        int pos = nextins;
        Integer uj = new Integer(nextins);
        if (unknownJumps.contains(uj)) {
        unknownJumps.remove(uj);
        }
        int endpos = 0;
        do {
        if (code.get(pos).definition instanceof LookupSwitchIns) {
        if (code.get(pos).operands[0] == 0) {
        if (adr2pos(pos2adr(pos) + code.get(pos).operands[2]) < pos) {
        endpos = pos - 1;
        newip = endpos + 1;
        break;
        }
        }
        }
        pos++;
        } while (pos < code.size());
        output.addAll(toSource(stack, scopeStack, abc, constants, method_info, body, nextins, endpos).output);
        } else {

        int pos = adr2pos(body.exceptions[g].target);
        int endpos = posAfterCatches - 1;
        for (int p = pos; p < posAfterCatches; p++) {
        if (code.get(p).definition instanceof JumpIns) {
        int nextAddr = pos2adr(p + 1) + code.get(p).operands[0];
        int nextPos = adr2pos(nextAddr);
        if (nextPos == posAfterCatches) {
        endpos = p - 1;
        break;
        }
        }
        }
        Stack cstack = new Stack<TreeItem>();
        cstack.push("catched " + body.exceptions[g].getVarName(constants));
        List outcatch = toSource(cstack, new Stack<TreeItem>(), abc, constants, method_info, body, pos, endpos).output;
        output.addAll(outcatch);
        newip = endpos + 1;
        }
        output.add("}");
        }
        }
        }
        }
        }
        return newip;*/
        return ip;
    }

    boolean isCatched = false;

    private boolean isKilled(int regName, int start, int end) {
        for (int k = start; k <= end; k++) {
            if (code.get(k).definition instanceof KillIns) {
                if (code.get(k).operands[0] == regName) {
                    return true;
                }
            }
        }
        return false;
    }

    private int toSourceCount = 0;

    private ConvertOutput toSource(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, Stack<TreeItem> scopeStack, ABC abc, ConstantPool constants, MethodInfo method_info[], MethodBody body, int start, int end) throws ConvertException {
        boolean debugMode = false;
        if (debugMode)
            System.out.println("OPEN SubSource:" + start + "-" + end + " " + code.get(start).toString() + " to " + code.get(end).toString());
        //if(true) return "";
        toSourceCount++;
        if (toSourceCount > 255) {
            throw new ConvertException("StackOverflow", start);
        }
        List<TreeItem> output = new ArrayList();
        String ret = "";
        int ip = start;
        try {
            int addr;
            iploop:
            while (ip <= end) {

                addr = pos2adr(ip);
                int maxend = -1;
                List<ABCException> catchedExceptions = new ArrayList<ABCException>();
                for (int e = 0; e < body.exceptions.length; e++) {
                    if (addr == body.exceptions[e].start) {
                        if (!body.exceptions[e].isFinally()) {
                            if ((body.exceptions[e].end > maxend) && (!parsedExceptions.contains(body.exceptions[e]))) {
                                catchedExceptions.clear();
                                maxend = body.exceptions[e].end;
                                catchedExceptions.add(body.exceptions[e]);
                            } else if (body.exceptions[e].end == maxend) {
                                catchedExceptions.add(body.exceptions[e]);
                            }
                        }
                    }
                }
                if (catchedExceptions.size() > 0) {
                    parsedExceptions.addAll(catchedExceptions);
                    int endpos = adr2pos(catchedExceptions.get(0).end);


                    List<List<TreeItem>> catchedCommands = new ArrayList<List<TreeItem>>();
                    if (code.get(endpos).definition instanceof JumpIns) {
                        int afterCatchAddr = pos2adr(endpos + 1) + code.get(endpos).operands[0];
                        int afterCatchPos = adr2pos(afterCatchAddr);
                        Collections.sort(catchedExceptions, new Comparator<ABCException>() {

                            public int compare(ABCException o1, ABCException o2) {
                                return o1.target - o2.target;
                            }
                        });


                        List<TreeItem> finallyCommands = new ArrayList<TreeItem>();
                        int returnPos = afterCatchPos;
                        for (int e = 0; e < body.exceptions.length; e++) {
                            if (body.exceptions[e].isFinally()) {
                                if (addr == body.exceptions[e].start) {
                                    if (afterCatchPos + 1 == adr2pos(body.exceptions[e].end)) {
                                        AVM2Instruction jmpIns = code.get(adr2pos(body.exceptions[e].end));
                                        if (jmpIns.definition instanceof JumpIns) {
                                            int finStart = adr2pos(body.exceptions[e].end + jmpIns.getBytes().length + jmpIns.operands[0]);
                                            finallyJumps.add(finStart);
                                            if (unknownJumps.contains(finStart)) {
                                                unknownJumps.remove((Integer) finStart);
                                            }
                                            for (int f = finStart; f <= end; f++) {
                                                if (code.get(f).definition instanceof LookupSwitchIns) {
                                                    AVM2Instruction swins = code.get(f);
                                                    if (swins.operands.length >= 3) {
                                                        if (swins.operands[0] == swins.getBytes().length) {
                                                            if (adr2pos(pos2adr(f) + swins.operands[2]) < finStart) {
                                                                finallyCommands = toSource(isStatic, classIndex, localRegs, stack, scopeStack, abc, constants, method_info, body, finStart, f - 1).output;
                                                                returnPos = f + 1;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        for (int e = 0; e < catchedExceptions.size(); e++) {
                            int eendpos = 0;
                            if (e < catchedExceptions.size() - 1) {
                                eendpos = adr2pos(catchedExceptions.get(e + 1).target) - 2;
                            } else {
                                eendpos = afterCatchPos - 1;
                            }
                            Stack<TreeItem> substack = new Stack<TreeItem>();
                            substack.add(new ExceptionTreeItem(catchedExceptions.get(e)));
                            catchedCommands.add(toSource(isStatic, classIndex, localRegs, substack, new Stack<TreeItem>(), abc, constants, method_info, body, adr2pos(catchedExceptions.get(e).target), eendpos).output);
                        }

                        List<TreeItem> tryCommands = toSource(isStatic, classIndex, localRegs, stack, scopeStack, abc, constants, method_info, body, ip, endpos - 1).output;


                        output.add(new TryTreeItem(tryCommands, catchedExceptions, catchedCommands, finallyCommands));
                        ip = returnPos;
                        addr = pos2adr(ip);
                    }

                }

                if (ip > end)
                    break;

                if (unknownJumps.contains(ip)) {
                    unknownJumps.remove(new Integer(ip));
                    throw new UnknownJumpException(stack, ip, output);
                }
                AVM2Instruction ins = code.get(ip);
                //Ify s vice podminkama
                if (ins.definition instanceof JumpIns) {
                    if (ins.operands[0] == 0) {
                        ip++;
                        addr = pos2adr(ip);
                    } else if (ins.operands[0] > 0) {
                        int secondAddr = addr + ins.getBytes().length;
                        int jumpAddr = secondAddr + ins.operands[0];
                        int jumpPos = adr2pos(jumpAddr);//

                        if (finallyJumps.contains(jumpPos)) {
                            if (code.get(ip + 1).definition instanceof LabelIns) {
                                if (code.get(ip + 2).definition instanceof PopIns) {
                                    if (code.get(ip + 3).definition instanceof LabelIns) {
                                        if (code.get(ip + 4).definition instanceof GetLocalTypeIns) {
                                            if (code.get(ip - 1).definition instanceof PushByteIns) {
                                                if (code.get(ip - 2).definition instanceof SetLocalTypeIns) {
                                                    if (((SetLocalTypeIns) code.get(ip - 2).definition).getRegisterId(code.get(ip - 2)) == ((GetLocalTypeIns) code.get(ip + 4).definition).getRegisterId(code.get(ip + 4))) {
                                                        SetLocalTreeItem ti = (SetLocalTreeItem) output.remove(output.size() - 1);
                                                        stack.add(ti.value);
                                                        ip = ip + 5;
                                                        continue;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            //continue;
                            ip++;
                            continue;
                        }
                        for (Loop l : loopList) {
                            if (l.loopBreak == jumpPos) {
                                output.add(new BreakTreeItem(ins, l.loopBreak));
                                addr = secondAddr;
                                ip = ip + 1;
                                continue iploop;
                            }
                            if (l.loopContinue == jumpPos) {
                                l.continueCount++;
                                output.add(new ContinueTreeItem(ins, l.loopBreak));
                                addr = secondAddr;
                                ip = ip + 1;
                                continue iploop;
                            }
                        }


                        boolean backJumpFound = false;
                        int afterBackJumpAddr = 0;
                        AVM2Instruction backJumpIns = null;
                        boolean isSwitch = false;
                        int switchPos = 0;
                        loopj:
                        for (int j = jumpPos; j <= end; j++) {
                            if (code.get(j).definition instanceof IfTypeIns) {
                                afterBackJumpAddr = pos2adr(j + 1);

                                if (afterBackJumpAddr + code.get(j).operands[0] == secondAddr) {
                                    backJumpFound = true;
                                    backJumpIns = code.get(j);
                                    break;
                                }
                            }
                            if (code.get(j).definition instanceof LookupSwitchIns) {
                                for (int h = 2; h < code.get(j).operands.length; h++) {
                                    int ofs = code.get(j).operands[h] + pos2adr(j);
                                    if (ofs == secondAddr) {
                                        isSwitch = true;
                                        switchPos = j;
                                        break loopj;
                                    }
                                }
                            }
                        }
                        if (isSwitch) {
                            AVM2Instruction killIns = code.get(switchPos - 1);
                            if (!(killIns.definition instanceof KillIns)) {
                                throw new ConvertException("Unknown pattern: no kill before lookupswitch", switchPos - 1);
                            }
                            int userReg = killIns.operands[0];
                            int evalTo = -1;
                            for (int g = jumpPos; g < switchPos; g++) {
                                if ((code.get(g).definition instanceof SetLocal0Ins) && (userReg == 0)) {
                                    evalTo = g;
                                    break;
                                } else if ((code.get(g).definition instanceof SetLocal1Ins) && (userReg == 1)) {
                                    evalTo = g;
                                    break;
                                } else if ((code.get(g).definition instanceof SetLocal2Ins) && (userReg == 2)) {
                                    evalTo = g;
                                    break;
                                } else if ((code.get(g).definition instanceof SetLocal3Ins) && (userReg == 3)) {
                                    evalTo = g;
                                    break;
                                }
                                if ((code.get(g).definition instanceof SetLocalIns) && (userReg == code.get(g).operands[0])) {
                                    evalTo = g;
                                    break;
                                }
                            }
                            if (evalTo == -1) {
                                throw new ConvertException("Unknown pattern: no setlocal before lookupswitch", switchPos);
                            }
                            loopList.add(new Loop(ip, switchPos + 1));
                            Stack<TreeItem> substack = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, jumpPos, evalTo - 1).stack;
                            TreeItem switchedValue = substack.pop();
                            //output.add("loop" + (switchPos + 1) + ":");
                            int switchBreak = switchPos + 1;
                            List<TreeItem> casesList = new ArrayList<TreeItem>();
                            List<List<TreeItem>> caseCommands = new ArrayList<List<TreeItem>>();
                            List<TreeItem> defaultCommands = new ArrayList<TreeItem>();
                            //output.add("switch(" + switchedValue + ")");
                            //output.add("{");
                            int curPos = evalTo + 1;
                            int casePos = 0;
                            do {
                                evalTo = -1;
                                for (int g = curPos; g < switchPos; g++) {
                                    if ((code.get(g).definition instanceof GetLocal0Ins) && (userReg == 0)) {
                                        evalTo = g;
                                        break;
                                    } else if ((code.get(g).definition instanceof GetLocal1Ins) && (userReg == 1)) {
                                        evalTo = g;
                                        break;
                                    } else if ((code.get(g).definition instanceof GetLocal2Ins) && (userReg == 2)) {
                                        evalTo = g;
                                        break;
                                    } else if ((code.get(g).definition instanceof GetLocal3Ins) && (userReg == 3)) {
                                        evalTo = g;
                                        break;
                                    }
                                    if ((code.get(g).definition instanceof GetLocalIns) && (userReg == code.get(g).operands[0])) {
                                        evalTo = g;
                                        break;
                                    }
                                }


                                if (evalTo > -1) {
                                    substack = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, curPos, evalTo - 1).stack;
                                    casesList.add(substack.pop());
                                }
                                int substart = adr2pos(code.get(switchPos).operands[2 + casePos] + pos2adr(switchPos));
                                int subend = jumpPos - 1;
                                if (casePos + 1 < code.get(switchPos).operands.length - 2) {
                                    subend = adr2pos(code.get(switchPos).operands[2 + casePos + 1] + pos2adr(switchPos)) - 1;
                                }

                                if (evalTo == -1)
                                    subend--;
                                List commands = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, substart, subend).output;
                                if ((evalTo == -1) && (casePos < code.get(switchPos).operands.length - 2)) {
                                    if (commands.size() == 1) {
                                        commands.remove(0);
                                    }
                                    if (commands.size() > 0) {
                                        //hasDefault=true;
                                    }
                                }
                                List<TreeItem> caseCommandPart = new ArrayList<TreeItem>();
                                if (evalTo == -1) {
                                    defaultCommands.addAll(commands);
                                } else {
                                    caseCommandPart.addAll(commands);
                                    caseCommands.add(caseCommandPart);
                                }
                                curPos = evalTo + 4;
                                casePos++;
                                if (evalTo == -1) {
                                    break;
                                }
                            } while (true);
                            output.add(new SwitchTreeItem(code.get(switchPos), switchBreak, switchedValue, casesList, caseCommands, defaultCommands));
                            ip = switchPos + 1;
                            addr = pos2adr(ip);
                            continue;
                        }

                        if (!backJumpFound) {
                            if (jumpPos <= end + 1) { //probably skipping catch
                                ip = jumpPos;
                                addr = pos2adr(ip);
                                continue;
                            }
                            output.add(new ContinueTreeItem(ins, jumpPos, false));
                            addr = secondAddr;
                            ip = ip + 1;
                            if (!unknownJumps.contains(jumpPos)) {
                                unknownJumps.add(jumpPos);
                            }
                            continue;
                            //throw new ConvertException("Unknown pattern: forjump with no backjump");
                        }
                        Loop currentLoop = new Loop(jumpPos, adr2pos(afterBackJumpAddr));
                        loopList.add(currentLoop);


                        ConvertOutput co = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, jumpPos, adr2pos(afterBackJumpAddr) - 2);
                        Stack<TreeItem> substack = co.stack;
                        backJumpIns.definition.translate(isStatic, classIndex, localRegs, substack, scopeStack, constants, backJumpIns, method_info, output, body, abc);

                        TreeItem expression = substack.pop();
                        List<TreeItem> subins = new ArrayList<TreeItem>();
                        boolean isFor = false;
                        List<TreeItem> finalExpression = new ArrayList<TreeItem>();
                        try {
                            subins = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, adr2pos(secondAddr) + 1/*label*/, jumpPos - 1).output;
                        } catch (UnknownJumpException uje) {
                            if ((uje.ip >= start) && (uje.ip <= end)) {
                                currentLoop.loopContinue = uje.ip;
                                subins = uje.output;

                                List<ContinueTreeItem> contList = new ArrayList<ContinueTreeItem>();
                                for (TreeItem ti : subins) {
                                    if (ti instanceof ContinueTreeItem) {
                                        contList.add((ContinueTreeItem) ti);
                                    }
                                    if (ti instanceof Block) {
                                        contList.addAll(((Block) ti).getContinues());
                                    }
                                }
                                for (int u = 0; u < contList.size(); u++) {
                                    if (contList.get(u) instanceof ContinueTreeItem) {
                                        if (((ContinueTreeItem) contList.get(u)).loopPos == uje.ip) {
                                            if (!((ContinueTreeItem) contList.get(u)).isKnown) {
                                                ((ContinueTreeItem) contList.get(u)).isKnown = true;
                                                ((ContinueTreeItem) contList.get(u)).loopPos = currentLoop.loopBreak;
                                            }
                                        }
                                    }
                                }
                                finalExpression = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, uje.ip, jumpPos - 1).output;
                                isFor = true;
                            } else {
                                throw new ConvertException("Unknown pattern: jump to nowhere", ip);
                            }
                        }
                        boolean isDoWhile = false;

                        if (jumpPos == ip + 2) {
                            if (code.get(ip + 1).definition instanceof LabelIns) {
                                isDoWhile = true;
                            }
                        }
                        if (!isDoWhile) {
                            if (!isFor) {
                                for (Loop l : loopList) {
                                    if (l.loopContinue == jumpPos) {
                                        if (l.continueCount == 0) {
                                            //isFor = true;
                                            //finalExpression = subins.remove(subins.size() - 1).toString();
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        String firstIns = "";
                        if (isFor) {
                            if (output.size() > 0) {
                                //firstIns = output.remove(output.size() - 1).toString();
                            }
                        }

                        List<TreeItem> loopBody = new ArrayList<TreeItem>();
                        loopBody.addAll(co.output);
                        loopBody.addAll(subins);

                        if (isFor) {
                            output.add(new ForTreeItem(ins, currentLoop.loopBreak, currentLoop.loopContinue, new ArrayList<TreeItem>(), expression, finalExpression, loopBody));
                        } else if (isDoWhile) {
                            output.add(new DoWhileTreeItem(ins, currentLoop.loopBreak, currentLoop.loopContinue, loopBody, expression));
                        } else {
                            if (expression instanceof EachTreeItem) {
                                output.add(new ForEachTreeItem(ins, currentLoop.loopBreak, currentLoop.loopContinue, (EachTreeItem) expression, loopBody));
                            } else {
                                output.add(new WhileTreeItem(ins, currentLoop.loopBreak, currentLoop.loopContinue, expression, loopBody));
                            }
                        }
                        addr = afterBackJumpAddr;
                        ip = adr2pos(addr);
                    } else {
                        throw new ConvertException("Unknown pattern: back jump ", ip);
                    }
                } else if (ins.definition instanceof DupIns) {
                    int nextPos = 0;
                    do {
                        AVM2Instruction insAfter = code.get(ip + 1);
                        AVM2Instruction insBefore = ins;
                        if (ip - 1 >= start) {
                            insBefore = code.get(ip - 1);
                        }
                        boolean isAnd = false;
                        if (insAfter.definition instanceof IfFalseIns) {
                            //stack.add("(" + stack.pop() + ")&&");
                            isAnd = true;
                        } else if (insAfter.definition instanceof IfTrueIns) {
                            //stack.add("(" + stack.pop() + ")||");
                            isAnd = false;
                        } else if ((insAfter.definition instanceof IncrementIIns) || ((insAfter.definition instanceof IncrementIns))) {
                            if (((ip - 1 >= start) && (ip + 2 <= end)) && ((code.get(ip + 2).definition instanceof SetLocalTypeIns) && (code.get(ip - 1).definition instanceof GetLocalTypeIns))) {
                                stack.add(new PostIncrementTreeItem(insAfter, stack.pop()));
                                ip += 3;
                                addr = pos2adr(ip);
                                break;
                            }
                            if (((ip - 1 >= start) && (ip + 2 <= end))
                                    && (code.get(ip + 2).definition instanceof SetLocalTypeIns)
                                    && (isKilled(((SetLocalTypeIns) code.get(ip + 2).definition).getRegisterId(code.get(ip + 2)), ip + 3, end))) {
                                int pos = -1;
                                for (int d = ip + 3; d <= end; d++) {
                                    if (!((code.get(d).definition instanceof GetLocalTypeIns)
                                            && (isKilled(((GetLocalTypeIns) code.get(d).definition).getRegisterId(code.get(d)), d + 1, end)))) {
                                        pos = d;
                                        break;
                                    }
                                }
                                if (pos > -1) {
                                    if (code.get(pos).definition instanceof SetTypeIns) {
                                        stack.push(new PostIncrementTreeItem(insAfter, stack.pop()));
                                        ip = pos + 1;
                                        addr = pos2adr(ip);
                                        break;
                                    }
                                }

                            }
                            ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc);
                            ip++;
                            addr = pos2adr(ip);
                            break;
                        } else if ((insAfter.definition instanceof DecrementIIns) || ((insAfter.definition instanceof DecrementIns))) {
                            if (((ip - 1 >= start) && (ip + 2 <= end)) && ((code.get(ip + 2).definition instanceof SetLocalTypeIns) && (code.get(ip - 1).definition instanceof GetLocalTypeIns))) {
                                stack.add(new PostDecrementTreeItem(insAfter, stack.pop()));
                                ip += 3;
                                addr = pos2adr(ip);
                                break;
                            }
                            if (((ip - 1 >= start) && (ip + 2 <= end))
                                    && (code.get(ip + 2).definition instanceof SetLocalTypeIns)
                                    && (isKilled(((SetLocalTypeIns) code.get(ip + 2).definition).getRegisterId(code.get(ip + 2)), ip + 3, end))) {
                                int pos = -1;
                                for (int d = ip + 3; d <= end; d++) {
                                    if (!((code.get(d).definition instanceof GetLocalTypeIns)
                                            && (isKilled(((GetLocalTypeIns) code.get(d).definition).getRegisterId(code.get(d)), d + 1, end)))) {
                                        pos = d;
                                        break;
                                    }
                                }
                                if (pos > -1) {
                                    if (code.get(pos).definition instanceof SetTypeIns) {
                                        stack.push(new PostDecrementTreeItem(insAfter, stack.pop()));
                                        ip = pos + 1;
                                        addr = pos2adr(ip);
                                        break;
                                    }
                                }

                            }
                            ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc);
                            ip++;
                            addr = pos2adr(ip);
                            break;
                        } else if ((insBefore.definition instanceof IncrementIIns) || ((insBefore.definition instanceof IncrementIns))) {
                            if (((ip - 2 >= start) && (ip + 2 <= end)) && (code.get(ip + 1).definition instanceof ConvertIIns) && (code.get(ip + 2).definition instanceof SetLocalTypeIns) && (code.get(ip - 2).definition instanceof GetLocalTypeIns)) {
                                stack.pop();
                                int regId = ((SetLocalTypeIns) code.get(ip + 2).definition).getRegisterId(code.get(ip + 2));
                                stack.add(new PreIncrementTreeItem(insBefore, new LocalRegTreeItem(code.get(ip + 2), regId, localRegs.get(regId))));
                                ip += 3;
                                addr = pos2adr(ip);
                                break;
                            }
                            if (((ip - 1 >= start) && (ip + 2 <= end))
                                    && (code.get(ip + 1).definition instanceof SetLocalTypeIns)
                                    && (isKilled(((SetLocalTypeIns) code.get(ip + 1).definition).getRegisterId(code.get(ip + 1)), ip + 2, end))) {
                                int pos = -1;
                                for (int d = ip + 2; d <= end; d++) {
                                    if (!((code.get(d).definition instanceof GetLocalTypeIns)
                                            && (isKilled(((GetLocalTypeIns) code.get(d).definition).getRegisterId(code.get(d)), d + 1, end)))) {
                                        pos = d;
                                        break;
                                    }
                                }
                                if (pos > -1) {
                                    if (code.get(pos).definition instanceof SetTypeIns) {
                                        TreeItem s = stack.pop();
                                        if (s instanceof IncrementTreeItem) {
                                            stack.push(new PreIncrementTreeItem(insBefore, ((IncrementTreeItem) s).object));
                                        }
                                        ip = pos + 1;
                                        addr = pos2adr(ip);
                                        break;
                                    }
                                }

                            }
                            ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc);
                            ip++;
                            addr = pos2adr(ip);
                            break;
                        } else if ((insBefore.definition instanceof DecrementIIns) || ((insBefore.definition instanceof DecrementIns))) {
                            if (((ip - 2 >= start) && (ip + 2 <= end)) && (code.get(ip + 1).definition instanceof ConvertIIns) && (code.get(ip + 2).definition instanceof SetLocalTypeIns) && (code.get(ip - 2).definition instanceof GetLocalTypeIns)) {
                                stack.pop();
                                int regId = ((SetLocalTypeIns) code.get(ip + 2).definition).getRegisterId(code.get(ip + 2));
                                stack.add(new PreDecrementTreeItem(insBefore, new LocalRegTreeItem(code.get(ip + 2), regId, localRegs.get(regId))));
                                ip += 3;
                                addr = pos2adr(ip);
                                break;
                            }
                            if (((ip - 1 >= start) && (ip + 2 <= end))
                                    && (code.get(ip + 1).definition instanceof SetLocalTypeIns)
                                    && (isKilled(((SetLocalTypeIns) code.get(ip + 1).definition).getRegisterId(code.get(ip + 1)), ip + 2, end))) {
                                int pos = -1;
                                for (int d = ip + 2; d <= end; d++) {
                                    if (!((code.get(d).definition instanceof GetLocalTypeIns)
                                            && (isKilled(((GetLocalTypeIns) code.get(d).definition).getRegisterId(code.get(d)), d + 1, end)))) {
                                        pos = d;
                                        break;
                                    }
                                }
                                if (pos > -1) {
                                    if (code.get(pos).definition instanceof SetTypeIns) {
                                        TreeItem s = stack.pop();
                                        if (s instanceof DecrementTreeItem) {
                                            stack.push(new PreDecrementTreeItem(insBefore, ((DecrementTreeItem) s).object));
                                        }
                                        ip = pos + 1;
                                        addr = pos2adr(ip);
                                        break;
                                    }
                                }

                            }
                            ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc);
                            ip++;
                            addr = pos2adr(ip);
                            break;
                        } else if (insAfter.definition instanceof SetLocalTypeIns) {
                            /*if (isKilled(((SetLocalTypeIns) insAfter.definition).getRegisterId(insAfter), ip + 2, end)) {
                                ip += 2;
                                addr = pos2adr(ip);
                                break;
                            } else {*/
                            ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc);
                            ip++;
                            addr = pos2adr(ip);
                            break;
                            //}

                        } else {
                            ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc);
                            ip++;
                            addr = pos2adr(ip);
                            break;
                            //throw new ConvertException("Unknown pattern after DUP:" + insComparsion.toString());
                        }
                        addr = addr + ins.getBytes().length + insAfter.getBytes().length + insAfter.operands[0];
                        nextPos = adr2pos(addr) - 1;
                        if (isAnd) {
                            stack.add(new AndTreeItem(insAfter, stack.pop(), toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, ip + 3, nextPos).stack.pop()));
                        } else {
                            stack.add(new OrTreeItem(insAfter, stack.pop(), toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, ip + 3, nextPos).stack.pop()));
                        }
                        ins = code.get(nextPos + 1);
                        ip = nextPos + 1;
                    } while (ins.definition instanceof DupIns);
                } else if (ins.definition instanceof IfTypeIns) {
                    int targetAddr = pos2adr(ip) + ins.getBytes().length + ins.operands[0];
                    int targetIns = adr2pos(targetAddr);
                    ((IfTypeIns) ins.definition).translateInverted(localRegs, stack, ins);

                    TreeItem condition = stack.pop();

                    if (condition.isFalse()) {
                        //ins.definition = new JumpIns();
                        //continue;
                    }
                    if (condition.isTrue()) {
                        //ip = targetIns;
                        //continue;
                    }
                    //stack.add("if"+stack.pop());
                    //stack.add("{");
                    boolean hasElse = false;
                    boolean hasReturn = false;
                    if (code.get(targetIns - 1).definition instanceof JumpIns) {

                        if ((targetIns - 2 > ip) && ((code.get(targetIns - 2).definition instanceof ReturnValueIns) || (code.get(targetIns - 2).definition instanceof ReturnVoidIns) || (code.get(targetIns - 2).definition instanceof ThrowIns))) {
                            hasElse = false;
                            hasReturn = true;
                        } else {
                            int jumpAddr = targetAddr + code.get(targetIns - 1).operands[0];
                            int jumpPos = adr2pos(jumpAddr);
                            hasElse = true;

                            for (Loop l : loopList) {
                                if (l.loopBreak == jumpPos) {
                                    hasElse = false;
                                    break;
                                }
                            }
                            if (hasElse) {
                                if (adr2pos(jumpAddr) > end + 1) {
                                    hasElse = false;
                                    //throw new ConvertException("Unknown pattern: forward jump outside of the block");
                                }
                            }
                        }
                    }
                    ConvertOutput onTrue = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, ip + 1, targetIns - 1 - ((hasElse || hasReturn) ? 1 : 0));
                    addr = targetAddr;
                    ip = targetIns;
                    ConvertOutput onFalse = new ConvertOutput(new Stack<TreeItem>(), new ArrayList<TreeItem>());
                    if (hasElse) {
                        int finalAddr = targetAddr + code.get(targetIns - 1).operands[0];
                        int finalIns = adr2pos(finalAddr);
                        onFalse = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), scopeStack, abc, constants, method_info, body, targetIns, finalIns - 1);
                        addr = finalAddr;
                        ip = finalIns;
                    }
                    if ((onTrue.stack.size() > 0) && (onFalse != null) && (onFalse.stack.size() > 0)) {
                        stack.add(new TernarOpTreeItem(ins, condition, onTrue.stack.pop(), onFalse.stack.pop()));
                    } else {
                        output.add(new IfTreeItem(ins, condition, onTrue.output, onFalse.output));
                    }

                } else if ((ins.definition instanceof ReturnValueIns) || (ins.definition instanceof ReturnVoidIns) || (ins.definition instanceof ThrowIns)) {
                    ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc);
                    ip = end + 1;
                    break;
                } else {
                    ins.definition.translate(isStatic, classIndex, localRegs, stack, scopeStack, constants, ins, method_info, output, body, abc);

                    addr += ins.getBytes().length;
                    ip++;
                }

            }
            if (debugMode)
                System.out.println("CLOSE SubSource:" + start + "-" + end + " " + code.get(start).toString() + " to " + code.get(end).toString());

            return new ConvertOutput(stack, output);
        } catch (ConvertException cex) {
            throw cex;
        } catch (Exception ex) {
            if (ex instanceof UnknownJumpException) {
                throw (UnknownJumpException) ex;
            }
            throw new ConvertException(ex.toString(), ip);
        }
    }

    public String tabString(int len) {
        String ret = "";
        for (int i = 0; i < len; i++) {
            ret += ABC.IDENT_STRING;
        }
        return ret;
    }

    public String toSource(boolean isStatic, int classIndex, ABC abc, ConstantPool constants, MethodInfo method_info[], MethodBody body) {
        return toSource(isStatic, classIndex, abc, constants, method_info, body, false);
    }

    public List<TreeItem> toTree(boolean isStatic, int classIndex, ABC abc, ConstantPool constants, MethodInfo method_info[], MethodBody body) {
        toSourceCount = 0;
        loopList = new ArrayList<Loop>();
        unknownJumps = new ArrayList<Integer>();
        parsedExceptions = new ArrayList<ABCException>();
        finallyJumps = new ArrayList<Integer>();
        HashMap<Integer, TreeItem> localRegs = new HashMap<Integer, TreeItem>();
        try {
            return toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), new Stack<TreeItem>(), abc, constants, method_info, body, 0, code.size() - 1).output;
        } catch (ConvertException ex) {
            return new ArrayList<TreeItem>();
        }
    }

    public String toSource(boolean isStatic, int classIndex, ABC abc, ConstantPool constants, MethodInfo method_info[], MethodBody body, boolean hilighted) {
        toSourceCount = 0;
        loopList = new ArrayList<Loop>();
        unknownJumps = new ArrayList<Integer>();
        finallyJumps = new ArrayList<Integer>();
        parsedExceptions = new ArrayList<ABCException>();
        List<TreeItem> list;
        String s = "";
        try {
            HashMap<Integer, TreeItem> localRegs = new HashMap<Integer, TreeItem>();
            list = toSource(isStatic, classIndex, localRegs, new Stack<TreeItem>(), new Stack<TreeItem>(), abc, constants, method_info, body, 0, code.size() - 1).output;
            s = listToString(list, constants);
        } catch (Exception ex) {
            ex.printStackTrace();
            s = "Convert error - " + ex.toString();
            return s;
        }

        String parts[] = s.split("\r\n");
        String sub = "";
        int level = 0;
        for (int t = 0; t < body.traits.traits.length; t++) {
            sub += body.traits.traits[t].convert(constants, method_info) + ";\r\n";
        }
        try {
            Stack<String> loopStack = new Stack<String>();
            for (int p = 0; p < parts.length; p++) {
                String stripped = Highlighting.stripHilights(parts[p]);
                if (stripped.endsWith(":") && (!stripped.startsWith("case ")) && (!stripped.equals("default:"))) {
                    loopStack.add(stripped.substring(0, stripped.length() - 1));
                }
                if (stripped.startsWith("break ")) {
                    if (stripped.equals("break " + loopStack.peek() + ";")) {
                        parts[p] = parts[p].replace(" " + loopStack.peek(), "");
                    }
                }
                if (stripped.startsWith("continue ")) {
                    if (loopStack.size() > 0) {
                        if (stripped.equals("continue " + loopStack.peek() + ";")) {
                            parts[p] = parts[p].replace(" " + loopStack.peek(), "");
                        }
                    }
                }
                if (stripped.startsWith(":")) {
                    loopStack.pop();
                }
            }
        } catch (Exception ex) {
        }
        for (int p = 0; p < parts.length; p++) {
            String strippedP = Highlighting.stripHilights(parts[p]);
            if (strippedP.endsWith(":") && (!strippedP.startsWith("case ")) && (!strippedP.equals("default:"))) {
                String loopname = strippedP.substring(0, strippedP.length() - 1);
                boolean dorefer = false;
                for (int q = p + 1; q < parts.length; q++) {
                    String strippedQ = Highlighting.stripHilights(parts[q]);
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
            if (strippedP.equals(IDENTOPEN)) {
                level++;
            } else if (strippedP.equals(IDENTCLOSE)) {
                level--;
            } else if (strippedP.equals("{")) {
                level++;
                sub += tabString(level) + parts[p] + "\r\n";
                level++;
            } else if (strippedP.equals("}")) {
                level--;
                sub += tabString(level) + parts[p] + "\r\n";
                level--;
            } else {
                sub += tabString(level) + parts[p] + "\r\n";
            }
        }
        if (!hilighted) {
            sub = Highlighting.stripHilights(sub);
        }
        return sub;
    }

    public static void main(String[] args) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("src/asdec/abc/avm2/AVM2Code.java");
            byte[] data = new byte[fis.available()];
            fis.read(data);

            String content = new String(data);
            Pattern partPat = Pattern.compile("private static InstructionDefinition instructionSet(.*)endoflist", Pattern.MULTILINE | Pattern.DOTALL);
            Matcher m = partPat.matcher(content);
            if (m.find()) {
                System.out.println("1 found");
                content = m.group(1);
                System.out.println(content);
                Pattern part2Pat = Pattern.compile("new InstructionDefinition(\\([^\\)]*\"([^\"]*)\"[^\\)]*\\))\\{(.*)\\},", Pattern.MULTILINE | Pattern.DOTALL);
                m = part2Pat.matcher(content);
                while (m.find()) {
                    System.out.println("2 found");
                    String superCall = m.group(1);
                    String name = m.group(2);
                    String methods = m.group(3);
                    FileOutputStream fos = new FileOutputStream("src/asdec/abc/avm2/instructions/generated/" + name + "Ins.java");
                    String out = "public class " + name + "Ins extends InstructionDefinition {\r\n public " + name + "Ins(){\r\nsuper" + superCall + ";\r\n}" + methods + "}";
                    fos.write(out.getBytes());
                    fos.close();
                }
            }
        } catch (IOException ex) {
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(AVM2Code.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void removeInstruction(int pos, MethodBody body) {
        if ((pos < 0) || (pos >= code.size())) {
            throw new IndexOutOfBoundsException();
        }
        int byteCount = code.get(pos).getBytes().length;
        long remOffset = code.get(pos).offset;
        for (int i = pos + 1; i < code.size(); i++) {
            code.get(i).offset -= byteCount;
        }

        for (ABCException ex : body.exceptions) {
            if (ex.start > remOffset) {
                ex.start -= byteCount;
            }
            if (ex.end > remOffset) {
                ex.start -= byteCount;
            }
            if (ex.target > remOffset) {
                ex.start -= byteCount;
            }
        }


        for (int i = 0; i < pos; i++) {
            if (code.get(i).definition instanceof LookupSwitchIns) {
                long target = code.get(i).offset + code.get(i).operands[0];
                if (target > remOffset) {
                    code.get(i).operands[0] -= byteCount;
                }
                for (int k = 2; k < code.get(i).operands.length; k++) {
                    target = code.get(i).offset + code.get(i).operands[k];
                    if (target > remOffset) {
                        code.get(i).operands[k] -= byteCount;
                    }
                }
            } else {
                for (int j = 0; j < code.get(i).definition.operands.length; j++) {
                    if (code.get(i).definition.operands[j] == AVM2Code.DAT_OFFSET) {
                        long target = code.get(i).offset + code.get(i).getBytes().length + code.get(i).operands[j];
                        if (target > remOffset) {
                            code.get(i).operands[j] -= byteCount;
                        }
                    }
                }
            }
        }
        for (int i = pos + 1; i < code.size(); i++) {
            if (code.get(i).definition instanceof LookupSwitchIns) {
                long target = code.get(i).offset + code.get(i).operands[0];
                if (target < remOffset) {
                    code.get(i).operands[0] += byteCount;
                }
                for (int k = 2; k < code.get(i).operands.length; k++) {
                    target = code.get(i).offset + code.get(i).operands[k];
                    if (target < remOffset) {
                        code.get(i).operands[k] += byteCount;
                    }
                }
            } else {
                for (int j = 0; j < code.get(i).definition.operands.length; j++) {
                    if (code.get(i).definition.operands[j] == AVM2Code.DAT_OFFSET) {
                        long target = code.get(i).offset + code.get(i).getBytes().length + code.get(i).operands[j];
                        if (target < remOffset) {
                            code.get(i).operands[j] += byteCount;
                        }
                    }
                }
            }
        }

        code.remove(pos);

    }

    public void insertInstruction(int pos, AVM2Instruction instruction) {
        if (pos < 0) {
            pos = 0;
        }
        if (pos > code.size()) {
            pos = code.size();
        }
        int byteCount = instruction.getBytes().length;
        if (pos == code.size()) {
            instruction.offset = code.get(pos - 1).offset + code.get(pos - 1).getBytes().length;
        } else {
            instruction.offset = code.get(pos).offset;
        }

        for (int i = 0; i < pos; i++) {
            for (int j = 0; j < code.get(i).definition.operands.length; j++) {
                if (code.get(i).definition.operands[j] == AVM2Code.DAT_OFFSET) {
                    long target = code.get(i).offset + code.get(i).getBytes().length + code.get(i).operands[j];
                    if (target >= instruction.offset) {
                        code.get(i).operands[j] += byteCount;
                    }
                }
            }
        }
        for (int i = pos; i < code.size(); i++) {
            for (int j = 0; j < code.get(i).definition.operands.length; j++) {
                if (code.get(i).definition.operands[j] == AVM2Code.DAT_OFFSET) {
                    long target = code.get(i).offset + code.get(i).getBytes().length + code.get(i).operands[j];
                    if (target < instruction.offset) {
                        code.get(i).operands[j] -= byteCount;
                    }
                }
            }
        }

        for (int i = pos + 1; i < code.size(); i++) {
            code.get(i).offset += byteCount;
        }
        code.add(pos, instruction);
    }

    private void removeFreeBlocks(ConstantPool constants, MethodBody body) throws ConvertException {
        List<Long> offsets = new ArrayList<Long>();
        for (AVM2Instruction ins : code) {
            offsets.addAll(ins.getOffsets());
        }
        for (ABCException ex : body.exceptions) {
            offsets.add((long) ex.start);
            offsets.add((long) ex.end);
            offsets.add((long) ex.target);
        }

        int clearedCount = 0;
        loopip:
        for (int ip = 0; ip < code.size(); ip++) {
            AVM2Instruction ins = code.get(ip);
            if (ins.definition instanceof JumpIns) {
                int secondAddr = pos2adr(ip + 1);
                int jumpAddr = secondAddr + ins.operands[0];
                int jumpPos = adr2pos(jumpAddr);
                if (jumpPos <= ip) {
                    continue;
                }
                if (jumpPos > code.size()) {
                    continue;
                }
                for (int k = ip + 1; k < jumpPos; k++) {
                    if (offsets.contains((long) pos2adr(k))) {
                        continue loopip;
                    }
                }
                for (int k = ip; k < jumpPos; k++) {
                    removeInstruction(ip, body);
                    clearedCount++;
                }
                offsets.clear();
                for (AVM2Instruction ins2 : code) {
                    offsets.addAll(ins2.getOffsets());
                }
                for (ABCException ex : body.exceptions) {
                    offsets.add((long) ex.start);
                    offsets.add((long) ex.end);
                    offsets.add((long) ex.target);
                }
                ip--;
                //ip=jumpPos;
            }
        }
        if (clearedCount > 0) {
            //System.out.println("Cleared " + clearedCount + " lines of code TO:");
            //System.out.println(toASMSource(constants));
            //System.exit(1);
        }
    }

    public void clearSecureSWF(ConstantPool constants, MethodBody body) throws ConvertException {
        if (code.size() > 4) {
            AVM2Instruction first = code.get(0);
            AVM2Instruction second = code.get(1);
            boolean firstValue = false;
            boolean secondValue = false;
            boolean isSecure = true;
            if (first.definition instanceof PushFalseIns) {
                firstValue = false;
            } else if (first.definition instanceof PushTrueIns) {
                firstValue = true;
            } else {
                isSecure = false;
            }
            if (isSecure) {
                if (second.definition instanceof PushFalseIns) {
                    secondValue = false;
                } else if (second.definition instanceof PushTrueIns) {
                    secondValue = true;
                } else {
                    isSecure = false;
                }
                if (isSecure) {
                    AVM2Instruction third = code.get(2);
                    int pos = 2;
                    if (third.definition instanceof SwapIns) {
                        pos++;
                        boolean dup = firstValue;
                        firstValue = secondValue;
                        secondValue = dup;
                    }
                    AVM2Instruction firstSet = code.get(pos);
                    AVM2Instruction secondSet = code.get(pos + 1);
                    int trueIndex = -1;
                    int falseIndex = -1;
                    if (firstSet.definition instanceof SetLocalTypeIns) {
                        if (secondValue == true) {
                            trueIndex = ((SetLocalTypeIns) firstSet.definition).getRegisterId(firstSet);
                        }
                        if (secondValue == false) {
                            falseIndex = ((SetLocalTypeIns) firstSet.definition).getRegisterId(firstSet);
                        }
                    } else {
                        isSecure = false;
                    }
                    if (isSecure) {
                        if (secondSet.definition instanceof SetLocalTypeIns) {
                            if (firstValue == true) {
                                trueIndex = ((SetLocalTypeIns) secondSet.definition).getRegisterId(firstSet);
                            }
                            if (firstValue == false) {
                                falseIndex = ((SetLocalTypeIns) secondSet.definition).getRegisterId(firstSet);
                            }

                            //Yes, secure
                            pos += 2;
                            for (int i = 0; i < pos; i++) {
                                code.get(i).ignored = true;
                                //removeInstruction(0, body);
                            }
                            System.out.println("trueIndex:" + trueIndex);
                            System.out.println("falseIndex:" + falseIndex);
                            boolean found = false;
                            do {
                                found = false;
                                for (int ip = pos; ip < code.size(); ip++) {
                                    if (code.get(ip).ignored) continue;
                                    if (code.get(ip).definition instanceof GetLocalTypeIns) {
                                        int regIndex = ((GetLocalTypeIns) code.get(ip).definition).getRegisterId(code.get(ip));
                                        if ((regIndex == trueIndex) || (regIndex == falseIndex)) {
                                            found = true;
                                            Stack<Boolean> myStack = new Stack<Boolean>();
                                            do {
                                                AVM2Instruction ins = code.get(ip);
                                                if (ins.ignored) {
                                                    ip++;
                                                    continue;
                                                } else if (ins.definition instanceof GetLocalTypeIns) {
                                                    regIndex = ((GetLocalTypeIns) ins.definition).getRegisterId(ins);
                                                    if (regIndex == trueIndex) myStack.push(true);
                                                    if (regIndex == falseIndex) myStack.push(false);
                                                    ip++;
                                                    ins.ignored = true;
                                                } else if (ins.definition instanceof DupIns) {
                                                    Boolean b = myStack.pop();
                                                    myStack.push(b);
                                                    myStack.push(b);
                                                    ins.ignored = true;
                                                    ip++;
                                                } else if (ins.definition instanceof PopIns) {
                                                    myStack.pop();
                                                    ins.ignored = true;
                                                    ip++;
                                                } else if (ins.definition instanceof IfTrueIns) {
                                                    System.out.println("iftrue found");
                                                    boolean val = myStack.pop();
                                                    if (val) {
                                                        code.get(ip).definition = new JumpIns();
                                                        System.out.println("changed to jump");
                                                        ip = adr2pos(pos2adr(ip + 1) + code.get(ip).operands[0]);
                                                    } else {
                                                        code.get(ip).ignored = true;
                                                        ip++;
                                                    }
                                                } else if (ins.definition instanceof IfFalseIns) {
                                                    boolean val = myStack.pop();
                                                    if (!val) {
                                                        code.get(ip).definition = new JumpIns();
                                                        ip = adr2pos(pos2adr(ip + 1) + code.get(ip).operands[0]);
                                                    } else {
                                                        code.get(ip).ignored = true;
                                                        ip++;
                                                    }
                                                } else if (ins.definition instanceof JumpIns) {
                                                    ip = adr2pos(pos2adr(ip + 1) + code.get(ip).operands[0]);
                                                } else {

                                                }

                                            } while (myStack.size() > 0);

                                            /*for(int rem=code.size();rem>=0;rem--){
                                                if(code.get(rem).ignored){
                                                    code.remove(rem);
                                                }
                                            } */
                                            break;
                                        }

                                    }
                                }
                            }
                            while (found);
                        } else {
                            isSecure = false;
                        }
                    }

                }
            }
        }
    }

    public void clearCode(ConstantPool constants, MethodBody body) throws ConvertException {

        if (code.size() > 3) {
            if (code.get(0).definition instanceof PushByteIns) {
                if (code.get(1).definition instanceof PushByteIns) {
                    if (code.get(2).definition instanceof IfNeIns) {
                        if (code.get(0).operands[0] != code.get(1).operands[0]) {
                            int targetAddr = pos2adr(2) + code.get(2).getBytes().length + code.get(2).operands[0];
                            int targetPos = adr2pos(targetAddr);
                            for (int i = 0; i < targetPos; i++) {
                                removeInstruction(0, body);
                            }
                        }
                    }
                }
            }
        }

        removeFreeBlocks(constants, body);
        //clearSecureSWF(constants, body);


    }
}
