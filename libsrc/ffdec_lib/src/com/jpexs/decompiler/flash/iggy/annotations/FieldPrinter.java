package com.jpexs.decompiler.flash.iggy.annotations;

import java.lang.reflect.Field;

/**
 *
 * @author JPEXS
 */
public class FieldPrinter {

    public static String getObjectSummary(Object val) {
        StringBuilder sb = new StringBuilder();
        sb.append("/---" + val.getClass().getSimpleName() + "---\r\n");
        for (Field f : val.getClass().getDeclaredFields()) {
            sb.append(f.getName()).append(": ");
            IggyFieldType an = f.getAnnotation(IggyFieldType.class);
            if (an != null) {
                f.setAccessible(true);
                try {
                    switch (an.value()) {
                        case float_t:
                            sb.append(f.getFloat(val));
                            break;
                        case uint8_t:
                        case uint16_t:
                            sb.append(f.getInt(val));
                            break;
                        case uint32_t:
                        case uint64_t:
                            sb.append(f.getLong(val));
                            break;
                        case wchar_t:
                            sb.append("\"").append(f.get(val)).append("\"");
                            break;
                    }
                } catch (IllegalAccessException ex) {

                }
            }
            sb.append("\r\n");
        }
        sb.append("---/");
        return sb.toString();
    }

    public static String getObjectWriteTo(Class<?> cls) {
        StringBuilder sb = new StringBuilder();
        for (Field f : cls.getDeclaredFields()) {
            sb.append("stream.write");
            IggyFieldType an = f.getAnnotation(IggyFieldType.class);
            if (an != null) {
                switch (an.value()) {
                    case float_t:
                        sb.append("Float");
                        break;
                    case uint8_t:
                        sb.append("UI8");
                        break;
                    case uint16_t:
                        sb.append("UI16");
                        break;
                    case uint32_t:
                        sb.append("UI32");
                        break;
                    case uint64_t:
                        sb.append("UI64");
                        break;
                    case wchar_t:
                        sb.append("//FIXME");
                        break;
                }
                sb.append("(").append(f.getName()).append(");\r\n");
                if (an.count() > 0) {
                    sb.append("//FIXME count");
                }
            }
        }
        return sb.toString();
    }
}
