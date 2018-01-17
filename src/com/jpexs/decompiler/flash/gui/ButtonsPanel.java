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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class ButtonsPanel extends JPanel {

    private ComponentListener listener;

    public ButtonsPanel() {
        super(new FlowLayout());

        listener = new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                updateVisibility();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                updateVisibility();
            }
        };

        this.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {
                e.getComponent().addComponentListener(listener);
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                e.getComponent().removeComponentListener(listener);
            }
        });
    }

    private void updateVisibility() {

        // hide button panel when no button is visible
        boolean visible = false;
        for (Component component : getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button.isVisible()) {
                    visible = true;
                }
            }
        }
        setVisible(visible);
    }
}
