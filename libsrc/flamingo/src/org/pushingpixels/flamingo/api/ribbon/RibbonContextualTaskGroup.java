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
package org.pushingpixels.flamingo.api.ribbon;

import java.awt.Color;
import java.util.ArrayList;

/**
 * A contextual group of {@link RibbonTask}s. The contextual ribbon task groups
 * allow showing and hiding ribbon tasks based on the current selection in the
 * application. For example, Word only shows the table tasks when a table is
 * selected in the document. By default, tasks belonging to the groups added by
 * {@link JRibbon#addContextualTaskGroup(RibbonContextualTaskGroup)} are not
 * visible. To show the tasks belonging to the specific group, call
 * {@link JRibbon#setVisible(RibbonContextualTaskGroup, boolean)} API. Note that
 * you can have multiple task groups visible at the same time. This class is a
 * logical entity that groups ribbon tasks belonging to the same contextual
 * group.
 * 
 * @author Kirill Grouchnikov
 */
public class RibbonContextualTaskGroup {
	/**
	 * The ribbon that contains this task group.
	 */
	private JRibbon ribbon;

	/**
	 * List of all tasks.
	 * 
	 * @see #RibbonContextualTaskGroup(String, Color, RibbonTask...)
	 * @see #getTaskCount()
	 * @see #getTask(int)
	 */
	private ArrayList<RibbonTask> tasks;

	/**
	 * Group title.
	 * 
	 * @see #RibbonContextualTaskGroup(String, Color, RibbonTask...)
	 * @see #getTitle()
	 * @see #setTitle(String)
	 */
	private String title;

	/**
	 * Hue color for this group.
	 * 
	 * @see #RibbonContextualTaskGroup(String, Color, RibbonTask...)
	 * @see #getHueColor()
	 */
	private Color hueColor;

	/**
	 * Alpha factor for colorizing the toggle tab buttons of tasks in contextual
	 * groups.
	 */
	public static final double HUE_ALPHA = 0.25;

	/**
	 * Creates a task contextual group that contains the specified tasks.
	 * 
	 * @param title
	 *            Group title.
	 * @param hueColor
	 *            Hue color for this group. Should be a saturated non-dark color
	 *            for good visuals.
	 * @param tasks
	 *            Tasks to add to the group.
	 */
	public RibbonContextualTaskGroup(String title, Color hueColor,
			RibbonTask... tasks) {
		this.title = title;
		this.hueColor = hueColor;
		this.tasks = new ArrayList<RibbonTask>();
		for (RibbonTask ribbonTask : tasks) {
			ribbonTask.setContextualGroup(this);
			this.tasks.add(ribbonTask);
		}
	}

	/**
	 * Returns the number of tasks in <code>this</code> group.
	 * 
	 * @return Number of tasks in <code>this</code> group.
	 * @see #getTask(int)
	 */
	public int getTaskCount() {
		return this.tasks.size();
	}

	/**
	 * Returns task at the specified index from <code>this</code> group.
	 * 
	 * @param index
	 *            Task index.
	 * @return Task at the specified index.
	 * @see #getTaskCount()
	 */
	public RibbonTask getTask(int index) {
		return this.tasks.get(index);
	}

	/**
	 * Returns the name of this group.
	 * 
	 * @return The name of this group.
	 * @see #setTitle(String)
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Returns the hue color for this group.
	 * 
	 * @return The hue color for this group.
	 */
	public Color getHueColor() {
		return this.hueColor;
	}

	/**
	 * Changes the title of this ribbon contextual task group.
	 * 
	 * @param title
	 *            The new title for this ribbon contextual task group.
	 * @see #getTitle()
	 */
	public void setTitle(String title) {
		this.title = title;
		if (this.ribbon != null)
			this.ribbon.fireStateChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getTitle() + " (" + getTaskCount() + " tasks)";
	}

	/**
	 * Associates this ribbon contextual task group with the specified ribbon.
	 * This method is package protected and is for internal use only.
	 * 
	 * @param ribbon
	 *            The associated ribbon.
	 */
	void setRibbon(JRibbon ribbon) {
		if (this.ribbon != null) {
			throw new IllegalStateException(
					"The contextual task group already belongs to another ribbon");
		}
		this.ribbon = ribbon;
	}

}
