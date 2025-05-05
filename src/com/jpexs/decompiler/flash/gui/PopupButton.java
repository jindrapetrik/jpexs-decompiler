/*
 * Copyright (C) 2025 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Button with a popup on click.
 * The logic ensures that it stays down when menu is shown.
 * @author JPEXS
 */
public abstract class PopupButton extends JToggleButton {

    private boolean insideButton = false;
    
    private PopupMenuListener popupListener;
    
    private JPopupMenu popupMenu;
    
    public PopupButton(Icon icon) {
        super(icon);
        initListeners();
    }      
    
    public PopupButton(String text) {
        super(text);
        initListeners();
    }        
    
    public PopupButton(String text, Icon icon) {
        super(text, icon);
        initListeners();
    }
    
    private void initListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                insideButton = true;
            }            

            @Override
            public void mouseExited(MouseEvent e) {
                insideButton = false;
            }            
        });
        addActionListener(this::popupOpenActionPerformed);
        popupListener = new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (!insideButton) {
                    PopupButton.this.setSelected(false);
                }
                if (popupMenu != null) {
                    popupMenu.removePopupMenuListener(popupListener);
                }
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                
            }
        };
    }
    
    /**
     * Method for getting (constructing?) a popup menu
     * @return A popup menu
     */
    protected abstract JPopupMenu getPopupMenu();
    
    private void popupOpenActionPerformed(ActionEvent evt) {
        if (!isSelected()) {
            return;
        }
       
        popupMenu = getPopupMenu();
        popupMenu.addPopupMenuListener(popupListener);
        
        JToggleButton sourceButton = (JToggleButton) evt.getSource();
        popupMenu.show(sourceButton, 0, sourceButton.getHeight());        
    }
}
