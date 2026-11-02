/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.fill;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.rectangle;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.selection;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.shape;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.helpers.Helper;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.util.Collections;
import javax.swing.SwingUtilities;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ShapeSceneEditTest {

    static PlaceObject2Tag place(Timelined timeline, int id, int depth, Matrix matrix) {
        PlaceObject2Tag place = new PlaceObject2Tag(timeline.getSwf());
        place.characterId = id;
        place.depth = depth;
        place.placeFlagHasCharacter = true;
        place.placeFlagHasMatrix = true;
        place.matrix = matrix.toMATRIX();
        timeline.addTag(place);
        return place;
    }

    static DefineShape4Tag scene() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 4000, 2400, 1, 1);
        selection(shape);
        shape.updateBounds();
        SWF swf = shape.getSwf();
        swf.version = 15;
        swf.frameRate = 24;
        swf.frameCount = 1;
        swf.displayRect = new RECT(0, 12000, 0, 8000);
        swf.addTag(shape);
        DefineShape4Tag background = new DefineShape4Tag(swf);
        background.shapes = Helper.deepCopy(shape.shapes);
        background.shapes.fillStyles.fillStyles[0] = fill(Color.blue);
        background.updateBounds();
        swf.addTag(background);
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        sprite.frameCount = 1;
        place(sprite, background.shapeId, 1, Matrix.getTranslateInstance(4500, 1500));
        place(sprite, shape.shapeId, 2, new Matrix(1, 0.8, 0.25, -0.35, 1800, 1000));
        sprite.addTag(new ShowFrameTag(swf));
        swf.addTag(sprite);
        place(swf, background.shapeId, 1, Matrix.getTranslateInstance(500, 4500));
        place(swf, sprite.spriteId, 2, new Matrix(1, 1, 0.1, 0.15, 1000, 500));
        swf.addTag(new ShowFrameTag(swf));
        return shape;
    }

    @Test
    public void sceneMatrixViewportAndInstancePathSurviveStyleUndoAndRedo() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            ImagePanel panel = new ImagePanel();
            try {
                DefineShape4Tag shape = scene();
                SWF swf = shape.getSwf();
                panel.setSize(800, 600);
                panel.doLayout();
                panel.setSelectionMode(true);
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                panel.openDepth(0, 2);
                Timelined sprite = panel.getTimelined();
                Matrix beforeMatrix = panel.getParentMatrix().concatenate(new Matrix(sprite.getTimeline().getDepthState(0, 2).matrix));
                Point2D localPoint = new Point2D.Double(1000, 700);
                Point2D screenPoint = panel.getTimelinedToPanelTransform().transform(
                        new Matrix(sprite.getTimeline().getDepthState(0, 2).matrix).transform(localPoint), null);
                panel.openDepth(0, 2);
                assertSame(panel.getEditedShape(), shape);
                assertEquals(panel.getParentMatrix().toTransform(), beforeMatrix.toTransform());
                Point2D afterPoint = panel.getTimelinedToPanelTransform().transform(localPoint, null);
                assertEquals(afterPoint.getX(), screenPoint.getX(), 1e-6);
                assertEquals(afterPoint.getY(), screenPoint.getY(), 1e-6);
                assertEquals(panel.toTimelinedPoint(screenPoint).distance(localPoint), 0.0, 1e-6);
                Rectangle2D screenBox = new Rectangle2D.Double(screenPoint.getX() - 20, screenPoint.getY() - 10, 40, 20);
                Shape localBox = panel.toTimelinedArea(screenBox);
                AffineTransform transform = panel.getTimelinedToPanelTransform();
                assertEquals(transform.createTransformedShape(localBox).getBounds2D().getWidth(), 40.0, 1e-6);
                assertEquals(transform.createTransformedShape(localBox).getBounds2D().getHeight(), 20.0, 1e-6);

                ImagePanel.ShapeEditContext context = panel.getShapeEditContext();
                ShapeSelection model = new ShapeSelection(shape);
                ShapeSelection.BatchEdit edit = model.changeAll(Collections.singletonList(model.select(localPoint, 0)), item -> fill(Color.green), null);
                UndoManager history = new UndoManager();
                history.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Fill", (tag, forward) -> panel.restoreShapeEditContext(tag, context)), swf);
                assertTrue(context.sameLocation(panel.getShapeEditContext()));
                assertEquals(panel.getTimelinedToPanelTransform(), transform);
                assertSame(panel.getEditedShape(), swf.getCharacter(shape.shapeId));
                assertTrue(panel.closeShapeEdit());
                assertSame(panel.getTimelined(), sprite);
                assertNull(panel.getEditedShape());
                history.undo(swf);
                assertSame(panel.getEditedShape(), shape);
                assertEquals(panel.getTimelinedToPanelTransform(), transform);
                history.redo(swf);
                assertTrue(context.sameLocation(panel.getShapeEditContext()));
                assertEquals(panel.getParentMatrix().toTransform(), beforeMatrix.toTransform());
                assertTrue(panel.closeShapeEdit());
                assertFalse(panel.closeShapeEdit());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        ShapeTag shape = scene();
        try (FileOutputStream output = new FileOutputStream(args[0])) {
            shape.getSwf().saveTo(output);
        }
    }
}
