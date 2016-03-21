package com.jpexs.decompiler.flash.abc;

public class ABCVersion implements Comparable<ABCVersion> {

    public int major = 46;
    public int minor = 16;

    public ABCVersion() {

    }

    public ABCVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    @Override
    public int compareTo(ABCVersion o) {
        if (major != o.major) {
            return major - o.major;
        }
        return minor - o.minor;
    }

    @Override
    public String toString() {
        return "" + major + "." + minor;
    }

}
