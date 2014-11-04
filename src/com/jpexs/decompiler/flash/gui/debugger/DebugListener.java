package com.jpexs.decompiler.flash.gui.debugger;

/**
 *
 * @author JPEXS
 */
public interface DebugListener {

    public void onMessage(String clientId, String msg);

    public void onFinish(String clientId);
}
