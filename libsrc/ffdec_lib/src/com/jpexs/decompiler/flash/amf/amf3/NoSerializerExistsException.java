package com.jpexs.decompiler.flash.amf.amf3;

public class NoSerializerExistsException extends Exception {

    private final String className;
    private final Object incompleteData;

    /*public NoSerializerExistsException(String className, Object incompleteData) {
        this(className, incompleteData, null);
    }*/
    public NoSerializerExistsException(String className, Object incompleteData, Throwable cause) {
        super("Cannot read AMF - no deserializer defined for class \"" + className + "\".", cause);
        this.className = className;
        this.incompleteData = incompleteData;
    }

    public String getClassName() {
        return className;
    }

    public Object getIncompleteData() {
        return incompleteData;
    }

}
