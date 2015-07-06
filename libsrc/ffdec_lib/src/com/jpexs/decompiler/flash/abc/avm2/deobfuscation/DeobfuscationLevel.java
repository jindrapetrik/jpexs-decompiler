package com.jpexs.decompiler.flash.abc.avm2.deobfuscation;

/**
 *
 * @author JPEXS
 */
public enum DeobfuscationLevel {

    LEVEL_REMOVE_DEAD_CODE(1),
    LEVEL_REMOVE_TRAPS(2),
    LEVEL_RESTORE_CONTROL_FLOW(3);

    private final int level;

    public int getLevel() {
        return level;
    }

    DeobfuscationLevel(int level) {
        this.level = level;
    }
}
