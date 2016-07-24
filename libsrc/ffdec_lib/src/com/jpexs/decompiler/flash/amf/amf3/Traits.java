package com.jpexs.decompiler.flash.amf.amf3;

import java.util.Collection;
import java.util.Set;

public class Traits {

    private String className;
    private boolean dynamic;
    private Set<String> sealedMemberNames;

    public Traits(String className, boolean dynamic, Collection<? extends String> sealedMemberNames) {
        this.className = className;
        this.dynamic = dynamic;
        this.sealedMemberNames = new ListSet<>(sealedMemberNames);
    }

    public String getClassName() {
        return className;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public Set<String> getSealedMemberNames() {
        return new ListSet<>(sealedMemberNames);
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public void setSealedMemberNames(Collection<? extends String> sealedMemberNames) {
        this.sealedMemberNames = new ListSet<>(sealedMemberNames);
    }

}
