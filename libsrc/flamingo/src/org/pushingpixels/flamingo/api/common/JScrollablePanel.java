/*
 * Copyright (c) 2005-2010 Flamingo Kirill Grouchnikov. All Rights Reserved.
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
 *  o Neither the name of Flamingo Kirill Grouchnikov nor the names of 
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
package org.pushingpixels.flamingo.api.common;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.pushingpixels.flamingo.internal.ui.common.BasicScrollablePanelUI;
import org.pushingpixels.flamingo.internal.ui.common.ScrollablePanelUI;

/**
 * ScrollablePanel allows to have scrolling buttons on each side.
 */
public class JScrollablePanel<T extends JComponent> extends JPanel {
	/**
	 * @see #getUIClassID
	 */
	public static final String uiClassID = "ScrollablePanelUI";

	private T view;

	private ScrollType scrollType;

	private boolean isScrollOnRollover;

	public enum ScrollType {
		VERTICALLY, HORIZONTALLY
	}

	public JScrollablePanel(T c, final ScrollType scrollType) {
		super();

		this.view = c;
		this.scrollType = scrollType;
		this.isScrollOnRollover = true;

		this.updateUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JPanel#getUI()
	 */
	@Override
	public ScrollablePanelUI getUI() {
		return (ScrollablePanelUI) ui;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JPanel#getUIClassID()
	 */
	@Override
	public String getUIClassID() {
		return uiClassID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JPanel#updateUI()
	 */
	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			setUI((ScrollablePanelUI) UIManager.getUI(this));
		} else {
			setUI(BasicScrollablePanelUI.createUI(this));
		}
	}

	public void setScrollOnRollover(boolean toScrollOnRollover) {
		boolean old = this.isScrollOnRollover;
		this.isScrollOnRollover = toScrollOnRollover;

		if (old != this.isScrollOnRollover) {
			this.firePropertyChange("scrollOnRollover", old,
					this.isScrollOnRollover);
		}
	}

	public void scrollToIfNecessary(int startPosition, int span) {
		this.getUI().scrollToIfNecessary(startPosition, span);
	}

	public T getView() {
		return view;
	}

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	protected void fireStateChanged() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		ChangeEvent changeEvent = new ChangeEvent(this);
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
			}
		}
	}

	@Override
	public void doLayout() {
		super.doLayout();
		this.fireStateChanged();
	}

	public ScrollType getScrollType() {
		return scrollType;
	}

	public boolean isScrollOnRollover() {
		return this.isScrollOnRollover;
	}

	public boolean isShowingScrollButtons() {
		return this.getUI().isShowingScrollButtons();
	}
}
