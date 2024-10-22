/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action.swf7;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.model.CastOpActionItem;
import com.jpexs.decompiler.flash.action.model.DefineLocalActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.TemporaryRegister;
import com.jpexs.decompiler.flash.action.model.clauses.TryActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.ExitItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Try action - Try block with catch and finally blocks.
 *
 * @author JPEXS
 */
@SWFVersion(from = 7)
public class ActionTry extends Action implements GraphSourceItemContainer {

    /**
     * Reserved
     */
    @Reserved
    public int reserved;

    /**
     * Catch in register flag
     */
    public boolean catchInRegisterFlag;

    /**
     * Finally block flag
     */
    public boolean finallyBlockFlag;

    /**
     * Catch block flag
     */
    public boolean catchBlockFlag;

    /**
     * Catch name
     */
    public String catchName;

    /**
     * Catch register
     */
    public int catchRegister;

    /**
     * Try size
     */
    long trySize;

    /**
     * Catch size
     */
    long catchSize;

    /**
     * Finally size
     */
    long finallySize;

    /**
     * Version
     */
    private final int version;

    @Override
    public boolean execute(LocalDataArea lda) {
        //FIXME!!!
        return false;
    }

    /**
     * Constructor.
     *
     * @param catchInRegisterFlag Catch in register flag
     * @param finallyBlockFlag Finally block flag
     * @param catchBlockFlag Catch block flag
     * @param catchName Catch name
     * @param catchRegister Catch register
     * @param trySize Try size
     * @param catchSize Catch size
     * @param finallySize Finally size
     * @param version Version
     * @param charset Charset
     */
    public ActionTry(boolean catchInRegisterFlag, boolean finallyBlockFlag, boolean catchBlockFlag, String catchName, int catchRegister, long trySize, long catchSize, long finallySize, int version, String charset) {
        super(0x8F, 0, charset);
        this.catchInRegisterFlag = catchInRegisterFlag;
        this.finallyBlockFlag = finallyBlockFlag;
        this.catchBlockFlag = catchBlockFlag;
        this.catchName = catchName;
        this.catchRegister = catchRegister;
        this.trySize = trySize;
        this.catchSize = catchSize;
        this.finallySize = finallySize;
        this.version = version;
    }

    /**
     * Constructor.
     *
     * @param actionLength Action length
     * @param sis SWF input stream
     * @param version Version
     * @throws IOException On I/O error
     */
    public ActionTry(int actionLength, SWFInputStream sis, int version) throws IOException {
        super(0x8F, actionLength, sis.getCharset());
        long startPos = sis.getPos();
        this.version = version;
        reserved = (int) sis.readUB(5, "reserved");
        catchInRegisterFlag = sis.readUB(1, "catchInRegisterFlag") == 1;
        finallyBlockFlag = sis.readUB(1, "finallyBlockFlag") == 1;
        catchBlockFlag = sis.readUB(1, "catchBlockFlag") == 1;
        trySize = sis.readUI16("trySize");
        catchSize = sis.readUI16("catchSize");
        finallySize = sis.readUI16("finallySize");
        if (catchInRegisterFlag) {
            catchRegister = sis.readUI8("catchRegister");
        } else {
            catchName = sis.readString("catchName");
        }
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.writeUB(5, reserved);
        sos.writeUB(1, catchInRegisterFlag ? 1 : 0);
        sos.writeUB(1, finallyBlockFlag ? 1 : 0);
        sos.writeUB(1, catchBlockFlag ? 1 : 0);
        sos.writeUI16((int) trySize);
        sos.writeUI16((int) catchSize);
        sos.writeUI16((int) finallySize);
        if (catchInRegisterFlag) {
            sos.writeUI8(catchRegister);
        } else {
            sos.writeString(catchName == null ? "" : catchName);
        }
    }

    /**
     * Gets the length of action converted to bytes
     *
     * @return Length
     */
    @Override
    protected int getContentBytesLength() {
        int res = 8;
        if (!catchInRegisterFlag) {
            res += Utf8Helper.getBytesLength(catchName == null ? "" : catchName);
        }

        return res;
    }

