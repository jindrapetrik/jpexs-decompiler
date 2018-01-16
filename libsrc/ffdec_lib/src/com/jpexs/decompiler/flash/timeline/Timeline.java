/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.FrameExporter;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.StartSound2Tag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ASMSourceContainer;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.filters.BlendComposite;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.w3c.dom.Element;

/**
 *
 * @author JPEXS
 */
public class Timeline {

    public int id;

    public SWF swf;

    public RECT displayRect;

    public float frameRate;

    public Timelined timelined;

    public int maxDepth;

    public int fontFrameNum = -1;

    private final List<Frame> frames = new ArrayList<>();

    private final Map<Integer, Integer> depthMaxFrame = new HashMap<>();

    private final List<ASMSource> asmSources = new ArrayList<>();

    private final List<ASMSourceContainer> asmSourceContainers = new ArrayList<>();

    private final Map<ASMSource, Integer> actionFrames = new HashMap<>();

    private final Map<SoundStreamHeadTypeTag, List<SoundStreamBlockTag>> soundStramBlocks = new HashMap<>();

    private AS2Package as2RootPackage;

    public final List<Tag> otherTags = new ArrayList<>();

    private boolean initialized = false;

    private Map<String, Integer> labelToFrame = new HashMap<>();

    private void ensureInitialized() {
        if (!initialized) {
            initialize();
            initialized = true;
        }
    }

    public List<Frame> getFrames() {
        ensureInitialized();
        return frames;
    }

    public Frame getFrame(int index) {
        ensureInitialized();
        if (index >= frames.size()) {
            return null;
        }
        return frames.get(index);
    }

    public void addFrame(Frame frame) {
        ensureInitialized();
        frames.add(frame);
        maxDepth = getMaxDepthInternal();
        calculateMaxDepthFrames();
    }

    public AS2Package getAS2RootPackage() {
        ensureInitialized();
        return as2RootPackage;
    }

    public Map<Integer, Integer> getDepthMaxFrame() {
        ensureInitialized();
        return depthMaxFrame;
    }

    public List<SoundStreamBlockTag> getSoundStreamBlocks(SoundStreamHeadTypeTag head) {
        ensureInitialized();
        return soundStramBlocks.get(head);
    }

    public Tag getParentTag() {
        return timelined instanceof Tag ? (Tag) timelined : null;
    }

    public void reset(SWF swf) {
        reset(swf, swf, 0, swf.displayRect);
    }

    public void reset(SWF swf, Timelined timelined, int id, RECT displayRect) {
        initialized = false;
        frames.clear();
        depthMaxFrame.clear();
        asmSources.clear();
        asmSourceContainers.clear();
        actionFrames.clear();
        soundStramBlocks.clear();
        otherTags.clear();
        this.id = id;
        this.swf = swf;
        this.displayRect = displayRect;
        this.frameRate = swf.frameRate;
        this.timelined = timelined;
        as2RootPackage = new AS2Package(null, null, swf);
    }

    public final int getMaxDepth() {
        ensureInitialized();
        return maxDepth;
    }

    private int getMaxDepthInternal() {
        int max_depth = 0;
        for (Frame f : frames) {
            for (int depth : f.layers.keySet()) {
                if (depth > max_depth) {
                    max_depth = depth;
                }
                int clipDepth = f.layers.get(depth).clipDepth;
                if (clipDepth > max_depth) {
                    max_depth = clipDepth;
                }
            }
        }
        return max_depth;
    }

    public int getFrameCount() {
        ensureInitialized();
        return frames.size();
    }

    public int getRealFrameCount() {
        ensureInitialized();

        int cnt = 1;
        for (int i = 1; i < frames.size(); i++) {
            if (!frames.get(i).actions.isEmpty()) {
                cnt++;
                continue;
            }
            if (frames.get(i).layersChanged) {
                cnt++;
            }
        }

        return cnt;
    }

    public int getFrameForAction(ASMSource asm) {
        Integer frame = actionFrames.get(asm);
        if (frame == null) {
            return -1;
        }

        return frame;
    }

    public Timeline(SWF swf) {
        this(swf, swf, 0, swf.displayRect);
    }

    public Timeline(SWF swf, Timelined timelined, int id, RECT displayRect) {
        this.id = id;
        this.swf = swf;
        this.displayRect = displayRect;
        this.frameRate = swf.frameRate;
        this.timelined = timelined;
        as2RootPackage = new AS2Package(null, null, swf);
    }

    public int getFrameWithLabel(String label) {
        if (labelToFrame.containsKey(label)) {
            return labelToFrame.get(label);
        }
        return -1;
    }

