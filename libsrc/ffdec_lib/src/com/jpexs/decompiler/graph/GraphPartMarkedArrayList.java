/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * List which can store parts of the graph for each element.
 *
 * @author JPEXS
 * @param <E> Element type
 */
public class GraphPartMarkedArrayList<E> extends ArrayList<E> {

    /**
     * List of parts for each element.
     */
    private List<List<GraphPart>> listParts = new ArrayList<>();

    /**
     * Current parts.
     */
    private List<GraphPart> currentParts = new ArrayList<>();

    /**
     * Constructs GraphPartMarkedArrayList from another collection.
     *
     * @param collection Collection
     */
    @SuppressWarnings("unchecked")
    public GraphPartMarkedArrayList(Collection<? extends E> collection) {
        super(collection);
        if (collection instanceof GraphPartMarkedArrayList) {
            for (int i = 0; i < collection.size(); i++) {
                listParts.add((List<GraphPart>) ((GraphPartMarkedArrayList) collection).listParts.get(i));
            }
            currentParts = ((GraphPartMarkedArrayList) collection).currentParts;
        } else {
            for (int i = 0; i < collection.size(); i++) {
                listParts.add(currentParts);
            }
        }
    }

    /**
     * Constructs GraphPartMarkedArrayList.
     */
    public GraphPartMarkedArrayList() {
    }

    /**
     * Starts new part.
     *
     * @param part Part
     */
    public void startPart(GraphPart part) {
        currentParts.add(part);
    }

    /**
     * Clears current parts.
     */
    public void clearCurrentParts() {
        currentParts = new ArrayList<>();
    }

    /**
     * Adds element to the collection.
     *
     * @param e element whose presence in this collection is to be ensured
     * @return true if this collection changed as a result of the call
     */
    @Override
    public boolean add(E e) {
        listParts.add(currentParts);
        return super.add(e);
    }

    /**
     * Inserts the specified element at the specified position in this list.
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     */
    @Override
    public void add(int index, E element) {
        listParts.add(index, currentParts);
        super.add(index, element);
    }

    /**
     * Returns the parts at the specified index.
     *
     * @param index index of the element
     * @return parts at the specified index
     */
    public List<GraphPart> getPartsAt(int index) {
        return listParts.get(index);
    }

    /**
     * Gets the index of the part in the list.
     *
     * @param part Part
     * @return Index of the part in the list
     */
    public int indexOfPart(GraphPart part) {
        for (int i = 0; i < listParts.size(); i++) {
            List<GraphPart> list = listParts.get(i);
            if (list.indexOf(part) > -1) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Adds all elements from the collection to this collection.
     *
     * @param c collection containing elements to be added to this collection
     * @return true if this collection changed as a result of the call
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c instanceof GraphPartMarkedArrayList) {
            for (int i = 0; i < c.size(); i++) {
                listParts.add((List<GraphPart>) ((GraphPartMarkedArrayList) c).listParts.get(i));
            }
        } else {
            for (int i = 0; i < c.size(); i++) {
                listParts.add(currentParts);
            }
        }
        return super.addAll(c);
    }

    /**
     * Inserts all elements in the specified collection into this list at the
     *
     * @param index index at which to insert the first element from the
     * specified collection
     * @param c collection containing elements to be added to this list
     * @return true if this list changed as a result of the call
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        if (c instanceof GraphPartMarkedArrayList) {
            for (int i = 0; i < c.size(); i++) {
                listParts.add(index + i, (List<GraphPart>) ((GraphPartMarkedArrayList) c).listParts.get(i));
            }
        } else {
            for (int i = 0; i < c.size(); i++) {
                listParts.add(index + i, currentParts);
            }
        }
        return super.addAll(index, c);
    }

    /**
     * Removes the first occurrence of the specified element from this list, if
     *
     * @param o element to be removed from this list, if present
     * @return true if an element was removed as a result of this call
     */
    @Override
    public boolean remove(Object o) {
        if (contains(o)) {
            listParts.remove(indexOf(o));
        }
        return super.remove(o);
    }

    /**
     * Removes the element at the specified position in this list. Shifts any
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     */
    @Override
    public E remove(int index) {
        listParts.remove(index);
        return super.remove(index);
    }

    /**
     * Clears the list.
     */
    @Override
    public void clear() {
        listParts.clear();
        super.clear();
    }

    /**
     * Returns a view of the portion of this list between the specified
     *
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * @return a view of the specified range within this list
     */
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

    /**
     * Returns a shallow copy of this ArrayList instance.
     *
     * @return a clone of this ArrayList instance
     */
    @Override
    public Object clone() {
        return new GraphPartMarkedArrayList<>(this);
    }

    /**
     * Removes from this list all of its elements that are contained in the
     *
     * @param c collection containing elements to be removed from this list
     * @return true if this list changed as a result of the call
     */
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
