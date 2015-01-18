/*
 *  Copyright (C) 2010-2015 JPEXS
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

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import javax.swing.JOptionPane;

/**
 *
 * @author JPEXS
 */
public class GuiAbortRetryIgnoreHandler implements AbortRetryIgnoreHandler {

    @Override
    public int handle(Throwable thrown) {
        synchronized (GuiAbortRetryIgnoreHandler.class) {
            String[] options = new String[]{AppStrings.translate("button.abort"), AppStrings.translate("button.retry"), AppStrings.translate("button.ignore")};
            return View.showOptionDialog(null, AppStrings.translate("error.occured").replace("%error%", thrown.getLocalizedMessage()), AppStrings.translate("error"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, "");
        }
    }

    @Override
    public AbortRetryIgnoreHandler getNewInstance() {
        // there are no non-static field in this class, so return the original instance
        return this;
    }
}
