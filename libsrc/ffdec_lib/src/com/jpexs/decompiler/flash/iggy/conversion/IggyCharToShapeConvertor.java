package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.IggyChar;
import com.jpexs.decompiler.flash.iggy.IggyCharNode;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggyCharToShapeConvertor {

    private static int convertDistance(double val) {
        return (int) (val * SWF.unitDivisor);
    }

    public static SHAPE convertCharToShape(IggyChar igchar) {
        SHAPE shape = new SHAPE();
        List<SHAPERECORD> retList = new ArrayList<>();
        List<IggyCharNode> ignodes = igchar.getNodes();
        for (IggyCharNode ign : ignodes) {
            if (ign.getNodeType() == IggyCharNode.NODE_TYPE_MOVE) {
                StyleChangeRecord scr = new StyleChangeRecord();
                scr.fillStyle0 = 1;
                scr.stateFillStyle0 = true;
                scr.stateMoveTo = true;
                scr.moveDeltaX = convertDistance(ign.getX1());
                scr.moveDeltaY = convertDistance(ign.getY1());
                scr.lineStyles = new LINESTYLEARRAY();
                scr.fillStyles = new FILLSTYLEARRAY();
                scr.calculateBits();
                retList.add(scr);
            }
            if (ign.getNodeType() == IggyCharNode.NODE_TYPE_LINE_TO) {
                StraightEdgeRecord ser = new StraightEdgeRecord();
                ser.deltaX = convertDistance(ign.getX2() - ign.getX1());
                ser.deltaY = convertDistance(ign.getY2() - ign.getY1());
                ser.generalLineFlag = true;
                ser.vertLineFlag = false;
                ser.calculateBits();
                retList.add(ser);
            }

            if (ign.getNodeType() == IggyCharNode.NODE_TYPE_CURVE_POINT) {
                //TODO: Make curve record
                StraightEdgeRecord ser = new StraightEdgeRecord();
                ser.deltaX = convertDistance(ign.getX2() - ign.getX1());
                ser.deltaY = convertDistance(ign.getY2() - ign.getY1());
                ser.generalLineFlag = true;
                ser.vertLineFlag = false;
                ser.calculateBits();
                retList.add(ser);
            }
        }

        StyleChangeRecord init;
        if (!retList.isEmpty() && retList.get(0) instanceof StyleChangeRecord) {
            init = (StyleChangeRecord) retList.get(0);
        } else {
            init = new StyleChangeRecord();
            retList.add(0, init);
        }

        retList.add(new EndShapeRecord());
        init.stateFillStyle0 = true;
        init.fillStyle0 = 1;
        shape.shapeRecords = retList;
        shape.numFillBits = 1;
        shape.numLineBits = 0;

        return shape;
    }
}
