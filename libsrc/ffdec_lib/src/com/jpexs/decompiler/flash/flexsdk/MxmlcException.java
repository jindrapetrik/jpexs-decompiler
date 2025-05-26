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
package com.jpexs.decompiler.flash.flexsdk;

/**
 * Exception thrown when mxmlc fails to compile a file.
 */
public class MxmlcException extends Exception {

    private String mxmlcErrorOutput;

    /**
     * Constructor.
     * @param mxmlcErrorOutput Output of mxmlc command.
     */
    public MxmlcException(String mxmlcErrorOutput) {
        this.mxmlcErrorOutput = mxmlcErrorOutput;
    }

    /**
     * Gets the output of mxmlc command.
     * @return Output of mxmlc command.
     */
    public String getMxmlcErrorOutput() {
        return mxmlcErrorOutput;
    }

}
