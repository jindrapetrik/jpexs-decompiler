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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.PopupButton;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author JPEXS
 */
public class SnapOptionsButton extends PopupButton {
 
    
    public SnapOptionsButton() {                
        super(View.getIcon("snap16"));        
        setToolTipText(AppStrings.translate("button.snap_options"));
        setMargin(new Insets(0, 0, 0, 0));        
    }

    @Override
    protected JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        
        JCheckBox snapAlignMenuItem = new JCheckBox(AppStrings.translate("snap_options.snap_align"));
        snapAlignMenuItem.setSelected(Configuration.snapAlign.get());
        snapAlignMenuItem.addActionListener(this::snapAlignMenuItemActionPerformed);
        popupMenu.add(snapAlignMenuItem);
        
        JCheckBox snapToGuidesMenuItem = new JCheckBox(AppStrings.translate("snap_options.snap_to_guides"));
        snapToGuidesMenuItem.setSelected(Configuration.snapToGuides.get());
        snapToGuidesMenuItem.addActionListener(this::snapToGuidesMenuItemActionPerformed);
        popupMenu.add(snapToGuidesMenuItem);
        
        JCheckBox snapToPixelsMenuItem = new JCheckBox(AppStrings.translate("snap_options.snap_to_pixels"));
        snapToPixelsMenuItem.setSelected(Configuration.snapToPixels.get());
        snapToPixelsMenuItem.addActionListener(this::snapToPixelsMenuItemActionPerformed);
        popupMenu.add(snapToPixelsMenuItem);
        
        JCheckBox snapToObjectsMenuItem = new JCheckBox(AppStrings.translate("snap_options.snap_to_objects"));
        snapToObjectsMenuItem.setSelected(Configuration.snapToObjects.get());
        snapToObjectsMenuItem.addActionListener(this::snapToObjectsMenuItemActionPerformed);
        popupMenu.add(snapToObjectsMenuItem);
                        
        return popupMenu;
    }
    
    private void snapAlignMenuItemActionPerformed(ActionEvent evt) {
        JCheckBox menuItem = (JCheckBox) evt.getSource();
        Configuration.snapAlign.set(menuItem.isSelected());        
    }
    
    private void snapToGuidesMenuItemActionPerformed(ActionEvent evt) {
        JCheckBox menuItem = (JCheckBox) evt.getSource();
        Configuration.snapToGuides.set(menuItem.isSelected());        
    }
    
    private void snapToPixelsMenuItemActionPerformed(ActionEvent evt) {
        JCheckBox menuItem = (JCheckBox) evt.getSource();
        Configuration.snapToPixels.set(menuItem.isSelected());        
    }
    
    private void snapToObjectsMenuItemActionPerformed(ActionEvent evt) {
        JCheckBox menuItem = (JCheckBox) evt.getSource();
        Configuration.snapToObjects.set(menuItem.isSelected());        
    }
    
}
