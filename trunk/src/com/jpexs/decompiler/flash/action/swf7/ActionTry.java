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
package com.jpexs.decompiler.flash.action.swf7;

import com.jpexs.decompiler.flash.ReReadableInputStream;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.*;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.treemodel.DirectValueTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.TreeItem;
import com.jpexs.decompiler.flash.action.treemodel.clauses.TryTreeItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionTry extends Action implements GraphSourceItemContainer {

    public boolean catchInRegisterFlag;
    public boolean finallyBlockFlag;
    public boolean catchBlockFlag;
    public String catchName;
    public int catchRegister;
    long trySize;
    long catchSize;
    long finallySize;
    private int version;

    public ActionTry(int actionLength, SWFInputStream sis, ReReadableInputStream rri, int version) throws IOException {
        super(0x8F, actionLength);
        long startPos = sis.getPos();
        sis.readUB(5);
        this.version = version;
        catchInRegisterFlag = sis.readUB(1) == 1;
        finallyBlockFlag = sis.readUB(1) == 1;
        catchBlockFlag = sis.readUB(1) == 1;
        trySize = sis.readUI16();
        catchSize = sis.readUI16();
        finallySize = sis.readUI16();
        if (catchInRegisterFlag) {
            catchRegister = sis.readUI8();
        } else {
            catchName = sis.readString();
        }
    }

    @Override
    public void setAddress(long address, int version, boolean recursive) {
        super.setAddress(address, version, recursive);
    }

    @Override
    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeUB(5, 0);
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
            sos.close();
        } catch (IOException e) {
        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    public ActionTry(long containerSWFPos, boolean ignoreNops, List<Label> labels, long address, FlasmLexer lexer, List<String> constantPool, int version) throws IOException, ParseException {
        super(0x8F, 0);
        this.version = version;

        ParsedSymbol symb = lexer.yylex();
        if (symb.type == ParsedSymbol.TYPE_STRING) {
            catchInRegisterFlag = false;
            catchName = (String) symb.value;
        } else if (symb.type == ParsedSymbol.TYPE_REGISTER) {
            catchRegister = (Integer) symb.value;
            catchInRegisterFlag = true;
        } else if (symb.type == ParsedSymbol.TYPE_BLOCK_START) {
            return;
        } else {
            throw new ParseException("Unknown symbol after Try", lexer.yyline());
        }
        lexBlockOpen(lexer);
    }

    @Override
    public String getASMSourceBetween(int pos) {
        String ret = "";
        if (pos == 0) {
            if (catchBlockFlag) {
                ret += "Catch";
                ret += " {\r\n";
                return ret;
            }
            if (finallyBlockFlag) {
                ret += "Finally {\r\n";
                return ret;
            }
        }
        if (pos == 1) {
            if (catchBlockFlag && finallyBlockFlag) {
                ret += "Finally {\r\n";
                return ret;
            }
        }
        return ret;
    }

    @Override
    public String getASMSource(List<GraphSourceItem> container, List<Long> knownAddreses, List<String> constantPool, int version, boolean hex) {
        String ret = "";
        ret += "Try ";
        if (catchBlockFlag) {
            if (catchInRegisterFlag) {
                ret += "register" + catchRegister;
            } else {
                ret += "\"" + Helper.escapeString(catchName) + "\"";
            }
            ret += " ";
        }
        ret += "{";
        return ret;
    }

    @Override
    public List<Long> getAllRefs(int version) {
        List<Long> ret = new ArrayList<Long>();
        return ret;
    }

    @Override
    public List<Action> getAllIfsOrJumps() {
        List<Action> ret = new ArrayList<Action>();
        return ret;
    }

    @Override
    public long getHeaderSize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeUB(5, 0);
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
            /*sos.write(tryBodyBytes);
             sos.write(catchBodyBytes);
             sos.write(finallyBodyBytes);*/
            sos.close();
        } catch (IOException e) {
        }
        return surroundWithAction(baos.toByteArray(), version).length;
    }

    @Override
    public List<Long> getContainerSizes() {
        List<Long> ret = new ArrayList<Long>();
        ret.add(trySize);
        ret.add(catchSize);
        ret.add(finallySize);
        return ret;
    }

    @Override
    public boolean parseDivision(int pos, long addr, FlasmLexer lexer) {
        try {
            ParsedSymbol symb = lexer.yylex();
            //catchBlockFlag = false;
            if (symb.type == ParsedSymbol.TYPE_INSTRUCTION_NAME) {
                if (((String) symb.value).toLowerCase().equals("catch")) {
                    trySize = addr - getAddress() - getHeaderSize();
                    catchBlockFlag = true;
                    lexBlockOpen(lexer);
                    return true;
                }
                if (symb.type == ParsedSymbol.TYPE_INSTRUCTION_NAME) {
                    if (((String) symb.value).toLowerCase().equals("finally")) {
                        if (catchBlockFlag) {
                            catchSize = addr - getAddress() - getHeaderSize() - trySize;
                        } else {
                            trySize = addr - getAddress() - getHeaderSize();
                        }
                        finallyBlockFlag = true;
                        lexBlockOpen(lexer);
                        return true;
                    } else {
                        //finallyBlockFlag = false;
                        lexer.yypushback(lexer.yylength());
                    }
                } else {
                    //finallyBlockFlag = false;
                    lexer.yypushback(lexer.yylength());
                }
            } else {
                lexer.yypushback(lexer.yylength());
            }
        } catch (Exception ex) {
        }

        if (finallyBlockFlag) {
            finallySize = addr - getAddress() - getHeaderSize() - trySize - catchSize;
        } else if (catchBlockFlag) {
            catchSize = addr - getAddress() - getHeaderSize() - trySize;
        }
        lexer.yybegin(0);
        return false;
    }

    @Override
    public void translateContainer(List<List<GraphTargetItem>> contents, Stack<GraphTargetItem> stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        List<GraphTargetItem> tryCommands = contents.get(0);
        TreeItem catchName;
        if (catchInRegisterFlag) {
            catchName = new DirectValueTreeItem(this, -1, new RegisterNumber(this.catchRegister), new ArrayList<String>());
        } else {
            catchName = new DirectValueTreeItem(this, -1, this.catchName, new ArrayList<String>());
        }
        List<GraphTargetItem> catchExceptions = new ArrayList<GraphTargetItem>();
        if (catchBlockFlag) {
            catchExceptions.add(catchName);
        }
        List<List<GraphTargetItem>> catchCommands = new ArrayList<List<GraphTargetItem>>();
        if (catchBlockFlag) {
            catchCommands.add(contents.get(1));
        }
        List<GraphTargetItem> finallyCommands = contents.get(2);
        output.add(new TryTreeItem(tryCommands, catchExceptions, catchCommands, finallyCommands));


    }

    @Override
    public String toString() {
        return "Try";
    }
    
    @Override
    public HashMap<Integer, String> getRegNames(){
        return new HashMap<Integer, String>();
    }
}
