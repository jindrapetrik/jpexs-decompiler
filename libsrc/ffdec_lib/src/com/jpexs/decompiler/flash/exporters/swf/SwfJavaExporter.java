/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.exporters.swf;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.LazyObject;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class SwfJavaExporter {

    private static final String javaIndentString = "    ";

    private static final String[] allowedSubTypes = new String[]{"List", "String", "ByteArrayRange", "RECT", "MATRIX", "CXFORMWITHALPHA",
        "CXFORM", "CLIPEVENTFLAGS", "CLIPACTIONRECORD", "CLIPACTIONS", "COLORMATRIXFILTER", "RGBA", "ARGB", "RGB",
        "CONVOLUTIONFILTER", "BLURFILTER", "DROPSHADOWFILTER", "GLOWFILTER", "BEVELFILTER", "GRADIENTGLOWFILTER", "GRADIENTBEVELFILTER",
        "FILTERLIST", "FILTER", "BUTTONRECORD", "BUTTONCONDACTION", "GRADRECORD", "GRADIENT", "FOCALGRADIENT", "FILLSTYLE",
        "FILLSTYLEARRAY", "LINESTYLE", "LINESTYLE2", "LINESTYLEARRAY", "SHAPERECORD", "SHAPE", "SHAPEWITHSTYLE", "SHAPERECORDS",
        "SOUNDINFO", "SOUNDENVELOPE", "GLYPHENTRY", "TEXTRECORD", "MORPHGRADRECORD", "MORPHGRADIENT", "MORPHFOCALGRADIENT",
        "MORPHFILLSTYLE", "MORPHFILLSTYLEARRAY", "MORPHLINESTYLE", "MORPHLINESTYLE2", "MORPHLINESTYLEARRAY", "KERNINGRECORD",
        "LANGCODE", "ZONERECORD", "ZONEDATA", "PIX15", "PIX24", "COLORMAPDATA", "BITMAPDATA", "ALPHABITMAPDATA", "ALPHACOLORMAPDATA"};

    public List<File> exportJavaCode(SWF swf, String outdir) throws IOException {
        final File file = new File(outdir + File.separator + Helper.makeFileName("SwfFile.java"));
        CodeFormatting codeFormatting = Configuration.getCodeFormatting();
        codeFormatting.indentString = javaIndentString;
        try (FileTextWriter writer = new FileTextWriter(codeFormatting, new FileOutputStream(file))) {
            exportJavaCode(swf, writer);
        }

        List<File> ret = new ArrayList<>();
        ret.add(file);
        return ret;
    }

    public void exportJavaCode(SWF swf, GraphTextWriter writer) throws IOException {
        Map<String, Integer> objectNames = new HashMap<>();
        writer.append("package com.jpexs.decompiler.flash.exporters.swf;").newLine();
        writer.newLine();
        writer.append("import com.jpexs.decompiler.flash.SWF;").newLine();
        writer.append("import com.jpexs.decompiler.flash.SWFCompression;").newLine();
        writer.append("import com.jpexs.decompiler.flash.tags.*;").newLine();
        writer.append("import com.jpexs.decompiler.flash.types.*;").newLine();
        writer.append("import com.jpexs.decompiler.flash.types.filters.*;").newLine();
        writer.append("import com.jpexs.decompiler.flash.types.shaperecords.*;").newLine();
        writer.append("import com.jpexs.helpers.ByteArrayRange;").newLine();
        writer.append("import java.io.FileOutputStream;").newLine();
        writer.append("import java.io.IOException;").newLine();
        writer.append("import java.util.ArrayList;").newLine();
        writer.append("import java.util.List;").newLine();
        writer.newLine();
        writer.append("@SuppressWarnings(\"unchecked\")").newLine();
        writer.append("public class SwfFile {").newLine();
        writer.newLine();
        writer.indent();
        IndentedStringBuilder sb = new IndentedStringBuilder(javaIndentString);
        generateJavaCode(writer, sb, objectNames, swf, 0);
        writer.unindent();
        writer.append("    public SWF getSwf() {").newLine();
        writer.append("        SWF swf = swf();").newLine();
        writer.append("        swf.updateCharacters();").newLine();
        writer.append("        return swf;").newLine();
        writer.append("    }").newLine();
        writer.newLine();
        writer.append("    public void saveTo(String fileName) throws IOException {").newLine();
        writer.append("        SWF swf = getSwf();").newLine();
        writer.append("        swf.clearModified();").newLine();
        writer.append("        try (FileOutputStream fos = new FileOutputStream(fileName)) {").newLine();
        writer.append("            swf.saveTo(fos, SWFCompression.ZLIB);").newLine();
        writer.append("        }").newLine();
        writer.append("    }").newLine();
        writer.append("}").newLine();
    }

    private static String getNextId(Map<String, Integer> objectNames, String type) {
        Integer nextId = objectNames.get(type);
        if (nextId == null) {
            nextId = 0;
        } else {
            nextId++;
        }

        objectNames.put(type, nextId);
        return type + nextId;
    }

    private static String getIndent(int indent, String indentStr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append(indentStr);
        }
        return sb.toString();
    }

    private static Object generateJavaCode(GraphTextWriter writer, IndentedStringBuilder sb, Map<String, Integer> objectNames, Object obj, int level) {
        if (obj == null) {
            return null;
        }

        Class cls = obj.getClass();
        Object value = null;

        if (cls == Byte.class || cls == byte.class
                || cls == Short.class || cls == short.class
                || cls == Integer.class || cls == int.class
                || cls == Long.class || cls == long.class
                || cls == Float.class || cls == float.class
                || cls == Double.class || cls == double.class
                || cls == Boolean.class || cls == boolean.class
                || cls == Character.class || cls == char.class
                || cls == String.class) {
            value = obj;
            if (value instanceof Float) {
                value = value + "f";
            } else if (value instanceof String) {
                value = "\"" + Helper.escapeJavaString((String) value) + "\"";
            }
        } else if (cls.isEnum()) {
            value = cls.getSimpleName() + "." + obj;
        } else if (obj instanceof ByteArrayRange) {
            ByteArrayRange range = (ByteArrayRange) obj;
            String className = "ByteArrayRange";
            String tagObjName = getNextId(objectNames, "objByteArrayRange");
            byte[] data = range.getRangeData();
            StringBuilder sb2 = new StringBuilder();
            final int maxBytePerString = 32767;
            boolean isLong = data.length > maxBytePerString;
            if (isLong) {
                // string should be splitted to avoid "constant string too long" compile error
                sb2.append("String.join(\"\", ");
            }

            int stringCount = (int) Math.ceil(data.length / (double) maxBytePerString);
            for (int i = 0; i < stringCount; i++) {
                if (i != 0) {
                    sb2.append(", ");
                }

                sb2.append("\"");
                int from = i * maxBytePerString;
                int to = Math.min(from + maxBytePerString, data.length);
                for (int j = from; j < to; j++) {
                    sb2.append(Helper.byteToHex(data[j]));
                }

                sb2.append("\"");
            }

            if (isLong) {
                sb2.append(")");
            }

            sb.appendLine(className + " " + tagObjName + " = new ByteArrayRange(" + sb2.toString() + ");");
            value = tagObjName;
        } else if (List.class.isAssignableFrom(cls)) {
            List list = (List) obj;
            String tagObjName = getNextId(objectNames, "objList");
            sb.appendLine("List " + tagObjName + " = new ArrayList();");
            for (int i = 0; i < list.size(); i++) {
                Object val = generateJavaCode(writer, sb, objectNames, list.get(i), level + 1);
                sb.appendLine(tagObjName + ".add(" + val + ");");
            }
            value = tagObjName;
        } else if (cls.isArray()) {
            String tagObjName = getNextId(objectNames, "objArray");
            String arrayType = cls.getComponentType().getSimpleName();
            int length = Array.getLength(obj);
            sb.appendLine(arrayType + "[] " + tagObjName + " = new " + arrayType + "[" + length + "];");
            for (int i = 0; i < length; i++) {
                Object val = generateJavaCode(writer, sb, objectNames, Array.get(obj, i), level + 1);
                sb.appendLine(tagObjName + "[" + i + "] = " + val + ";");
            }
            value = tagObjName;
        } else {
            if (obj instanceof LazyObject) {
                ((LazyObject) obj).load();
            }

            String className = obj.getClass().getSimpleName();
            boolean isSwf = level == 0;
            String resultName = isSwf ? "swf" : "result";
            String tagObjName = isSwf ? "swf" : getNextId(objectNames, "obj" + className);
            IndentedStringBuilder sb2 = new IndentedStringBuilder(javaIndentString);
            sb2.indent();
            sb2.indent();
            String indent = getIndent(writer.getIndent() + 1, javaIndentString);
            Field[] fields = obj.getClass().getFields();
            for (Field f : fields) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }

                Internal inter = f.getAnnotation(Internal.class);
                if (inter != null) {
                    continue;
                }

                try {
                    f.setAccessible(true);
                    Object value2 = generateJavaCode(writer, sb2, objectNames, f.get(obj), level + 1);
                    if (value2 != null) {
                        sb2.appendLine(resultName + "." + f.getName() + " = " + value2 + ";");
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(SwfJavaExporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            writer.append("private ").append(className).append(" ").append(tagObjName).append("(").append(isSwf ? "" : "SWF swf").append(") {").newLine();
            writer.indent();
            writer.append(className).append(" ").append(resultName).append(" = new ").append(className).append("(").append(obj instanceof Tag ? "swf" : "").append(");").newLine();
            writer.append(sb2.toString());
            writer.append(indent).append("return ").append(resultName).append(";").newLine();
            writer.unindent();
            writer.append("}").newLine().newLine();
            value = tagObjName + "(swf)";
        }

        return value;
    }
}
