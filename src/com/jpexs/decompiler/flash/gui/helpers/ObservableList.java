/*
 *  Copyright (C) 2010-2018 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author JPEXS
 */
public class ObservableList<E> implements List<E> {

    private final List<E> list = new ArrayList<>();

    private final List<CollectionChangedListener<E>> listeners = new ArrayList<>();

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(E e) {
        boolean result = list.add(e);
        fireCollectionChanged(new CollectionChangedEvent<>(CollectionChangedAction.ADD, e, size() - 1));
        return result;
    }

    @Override
    public boolean remove(Object o) {
        int idx = list.indexOf(o);
        if (idx != -1) {
            remove(idx);
            return true;
        }

        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean result = list.addAll(c);
        fireCollectionChanged(new CollectionChangedEvent<>(CollectionChangedAction.RESET));
        return result;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = list.removeAll(c);
        fireCollectionChanged(new CollectionChangedEvent<>(CollectionChangedAction.RESET));
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean result = list.retainAll(c);
        fireCollectionChanged(new CollectionChangedEvent<>(CollectionChangedAction.RESET));
        return result;
    }

    @Override
    public void clear() {
        list.clear();
        fireCollectionChanged(new CollectionChangedEvent<>(CollectionChangedAction.RESET));
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean result = list.addAll(index, c);
        fireCollectionChanged(new CollectionChangedEvent<>(CollectionChangedAction.RESET));
        return result;
    }

    @Override
    public E get(int index) {
        return list.get(index);
    }

    @Override
    public E set(int index, E element) {
        E result = list.set(index, element);
        fireCollectionChanged(new CollectionChangedEvent<>(CollectionChangedAction.RESET));
        return result;
    }

    @Override
    public void add(int index, E element) {
        list.add(index, element);
        fireCollectionChanged(new CollectionChangedEvent<>(CollectionChangedAction.ADD, element, index));
    }

    @Override
    public E remove(int index) {
        E result = list.remove(index);
        fireCollectionChanged(new CollectionChangedEvent<>(CollectionChangedAction.REMOVE, result, index));
        return result;
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    private void fireCollectionChanged(CollectionChangedEvent<E> e) {
        for (CollectionChangedListener<E> listener : listeners) {
            listener.collectionChanged(e);
        }
    }

    public void addCollectionChangedListener(CollectionChangedListener<E> listener) {
        listeners.add(listener);
    }

    public void removeCollectionChangedListener(CollectionChangedListener<E> listener) {
        listeners.remove(listener);
    }
}
