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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.major;
        hash = 53 * hash + this.minor;
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
        final ABCVersion other = (ABCVersion) obj;
        return true;
    }

}