    /**
     * Constructor.
     *
     * @param lexer Lexer
     * @param version Version
     * @param charset Charset
     * @throws IOException On I/O error
     * @throws ActionParseException On action parse error
     */
    public ActionTry(FlasmLexer lexer, int version, String charset) throws IOException, ActionParseException {
        super(0x8F, 0, charset);
        this.version = version;

        ASMParsedSymbol symb = lexer.lex();
        if (symb.type == ASMParsedSymbol.TYPE_STRING) {
            catchInRegisterFlag = false;
            catchName = (String) symb.value;
        } else if (symb.type == ASMParsedSymbol.TYPE_REGISTER) {
            catchRegister = ((RegisterNumber) symb.value).number;
            catchInRegisterFlag = true;
        } else if (symb.type == ASMParsedSymbol.TYPE_BLOCK_START) {
            return;
        } else {
            throw new ActionParseException("Unknown symbol after Try", lexer.yyline());
        }
        lexBlockOpen(lexer);
    }

    @Override
    public String getASMSourceBetween(int pos) {
        if (pos == 0) {
            if (catchBlockFlag) {
                return "Catch {\r\n";
            }
            if (finallyBlockFlag) {
                return "Finally {\r\n";
            }
        }
        if (pos == 1) {
            if (catchBlockFlag && finallyBlockFlag) {
                return "Finally {\r\n";
            }
        }
        return "";
    }

    @Override
    public String getASMSource(ActionList container, Set<Long> knownAddresses, ScriptExportMode exportMode) {
        StringBuilder ret = new StringBuilder();
        ret.append("Try ");
        if (catchBlockFlag) {
            if (catchInRegisterFlag) {
                ret.append("register").append(catchRegister);
            } else {
                ret.append("\"").append(Helper.escapeActionScriptString(catchName)).append("\"");
            }
            ret.append(" ");
        }
        ret.append("{");
        return ret.toString();
    }

    @Override
    public long getHeaderSize() {
        return getBytesLength();
    }

    /**
     * Gets the try size
     * @return Try size
     */
    public long getTrySize() {
        return trySize;
    }

    @Override
    public List<Long> getContainerSizes() {
        List<Long> ret = new ArrayList<>();
        ret.add(trySize);
        ret.add(catchSize);
        ret.add(finallySize);
        return ret;
    }

    @Override
    public void setContainerSize(int index, long size) {
        switch (index) {
            case 0:
                trySize = size;
                break;
            case 1:
                catchSize = size;
                break;
            case 2:
                finallySize = size;
                break;
            default:
                throw new IllegalArgumentException("Valid indexes are 0, 1, and 2.");
        }
    }

    @Override
    public boolean parseDivision(long size, FlasmLexer lexer) {
        try {
            ASMParsedSymbol symb = lexer.lex();
            //catchBlockFlag = false;
            if (symb.type == ASMParsedSymbol.TYPE_INSTRUCTION_NAME) {
                if (((String) symb.value).toLowerCase().equals("catch")) {
                    trySize = size - getHeaderSize();
                    catchBlockFlag = true;
                    lexBlockOpen(lexer);
                    return true;
                }
                if (symb.type == ASMParsedSymbol.TYPE_INSTRUCTION_NAME) {
                    if (((String) symb.value).toLowerCase().equals("finally")) {
                        if (catchBlockFlag) {
                            catchSize = size - getHeaderSize() - trySize;
                        } else {
                            trySize = size - getHeaderSize();
                        }
                        finallyBlockFlag = true;
                        lexBlockOpen(lexer);
                        return true;
                    } else {
                        //finallyBlockFlag = false;
                        lexer.pushback(symb);
                    }
                } else {
                    //finallyBlockFlag = false;
                    lexer.pushback(symb);
                }
            } else {
                lexer.pushback(symb);
            }
        } catch (IOException | ActionParseException ex) {
            //ignored
        }

        if (finallyBlockFlag) {
            finallySize = size - getHeaderSize() - trySize - catchSize;
        } else if (catchBlockFlag) {
            catchSize = size - getHeaderSize() - trySize;
        }
        return false;
    }

