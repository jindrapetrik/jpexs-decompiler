package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.iggy.IggyShape;
import com.jpexs.decompiler.flash.iggy.IggyShapeNode;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class SwfShapeToIggyConvertor {

    private static float normalizeLengths(int val) {
        return (val / 1024f);
    }

    public static IggyShape convertShape(SHAPE swfShape) {
        /*if (swfShape.shapeRecords.size() == 1) { //no glyphs, maybe space
            return null;
        }*/
        List<IggyShapeNode> nodes = new ArrayList<>();
        RECT bounds = swfShape.getBounds();
        boolean first = true;
        float curX = 0f;
        float curY = 0f;
        for (SHAPERECORD rec : swfShape.shapeRecords) {
            if (rec instanceof StraightEdgeRecord) {
                StraightEdgeRecord ser = (StraightEdgeRecord) rec;
                curX += normalizeLengths(ser.deltaX);
                curY += normalizeLengths(ser.deltaY);
                nodes.add(new IggyShapeNode(curX, curY, 0f, 0f, IggyShapeNode.NODE_TYPE_LINE_TO, 0, first));
            } else if (rec instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                float controlX = curX + normalizeLengths(cer.controlDeltaX);
                float controlY = curY + normalizeLengths(cer.controlDeltaY);
                curX = curX + normalizeLengths(cer.controlDeltaX + cer.anchorDeltaX);
                curY = curY + normalizeLengths(cer.controlDeltaY + cer.anchorDeltaY);
                nodes.add(new IggyShapeNode(curX, curY, controlX, controlY, IggyShapeNode.NODE_TYPE_CURVE_POINT, 0, first));
            } else if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                if (scr.stateMoveTo) {
                    curX = normalizeLengths(scr.moveDeltaX);
                    curY = normalizeLengths(scr.moveDeltaY);
                    nodes.add(new IggyShapeNode(curX, curY, 0f, 0f, IggyShapeNode.NODE_TYPE_MOVE, 0, first));
                }
            }
            first = false;
        }
        IggyShape ret = new IggyShape(normalizeLengths(bounds.Xmin), normalizeLengths(bounds.Ymin), normalizeLengths(bounds.Xmax), normalizeLengths(bounds.Ymax), nodes);
        return ret;
    }
}
