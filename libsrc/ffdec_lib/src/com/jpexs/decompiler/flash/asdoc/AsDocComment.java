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
package com.jpexs.decompiler.flash.asdoc;

import java.util.List;

/**
 *
 * @author JPEXS
 */
public class AsDocComment {
    public String startText;
    public List<AsDocTag> tags;   

    public AsDocComment(String startText, List<AsDocTag> tags) {
        this.startText = startText;
        this.tags = tags;
    }        

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (startText != null) {
            sb.append(startText);
            sb.append("\n");
        }
        for (AsDocTag tag : tags) {
            sb.append("@");
            sb.append(tag.tagName);
            sb.append(" ");
            sb.append(tag.tagText);
            sb.append("\n");
        }
        return sb.toString();
    }        
}
