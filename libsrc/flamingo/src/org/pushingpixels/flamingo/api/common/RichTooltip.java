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

import java.awt.Image;
import java.util.*;

/**
 * Rich tooltip for command buttons.
 * 
 * <p>
 * In its most basic form, the rich tooltip has a title and one (possible
 * multiline) description text:
 * </p>
 * 
 * <pre>
 * +--------------------------------+
 * | Title                          |
 * |        Some description text   |
 * +--------------------------------+
 * </pre>
 * 
 * <p>
 * The {@link #addDescriptionSection(String)} can be used to add multiple
 * sections to the description:
 * </p>
 * 
 * <pre>
 * +--------------------------------+
 * | Title                          |
 * |        First multiline         |
 * |        description section     |
 * |                                |
 * |        Second multiline        |
 * |        description section     |
 * |                                |
 * |        Third multiline         |
 * |        description section     |
 * +--------------------------------+
 * </pre>
 * 
 * <p>
 * The {@link #setMainImage(Image)} can be used to place an image below the
 * title and to the left of the description sections:
 * </p>
 * 
 * <pre>
 * +--------------------------------+
 * | Title                          |
 * | *******  First multiline       |
 * | *image*  description section   |
 * | *******                        |
 * |          Second multiline      |
 * |          description section   |
 * +--------------------------------+
 * </pre>
 * 
 * <p>
 * The {@link #addFooterSection(String)} can be used to add (possibly) multiple
 * footer sections that will be shown below a horizontal separator:
 * </p>
 * 
 * <pre>
 * +--------------------------------+
 * | Title                          |
 * |        First multiline         |
 * |        description section     |
 * |                                |
 * |        Second multiline        |
 * |        description section     |
 * |--------------------------------|
 * | A multiline footer section     |
 * | placed below a separator       |
 * +--------------------------------+
 * </pre>
 * 
 * <p>
 * The {@link #setFooterImage(Image)} can be used to place an image to the left
 * of the footer sections:
 * </p>
 * 
 * <pre>
 * +--------------------------------+
 * | Title                          |
 * |        First multiline         |
 * |        description section     |
 * |                                |
 * |        Second multiline        |
 * |        description section     |
 * |--------------------------------|
 * | *******  A multiline           |
 * | *image*  footer section        |
 * | *******                        |
 * +--------------------------------+
 * </pre>
 * 
 * <p>
 * Here is a fully fledged rich tooltip that shows all these APIs in action:
 * </p>
 * 
 * <pre>
 * +--------------------------------+
 * | Title                          |
 * | *******  First multiline       |
 * | *image*  description section   |
 * | *******                        |
 * |          Second multiline      |
 * |          description section   |
 * |--------------------------------|
 * | *******  First multiline       |
 * | *image*  footer section        |
 * | *******                        |
 * |          Second multiline      |
 * |          footer section        |
 * +--------------------------------+
 * </pre>
 * 
 * @author Kirill Grouchnikov
 */
public class RichTooltip {
	/**
	 * The main title of this tooltip.
	 * 
	 * @see #RichTooltip(String, String)
	 * @see #setTitle(String)
	 * @see #getTitle()
	 */
	protected String title;

	/**
	 * The main image of this tooltip. Can be <code>null</code>.
	 * 
	 * @see #getMainImage()
	 * @see #setMainImage(Image)
	 */
	protected Image mainImage;

	/**
	 * The description sections of this tooltip.
	 * 
	 * @see #RichTooltip(String, String)
	 * @see #addDescriptionSection(String)
	 * @see #getDescriptionSections()
	 */
	protected List<String> descriptionSections;

	/**
	 * The footer image of this tooltip. Can be <code>null</code>.
	 * 
	 * @see #getFooterImage()
	 * @see #setFooterImage(Image)
	 */
	protected Image footerImage;

