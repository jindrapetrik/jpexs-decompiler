package com.jpexs.decompiler.flash.exporters.script;

import com.jpexs.decompiler.graph.DottedChain;
import java.util.Objects;

public class Dependency {

    private DottedChain id;
    private DependencyType type;

    public Dependency(DottedChain id, DependencyType type) {
        this.id = id;
        this.type = type;
    }

    public DottedChain getId() {
        return id;
    }

    public DependencyType getType() {
        return type;
    }

    @Override
    public String toString() {
        return id.toString() + " (" + type + ")";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.id);
        hash = 79 * hash + Objects.hashCode(this.type);
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
        final Dependency other = (Dependency) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

}
