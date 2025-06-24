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
package com.jpexs.decompiler.flash.tags.base;

/**
 * Interface for tags that can have a password.
 *
 * @author JPEXS
 */
public interface PasswordTag {

    /**
     * Sets the password for this tag.
     *
     * @param password Password
     */
    public void setPassword(String password);

    /**
     * Checks if this tag has a password.
     *
     * @param password Password
     * @return True if this tag has the password
     */
    public boolean hasPassword(String password);
}
