package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.helpers.SerializableImage;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface GraphicsTextDrawable {
    public void drawTextRecords(SWF swf, List<TEXTRECORD> textRecords, int numText, MATRIX textMatrix, Matrix transformation, ColorTransform colorTransform);
}
