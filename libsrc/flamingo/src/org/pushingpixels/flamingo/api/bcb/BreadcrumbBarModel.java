/*
 * Copyright (c) 2003-2010 Flamingo Kirill Grouchnikov
 * and <a href="http://www.topologi.com">Topologi</a>. 
 * Contributed by <b>Rick Jelliffe</b> of <b>Topologi</b> 
 * in January 2006.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of Flamingo Kirill Grouchnikov Topologi nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.pushingpixels.flamingo.api.bcb;

import java.util.*;

import javax.swing.event.EventListenerList;

/**
 * Model for the breadcrumb bar component ({@link JBreadcrumbBar}).
 * 
 * @param <T>
 *            Type of data associated with each breadcrumb bar item.
 * @author Kirill Grouchnikov
 */
public class BreadcrumbBarModel<T> {
	/**
	 * The list of breadcrumb items.
	 */
	private LinkedList<BreadcrumbItem<T>> items;

	/**
	 * Listener list.
	 */
	protected EventListenerList listenerList;

	/**
	 * Indication whether the model is in cumulative mode.
	 * 
	 * @see #setCumulative(boolean)
	 */
	protected boolean isCumulative;

	/**
	 * Smallest index of path change since the last call to
	 * {@link #setCumulative(boolean)} with <code>true</code>.
	 */
	protected int smallestCumulativeIndex;

	/**
	 * Creates a new empty model.
	 */
	public BreadcrumbBarModel() {
		this.items = new LinkedList<BreadcrumbItem<T>>();
		this.listenerList = new EventListenerList();
		this.isCumulative = false;
		this.smallestCumulativeIndex = -1;
	}

	/**
	 * Returns the index of the specified item.
	 * 
	 * @param item
	 *            Item.
	 * @return Index of the item if it is in the model or -1 if it is not.
	 */
	public int indexOf(BreadcrumbItem<T> item) {
		return this.items.indexOf(item);
	}

	/**
	 * Removes the last item in this model.
	 */
	public void removeLast() {
		this.items.removeLast();
		this.firePathChanged(this.items.size());
	}

	/**
	 * Resets this model, removing all the items.
	 */
	public void reset() {
		this.items.clear();
		this.firePathChanged(0);
	}

	/**
	 * Returns an unmodifiable list of the items in this model.
	 * 
	 * @return Unmodifiable list of the items in this model.
	 */
	public List<BreadcrumbItem<T>> getItems() {
		return Collections.unmodifiableList(this.items);
	}

	/**
	 * Returns the number of items in this model.
	 * 
	 * @return Number of items in this model.
	 */
	public int getItemCount() {
		return this.items.size();
	}

	/**
	 * Returns the model item at the specified index.
	 * 
	 * @param index
	 *            Item index.
	 * @return The model item at the specified index. Will return
	 *         <code>null</code> if the index is negative or larger than the
	 *         number of items.
	 */
	public BreadcrumbItem<T> getItem(int index) {
		if (index < 0)
			return null;
		if (index >= this.getItemCount())
			return null;
		return this.items.get(index);
	}

	/**
	 * Replaces the current item list with the specified list.
	 * 
	 * @param items
	 *            New contents of the model.
	 */
	public void replace(List<BreadcrumbItem<T>> items) {
		this.items.clear();
		for (int i = 0; i < items.size(); i++) {
			this.items.addLast(items.get(i));
		}
		this.firePathChanged(0);
	}

	/**
	 * Adds the specified item at the end of the path.
	 * 
	 * @param item
	 *            Item to add.
	 */
	public void addLast(BreadcrumbItem<T> item) {
		this.items.addLast(item);
		this.firePathChanged(this.items.size() - 1);
	}

	/**
	 * Starts or ends the cumulative mode. In cumulative mode calls to
	 * {@link #addLast(BreadcrumbItem)}, {@link #removeLast()},
	 * {@link #replace(List)} and {@link #reset()} will not fire events on the
	 * listeners registered with
	 * {@link #addPathListener(BreadcrumbPathListener)}.
	 * 
	 * @param isCumulative
	 *            If <code>true</code>, the model enters cumulative mode. If
	 *            <code>false</code>, the model exist cumulative mode and fires
	 *            a path event on all registered listeners with the smallest
	 *            index of all changes that have happened since the last time
	 *            this method was called with <code>true</code>.
	 */
	public void setCumulative(boolean isCumulative) {
		boolean toFire = this.isCumulative && !isCumulative;
		this.isCumulative = isCumulative;

		if (toFire) {
			this.firePathChanged(Math.max(0, this.smallestCumulativeIndex));
			this.smallestCumulativeIndex = -1;
		}
	}

	/**
	 * Adds the specified path listener to this model.
	 * 
	 * @param l
	 *            Path listener to add.
	 */
	public void addPathListener(BreadcrumbPathListener l) {
		this.listenerList.add(BreadcrumbPathListener.class, l);
	}

	/**
	 * Removes the specified path listener from this model.
	 * 
	 * @param l
	 *            Path listener to remove.
	 */
	public void removePathListener(BreadcrumbPathListener l) {
		this.listenerList.remove(BreadcrumbPathListener.class, l);
	}

	/**
	 * Fires a {@link BreadcrumbPathEvent}.
	 * 
	 * @param indexOfFirstChange
	 *            Index of the first item that has changed in the model.
	 */
	protected void firePathChanged(int indexOfFirstChange) {
		if (this.isCumulative) {
			if (this.smallestCumulativeIndex == -1)
				this.smallestCumulativeIndex = indexOfFirstChange;
			else
				this.smallestCumulativeIndex = Math.min(
						this.smallestCumulativeIndex, indexOfFirstChange);
			return;
		}
		BreadcrumbPathEvent event = new BreadcrumbPathEvent(this,
				indexOfFirstChange);
		// Guaranteed to return a non-null array
		Object[] listeners = this.listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BreadcrumbPathListener.class) {
				((BreadcrumbPathListener) listeners[i + 1])
						.breadcrumbPathEvent(event);
			}
		}
	}

}