    private void initialize() {
        int frameIdx = 0;
        Frame frame = new Frame(this, frameIdx++);
        frame.layersChanged = true;
        boolean newFrameNeeded = false;
        for (Tag t : timelined.getTags()) {
            boolean isNested = ShowFrameTag.isNestedTagType(t.getId());
            if (isNested) {
                newFrameNeeded = true;
                frame.innerTags.add(t);
            }

            if (t instanceof ASMSourceContainer) {
                ASMSourceContainer asmSourceContainer = (ASMSourceContainer) t;
                if (!asmSourceContainer.getSubItems().isEmpty()) {
                    if (isNested) {
                        frame.actionContainers.add(asmSourceContainer);
                    } else {
                        asmSourceContainers.add(asmSourceContainer);
                    }
                }
            }

            if (t instanceof FrameLabelTag) {
                newFrameNeeded = true;
                frame.label = ((FrameLabelTag) t).getLabelName();
                frame.namedAnchor = ((FrameLabelTag) t).isNamedAnchor();
                labelToFrame.put(frame.label, frames.size());
            } else if (t instanceof StartSoundTag) {
                newFrameNeeded = true;
                frame.sounds.add(((StartSoundTag) t).soundId);
            } else if (t instanceof StartSound2Tag) {
                newFrameNeeded = true;
                frame.soundClasses.add(((StartSound2Tag) t).soundClassName);
            } else if (t instanceof SetBackgroundColorTag) {
                newFrameNeeded = true;
                frame.backgroundColor = ((SetBackgroundColorTag) t).backgroundColor;
            } else if (t instanceof PlaceObjectTypeTag) {
                newFrameNeeded = true;
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                int depth = po.getDepth();
                DepthState fl = frame.layers.get(depth);
                if (fl == null) {
                    frame.layers.put(depth, fl = new DepthState(swf, frame));
                }
                frame.layersChanged = true;
                fl.placeObjectTag = po;
                fl.minPlaceObjectNum = Math.max(fl.minPlaceObjectNum, po.getPlaceObjectNum());
                int characterId = po.getCharacterId();
                if (characterId != -1) {
                    fl.characterId = characterId;
                }
                if (po.flagMove()) {
                    MATRIX matrix2 = po.getMatrix();
                    if (matrix2 != null) {
                        fl.matrix = matrix2;
                    }
                    String instanceName2 = po.getInstanceName();
                    if (instanceName2 != null) {
                        fl.instanceName = instanceName2;
                    }
                    ColorTransform colorTransForm2 = po.getColorTransform();
                    if (colorTransForm2 != null) {
                        fl.colorTransForm = colorTransForm2;
                    }

                    CLIPACTIONS clipActions2 = po.getClipActions();
                    if (clipActions2 != null) {
                        fl.clipActions = clipActions2;
                    }
                    if (po.cacheAsBitmap()) {
                        fl.cacheAsBitmap = true;
                    }
                    int blendMode2 = po.getBlendMode();
                    if (blendMode2 > 0) {
                        fl.blendMode = blendMode2;
                    }
                    List<FILTER> filters2 = po.getFilters();
                    if (filters2 != null) {
                        fl.filters = filters2;
                    }
                    int ratio2 = po.getRatio();
                    if (ratio2 > -1) {
                        fl.ratio = ratio2;
                    }
                    int clipDepth2 = po.getClipDepth();
                    if (clipDepth2 > -1) {
                        fl.clipDepth = clipDepth2;
                    }
                } else {
                    fl.matrix = po.getMatrix();
                    fl.instanceName = po.getInstanceName();
                    fl.colorTransForm = po.getColorTransform();
                    fl.cacheAsBitmap = po.cacheAsBitmap();
                    fl.blendMode = po.getBlendMode();
                    fl.filters = po.getFilters();
                    fl.ratio = po.getRatio();
                    fl.clipActions = po.getClipActions();
                    fl.clipDepth = po.getClipDepth();
                }
                fl.key = characterId != -1;
            } else if (t instanceof RemoveTag) {
                newFrameNeeded = true;
                RemoveTag r = (RemoveTag) t;
                int depth = r.getDepth();
                frame.layers.remove(depth);
                frame.layersChanged = true;
            } else if (t instanceof DoActionTag) {
                newFrameNeeded = true;
                frame.actions.add((DoActionTag) t);
                actionFrames.put((DoActionTag) t, frame.frame);
            } else if (t instanceof ShowFrameTag) {
                frame.showFrameTag = (ShowFrameTag) t;
                frames.add(frame);
                frame = new Frame(frame, frameIdx++);
                newFrameNeeded = false;
            } else if (t instanceof ASMSource) {
                asmSources.add((ASMSource) t);
            } else {
                otherTags.add(t);
            }
        }
        if (newFrameNeeded) {
            frames.add(frame);
        }

        maxDepth = getMaxDepthInternal();

        detectTweens();
        calculateMaxDepthFrames();

        createASPackages();
        if (timelined instanceof SWF) {
            // popuplate only for main timeline
            populateSoundStreamBlocks(0, timelined.getTags());
        }

        initialized = true;
    }

