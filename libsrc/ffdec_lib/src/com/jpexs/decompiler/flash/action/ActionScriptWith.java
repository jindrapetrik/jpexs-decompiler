package com.jpexs.decompiler.flash.action;

/**
 *
 * @author JPEXS
 */
public class ActionScriptWith {

    protected ActionScriptObject obj;
    protected long startAddr;
    protected long length;

    public ActionScriptWith(ActionScriptObject obj, long startAddr, long length) {
        this.obj = obj;
        this.startAddr = startAddr;
        this.length = length;
    }

}
