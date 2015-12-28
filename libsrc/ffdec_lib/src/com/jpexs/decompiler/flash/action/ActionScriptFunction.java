package com.jpexs.decompiler.flash.action;

import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class ActionScriptFunction extends ActionScriptObject {

    protected long functionOffset;
    protected long functionLength;
    protected String functionName;
    protected List<String> paramNames;
    protected Map<Integer, String> funcRegNames;

    public ActionScriptFunction(long functionOffset, long functionLength, String functionName, List<String> paramNames, Map<Integer, String> funcRegNames) {
        this.functionOffset = functionOffset;
        this.functionLength = functionLength;
        this.functionName = functionName;
        this.paramNames = paramNames;
        this.funcRegNames = funcRegNames;
    }

    public Object execute(Object thisObj, List<Object> args) {
        //TODO!!!
        return null;
    }

    public long getFunctionLength() {
        return functionLength;
    }

    public long getFunctionOffset() {
        return functionOffset;
    }

}
