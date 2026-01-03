/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.ActionScript3Parser;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML.
 *
 * @author JPEXS
 */
public class XMLAVM2Item extends AVM2Item {

    /**
     * Parts of XML.
     */
    public List<GraphTargetItem> parts;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param parts Parts
     */
    public XMLAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, List<GraphTargetItem> parts) {
        super(instruction, lineStartIns, NOPRECEDENCE);
        this.parts = parts;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visitAll(parts);
    }

    private String handleSingleXml(String s, Reference<Boolean> inAttributeRef, Reference<Boolean> inOpeningTagRef) {

        String identRegexp = "[:A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][\\-\\.0-9:A-Z_a-z\\u00B7\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0300-\\u036F\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u203F-\\u2040\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD]*";

        StringBuilder writer = new StringBuilder();

        for (int j = 0; j < s.length(); j++) {
            char c = s.charAt(j);
            switch (c) {
                case '"':
                    if (inOpeningTagRef.getVal()) {
                        inAttributeRef.setVal(!inAttributeRef.getVal());
                    }
                    writer.append(c);
                    break;
                case '<':
                    Pattern p = Pattern.compile("^(" + identRegexp + ").*", Pattern.MULTILINE | Pattern.DOTALL);
                    String sub = s.substring(j + 1);
                    Matcher m = p.matcher(sub);
                    writer.append(c);
                    if (m.matches()) {
                        inOpeningTagRef.setVal(true);
                        String tag = m.group(1);
                        writer.append(tag);
                        j += tag.length();
                    }
                    break;
                case '>':
                    if (inOpeningTagRef.getVal()) {
                        inOpeningTagRef.setVal(false);
                    }
                    writer.append(c);
                    break;
                default:
                    if (inAttributeRef.getVal()) {
                        switch (c) {
                            case '\r':
                                writer.append("&#13;");
                                break;
                            case '\n':
                                writer.append("&#10;");
                                break;
                            case '\t':
                                writer.append("&#9;");
                                break;
                            default:
                                writer.append(c);
                        }
                    } else {
                        writer.append(c);
                    }
            }
        }
        return writer.toString();
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {

        Reference<Boolean> inAttributeRef = new Reference<>(false);
        Reference<Boolean> inOpeningTagRef = new Reference<>(false);

        if (parts.size() == 1 && parts.get(0) instanceof StringAVM2Item) {
            String s = ((StringAVM2Item) parts.get(0)).getValue();
            s = handleSingleXml(s, inAttributeRef, inOpeningTagRef);
            boolean validXml = true;
            try {
                ActionScript3Parser par = new ActionScript3Parser(new AbcIndexing());
                if (!par.checkBasicXmlOnly(s)) {
                    validXml = false;
                }
            } catch (Throwable ex) {
                validXml = false;
            }

            if (!validXml) {
                writer.append("new XML");
                writer.spaceBeforeCallParenthesis(1);
                writer.append("(\"").append(Helper.escapeActionScriptString(s)).append("\")");
                return writer;
            }
            writer.spaceBeforeCallParenthesis(precedence);
            writer.append(s);
            return writer;
        }

        for (int i = 0; i < parts.size(); i++) {
            GraphTargetItem part = parts.get(i);
            GraphTargetItem partBefore = i > 0 ? parts.get(i - 1) : null;
            GraphTargetItem partAfter = i < parts.size() - 1 ? parts.get(i + 1) : null;

            /*
            Older versions of Flex allow inserting escape sequences like \r, \n, \t
            into attributes. Air does not allow this. We handle this by converting it into XML entities.
             */
            if (part instanceof StringAVM2Item) {
                String s = ((StringAVM2Item) part).getValue();

                if (partAfter instanceof EscapeXAttrAVM2Item) {
                    if (s.endsWith("\"")) {
                        s = s.substring(0, s.length() - 1);
                    }
                }
                if (partBefore instanceof EscapeXAttrAVM2Item) {
                    if (s.startsWith("\"")) {
                        s = s.substring(1);
                    }
                }

                writer.append(handleSingleXml(s, inAttributeRef, inOpeningTagRef));
            } else if ((part instanceof EscapeXElemAVM2Item) || (part instanceof EscapeXAttrAVM2Item)) {
                part.toString(writer, localData);
            } else {
                writer.append("{");
                part.appendTo(writer, localData);
                writer.append("}");
            }
        }
        return writer;
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem(DottedChain.XML);
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.parts);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final XMLAVM2Item other = (XMLAVM2Item) obj;
        if (!Objects.equals(this.parts, other.parts)) {
            return false;
        }
        return true;
    }

}
