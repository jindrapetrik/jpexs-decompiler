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

import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.Callable;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Call function.
 *
 * @author JPEXS
 */
public class CallAVM2Item extends AVM2Item {

    /**
     * Receiver.
     */
    public GraphTargetItem receiver;

    /**
     * Function.
     */
    public GraphTargetItem function;

    /**
     * Arguments.
     */
    public List<GraphTargetItem> arguments;

    private abstract static class Func implements Callable {

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

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param receiver Receiver
     * @param function Function
     * @param arguments Arguments
     */
    public CallAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem receiver, GraphTargetItem function, List<GraphTargetItem> arguments) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.receiver = receiver;
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(receiver);
        visitor.visit(function);
        visitor.visitAll(arguments);
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
        if (function.getPrecedence() > precedence || (function instanceof NewFunctionAVM2Item)) {
            writer.append("(");
            function.toString(writer, localData);
            writer.append(")");
        } else {
            function.toString(writer, localData);
        }
        writer.spaceBeforeCallParenthesis(arguments.size());
        writer.append("(");
        for (int a = 0; a < arguments.size(); a++) {
            if (a > 0) {
                writer.allowWrapHere().append(",");
            }
            arguments.get(a).toString(writer, localData);
        }
        return writer.append(")");
    }

    @Override
    public GraphTargetItem returnType() {
        if (function instanceof GetPropertyAVM2Item) {
            return ((GetPropertyAVM2Item) function).callType;
        }
        if (function instanceof GetLexAVM2Item) {
            return ((GetLexAVM2Item) function).callType;
        }
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.receiver);
        hash = 97 * hash + Objects.hashCode(this.function);
        hash = 97 * hash + Objects.hashCode(this.arguments);
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
        final CallAVM2Item other = (CallAVM2Item) obj;
        if (!Objects.equals(this.receiver, other.receiver)) {
            return false;
        }
        if (!Objects.equals(this.function, other.function)) {
            return false;
        }
        if (!Objects.equals(this.arguments, other.arguments)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

}
