/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.action.swf4.ActionGotoFrame2;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;

public class GotoFrame2ActionItem extends ActionItem {

    public GraphTargetItem frame;
    public boolean sceneBiasFlag;
    public boolean playFlag;
    public int sceneBias;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(frame);
        return ret;
    }

    public GotoFrame2ActionItem(GraphSourceItem instruction, GraphTargetItem frame, boolean sceneBiasFlag, boolean playFlag, int sceneBias) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.frame = frame;
        this.sceneBiasFlag = sceneBiasFlag;
        this.playFlag = playFlag;
        this.sceneBias = sceneBias;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants) {
        String prefix = "gotoAndStop";
        if (playFlag) {
            prefix = "gotoAndPlay";
        }
        return hilight(prefix + "(", highlight) + frame.toString(highlight, constants) + hilight((sceneBiasFlag ? "," + sceneBias : ""), highlight) + hilight(")", highlight);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(frame.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, frame, new ActionGotoFrame2(playFlag, sceneBiasFlag, sceneBias));
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
