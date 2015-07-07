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

    public static DeobfuscationLevel getByLevel(int level) {
        switch (level) {
            case 1:
                return LEVEL_REMOVE_DEAD_CODE;
            case 2:
                return LEVEL_REMOVE_TRAPS;
            case 3:
                return LEVEL_RESTORE_CONTROL_FLOW;
        }

        return null;
    }

    DeobfuscationLevel(int level) {
        this.level = level;
    }
}
