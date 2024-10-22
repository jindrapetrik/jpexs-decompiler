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
package com.jpexs.decompiler.flash.console;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author JPEXS
 */
public class ConsoleAbortRetryIgnoreHandler implements AbortRetryIgnoreHandler {

    int errorCount = 0;

    int errorMode;

    int retryCount;

    public ConsoleAbortRetryIgnoreHandler(int errorMode, int retryCount) {
        this.errorMode = errorMode;
        this.retryCount = retryCount;
    }

    @Override
    public int handle(Throwable thrown) {
        if (thrown instanceof InterruptedException) {
            return AbortRetryIgnoreHandler.ABORT;
        }
        if (errorMode != AbortRetryIgnoreHandler.UNDEFINED) {
            int result = errorMode;

            if (errorMode == AbortRetryIgnoreHandler.RETRY && errorCount < retryCount) {
                errorCount++;
            } else {
                result = AbortRetryIgnoreHandler.IGNORE;
            }

            return result;
        }
        Scanner sc = new Scanner(System.in);
        if (thrown != null) {
            Logger.getLogger(ConsoleAbortRetryIgnoreHandler.class.getName()).log(Level.SEVERE, "Error occurred", thrown);
            System.out.println("Error occurred: " + thrown.getLocalizedMessage());
        }
        do {
            System.out.print("Select action: (A)bort, (R)Retry, (I)Ignore:");
            String n = sc.nextLine();
            switch (n.toLowerCase(Locale.ENGLISH)) {
                case "a":
                    return AbortRetryIgnoreHandler.ABORT;
                case "r":
                    return AbortRetryIgnoreHandler.RETRY;
                case "i":
                    return AbortRetryIgnoreHandler.IGNORE;
            }
        } while (true);
    }

    @Override
    public AbortRetryIgnoreHandler getNewInstance() {
        return new ConsoleAbortRetryIgnoreHandler(errorMode, retryCount);
    }
}