	/**
	 * The footer sections of this tooltip. Can be empty.
	 * 
	 * @see #addFooterSection(String)
	 * @see #getFooterSections()
	 */
	protected List<String> footerSections;

	/**
	 * Creates an empty tooltip.
	 */
	public RichTooltip() {
	}

	/**
	 * Creates a tooltip with the specified title and description section.
	 * 
	 * @param title
	 *            Tooltip title.
	 * @param descriptionSection
	 *            Tooltip main description section.
	 */
	public RichTooltip(String title, String descriptionSection) {
		this.setTitle(title);
		this.addDescriptionSection(descriptionSection);
	}

	/**
	 * Sets the title for this tooltip.
	 * 
	 * @param title
	 *            The new tooltip title.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Sets the main image for this tooltip.
	 * 
	 * @param image
	 *            The main image for this tooltip.
	 * @see #getMainImage()
	 * @see #addDescriptionSection(String)
	 */
	public void setMainImage(Image image) {
		this.mainImage = image;
	}

	/**
	 * Adds the specified description section to this tooltip.
	 * 
	 * @param section
	 *            The description section to add.
	 * @see #getDescriptionSections()
	 * @see #setMainImage(Image)
	 * @see #setTitle(String)
	 */
	public void addDescriptionSection(String section) {
		if (this.descriptionSections == null) {
			this.descriptionSections = new LinkedList<String>();
		}
		this.descriptionSections.add(section);
	}

	/**
	 * Sets the footer image for this tooltip.
	 * 
	 * @param image
	 *            The footer image for this tooltip.
	 * @see #getFooterImage()
	 * @see #addFooterSection(String)
	 */
	public void setFooterImage(Image image) {
		this.footerImage = image;
	}

	/**
	 * Adds the specified footer section to this tooltip.
	 * 
	 * @param section
	 *            The footer section to add.
	 * @see #getFooterSections()
	 * @see #setFooterImage(Image)
	 */
	public void addFooterSection(String section) {
		if (this.footerSections == null) {
			this.footerSections = new LinkedList<String>();
		}
		this.footerSections.add(section);
	}

	/**
	 * Returns the main title of this tooltip.
	 * 
	 * @return The main title of this tooltip.
	 * @see #RichTooltip(String, String)
	 * @see #setTitle(String)
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Returns the main image of this tooltip. Can return <code>null</code>.
	 * 
	 * @return The main image of this tooltip.
	 * @see #setMainImage(Image)
	 * @see #getDescriptionSections()
	 */
	public Image getMainImage() {
		return this.mainImage;
	}

	/**
	 * Returns an unmodifiable list of description sections of this tooltip.
	 * Guaranteed to return a non-<code>null</code> list.
	 * 
	 * @return An unmodifiable list of description sections of this tooltip.
	 * @see #RichTooltip(String, String)
	 * @see #addDescriptionSection(String)
	 * @see #getTitle()
	 * @see #getMainImage()
	 */
	@SuppressWarnings("unchecked")
	public List<String> getDescriptionSections() {
		if (this.descriptionSections == null)
			return Collections.EMPTY_LIST;
		return Collections.unmodifiableList(this.descriptionSections);
	}

	/**
	 * Returns the footer image of this tooltip. Can return <code>null</code>.
	 * 
	 * @return The footer image of this tooltip.
	 * @see #setFooterImage(Image)
	 * @see #getFooterSections()
	 */
	public Image getFooterImage() {
		return this.footerImage;
	}

	/**
	 * Returns an unmodifiable list of footer sections of this tooltip.
	 * Guaranteed to return a non-<code>null</code> list.
	 * 
	 * @return An unmodifiable list of footer sections of this tooltip.
	 * @see #addFooterSection(String)
	 * @see #getFooterImage()
	 */
	@SuppressWarnings("unchecked")
	public List<String> getFooterSections() {
		if (this.footerSections == null)
			return Collections.EMPTY_LIST;
		return Collections.unmodifiableList(this.footerSections);
	}
}
