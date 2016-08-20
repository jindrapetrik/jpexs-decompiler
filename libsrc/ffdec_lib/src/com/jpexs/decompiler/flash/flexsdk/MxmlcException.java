package com.jpexs.decompiler.flash.flexsdk;

public class MxmlcException extends Exception {

    private String mxmlcErrorOutput;

    public MxmlcException(String mxmlcErrorOutput) {
        this.mxmlcErrorOutput = mxmlcErrorOutput;
    }

    public String getMxmlcErrorOutput() {
        return mxmlcErrorOutput;
    }

}
