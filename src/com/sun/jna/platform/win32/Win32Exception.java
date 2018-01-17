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
package com.sun.jna.platform.win32;

/**
 *
 * @author JPEXS
 */
/* Copyright (c) 2010 Daniel Doubrovkine, All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
import com.sun.jna.platform.win32.WinNT.HRESULT;

/**
 * Win32 exception.
 *
 * @author dblock[at]dblock[dot]org
 */
public class Win32Exception extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private HRESULT _hr;

    /**
     * Returns the error code of the error.
     *
     * @return Error code.
     */
    public HRESULT getHR() {
        return _hr;
    }

    /**
     * New Win32 exception from HRESULT.
     *
     * @param hr HRESULT
     */
    public Win32Exception(HRESULT hr) {
        //super(Kernel32Util.formatMessageFromHR(hr));
        _hr = hr;
    }

    /**
     * New Win32 exception from an error code, usually obtained from
     * GetLastError.
     *
     * @param code Error code.
     */
    public Win32Exception(int code) {
        this(W32Errors.HRESULT_FROM_WIN32(code));
    }
}
