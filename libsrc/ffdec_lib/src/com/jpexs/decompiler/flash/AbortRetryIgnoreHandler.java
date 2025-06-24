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

/**
 * A handler for abort, retry, ignore, ignore all dialog
 *
 * @author JPEXS
 */
public interface AbortRetryIgnoreHandler {

    /**
     * Undefined result
     */
    public static int UNDEFINED = -1;

    /**
     * Abort result
     */
    public static int ABORT = 0;

    /**
     * Retry result
     */
    public static int RETRY = 1;

    /**
     * Ignore result
     */
    public static int IGNORE = 2;

    /**
     * Ignore all result
     */
    public static int IGNORE_ALL = 3;

    /**
     * Handles the thrown exception
     *
     * @param thrown The thrown exception
     * @return The result
     */
    public int handle(Throwable thrown);

    /**
     * Returns a new instance of this handler
     *
     * @return A new instance of this handler
     */
    public AbortRetryIgnoreHandler getNewInstance();
}
