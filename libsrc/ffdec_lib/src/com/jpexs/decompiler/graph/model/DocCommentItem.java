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
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;

/**
 * AsDoc comment item. Can contain tags starting with at sign.
 *
 * @author JPEXS
 */
public class DocCommentItem extends GraphTargetItem {

    /**
     * Comment lines.
     */
    private final String[] commentLines;

    /**
     * Constructor.
     * @param comment Comment
     */
    public DocCommentItem(String comment) {
        super(null, null, null, NOPRECEDENCE);
        this.commentLines = new String[]{comment};
    }

    /**
     * Constructor.
     * @param commentLines Comment lines
     */
    public DocCommentItem(String[] commentLines) {
        super(null, null, null, NOPRECEDENCE);
        this.commentLines = commentLines;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        int commentLinesCount = 0;
        for (int i = 0; i < commentLines.length; i++) {
            if (commentLines[i] == null) {
                continue;
            }
            commentLinesCount++;
        }
        writer.append("/** ");
        writer.newLine();
        for (int i = 0; i < commentLines.length; i++) {
            if (commentLines[i] == null) {
                continue;
            }
            writer.append(" * ");
            writer.append(commentLines[i]);
            writer.newLine();
        }
        writer.append(" */");
        writer.newLine();
        return writer;
    }

    /**
     * Gets comment lines.
     * @return Comment lines
     */
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
