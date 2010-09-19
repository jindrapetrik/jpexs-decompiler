/*
 *  Copyright (C) 2010 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.abc.avm2.flowgraph;

/**
 *
 * @author JPEXS
 */
public class GraphBlock extends GraphPart {
    public int end;    

    public GraphBlock(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean contains(int ip){
        return ip>=start && ip<=end;
    }

    @Override
    public String toString() {
        return "Block "+(start+1)+"-"+(end+1)+(linkCount>0?" ("+linkCount+" links)":"");
    }


}
