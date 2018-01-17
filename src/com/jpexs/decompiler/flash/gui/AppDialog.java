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

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.awt.Window;
import java.util.ResourceBundle;
import javax.swing.JDialog;
import javax.swing.JRootPane;

/**
 *
 * @author JPEXS
 */
public abstract class AppDialog extends JDialog {

    public static final int ERROR_OPTION = -1;

    public static final int OK_OPTION = 0;

    public static final int CANCEL_OPTION = 1;

    private ResourceBundle resourceBundle = ResourceBundle.getBundle(AppStrings.getResourcePath(getClass()));

    public AppDialog() {
        View.installEscapeCloseOperation(this);
        if (Configuration.useRibbonInterface.get()) {
            getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        }
    }

    public AppDialog(Window owner) {
        super(owner);
        View.installEscapeCloseOperation(this);
        if (Configuration.useRibbonInterface.get()) {
            getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        }
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public String translate(String key) {
        return resourceBundle.getString(key);
    }

    public void updateLanguage() {
        resourceBundle = ResourceBundle.getBundle(AppStrings.getResourcePath(getClass()));
    }
}
