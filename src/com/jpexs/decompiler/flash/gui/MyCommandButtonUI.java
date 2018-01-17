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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelManager;
import org.pushingpixels.substance.flamingo.common.ui.SubstanceCommandButtonUI;

/**
 *
 * Own CommandButtonUI because original Flamingo UI throws Exception in some
 * cases
 *
 * @author JPEXS
 */
public class MyCommandButtonUI extends SubstanceCommandButtonUI {

    public static ComponentUI createUI(JComponent comp) {
        return new MyCommandButtonUI((JCommandButton) comp);
    }

    public MyCommandButtonUI(JCommandButton jcb) {
        super(jcb);
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        this.commandButton.removeActionListener(this.disposePopupsActionListener);
        this.disposePopupsActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (commandButton == null) { //Added by JPEXS
                    return;
                }
                boolean toDismiss = !Boolean.TRUE.equals(commandButton
                        .getClientProperty(DONT_DISPOSE_POPUPS));
                if (toDismiss) {
                    JCommandPopupMenu menu = (JCommandPopupMenu) SwingUtilities
                            .getAncestorOfClass(JCommandPopupMenu.class,
                                    commandButton);
                    if (menu != null) {
                        toDismiss = menu.isToDismissOnChildClick();
                    }
                }
                if (toDismiss) {
                    if (SwingUtilities.getAncestorOfClass(JPopupPanel.class,
                            commandButton) != null) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                // command button may be cleared if the
                                // button click resulted in LAF switch
                                if (commandButton != null) {
                                    // clear the active states
                                    commandButton.getActionModel().setPressed(
                                            false);
                                    commandButton.getActionModel().setRollover(
                                            false);
                                    commandButton.getActionModel().setArmed(
                                            false);
                                }
                            }
                        });
                    }
                    PopupPanelManager.defaultManager().hidePopups(null);
                }
            }
        };
        this.commandButton.addActionListener(disposePopupsActionListener);
    }
}
