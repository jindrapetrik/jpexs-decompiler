/*
 *  Copyright (C) 2010-2014 JPEXS
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

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.Tag;
import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 *
 * @author JPEXS
 */
public class GenericTagPanel extends JPanel implements ComponentListener {

    private final JEditorPane genericTagPropertiesEditorPane;
    private final JPanel genericTagPropertiesEditPanel;
    private final JScrollPane genericTagPropertiesEditorPaneScrollPanel;
    private final JScrollPane genericTagPropertiesEditPanelScrollPanel;

    public GenericTagPanel() {
        super(new BorderLayout());

        genericTagPropertiesEditorPane = new JEditorPane() {
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        genericTagPropertiesEditorPane.setEditable(false);
        genericTagPropertiesEditorPaneScrollPanel = new JScrollPane(genericTagPropertiesEditorPane);
        add(genericTagPropertiesEditorPaneScrollPanel);

        genericTagPropertiesEditPanel = new JPanel();
        genericTagPropertiesEditPanelScrollPanel = new JScrollPane(genericTagPropertiesEditPanel);
    }

    public void setEditMode(boolean edit) {
        if (edit) {
            remove(genericTagPropertiesEditorPaneScrollPanel);
            add(genericTagPropertiesEditPanelScrollPanel);
        } else {
            remove(genericTagPropertiesEditPanelScrollPanel);
            add(genericTagPropertiesEditorPaneScrollPanel);
        }        
        repaint();
    }

    public void setTagText(Tag tag) {
        StringBuilder sb = new StringBuilder();
        Field[] fields = tag.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                sb.append(field.getName()).append(": ").append(field.get(tag));
                sb.append(GraphTextWriter.NEW_LINE);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        genericTagPropertiesEditorPane.setText(sb.toString());
        genericTagPropertiesEditorPane.setSize(0, 0);
    }

    public void generateEditControls() {
        genericTagPropertiesEditPanel.removeAll();
        genericTagPropertiesEditPanel.add(new JLabel("aa"));
        repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        genericTagPropertiesEditorPane.setSize(0, 0);
    }

    @Override
    public void componentMoved(ComponentEvent ce) {
    }

    @Override
    public void componentShown(ComponentEvent ce) {
    }

    @Override
    public void componentHidden(ComponentEvent ce) {
    }
}
