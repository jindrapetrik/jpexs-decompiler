package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.ASMParser;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.Label;
import com.jpexs.asdec.action.parser.ParseException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ActionWith extends Action {
    public List<Action> actions;
    public int size;

    public ActionWith(SWFInputStream sis, int version) throws IOException {
        super(0x94, 2);
        size = sis.readUI16();
        actions = (new SWFInputStream(new ByteArrayInputStream(sis.readBytes(size)), version)).readActionList();
    }

    public ActionWith(List<Label> labels, long address, FlasmLexer lexer, List<String> constantPool, int version) throws IOException, ParseException {
        super(0x94, 2);
        lexBlockOpen(lexer);
        actions = ASMParser.parse(labels, address + 5, lexer, constantPool, version);
    }

    @Override
    public void setAddress(long address, int version) {
        super.setAddress(address, version);
        Action.setActionsAddresses(actions, address + 5, version);
    }

    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            byte codeBytes[] = Action.actionsToBytes(actions, false, version);
            sos.writeUI16(codeBytes.length);
            sos.close();
            baos2.write(surroundWithAction(baos.toByteArray(), version));
            baos2.write(codeBytes);
        } catch (IOException e) {

        }
        return baos2.toByteArray();
    }

    @Override
    public String getASMSource(List<Long> knownAddreses, List<String> constantPool, int version) {
        return "With {\r\n" + Action.actionsToString(actions, knownAddreses, constantPool, version) + "}";
    }

    @Override
    public List<Long> getAllRefs(int version) {
        return Action.getActionsAllRefs(actions, version);
    }

    @Override
    public List<Action> getAllIfsOrJumps() {
        return Action.getActionsAllIfsOrJumps(actions);
    }
}