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
package com.jpexs.decompiler.flash.gui;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;

/**
 * An extension of JLabel which looks like a link and responds appropriately
 * when clicked. Note that this class will only work with Swing 1.1.1 and later.
 * Note that because of the way this class is implemented, getText() will not
 * return correct values, user <code>getNormalText</code> instead.
 *
 * @author JPEXS
 */
public class LinkLabel extends JLabel {

    /**
     * The normal text set by the user.
     */
    private String text;

    private String url;

    /**
     * Creates a new LinkLabel with the given text.
     *
     * @param text Text
     * @param url URL
     */
    public LinkLabel(String text, String url) {
        super(text);
        this.url = url;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        enableEvents(MouseEvent.MOUSE_EVENT_MASK);
    }

    public LinkLabel(String text) {
        this(text, text);
    }

    /**
     * Sets the text of the label.
     *
     * @param text Text to show
     */
    @Override
    public void setText(String text) {
        super.setText("<html><font color=\"#0000CF\"><u>" + text + "</u></font></html>");
        this.text = text;
    }

    /**
     * Returns the text set by the user.
     *
     * @return Text
     */
    public String getNormalText() {
        return text;
    }

    /**
     * Processes mouse events and responds to clicks.
     *
     * @param evt Event
     */
    @Override
    protected void processMouseEvent(MouseEvent evt) {
        super.processMouseEvent(evt);
        if (evt.getID() == MouseEvent.MOUSE_CLICKED) {
            clicked();
        }
    }

    protected void clicked() {
        View.navigateUrl(url);
    }
}
