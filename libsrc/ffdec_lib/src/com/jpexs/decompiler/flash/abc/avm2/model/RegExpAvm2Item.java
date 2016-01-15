package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class RegExpAvm2Item extends AVM2Item {

    public String pattern;

    public String modifier;

    public RegExpAvm2Item(String pattern, String modifier, GraphSourceItem instruction, GraphSourceItem lineStartIns) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.pattern = pattern;
        this.modifier = modifier;
    }

    public static String escapeRegExpString(String s) {
        StringBuilder ret = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\n') {
                ret.append("\\n");
            } else if (c == '\r') {
                ret.append("\\r");
            } else if (c == '\t') {
                ret.append("\\t");
            } else if (c == '\b') {
                ret.append("\\b");
            } else if (c == '\f') {
                ret.append("\\f");
            } else if (c == '/') {
                ret.append("\\/");
            } else if (c < 32) {
                ret.append("\\x").append(Helper.byteToHex((byte) c));
            } else {
                ret.append(c);
            }
        }

        return ret.toString();
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (Configuration.useRegExprLiteral.get()) {
            writer.append("/");
            writer.append(escapeRegExpString(pattern));
            writer.append("/");
            writer.append(modifier);
        } else {
            writer.append("new RegExp(");
            writer.append("\"" + Helper.escapeActionScriptString(pattern) + "\"");
            if (!(modifier == null || modifier.isEmpty())) {
                writer.append(",");
                writer.append("\"" + modifier + "\"");
            }
            writer.append(")");
        }
        return writer;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem("RegExp");
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        ABC abc = g.abcIndex.getSelectedAbc();
        AVM2ConstantPool constants = abc.constants;
        boolean hasModifier = !(modifier == null || modifier.isEmpty());
        return toSourceMerge(localData, generator,
                ins(AVM2Instructions.GetLex, constants.getQnameId("RegExp", Namespace.KIND_PACKAGE, "", true)),
                new StringAVM2Item(null, null, pattern),
                hasModifier ? new StringAVM2Item(null, null, modifier) : null,
                ins(AVM2Instructions.Construct, hasModifier ? 2 : 1)
        );
    }
}