    private void detectTweens() {
        for (int d = 1; d <= maxDepth; d++) {
            int characterId = -1;
            int len = 0;
            for (int f = 0; f <= frames.size(); f++) {
                DepthState ds = f >= frames.size() ? null : frames.get(f).layers.get(d);

                if (ds != null && characterId != -1 && ds.characterId == characterId) {
                    len++;
                } else {
                    if (characterId != -1) {
                        int startPos = f - len;
                        List<DepthState> matrices = new ArrayList<>(len);
                        for (int k = 0; k < len; k++) {
                            matrices.add(frames.get(startPos + k).layers.get(d));
                        }

                        List<TweenRange> ranges = TweenDetector.detectRanges(matrices);
                        for (TweenRange r : ranges) {
                            for (int t = r.startPosition; t <= r.endPosition; t++) {
                                DepthState layer = frames.get(startPos + t).layers.get(d);
                                layer.motionTween = true;
                                layer.key = false;
                            }

                            frames.get(startPos + r.startPosition).layers.get(d).key = true;
                        }
                    }

                    len = 1;
                }

                characterId = ds == null ? -1 : ds.characterId;
            }
        }
    }

    private void calculateMaxDepthFrames() {
        depthMaxFrame.clear();
        for (int d = 1; d <= maxDepth; d++) {
            for (int f = frames.size() - 1; f >= 0; f--) {
                if (frames.get(f).layers.get(d) != null) {
                    depthMaxFrame.put(d, f);
                    break;
                }
            }
        }
    }

    private void createASPackages() {
        for (ASMSource asm : asmSources) {
            if (asm instanceof DoInitActionTag) {
                DoInitActionTag initAction = (DoInitActionTag) asm;
                String path = swf.getExportName(initAction.spriteId);
                path = path != null ? path : "_unk_";
                if (path.isEmpty()) {
                    path = initAction.getExportFileName();
                }

                String[] pathParts = path.contains(".") ? path.split("\\.") : new String[]{path};
                AS2Package pkg = as2RootPackage;
                for (int pos = 0; pos < pathParts.length - 1; pos++) {
                    String pathPart = pathParts[pos];
                    AS2Package subPkg = pkg.subPackages.get(pathPart);
                    if (subPkg == null) {
                        subPkg = new AS2Package(pathPart, pkg, swf);
                        pkg.subPackages.put(pathPart, subPkg);
                    }

                    pkg = subPkg;
                }

                pkg.scripts.put(pathParts[pathParts.length - 1], asm);
            }
        }
    }

    private void populateSoundStreamBlocks(int containerId, Iterable<Tag> tags) {
        List<SoundStreamBlockTag> blocks = null;
        for (Tag t : tags) {
            if (t instanceof SoundStreamHeadTypeTag) {
                SoundStreamHeadTypeTag head = (SoundStreamHeadTypeTag) t;
                head.setVirtualCharacterId(containerId);
                blocks = new ArrayList<>();
                soundStramBlocks.put(head, blocks);
                continue;
            }

            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) t;
                populateSoundStreamBlocks(sprite.getCharacterId(), sprite.getTags());
            }

            if (blocks == null) {
                continue;
            }

