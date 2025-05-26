/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash;

import java.io.IOException;

/**
 * Task that can be retried on error.
 *
 * @author JPEXS
 */
public class RetryTask {

    /**
     * Runnable that can throw IOException.
     */
    private final RunnableIOEx r;

    /**
     * Handler for retrying.
     */
    private final AbortRetryIgnoreHandler handler;

    /**
     * Constructs a new RetryTask.
     *
     * @param r Runnable that can throw IOException
     * @param handler Handler for retrying
     */
    public RetryTask(RunnableIOEx r, AbortRetryIgnoreHandler handler) {
        this.r = r;
        this.handler = handler;
    }

    /**
     * Runs the task.
     *
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public void run() throws IOException, InterruptedException {
        boolean retry;
        do {
            retry = false;
            try {
                r.run();
            } catch (InterruptedException ex) {
                throw ex;
            } catch (Exception ex) {
                if (handler == null) {
                    throw ex;
                }

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
