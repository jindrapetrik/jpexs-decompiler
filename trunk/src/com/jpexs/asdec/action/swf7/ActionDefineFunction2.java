package com.jpexs.asdec.action.swf7;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.ASMParser;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.Label;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.helpers.Helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActionDefineFunction2 extends Action {
    public String functionName;
    public List<String> paramNames = new ArrayList<String>();
    public List<Integer> paramRegisters = new ArrayList<Integer>();

    public boolean preloadParentFlag;
    public boolean preloadRootFlag;
    public boolean suppressSuperFlag;
    public boolean preloadSuperFlag;
    public boolean suppressArgumentsFlag;
    public boolean preloadArgumentsFlag;
    public boolean suppressThisFlag;
    public boolean preloadThisFlag;
    public boolean preloadGlobalFlag;
    public int registerCount;

    public int codeSize;
    public List<Action> code;

    public ActionDefineFunction2(int actionLength, SWFInputStream sis, int version) throws IOException {
        super(0x8E, actionLength);
        functionName = sis.readString();
        int numParams = sis.readUI16();
        registerCount = sis.readUI8();
        preloadParentFlag = sis.readUB(1) == 1;
        preloadRootFlag = sis.readUB(1) == 1;
        suppressSuperFlag = sis.readUB(1) == 1;
        preloadSuperFlag = sis.readUB(1) == 1;
        suppressArgumentsFlag = sis.readUB(1) == 1;
        preloadArgumentsFlag = sis.readUB(1) == 1;
        suppressThisFlag = sis.readUB(1) == 1;
        preloadThisFlag = sis.readUB(1) == 1;
        sis.readUB(7);//reserved
        preloadGlobalFlag = sis.readUB(1) == 1;
        for (int i = 0; i < numParams; i++) {
            paramRegisters.add(sis.readUI8());
            paramNames.add(sis.readString());
        }
        codeSize = sis.readUI16();

        code = (new SWFInputStream(new ByteArrayInputStream(sis.readBytes(codeSize)), version)).readActionList();
    }

    public ActionDefineFunction2(List<Label> labels, long address, FlasmLexer lexer, List<String> constantPool, int version) throws IOException, ParseException {
        super(0x8E, 0);
        functionName = lexString(lexer);
        int numParams = (int) lexLong(lexer);
        registerCount = (int) lexLong(lexer);
        preloadParentFlag = lexBoolean(lexer);
        preloadRootFlag = lexBoolean(lexer);
        suppressSuperFlag = lexBoolean(lexer);
        preloadSuperFlag = lexBoolean(lexer);
        suppressArgumentsFlag = lexBoolean(lexer);
        preloadArgumentsFlag = lexBoolean(lexer);
        suppressThisFlag = lexBoolean(lexer);
        preloadThisFlag = lexBoolean(lexer);
        preloadGlobalFlag = lexBoolean(lexer);
        for (int i = 0; i < numParams; i++) {
            paramRegisters.add((int) lexLong(lexer));
            paramNames.add(lexString(lexer));
        }
        lexBlockOpen(lexer);
        code = ASMParser.parse(labels, address + getPreLen(version), lexer, constantPool, version);
    }

    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        try {
            sos.writeString(functionName);
            sos.writeUI16(paramNames.size());
            sos.writeUI8(registerCount);
            sos.writeUB(1, preloadParentFlag ? 1 : 0);
            sos.writeUB(1, preloadRootFlag ? 1 : 0);
            sos.writeUB(1, suppressSuperFlag ? 1 : 0);
            sos.writeUB(1, preloadSuperFlag ? 1 : 0);
            sos.writeUB(1, suppressArgumentsFlag ? 1 : 0);
            sos.writeUB(1, preloadArgumentsFlag ? 1 : 0);
            sos.writeUB(1, suppressThisFlag ? 1 : 0);
            sos.writeUB(1, preloadThisFlag ? 1 : 0);
            sos.writeUB(7, 0);
            sos.writeUB(1, preloadGlobalFlag ? 1 : 0);
            for (int i = 0; i < paramNames.size(); i++) {
                sos.writeUI8(paramRegisters.get(i));
                sos.writeString(paramNames.get(i));
            }
            byte codeBytes[] = Action.actionsToBytes(code, false, version);
            sos.writeUI16(codeBytes.length);
            sos.close();


            baos2.write(surroundWithAction(baos.toByteArray(), version));
            baos2.write(codeBytes);
        } catch (IOException e) {

        }
        return baos2.toByteArray();
    }


    private long getPreLen(int version) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeString(functionName);
            sos.writeUI16(paramNames.size());
            sos.writeUI8(registerCount);
            sos.writeUB(1, preloadParentFlag ? 1 : 0);
            sos.writeUB(1, preloadRootFlag ? 1 : 0);
            sos.writeUB(1, suppressSuperFlag ? 1 : 0);
            sos.writeUB(1, preloadSuperFlag ? 1 : 0);
            sos.writeUB(1, suppressArgumentsFlag ? 1 : 0);
            sos.writeUB(1, preloadArgumentsFlag ? 1 : 0);
            sos.writeUB(1, suppressThisFlag ? 1 : 0);
            sos.writeUB(1, preloadThisFlag ? 1 : 0);
            sos.writeUB(7, 0);
            sos.writeUB(1, preloadGlobalFlag ? 1 : 0);
            for (int i = 0; i < paramNames.size(); i++) {
                sos.writeUI8(paramRegisters.get(i));
                sos.writeString(paramNames.get(i));
            }
            sos.writeUI16(0);
            sos.close();
        } catch (IOException e) {

        }
        return surroundWithAction(baos.toByteArray(), version).length;
    }

    @Override
    public void setAddress(long address, int version) {
        super.setAddress(address, version);
        Action.setActionsAddresses(code, address + getPreLen(version), version);
    }

    @Override
    public String getASMSource(List<Long> knownAddreses, List<String> constantPool, int version) {
        String paramStr = "";
        for (int i = 0; i < paramNames.size(); i++) {
            paramStr += paramRegisters.get(i) + " \"" + Helper.escapeString(paramNames.get(i)) + "\"";
            paramStr += " ";
        }

        return ("DefineFunction2 \"" + Helper.escapeString(functionName) + "\" " + paramRegisters.size() + " " + registerCount
                + " " + preloadParentFlag
                + " " + preloadRootFlag
                + " " + suppressSuperFlag
                + " " + preloadSuperFlag
                + " " + suppressArgumentsFlag
                + " " + preloadArgumentsFlag
                + " " + suppressThisFlag
                + " " + preloadThisFlag
                + " " + preloadGlobalFlag
        ).trim() + " " + paramStr + " {\r\n" + Action.actionsToString(code, knownAddreses, constantPool, version) + "}";
    }


    @Override
    public List<Long> getAllRefs(int version) {
        return Action.getActionsAllRefs(code, version);
    }

    @Override
    public List<Action> getAllIfsOrJumps() {
        return Action.getActionsAllIfsOrJumps(code);
    }
}