package com.jpexs.decompiler.flash.amf.amf3.types;

public enum BasicType {
    NULL {
        @Override
        public String toString() {
            return "null";
        }

    },
    UNDEFINED {
        @Override
        public String toString() {
            return "undefined";
        }

    },
    //Special types for errors while reading
    UNKNOWN {
        @Override
        public String toString() {
            return "unknown";
        }

    }
}
