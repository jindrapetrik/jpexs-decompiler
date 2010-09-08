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

package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class GetURL2TreeItem extends TreeItem {

    public TreeItem urlString;
    public TreeItem targetString;
    public int method;
    public boolean loadTargetFlag;
    public boolean loadVariablesFlag;


    public String toString(ConstantPool constants) {
        String methodStr = "";
        if (method == 1) methodStr = ",\"GET\"";
        if (method == 2) methodStr = ",\"POST\"";
        String prefix = "getUrl";
        if (loadVariablesFlag) prefix = "loadVariables";
        if (loadTargetFlag && (!loadVariablesFlag)) prefix = "loadMovie";

        return prefix + "(" + urlString.toString(constants) + "," + targetString.toString(constants) + methodStr + ");";
    }

    public GetURL2TreeItem(Action instruction, TreeItem urlString, TreeItem targetString, int method, boolean loadTargetFlag, boolean loadVariablesFlag) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.urlString = urlString;
        this.targetString = targetString;
        this.method = method;
        this.loadTargetFlag = loadTargetFlag;
        this.loadVariablesFlag = loadVariablesFlag;
    }
}
