/*
 *  Copyright (C) 2010-2014 PEXS
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
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;

/**
 *
 * @author JPEXS
 */
public class CommentItem extends GraphTargetItem {

    private final String[] commentLines;

    public CommentItem(String comment) {
        super(null, NOPRECEDENCE);
        this.commentLines = new String[]{comment};
    }

    public CommentItem(String[] commentLines) {
        super(null, NOPRECEDENCE);
        this.commentLines = commentLines;
    }

    @Override
    protected GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        writer.append("/* ");
        for (int i = 0; i < commentLines.length; i++) {
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
}
