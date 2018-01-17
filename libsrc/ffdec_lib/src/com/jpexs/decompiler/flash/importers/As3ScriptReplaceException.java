/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.importers;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class As3ScriptReplaceException extends Exception {

    private List<As3ScriptReplaceExceptionItem> exceptionItems;

    public As3ScriptReplaceException(List<As3ScriptReplaceExceptionItem> exceptionItems) {
        this.exceptionItems = exceptionItems;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        for (As3ScriptReplaceExceptionItem item : exceptionItems) {
            sb.append(item.toString()).append("\r\n");
        }
        return sb.toString();
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    public As3ScriptReplaceException(As3ScriptReplaceExceptionItem exceptionItem) {
        this.exceptionItems = new ArrayList<>();
        this.exceptionItems.add(exceptionItem);
    }

    public List<As3ScriptReplaceExceptionItem> getExceptionItems() {
        return exceptionItems;
    }

}