            if (t instanceof SoundStreamBlockTag) {
                blocks.add((SoundStreamBlockTag) t);
            }
        }
    }

    public void getNeededCharacters(Set<Integer> usedCharacters) {
        for (int i = 0; i < getFrameCount(); i++) {
            getNeededCharacters(i, usedCharacters);
        }
    }

    public void getNeededCharacters(List<Integer> frames, Set<Integer> usedCharacters) {
        for (int frame = 0; frame < getFrameCount(); frame++) {
            getNeededCharacters(frame, usedCharacters);
        }
    }

    public void getNeededCharacters(int frame, Set<Integer> usedCharacters) {
        Frame frameObj = getFrame(frame);
        for (int depth : frameObj.layers.keySet()) {
            DepthState layer = frameObj.layers.get(depth);
            if (layer.characterId != -1) {
                if (!swf.getCharacters().containsKey(layer.characterId)) {
                    continue;
                }
                usedCharacters.add(layer.characterId);
                swf.getCharacter(layer.characterId).getNeededCharactersDeep(usedCharacters);
            }
        }
    }

    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = false;
        for (int i = 0; i < timelined.getTags().size(); i++) {
            Tag t = timelined.getTags().get(i);
            if (t instanceof CharacterIdTag && ((CharacterIdTag) t).getCharacterId() == oldCharacterId) {
                ((CharacterIdTag) t).setCharacterId(newCharacterId);
                ((Tag) t).setModified(true);
                modified = true;
            }
        }
        return modified;
    }

    public boolean removeCharacter(int characterId) {
        boolean modified = false;
        for (int i = 0; i < timelined.getTags().size(); i++) {
            Tag t = timelined.getTags().get(i);
            if (t instanceof CharacterIdTag && ((CharacterIdTag) t).getCharacterId() == characterId) {
                timelined.removeTag(i);
                i--;
                modified = true;
            }
        }
        return modified;
    }

    public double roundToPixel(double val) {
        return Math.rint(val / SWF.unitDivisor) * SWF.unitDivisor;
    }

    /*public void toImage(int frame, int time, RenderContext renderContext, SerializableImage image, boolean isClip, Matrix transformation, Matrix prevTransformation, Matrix absoluteTransformation, ColorTransform colorTransform) {
     ExportRectangle scalingGrid = null;
     if (timelined instanceof CharacterTag) {
     DefineScalingGridTag sgt = ((CharacterTag) timelined).getScalingGridTag();
     if (sgt != null) {
     scalingGrid = new ExportRectangle(sgt.splitter);
     }
     }

     if (scalingGrid == null || transformation.rotateSkew0 != 0 || transformation.rotateSkew1 != 0) {
     toImage(frame, time, renderContext, image, isClip, transformation, absoluteTransformation, colorTransform, null);
     return;
     }

     //9-slice scaling using DefineScalingGrid
     Matrix diffTransform = prevTransformation.inverse().preConcatenate(transformation);
     transformation = diffTransform;

     Matrix prevScale = new Matrix();
     prevScale.scaleX = prevTransformation.scaleX;
     prevScale.scaleY = prevTransformation.scaleY;

     ExportRectangle boundsRect = new ExportRectangle(timelined.getRect());


     0 |  1  | 2
     ------------
     3 |  4  | 5
     ------------
     6 |  7  | 8

     ExportRectangle[] targetRect = new ExportRectangle[9];
     ExportRectangle[] sourceRect = new ExportRectangle[9];
     Matrix[] transforms = new Matrix[9];

     DefineScalingGridTag.getSlices(transformation, prevScale, boundsRect, scalingGrid, sourceRect, targetRect, transforms);

     for (int i = 0; i < targetRect.length; i++) {
     toImage(frame, time, renderContext, image, isClip, transforms[i], absoluteTransformation, colorTransform, targetRect[i]);
     }
     }*/
    private void drawDrawable(Matrix strokeTransform, DepthState layer, Matrix layerMatrix, Graphics2D g, ColorTransform colorTransForm, int blendMode, List<Clip> clips, Matrix transformation, boolean isClip, int clipDepth, Matrix absMat, int time, int ratio, RenderContext renderContext, SerializableImage image, DrawableTag drawable, List<FILTER> filters, double unzoom, ColorTransform clrTrans) {
        Matrix drawMatrix = new Matrix();
        int drawableFrameCount = drawable.getNumFrames();
        if (drawableFrameCount == 0) {
            drawableFrameCount = 1;
        }

        RECT boundRect = drawable.getRect();

        ExportRectangle rect = new ExportRectangle(boundRect);
        Matrix mat = transformation.concatenate(layerMatrix);
        rect = mat.transform(rect);

        boolean cacheAsBitmap = layer.cacheAsBitmap() && layer.placeObjectTag != null && drawable.isSingleFrame();
        /* // draw bounds
         AffineTransform trans = mat.preConcatenate(Matrix.getScaleInstance(1 / SWF.unitDivisor)).toTransform();
         g.setTransform(trans);
         BoundedTag b = (BoundedTag) drawable;
         g.setPaint(new Color(255, 255, 255, 128));
         g.setComposite(BlendComposite.Invert);
         g.setStroke(new BasicStroke((int) SWF.unitDivisor));
         RECT r = b.getRect();
         g.setFont(g.getFont().deriveFont((float) (12 * SWF.unitDivisor)));
         g.drawString(drawable.toString(), r.Xmin + (int) (3 * SWF.unitDivisor), r.Ymin + (int) (15 * SWF.unitDivisor));
         g.draw(new Rectangle(r.Xmin, r.Ymin, r.getWidth(), r.getHeight()));
         g.drawLine(r.Xmin, r.Ymin, r.Xmax, r.Ymax);
         g.drawLine(r.Xmax, r.Ymin, r.Xmin, r.Ymax);
         g.setComposite(AlphaComposite.Dst);*/

        SerializableImage img = null;
        if (cacheAsBitmap && renderContext.displayObjectCache != null) {
            img = renderContext.displayObjectCache.get(layer.placeObjectTag);
        }

        int stateCount = renderContext.stateUnderCursor == null ? 0 : renderContext.stateUnderCursor.size();
        int dframe;
        if (fontFrameNum != -1) {
            dframe = fontFrameNum;
        } else {
            dframe = time % drawableFrameCount;
        }

        if (filters != null && filters.size() > 0) {
            // calculate size after applying the filters
            double deltaXMax = 0;
            double deltaYMax = 0;
            for (FILTER filter : filters) {
                double x = filter.getDeltaX();
                double y = filter.getDeltaY();
                deltaXMax = Math.max(x, deltaXMax);
                deltaYMax = Math.max(y, deltaYMax);
            }
            rect.xMin -= deltaXMax * unzoom;
            rect.xMax += deltaXMax * unzoom;
            rect.yMin -= deltaYMax * unzoom;
            rect.yMax += deltaYMax * unzoom;
        }

        rect.xMin -= unzoom;
        rect.yMin -= unzoom;
        rect.xMin = Math.max(0, rect.xMin);
        rect.yMin = Math.max(0, rect.yMin);
        drawMatrix.translate(rect.xMin, rect.yMin);

        if (img == null) {
            int newWidth = (int) (rect.getWidth() / unzoom);
            int newHeight = (int) (rect.getHeight() / unzoom);
            int deltaX = (int) (rect.xMin / unzoom);
            int deltaY = (int) (rect.yMin / unzoom);
            newWidth = Math.min(image.getWidth() - deltaX, newWidth) + 1;
            newHeight = Math.min(image.getHeight() - deltaY, newHeight) + 1;

            if (newWidth <= 0 || newHeight <= 0) {
                return;
            }

            Matrix m = mat.preConcatenate(Matrix.getTranslateInstance(-rect.xMin, -rect.yMin));
            //strokeTransform = strokeTransform.clone();
            //strokeTransform.translate(-rect.xMin, -rect.yMin);

            if (drawable instanceof ButtonTag) {
                dframe = ButtonTag.FRAME_UP;
                if (renderContext.cursorPosition != null) {
                    Shape buttonShape = drawable.getOutline(ButtonTag.FRAME_HITTEST, time, ratio, renderContext, absMat, true);
                    if (buttonShape.contains(renderContext.cursorPosition)) {
                        renderContext.mouseOverButton = (ButtonTag) drawable;
                        if (renderContext.mouseButton > 0) {
                            dframe = ButtonTag.FRAME_DOWN;
                        } else {
                            dframe = ButtonTag.FRAME_OVER;
                        }
                    }
                }
            }

            img = new SerializableImage(newWidth, newHeight, SerializableImage.TYPE_INT_ARGB_PRE);
            img.fillTransparent();

            if (!(drawable instanceof ImageTag)) {
                // image tags are not rendered, they should be embedded in shape tags
                drawable.toImage(dframe, time, ratio, renderContext, img, isClip || clipDepth > -1, m, strokeTransform, absMat, clrTrans);
            } else {
                // todo: show one time warning
            }

            if (filters != null) {
                for (FILTER filter : filters) {
                    img = filter.apply(img);
                }
            }
            if (blendMode > 1) {
                if (colorTransForm != null) {
                    img = colorTransForm.apply(img);
                }
            }

            if (cacheAsBitmap && renderContext.displayObjectCache != null) {
                renderContext.displayObjectCache.put(layer.placeObjectTag, img);
            }
        }

        drawMatrix.translateX /= unzoom;
        drawMatrix.translateY /= unzoom;
        AffineTransform trans = drawMatrix.toTransform();

        switch (blendMode) {
            case 0:
            case 1:
                g.setComposite(AlphaComposite.SrcOver);
                break;
            case 2: // Layer
                g.setComposite(AlphaComposite.SrcOver);
                break;
            case 3:
                g.setComposite(BlendComposite.Multiply);
                break;
            case 4:
                g.setComposite(BlendComposite.Screen);
                break;
            case 5:
                g.setComposite(BlendComposite.Lighten);
                break;
            case 6:
                g.setComposite(BlendComposite.Darken);
                break;
            case 7:
                g.setComposite(BlendComposite.Difference);
                break;
            case 8:
                g.setComposite(BlendComposite.Add);
                break;
            case 9:
                g.setComposite(BlendComposite.Subtract);
                break;
            case 10:
                g.setComposite(BlendComposite.Invert);
                break;
            case 11:
                g.setComposite(BlendComposite.Alpha);
                break;
            case 12:
                g.setComposite(BlendComposite.Erase);
                break;
            case 13:
                g.setComposite(BlendComposite.Overlay);
                break;
            case 14:
                g.setComposite(BlendComposite.HardLight);
                break;
            default: // Not implemented
                g.setComposite(AlphaComposite.SrcOver);
                break;
        }

        if (clipDepth > -1) {
            BufferedImage mask = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
            Graphics2D gm = (Graphics2D) mask.getGraphics();
            gm.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            gm.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            gm.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gm.setComposite(AlphaComposite.Src);
            gm.setColor(new Color(0, 0, 0, 0f));
            gm.fillRect(0, 0, image.getWidth(), image.getHeight());
            gm.setTransform(trans);
            gm.drawImage(img.getBufferedImage(), 0, 0, null);
            Clip clip = new Clip(Helper.imageToShape(mask), clipDepth); // Maybe we can get current outline instead converting from image (?)
            clips.add(clip);
        } else {
            if (renderContext.cursorPosition != null) {
                if (drawable instanceof DefineSpriteTag) {
                    if (renderContext.stateUnderCursor.size() > stateCount) {
                        renderContext.stateUnderCursor.add(layer);
                    }
                } else if (absMat.transform(new ExportRectangle(boundRect)).contains(renderContext.cursorPosition)) {
                    Shape shape = drawable.getOutline(dframe, time, layer.ratio, renderContext, absMat, true);
                    if (shape.contains(renderContext.cursorPosition)) {
                        renderContext.stateUnderCursor.add(layer);
                    }
                }
            }

            if (renderContext.borderImage != null) {
                Graphics2D g2 = (Graphics2D) renderContext.borderImage.getGraphics();
                g2.setPaint(Color.red);
                g2.setStroke(new BasicStroke(2));
                Shape shape = drawable.getOutline(dframe, time, layer.ratio, renderContext, absMat.preConcatenate(Matrix.getScaleInstance(1 / SWF.unitDivisor)), true);
                g2.draw(shape);
            }

            g.setTransform(trans);
            g.drawImage(img.getBufferedImage(), 0, 0, null);
        }
    }

    public void toImage(int frame, int time, RenderContext renderContext, SerializableImage image, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, ColorTransform colorTransform) {
        double unzoom = SWF.unitDivisor;
        if (getFrameCount() <= frame) {
            return;
        }

        Frame frameObj = getFrame(frame);
        Graphics2D g = (Graphics2D) image.getGraphics();
        Shape prevClip = g.getClip();
        //if (targetRect != null) {
        //    g.setClip(new Rectangle2D.Double(targetRect.xMin, targetRect.yMin, targetRect.getWidth(), targetRect.getHeight()));
        //}

        g.setPaint(frameObj.backgroundColor.toColor());
        g.fill(new Rectangle(image.getWidth(), image.getHeight()));

        g.setTransform(transformation.toTransform());
        List<Clip> clips = new ArrayList<>();

        int maxDepth = getMaxDepth();
        int clipCount = 0;
        for (int i = 1; i <= maxDepth; i++) {
            boolean clipChanged = clipCount != clips.size();
            for (int c = 0; c < clips.size(); c++) {
                if (clips.get(c).depth < i) {
                    clips.remove(c);
                    clipChanged = true;
                }
            }

            if (clipChanged) {
                if (clips.size() > 0) {
                    Area clip = null;
                    for (Clip clip1 : clips) {
                        Shape shape = clip1.shape;
                        if (clip == null) {
                            clip = new Area(shape);
                        } else {
                            clip.intersect(new Area(shape));
                        }
                    }

                    g.setTransform(new AffineTransform());
                    g.setClip(clip);

                    // draw clip border
                    //g.setPaint(Color.red);
                    //g.setStroke(new BasicStroke(2));
                    //g.draw(clip);
                } else {
                    g.setClip(null);
                }

                clipCount = clips.size();
            }

            if (!frameObj.layers.containsKey(i)) {
                continue;
            }
            DepthState layer = frameObj.layers.get(i);
            if (!swf.getCharacters().containsKey(layer.characterId)) {
                continue;
            }
            if (!layer.isVisible) {
                continue;
            }

            CharacterTag character = swf.getCharacter(layer.characterId);
            Matrix layerMatrix = new Matrix(layer.matrix);
            Matrix mat = transformation.concatenate(layerMatrix);
            Matrix absMat = absoluteTransformation.concatenate(layerMatrix);

            ColorTransform clrTrans = colorTransform;
            if (layer.colorTransForm != null && layer.blendMode <= 1) { // Normal blend mode
                clrTrans = clrTrans == null ? layer.colorTransForm : colorTransform.merge(layer.colorTransForm);
            }

            boolean showPlaceholder = false;
            if (character instanceof DrawableTag) {

                RECT scalingRect = null;
                DefineScalingGridTag sgt = character.getScalingGridTag();
                if (sgt != null) {
                    scalingRect = sgt.splitter;
                }

                if (scalingRect != null) {
                    ExportRectangle[] sourceRect = new ExportRectangle[9];
                    ExportRectangle[] targetRect = new ExportRectangle[9];
                    Matrix[] transforms = new Matrix[9];
                    //mat => image
                    //t =>
                    //Matrix tx = transformation.concatenate(layerMatrix);
                    DrawableTag dr = (DrawableTag) character;
                    ExportRectangle boundsRect = new ExportRectangle(dr.getRect());
                    ExportRectangle targetBoundsRect = layerMatrix.transform(boundsRect);
                    DefineScalingGridTag.getSlices(targetBoundsRect, boundsRect, new ExportRectangle(scalingRect), sourceRect, targetRect, transforms);
                    Shape c = g.getClip();
                    AffineTransform origTransform = g.getTransform();
                    for (int s = 0; s < 9; s++) {
                        g.setTransform(new AffineTransform());
                        ExportRectangle p1 = transformation.transform(targetRect[s]);
                        g.setClip(c);

                        Rectangle2D r = new Rectangle2D.Double(p1.xMin, p1.yMin, p1.getWidth(), p1.getHeight());
                        g.setClip(r);
                        drawDrawable(strokeTransformation.preConcatenate(layerMatrix), layer, transforms[s], g, colorTransform, layer.blendMode, clips, transformation, isClip, layer.clipDepth, absMat, time, layer.ratio, renderContext, image, (DrawableTag) character, layer.filters, unzoom, clrTrans);

                    }
                    g.setClip(c);

                    /*
                     for (int s = 0; s < 9; s++) {
                     g.setTransform(new AffineTransform());
                     ExportRectangle p1 = transformation.transform(targetRect[s]);
                     g.setClip(c);

                     Rectangle2D r = new Rectangle2D.Double(p1.xMin, p1.yMin, p1.getWidth(), p1.getHeight());
                     g.setColor(Color.blue);
                     g.draw(r);

                     }*/
                    g.setTransform(origTransform);
                } else {
                    drawDrawable(strokeTransformation, layer, layerMatrix, g, colorTransform, layer.blendMode, clips, transformation, isClip, layer.clipDepth, absMat, time, layer.ratio, renderContext, image, (DrawableTag) character, layer.filters, unzoom, clrTrans);
                }
            } else if (character instanceof BoundedTag) {
                showPlaceholder = true;
            }

            if (showPlaceholder) {
                AffineTransform trans = mat.preConcatenate(Matrix.getScaleInstance(1 / SWF.unitDivisor)).toTransform();
                g.setTransform(trans);
                BoundedTag b = (BoundedTag) character;
                g.setPaint(new Color(255, 255, 255, 128));
                g.setComposite(BlendComposite.Invert);
                g.setStroke(new BasicStroke((int) SWF.unitDivisor));
                RECT r = b.getRect();
                g.setFont(g.getFont().deriveFont((float) (12 * SWF.unitDivisor)));
                g.drawString(character.toString(), r.Xmin + (int) (3 * SWF.unitDivisor), r.Ymin + (int) (15 * SWF.unitDivisor));
                g.draw(new Rectangle(r.Xmin, r.Ymin, r.getWidth(), r.getHeight()));
                g.drawLine(r.Xmin, r.Ymin, r.Xmax, r.Ymax);
                g.drawLine(r.Xmax, r.Ymin, r.Xmin, r.Ymax);
                g.setComposite(AlphaComposite.Dst);
            }
        }

        g.setTransform(new AffineTransform());
        g.setClip(prevClip);
    }

    public void toSVG(int frame, int time, DepthState stateUnderCursor, int mouseButton, SVGExporter exporter, ColorTransform colorTransform, int level) throws IOException {
        if (getFrameCount() <= frame) {
            return;
        }

        Frame frameObj = getFrame(frame);
        List<SvgClip> clips = new ArrayList<>();

        int maxDepth = getMaxDepth();
        int clipCount = 0;
        Element clipGroup = null;
        for (int i = 1; i <= maxDepth; i++) {
            boolean clipChanged = clipCount != clips.size();
            for (int c = 0; c < clips.size(); c++) {
                if (clips.get(c).depth < i) {
                    clips.remove(c);
                    clipChanged = true;
                }
            }

            if (clipChanged) {
                if (clipGroup != null) {
                    exporter.endGroup();
                }

                if (clips.size() > 0) {
                    String clip = clips.get(clips.size() - 1).shape; // todo: merge clip areas
                    clipGroup = exporter.createSubGroup(null, null);
                    clipGroup.setAttribute("clip-path", "url(#" + clip + ")");
                }

                clipCount = clips.size();
            }

            if (!frameObj.layers.containsKey(i)) {
                continue;
            }
            DepthState layer = frameObj.layers.get(i);
            if (!swf.getCharacters().containsKey(layer.characterId)) {
                continue;
            }
            if (!layer.isVisible) {
                continue;
            }

            CharacterTag character = swf.getCharacter(layer.characterId);

            ColorTransform clrTrans = colorTransform;
            if (layer.colorTransForm != null && layer.blendMode <= 1) { // Normal blend mode
                clrTrans = clrTrans == null ? layer.colorTransForm : colorTransform.merge(layer.colorTransForm);
            }

            if (character instanceof DrawableTag) {
                DrawableTag drawable = (DrawableTag) character;

                String assetName;
                Tag drawableTag = (Tag) drawable;
                RECT boundRect = drawable.getRect();
                if (exporter.exportedTags.containsKey(drawableTag)) {
                    assetName = exporter.exportedTags.get(drawableTag);
                } else {
                    assetName = getTagIdPrefix(drawableTag, exporter);
                    exporter.exportedTags.put(drawableTag, assetName);
                    exporter.createDefGroup(new ExportRectangle(boundRect), assetName);
                    drawable.toSVG(exporter, layer.ratio, clrTrans, level + 1);
                    exporter.endGroup();
                }
                ExportRectangle rect = new ExportRectangle(boundRect);

                // TODO: if (layer.filters != null)
                // TODO: if (layer.blendMode > 1)
                if (layer.clipDepth > -1) {
                    String clipName = exporter.getUniqueId("clipPath");
                    exporter.createClipPath(new Matrix(), clipName);
                    SvgClip clip = new SvgClip(clipName, layer.clipDepth);
                    clips.add(clip);
                    Matrix mat = Matrix.getTranslateInstance(rect.xMin, rect.yMin).preConcatenate(new Matrix(layer.matrix));
                    exporter.addUse(mat, boundRect, assetName, layer.instanceName);
                    exporter.endGroup();
                } else {
                    Matrix mat = Matrix.getTranslateInstance(rect.xMin, rect.yMin).preConcatenate(new Matrix(layer.matrix));
                    exporter.addUse(mat, boundRect, assetName, layer.instanceName);
                }
            }
        }

        if (clipGroup != null) {
            exporter.endGroup();
        }
    }

    private static String getTagIdPrefix(Tag tag, SVGExporter exporter) {
        if (tag instanceof ShapeTag) {
            return exporter.getUniqueId("shape");
        }
        if (tag instanceof MorphShapeTag) {
            return exporter.getUniqueId("morphshape");
        }
        if (tag instanceof DefineSpriteTag) {
            return exporter.getUniqueId("sprite");
        }
        if (tag instanceof TextTag) {
            return exporter.getUniqueId("text");
        }
        if (tag instanceof ButtonTag) {
            return exporter.getUniqueId("button");
        }
        return exporter.getUniqueId("tag");
    }

    public void toHtmlCanvas(StringBuilder result, double unitDivisor, List<Integer> frames) {
        FrameExporter.framesToHtmlCanvas(result, unitDivisor, this, frames, 0, null, 0, displayRect, null, null);
    }

    public void getSounds(int frame, int time, ButtonTag mouseOverButton, int mouseButton, List<Integer> sounds, List<String> soundClasses) {
        Frame fr = getFrame(frame);
        sounds.addAll(fr.sounds);
        soundClasses.addAll(fr.soundClasses);
        for (int d = maxDepth; d >= 0; d--) {
            DepthState ds = fr.layers.get(d);
            if (ds != null) {
                CharacterTag c = swf.getCharacter(ds.characterId);
                if (c instanceof Timelined) {
                    int frameCount = ((Timelined) c).getTimeline().frames.size();
                    if (frameCount == 0) {
                        continue;
                    }
                    int dframe = time % frameCount;
                    if (c instanceof ButtonTag) {
                        dframe = ButtonTag.FRAME_UP;
                        if (mouseOverButton == c) {
                            if (mouseButton > 0) {
                                dframe = ButtonTag.FRAME_DOWN;
                            } else {
                                dframe = ButtonTag.FRAME_OVER;
                            }
                        }
                    }
                    ((Timelined) c).getTimeline().getSounds(dframe, time, mouseOverButton, mouseButton, sounds, soundClasses);
                }
            }
        }
    }

    public Shape getOutline(int frame, int time, RenderContext renderContext, Matrix transformation, boolean stroked) {
        Frame fr = getFrame(frame);
        Area area = new Area();
        Stack<Clip> clips = new Stack<>();
        for (int d = maxDepth; d >= 0; d--) {
            Clip currentClip = null;
            for (int i = clips.size() - 1; i >= 0; i--) {
                Clip cl = clips.get(i);
                if (cl.depth <= d) {
                    clips.remove(i);
                }
            }
            if (!clips.isEmpty()) {
                currentClip = clips.peek();
            }
            DepthState layer = fr.layers.get(d);
            if (layer == null) {
                continue;
            }
            if (!layer.isVisible) {
                continue;
            }
            CharacterTag character = swf.getCharacter(layer.characterId);
            if (character instanceof DrawableTag) {
                DrawableTag drawable = (DrawableTag) character;
                Matrix m = transformation.concatenate(new Matrix(layer.matrix));

                int drawableFrameCount = drawable.getNumFrames();
                if (drawableFrameCount == 0) {
                    drawableFrameCount = 1;
                }

                int dframe = time % drawableFrameCount;
                if (character instanceof ButtonTag) {
                    dframe = ButtonTag.FRAME_UP;
                    if (renderContext.cursorPosition != null) {
                        ButtonTag buttonTag = (ButtonTag) character;
                        Shape buttonShape = buttonTag.getOutline(ButtonTag.FRAME_HITTEST, time, layer.ratio, renderContext, m, stroked);
                        if (buttonShape.contains(renderContext.cursorPosition)) {
                            if (renderContext.mouseButton > 0) {
                                dframe = ButtonTag.FRAME_DOWN;
                            } else {
                                dframe = ButtonTag.FRAME_OVER;
                            }
                        }
                    }
                }

                Shape cshape = ((DrawableTag) character).getOutline(dframe, time, layer.ratio, renderContext, m, stroked);
                Area addArea = new Area(cshape);
                if (currentClip != null) {
                    Area a = new Area(new Rectangle(displayRect.Xmin, displayRect.Ymin, displayRect.getWidth(), displayRect.getHeight()));
                    a.subtract(new Area(currentClip.shape));
                    addArea.subtract(a);
                }

                if (layer.clipDepth > -1) {
                    Clip clip = new Clip(addArea, layer.clipDepth);
                    clips.push(clip);
                } else {
                    area.add(addArea);
                }
            }
        }
        return area;
    }

    public boolean isSingleFrame() {
        for (int i = 0; i < getFrameCount(); i++) {
            if (!isSingleFrame(i)) {
                return false;
            }
        }
        return true;
    }

    public boolean isSingleFrame(int frame) {
        Frame frameObj = getFrame(frame);
        for (int i = 1; i <= maxDepth; i++) {
            if (!frameObj.layers.containsKey(i)) {
                continue;
            }
            DepthState layer = frameObj.layers.get(i);
            if (!swf.getCharacters().containsKey(layer.characterId)) {
                continue;
            }
            if (!layer.isVisible) {
                continue;
            }
            CharacterTag character = swf.getCharacter(layer.characterId);
            if (character instanceof DrawableTag) {
                DrawableTag drawable = (DrawableTag) character;
                if (!drawable.isSingleFrame()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Timeline) {
            Timeline timelineObj = (Timeline) obj;
            return timelined.equals(timelineObj.timelined);
        }

        return false;
    }
}
