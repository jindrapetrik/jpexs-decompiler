package com.jpexs.decompiler.flash.amf.amf3;

import java.util.List;

public class Traits {

    private String className;
    private boolean dynamic;
    private List<String> sealedMemberNames;

    public Traits(String className, boolean dynamic, List<String> sealedMemberNames) {
        this.className = className;
        this.dynamic = dynamic;
        this.sealedMemberNames = sealedMemberNames;
    }

    public String getClassName() {
        return className;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public List<String> getSealedMemberNames() {
        return sealedMemberNames;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public void setSealedMemberNames(List<String> sealedMemberNames) {
        this.sealedMemberNames = sealedMemberNames;
    }

}
