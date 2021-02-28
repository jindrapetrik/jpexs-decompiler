package com.jpexs.decompiler.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GraphPartMarkedArrayList<E> extends ArrayList<E> {

    private List<GraphPart> listParts = new ArrayList<>();
    private GraphPart currentPart = null;

    public GraphPartMarkedArrayList(Collection<? extends E> collection) {
        super(collection);
        if (collection instanceof GraphPartMarkedArrayList) {
            for (int i = 0; i < collection.size(); i++) {
                listParts.add((GraphPart) ((GraphPartMarkedArrayList) collection).listParts.get(i));
            }
            currentPart = ((GraphPartMarkedArrayList) collection).currentPart;
        } else {
            for (int i = 0; i < collection.size(); i++) {
                listParts.add(currentPart);
            }
        }
    }

    public GraphPartMarkedArrayList() {
    }

    public void startPart(GraphPart part) {
        currentPart = part;
    }

    @Override
    public boolean add(E e) {
        listParts.add(currentPart);
        return super.add(e);
    }

    @Override
    public void add(int index, E element) {
        listParts.add(index, currentPart);
        super.add(index, element);
    }

    public GraphPart getPartAt(int index) {
        return listParts.get(index);
    }

    public int indexOfPart(GraphPart part) {
        return listParts.indexOf(part);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c instanceof GraphPartMarkedArrayList) {
            for (int i = 0; i < c.size(); i++) {
                listParts.add((GraphPart) ((GraphPartMarkedArrayList) c).listParts.get(i));
            }
        } else {
            for (int i = 0; i < c.size(); i++) {
                listParts.add(currentPart);
            }
        }
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        if (c instanceof GraphPartMarkedArrayList) {
            for (int i = 0; i < c.size(); i++) {
                listParts.add(index + i, (GraphPart) ((GraphPartMarkedArrayList) c).listParts.get(i));
            }
        } else {
            for (int i = 0; i < c.size(); i++) {
                listParts.add(index + i, currentPart);
            }
        }
        return super.addAll(index, c);
    }

    @Override
    public boolean remove(Object o) {
        if (contains(o)) {
            listParts.remove(indexOf(o));
        }
        return super.remove(o);
    }

    @Override
    public E remove(int index) {
        listParts.remove(index);
        return super.remove(index);
    }

    @Override
    public void clear() {
        listParts.clear();
        super.clear();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        GraphPartMarkedArrayList<E> ret = new GraphPartMarkedArrayList<E>(this);
        for (int i = size(); i > toIndex; i--) {
            ret.remove(i);
        }
        for (int i = 0; i < fromIndex; i++) {
            ret.remove(i);
        }
        return ret;
    }

    @Override
    public Object clone() {
        return new GraphPartMarkedArrayList<>(this);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object o : c) {
            if (contains(o)) {
                listParts.remove(indexOf(o));
            }
        }

        return super.removeAll(c);
    }

}
