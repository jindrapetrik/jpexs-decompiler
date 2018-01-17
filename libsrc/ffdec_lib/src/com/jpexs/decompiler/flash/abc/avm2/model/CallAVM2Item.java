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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.Callable;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class CallAVM2Item extends AVM2Item {

    public GraphTargetItem receiver;

    public GraphTargetItem function;

    public List<GraphTargetItem> arguments;

    private static abstract class Func implements Callable {

        @Override
        public Object call(String methodName, List<Object> args) {
            return call(args);
        }
    }

    private static Map<String, Func> bundledFunctions = new HashMap<>();

    static {
        bundledFunctions.put("parseInt", new Func() {
            @Override
            public Object call(List<Object> args) {
                Object v = args.get(0);
                Object r = args.size() > 1 ? args.get(0) : 0L;
                return EcmaScript.parseInt(v, r);
            }
        });
        bundledFunctions.put("parseFloat", new Func() {
            @Override
            public Object call(List<Object> args) {
                return EcmaScript.parseFloat(args.get(0));
            }
        });
        bundledFunctions.put("Number", new Func() {
            @Override
            public Object call(List<Object> args) {
                return EcmaScript.toNumber(args.get(0));
            }
        });
        bundledFunctions.put("isNaN", new Func() {
            @Override
            public Object call(List<Object> args) {
                return EcmaScript.isNaN(args.get(0));
            }
        });

        bundledFunctions.put("int", new Func() {
            @Override
            public Object call(List<Object> args) {
                return EcmaScript.toInt32(args.get(0));
            }
        });

        bundledFunctions.put("uint", new Func() {
            @Override
            public Object call(List<Object> args) {
                return EcmaScript.toUint32(args.get(0));
            }
        });

        bundledFunctions.put("encodeURI", new Func() {
            @Override
            public Object call(List<Object> args) {
                return EcmaScript.encodeUri(args.get(0));
            }
        });

        bundledFunctions.put("decodeURI", new Func() {
            @Override
            public Object call(List<Object> args) {
                return EcmaScript.decodeUri(args.get(0));
            }
        });

        bundledFunctions.put("encodeURIComponent", new Func() {
            @Override
            public Object call(List<Object> args) {
                return EcmaScript.encodeUriComponent(args.get(0));
            }
        });

        bundledFunctions.put("decodeURIComponent", new Func() {
            @Override
            public Object call(List<Object> args) {
                return EcmaScript.decodeUriComponent(args.get(0));
            }
        });

        bundledFunctions.put("escape", new Func() {
            @Override
            public Object call(List<Object> args) {
                return EcmaScript.escape(args.get(0));
            }
        });

        bundledFunctions.put("unescape", new Func() {
            @Override
            public Object call(List<Object> args) {
                return EcmaScript.unescape(args.get(0));
            }
        });
    }

    public CallAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem receiver, GraphTargetItem function, List<GraphTargetItem> arguments) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.receiver = receiver;
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public Object getResult() {
        if (!isCompileTime()) {
            return null;
        }
        List<Object> oargs = new ArrayList<>();
        for (GraphTargetItem ot : arguments) {
            Object r = ot.getResult();
            if (r == null) {
                return false;
            }
            oargs.add(r);
        }
        if (function instanceof GetLexAVM2Item) {
            String propName = ((GetLexAVM2Item) function).getRawPropertyName();
            if (bundledFunctions.containsKey(propName)) {
                return bundledFunctions.get(propName).call(oargs);
            }
        } else if (function instanceof Callable) {
            return ((Callable) function).call(oargs);
        }
        return null;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {

        for (GraphTargetItem a : arguments) {
            if (!a.isCompileTime(dependencies)) {
                return false;
            }
        }

        //TODO: receiver?
        if ((function instanceof Callable) && (function.isCompileTime())) {
            return true;
        } else if (function instanceof GetLexAVM2Item) {
            String propName = ((GetLexAVM2Item) function).getRawPropertyName();
            if (bundledFunctions.containsKey(propName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        /*String recPart = ""; receiver.toString(constants, localRegNames) + writer.append(".");
         if (receiver instanceof NewActivationAVM2Item) {
         recPart = "";
         }
         if (receiver instanceof ThisAVM2Item) {
         recPart = "";
         }*/
        if (function.getPrecedence() > precedence) {
            writer.append("(");
            function.toString(writer, localData);
            writer.append(")");
        } else {
            function.toString(writer, localData);
        }
        writer.spaceBeforeCallParenthesies(arguments.size());
        writer.append("(");
        for (int a = 0; a < arguments.size(); a++) {
            if (a > 0) {
                writer.append(",");
            }
            arguments.get(a).toString(writer, localData);
        }
        return writer.append(")");
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