    @Override
    public void translateContainer(List<List<GraphTargetItem>> contents, GraphSourceItem lineStartItem, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        List<GraphTargetItem> tryCommands = contents.get(0);

        List<GraphTargetItem> catchExceptionNames = new ArrayList<>();
        List<GraphTargetItem> catchExceptionTypes = new ArrayList<>();
        List<List<GraphTargetItem>> catchCommands = new ArrayList<>();

        if (catchBlockFlag) {
            List<GraphTargetItem> body = contents.get(1);
            if (catchInRegisterFlag) {
                if (body.size() >= 2) {
                    int pos = 0;
                    loopex:
                    while (body.get(pos) instanceof PushItem) {
                        PushItem pi = (PushItem) body.get(pos);
                        if (pi.value instanceof CastOpActionItem) {
                            CastOpActionItem co = (CastOpActionItem) pi.value;
                            if (((co.object instanceof DirectValueActionItem) && (((DirectValueActionItem) co.object).value instanceof RegisterNumber))
                                    || (co.object instanceof TemporaryRegister)) { //Can be in for in loop
                                int regNumber;

                                if (co.object instanceof TemporaryRegister) {
                                    TemporaryRegister tr = (TemporaryRegister) co.object;
                                    regNumber = tr.getRegId();
                                } else {
                                    RegisterNumber rn = (RegisterNumber) ((DirectValueActionItem) co.object).value;
                                    regNumber = rn.number;
                                }

                                if (regNumber == catchRegister) {
                                    catchExceptionTypes.add(co.constructor);
                                    if (body.get(pos + 1) instanceof IfItem) {
                                        IfItem ifi = (IfItem) body.get(pos + 1);
                                        List<GraphTargetItem> onFalse = ifi.onFalse;
                                        int onFalsePos = 0;
                                        if (ifi.onFalse.isEmpty() && !ifi.onTrue.isEmpty() && ((ifi.onTrue.get(ifi.onTrue.size() - 1) instanceof ExitItem)
                                                || (ifi.onTrue.get(ifi.onTrue.size() - 1) instanceof BreakItem)
                                                || (ifi.onTrue.get(ifi.onTrue.size() - 1) instanceof ContinueItem))) {
                                            onFalse = body;
                                            onFalsePos = pos + 2;
                                        }

                                        if (!ifi.onTrue.isEmpty() && (ifi.onTrue.get(0) instanceof DefineLocalActionItem)) {
                                            DefineLocalActionItem dl = (DefineLocalActionItem) ifi.onTrue.get(0);
                                            catchExceptionNames.add(dl.name);
                                            List<GraphTargetItem> catchBody = new ArrayList<>(ifi.onTrue);
                                            catchBody.remove(0);
                                            catchCommands.add(catchBody);
                                            if (onFalse.size() > onFalsePos) {
                                                if (onFalse.get(onFalsePos) instanceof PopItem) {
                                                    pos = onFalsePos + 1;
                                                    body = onFalse;
                                                    continue loopex;
                                                } else {
                                                    break;
                                                }
                                            } else {
                                                break;
                                            }
                                        } else if (onFalse.size() > onFalsePos && (onFalse.get(onFalsePos) instanceof DefineLocalActionItem)) {
                                            DefineLocalActionItem dl = (DefineLocalActionItem) onFalse.get(onFalsePos);
                                            catchExceptionNames.add(dl.name);

                                            List<GraphTargetItem> catchBody = new ArrayList<>();
                                            for (int i = onFalsePos; i < onFalse.size(); i++) {
                                                catchBody.add(onFalse.get(i));
                                            }
                                            catchBody.remove(0);
                                            catchCommands.add(catchBody);
                                            if (!ifi.onTrue.isEmpty()) {
                                                if (ifi.onTrue.get(0) instanceof PopItem) {
                                                    pos = 1;
                                                    body = ifi.onTrue;
                                                    continue loopex;
                                                }
                                            }
                                        }
                                    }
                                    break;
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }

                        } else {
                            break;
                        }
                    }
                    if (body.get(pos) instanceof DefineLocalActionItem) {
                        DefineLocalActionItem dl = (DefineLocalActionItem) body.get(pos);
                        catchExceptionNames.add(dl.name);
                        catchExceptionTypes.add(null);
                        List<GraphTargetItem> catchBody = new ArrayList<>(body);
                        catchBody.remove(0); //pop
                        catchBody.remove(0); //definelocal
                        catchCommands.add(catchBody);
                    }
                }
            } else {
                catchExceptionNames.add(new DirectValueActionItem(this, lineStartItem, -1, this.catchName, new ArrayList<>()));
                catchExceptionTypes.add(null);
                catchCommands.add(body);
            }
        }
        List<GraphTargetItem> finallyCommands = contents.get(2);
        output.add(new TryActionItem(tryCommands, catchExceptionNames, catchExceptionTypes, catchCommands, finallyCommands));
    }

    @Override
    public String toString() {
        return "Try";
    }

    @Override
    public HashMap<Integer, String> getRegNames() {
        return new HashMap<>();
    }

    @Override
    public String getName() {
        return null;
    }
}
