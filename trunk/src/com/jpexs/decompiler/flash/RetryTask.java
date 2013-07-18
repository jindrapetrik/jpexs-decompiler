/*
 * Copyright (C) 2013 JPEXS
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
package com.jpexs.decompiler.flash;

import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public class RetryTask {

    private RunnableIOEx r;
    private AbortRetryIgnoreHandler handler;
    public Object result;

    public RetryTask(RunnableIOEx r, AbortRetryIgnoreHandler handler) {
        this.r = r;
        this.handler = handler;
    }

    public void run() throws IOException {
        boolean retry;
        do {
            retry = false;
            try {
                r.run();
            } catch (Exception ex) {
                switch (handler.handle(ex)) {
                    case AbortRetryIgnoreHandler.ABORT:
                        throw ex;
                    case AbortRetryIgnoreHandler.RETRY:
                        retry = true;
                        break;
                    case AbortRetryIgnoreHandler.IGNORE:
                        break;
                }
            }
        } while (retry);
    }
}
