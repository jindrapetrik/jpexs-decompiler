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
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;

/**
 *
 * @author JPEXS
 */
public class CommentItem extends GraphTargetItem {

    private final String[] commentLines;

    public CommentItem(String comment) {
        super(null, null, NOPRECEDENCE);
        this.commentLines = new String[]{comment};
    }

    public CommentItem(String[] commentLines) {
        super(null, null, NOPRECEDENCE);
        this.commentLines = commentLines;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        writer.append("/* ");
        for (int i = 0; i < commentLines.length; i++) {
            if (commentLines[i] == null) {
                continue;
            }
            writer.append(commentLines[i]);
            if (i != commentLines.length - 1) {
                writer.newLine();
            }
        }
        return writer.append(" */");
    }

    public String[] getCommentLines() {
        return commentLines;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }
}
