/*
 *  Copyright (C) 2010-2024 JPEXS
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
import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * @author JPEXS
 */
public class GuiAbortRetryIgnoreHandler implements AbortRetryIgnoreHandler {

    private boolean ignoreAll = false;

    @Override
    public int handle(Throwable thrown) {
        synchronized (GuiAbortRetryIgnoreHandler.class) {
            String[] options = new String[]{
                AppStrings.translate("button.abort"),
                AppStrings.translate("button.retry"),
                AppStrings.translate("button.ignore"),
                AppStrings.translate("button.ignoreAll")
            };

            if (ignoreAll) {
                return AbortRetryIgnoreHandler.IGNORE;
            }

            String msg = null;
            if (thrown != null) {
                msg = thrown.getLocalizedMessage();
                if (msg == null) {
                    msg = thrown.toString();
                }
            }

            if (msg == null) {
                msg = "";
            }

            MainFrame mf = Main.getMainFrame();
            Component cmp = null;
            if (mf != null) {
                cmp = mf.getPanel();
            }
            int result = ViewMessages.showOptionDialog(cmp, AppStrings.translate("error.occurred").replace("%error%", msg), AppStrings.translate("error"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options, "");
            if (result == AbortRetryIgnoreHandler.IGNORE_ALL) {
                ignoreAll = true;
                result = AbortRetryIgnoreHandler.IGNORE;
            }

            return result;
        }
    }

    @Override
    public AbortRetryIgnoreHandler getNewInstance() {
        // there are no non-static field in this class, so return the original instance
        return this;
    }
}
