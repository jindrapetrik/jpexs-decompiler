package com.jpexs.decompiler.flash.action.deobfuscation;

/**
 *
 * @author JPEXS
 */
public class BrokenScriptDetector {

    public boolean codeIsBroken(String code) {
        return code.contains("\u00A7\u00A7");
    }
}
