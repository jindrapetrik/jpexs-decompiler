package com.jpexs.decompiler.graph.precontinues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author JPEXS
 */
class NonNullList<T> extends ArrayList<T> {

    public NonNullList(Collection<? extends T> col) {
        super(col);
    }

    public NonNullList() {
    }


    @Override
    public boolean add(T item) {
        if (item == null) {
            throw new NullPointerException("The collection does not support null values");
        } else {
            return super.add(item);
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> items) {
        if (items.contains(null)) {
            throw new NullPointerException("The collection does not support null values");
        } else {
            return super.addAll(items);
        }
    }

}
