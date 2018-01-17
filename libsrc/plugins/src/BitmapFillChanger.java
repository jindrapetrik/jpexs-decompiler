
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerListener;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BitmapFillChanger implements SWFDecompilerListener {

    @Override
    public byte[] proxyFileCatched(byte[] data) {
        return null;
    }

    @Override
    public void actionListParsed(ActionList actions, SWF swf) {
    }

    @Override
    public void actionTreeCreated(List<GraphTargetItem> tree, SWF swf) {
    }

    @Override
    public void swfParsed(SWF swf) {
        String[] custom = SWFDecompilerPlugin.customParameters;
        if (custom == null || custom.length == 0) {
            // change it in command line mode only
            return;
        }

        try {
            for (Tag tag : swf.getTags()) {
                if (tag instanceof ShapeTag) {
                    boolean modified = false;
                    SHAPEWITHSTYLE shapes = ((ShapeTag) tag).getShapes();
                    for (FILLSTYLE fillStyle : shapes.fillStyles.fillStyles) {
                        modified |= changeFillStyle(fillStyle);
                    }

                    for (SHAPERECORD shapeRecord : shapes.shapeRecords) {
                        if (shapeRecord instanceof StyleChangeRecord) {
                            StyleChangeRecord scr = (StyleChangeRecord) shapeRecord;
                            if (scr.stateNewStyles) {
                                for (FILLSTYLE fillStyle : scr.fillStyles.fillStyles) {
                                    modified |= changeFillStyle(fillStyle);
                                }
                            }
                        }
                    }

                    if (modified) {
                        tag.setModified(true);
                    }
                }
            }

            swf.saveTo(new FileOutputStream(swf.getFile() + ".patched.swf"));
        } catch (IOException ex) {
            Logger.getLogger(BitmapFillChanger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean changeFillStyle(FILLSTYLE fillStyle) {
        if (fillStyle.fillStyleType == FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP) {
            fillStyle.fillStyleType = FILLSTYLE.CLIPPED_BITMAP;
            return true;
        }

        if (fillStyle.fillStyleType == FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP) {
            fillStyle.fillStyleType = FILLSTYLE.REPEATING_BITMAP;
            return true;
        }

        return false;
    }

    @Override
    public void abcParsed(ABC abc, SWF swf) {
    }

    @Override
    public void methodBodyParsed(ABC abc, MethodBody body, SWF swf) {
    }

    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
    }
}
