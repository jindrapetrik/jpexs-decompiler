/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.TagRemoveListener;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.BlendModeSettable;
import com.jpexs.decompiler.flash.exporters.FrameExporter;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DefineSceneAndFrameLabelDataTag;
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
import com.jpexs.decompiler.flash.tags.base.DisplayObjectCacheKey;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalStreamSound;
import com.jpexs.decompiler.flash.types.BlendMode;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.types.filters.BlendComposite;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import org.w3c.dom.Element;

/**
 * Timeline object created from a Timelined object which contains organized list
 * of frames and their contents.
 *
 * @author JPEXS
 */
public class Timeline {

    /**
     * Id of timeline. Generally a characterId. Like spriteId or 0 for main SWF
     * timeline.
     */
    public int id;

    /**
     * SWF of the timeline.
     */
    public SWF swf;

    /**
     * Display rect.
     */
    public RECT displayRect;

    /**
     * Frame rate.
     */
    public float frameRate;

    /**
     * Timelined object which this timeline was created from.
     */
    public Timelined timelined;

    /**
     * The largest depth this timeline uses
     */
    public int maxDepth;

    /**
     * Special frame number when this timeline shows a FontTag.
     */
    public int fontFrameNum = -1;

    /**
     * List of frames.
     */
    private final List<Frame> frames = new ArrayList<>();

    /**
     * Map of depth to maximum frame.
     */
    private final Map<Integer, Integer> depthMaxFrame = new HashMap<>();

    /**
     * List of all ASMSources.
     */
    public final List<ASMSource> asmSources = new ArrayList<>();

    /**
     * List of all ASMSourceContainers.
     */
    private final List<ASMSourceContainer> asmSourceContainers = new ArrayList<>();

    /**
     * Map of ASMSource to frame number.
     */
    private final Map<ASMSource, Integer> actionFrames = new HashMap<>();

    /**
     * Map of soundStream id to SoundStreamFrameRanges.
     */
    private final Map<Integer, List<SoundStreamFrameRange>> soundStreamRanges = new LinkedHashMap<>();

    /**
     * Root AS2 package.
     */
    private AS2Package as2RootPackage;

    /**
     * Other nonstandard tags that the taglist contain.
     */
    public final List<Tag> otherTags = new ArrayList<>();

    /**
     * Whether the timeline is already initialized.
     */
    private boolean initialized = false;

    /**
     * Map of frame label to frame.
     */
    private Map<String, Integer> labelToFrame = new HashMap<>();

    /**
     * List of scenes.
     */
    private List<Scene> scenes = new ArrayList<>();

    /**
     * Mode of drawing everything.
     */
    public static final int DRAW_MODE_ALL = 0;
    /**
     * Mode of drawing only shapes.
     */
    public static final int DRAW_MODE_SHAPES = 1;
    /**
     * Mode of drawing only sprites.
     */
    public static final int DRAW_MODE_SPRITES = 2;

    /**
     * Ensures that the timeline is initialized.
     */
    private void ensureInitialized() {
        if (!initialized) {
            initialize();
            initialized = true;
        }
    }

    /**
     * Gets list of frames.
     *
     * @return List of frames
     */
    public synchronized List<Frame> getFrames() {
        ensureInitialized();
        return frames;
    }

    /**
     * Gets frame object.
     *
     * @param index 0-based frame index
     * @return Frame object or null if index is out of bounds
     */
    public synchronized Frame getFrame(int index) {
        ensureInitialized();
        if (index >= frames.size()) {
            return null;
        }
        return frames.get(index);
    }

    /**
     * Gets depth state at specified frame and depth
     *
     * @param frame Frame
     * @param depth Depth
     * @return DepthState or null if it does not exist or frame does not exist
     */
    public synchronized DepthState getDepthState(int frame, int depth) {
        Frame fr = getFrame(frame);
        if (fr == null) {
            return null;
        }
        return fr.layers.get(depth);
    }

    /**
     * Adds a frame.
     *
     * @param frame Frame to add
     */
    public synchronized void addFrame(Frame frame) {
        ensureInitialized();
        frames.add(frame);
        maxDepth = getMaxDepthInternal();
        calculateMaxDepthFrames();
    }

    /**
     * Gets AS2 root package.
     *
     * @return AS2 root package
     */
    public AS2Package getAS2RootPackage() {
        ensureInitialized();
        return as2RootPackage;
    }

    /**
     * Gets map of depth to max frame.
     *
     * @return Map of depth to max frame
     */
    public Map<Integer, Integer> getDepthMaxFrame() {
        ensureInitialized();
        return depthMaxFrame;
    }

    /**
     * Gets map of soundStream id to SoundStreamFrameRanges.
     *
     * @param head Head
     * @return List of SoundStreamFrameRanges
     */
    public List<SoundStreamFrameRange> getSoundStreamBlocks(SoundStreamHeadTypeTag head) {
        ensureInitialized();
        return soundStreamRanges.get(head.getCharacterId());
    }

    /**
     * Gets parent tag.
     *
     * @return Parent tag
     */
    public Tag getParentTag() {
        return timelined instanceof Tag ? (Tag) timelined : null;
    }

    /**
     * Resets timeline to given SWF file timeline.
     *
     * @param swf SWF
     */
    public void reset(SWF swf) {
        reset(swf, swf, 0, swf.displayRect);
    }

    /**
     * Resets timeline to given Timelined object
     *
     * @param swf SWF this timelined resides
     * @param timelined Timelined
     * @param id Timeline id - usually characterId or 0 for SWF main timeline
     * @param displayRect Display rect
     */
    public synchronized void reset(SWF swf, Timelined timelined, int id, RECT displayRect) {
        initialized = false;
        frames.clear();
        depthMaxFrame.clear();
        asmSources.clear();
        asmSourceContainers.clear();
        actionFrames.clear();
        soundStreamRanges.clear();
        otherTags.clear();
        this.id = id;
        this.swf = swf;
        this.displayRect = displayRect;
        this.frameRate = swf.frameRate;
        this.timelined = timelined;
        as2RootPackage = new AS2Package(null, null, swf, false, false);
    }

    /**
     * Gets maximum depth.
     *
     * @return Maximum depth
     */
    public final int getMaxDepth() {
        ensureInitialized();
        return maxDepth;
    }

    /**
     * Calculates max depth manually.
     *
     * @return Maximum depth
     */
    private synchronized int getMaxDepthInternal() {
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

    /**
     * Gets frame count.
     *
     * @return Frame count
     */
    public synchronized int getFrameCount() {
        ensureInitialized();
        return frames.size();
    }

    /**
     * Gets real frame count. Real = when frame has actions or layerschanged.
     *
     * @return Real frame count
     */
    public synchronized int getRealFrameCount() {
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

    /**
     * Gets frame for ASMSource.
     *
     * @param asm ASMSource
     * @return Frame index or -1 if not found
     */
    public int getFrameForAction(ASMSource asm) {
        Integer frame = actionFrames.get(asm);
        if (frame == null) {
            return -1;
        }

        return frame;
    }

    /**
     * Constructs timeline from SWF.
     *
     * @param swf SWF
     */
    public Timeline(SWF swf) {
        this(swf, swf, 0, swf.displayRect);
    }

    /**
     * Constructs timeline from Timelined object.
     *
     * @param swf SWF the timelined object resides in
     * @param timelined Timelined
     * @param id Timeline id - usually characterId or 0 for SWF main timeline
     * @param displayRect Display rect
     */
    public Timeline(SWF swf, Timelined timelined, int id, RECT displayRect) {
        this.id = id;
        this.swf = swf;
        this.displayRect = displayRect;
        this.frameRate = swf.frameRate;
        this.timelined = timelined;
        as2RootPackage = new AS2Package(null, null, swf, false, false);
    }

    /**
     * Gets frame with label name.
     *
     * @param label Label name
     * @return frame index (zero based) or -1 when not found
     */
    public int getFrameWithLabel(String label) {
        if (labelToFrame.containsKey(label)) {
            return labelToFrame.get(label);
        }
        return -1;
    }

    /**
     * Initialized the timeline. It walks all the tags and creates Frame object
     * lists.
     */
    private synchronized void initialize() {
        int frameIdx = 0;
        Frame frame = new Frame(this, frameIdx++);
        frame.layersChanged = true;
        boolean newFrameNeeded = false;
        scenes = new ArrayList<>();
        Scene prevScene = null;
        List<Tag> tagList = timelined.getTags().toArrayList();
        for (Tag t : tagList) {
            newFrameNeeded = true;
            boolean isNested = ShowFrameTag.isNestedTagType(t.getId());
            if (isNested) {
                frame.innerTags.add(t);
            }
            frame.allInnerTags.add(t);

            if (id == 0 && (t instanceof DefineSceneAndFrameLabelDataTag)) {
                DefineSceneAndFrameLabelDataTag sceneData = (DefineSceneAndFrameLabelDataTag) t;
                scenes = new ArrayList<>();
                for (int i = 0; i < sceneData.sceneNames.length; i++) {
                    int ioffset = (int) sceneData.sceneOffsets[i];
                    Scene currentScene = new Scene(swf, ioffset, -1, sceneData.sceneNames[i]);
                    scenes.add(currentScene);
                    if (prevScene != null) {
                        prevScene.endFrame = ioffset - 1;
                    }
                    prevScene = currentScene;
                }
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
                String labelName = ((FrameLabelTag) t).getLabelName();
                frame.labels.add(labelName);
                frame.namedAnchors.add(((FrameLabelTag) t).isNamedAnchor());
                labelToFrame.put(labelName, frames.size());
            } else if (t instanceof StartSoundTag) {
                frame.sounds.add(((StartSoundTag) t).soundId);
                frame.soundClasses.add(null);
                frame.soundInfos.add(((StartSoundTag) t).soundInfo);
            } else if (t instanceof StartSound2Tag) {
                frame.sounds.add(-1);
                frame.soundClasses.add(((StartSound2Tag) t).soundClassName);
                frame.soundInfos.add(((StartSound2Tag) t).soundInfo);
            } else if (t instanceof SetBackgroundColorTag) {
                frame.backgroundColor = ((SetBackgroundColorTag) t).backgroundColor;
            } else if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                int depth = po.getDepth();
                DepthState fl = frame.layers.get(depth);
                if (fl == null) {
                    frame.layers.put(depth, fl = new DepthState(swf, frame, frame));
                    fl.depth = depth;
                }
                frame.layersChanged = true;
                fl.placeObjectTag = po;
                fl.placeFrame = frame;
                fl.minPlaceObjectNum = Math.max(fl.minPlaceObjectNum, po.getPlaceObjectNum());

                boolean wasOccupied = fl.characterId != -1 || fl.className != null;

                if (po.flagMove() == wasOccupied) {
                    int characterId = po.getCharacterId();
                    if (characterId != -1) {
                        fl.characterId = characterId;
                        fl.hasImage = po.hasImage();
                    }
                    CharacterTag character = swf.getCharacter(characterId);
                    if (character instanceof DefineSpriteTag) {
                        if (swf.getCyclicCharacters().contains(characterId)) {
                            fl.characterId = -1;
                        }
                    }
                    String className = po.getClassName();
                    if (className != null) {
                        fl.className = className;
                        character = swf.getCharacterByClass(className);
                        if (character instanceof DefineSpriteTag) {
                            if (swf.getCyclicCharacters().contains(characterId)) {
                                fl.className = null;
                            }
                        }
                    }
                    //Special case, as FontTags are sometimes added to stage (like intestdata/as2.swf, Sprite 10)
                    //Do not display them.
                    //Steps to reproduce: Create new static text and set its orientation to vertical
                    //TODO: handle this better. Do not treat FontTag as drawable for example
                    if (character instanceof FontTag) {
                        fl.characterId = -1;
                        fl.className = null;
                        fl.key = true;
                    } else {
                        fl.key = characterId != -1 || className != null;
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

                        String className2 = po.getClassName();
                        if (className2 != null) {
                            fl.className = className2;
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

                        RGBA bgColor = po.getBackgroundColor();
                        if (bgColor != null) {
                            fl.backGroundColor = bgColor;
                        }

                        fl.isVisible = po.isVisible();
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
                        fl.isVisible = po.isVisible();
                        fl.backGroundColor = po.getBackgroundColor();
                    }
                }

            } else if (t instanceof RemoveTag) {
                RemoveTag r = (RemoveTag) t;
                int depth = r.getDepth();
                frame.layers.remove(depth);
                frame.layersChanged = true;
            } else if (t instanceof DoActionTag) {
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

        if (prevScene != null) {
            prevScene.endFrame = frames.size() - 1;
        }

        maxDepth = getMaxDepthInternal();

        detectTweens();
        calculateMaxDepthFrames();

        createASPackages();
        if (timelined instanceof SWF) {
            // populate only for main timeline
            populateSoundStreamBlocks(-1, timelined.getTags());
        }

        initialized = true;
    }

    /**
     * Detects tweens.
     */
    private synchronized void detectTweens() {
        for (int d = 0; d <= maxDepth; d++) {
            int characterId = -1;
            String charClassName = null;
            MATRIX matrix = null;
            int len = 0;
            for (int f = 0; f <= frames.size(); f++) {
                DepthState ds = f >= frames.size() ? null : frames.get(f).layers.get(d);

                if (ds != null
                        && (characterId != -1 || charClassName != null)
                        && (ds.characterId == characterId && Objects.equals(ds.className, charClassName))
                        && ((ds.getCharacter() instanceof MorphShapeTag)
                        || Objects.equals(ds.matrix, matrix))) {
                    len++;
                } else {
                    /*if (characterId != -1 || charClassName != null) {
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
                    }*/

                    len = 1;
                    if (ds != null) {
                        ds.key = true;
                    }
                }

                characterId = ds == null ? -1 : ds.characterId;
                charClassName = ds == null ? null : ds.className;
                matrix = ds == null ? null : ds.matrix;
            }
        }
    }

    /**
     * Calculates max depth frames.
     */
    private synchronized void calculateMaxDepthFrames() {
        depthMaxFrame.clear();
        for (int d = 0; d <= maxDepth; d++) {
            for (int f = frames.size() - 1; f >= 0; f--) {
                if (frames.get(f).layers.get(d) != null) {
                    depthMaxFrame.put(d, f);
                    break;
                }
            }
        }
    }

    /**
     * Creates AS2 packages structure.
     */
    private void createASPackages() {
        for (ASMSource asm : asmSources) {
            if (asm instanceof DoInitActionTag) {
                DoInitActionTag initAction = (DoInitActionTag) asm;
                String path = swf.getExportName(initAction.spriteId);
                if (path == null) {
                    continue;
                }
                if (path.isEmpty()) {
                    path = initAction.getExportFileName();
                }

                String[] pathParts = path.contains(".") ? path.split("\\.") : new String[]{path};
                AS2Package pkg = as2RootPackage;

                boolean isNamedPackages = "__Packages".equals(pathParts[0]);

                for (int pos = 0; pos < pathParts.length - 1; pos++) {
                    if (Configuration.flattenASPackages.get()) {
                        if (isNamedPackages && pos == 0) {
                            //nothing
                        } else {

                            String fullPath;
                            if (isNamedPackages) {
                                fullPath = path.substring(pathParts[0].length() + 1, path.length() - pathParts[pathParts.length - 1].length() - 1);
                            } else {
                                fullPath = path.substring(0, path.length() - pathParts[pathParts.length - 1].length() - 1);
                            }

                            AS2Package subPkg = pkg.subPackages.get(fullPath);
                            if (subPkg == null) {
                                subPkg = new AS2Package(fullPath, pkg, swf, true, false);
                                pkg.subPackages.put(fullPath, subPkg);
                            }
                            pkg = subPkg;
                            break;
                        }
                    }

                    String pathPart = pathParts[pos];
                    AS2Package subPkg = pkg.subPackages.get(pathPart);
                    if (subPkg == null) {
                        subPkg = new AS2Package(pathPart, pkg, swf, false, false);
                        pkg.subPackages.put(pathPart, subPkg);
                    }

                    pkg = subPkg;
                }

                if (Configuration.flattenASPackages.get() && ((pathParts.length == 2 && isNamedPackages) || pathParts.length == 1)) {
                    String fullPath = AppResources.translate("package.default");
                    AS2Package subPkg = pkg.subPackages.get(fullPath);
                    if (subPkg == null) {
                        subPkg = new AS2Package(fullPath, pkg, swf, true, true);
                        pkg.subPackages.put(fullPath, subPkg);
                    }
                    pkg = subPkg;
                }

                pkg.scripts.put(pathParts[pathParts.length - 1], asm);
            }
        }
    }

    /**
     * Populates sound stream blocks.
     *
     * @param containerId Container id
     * @param tags Tags
     */
    private void populateSoundStreamBlocks(int containerId, Iterable<Tag> tags) {
        List<SoundStreamFrameRange> ranges = null;
        SoundStreamFrameRange range = null;
        final int MIN_NUM_FRAMES_NO_SOUND = 2;
        int numFramesNoSound = MIN_NUM_FRAMES_NO_SOUND;
        boolean frameHasSound = false;
        int frame = 0;
        SoundStreamHeadTypeTag head = null;
        List<Integer> sceneOffsets = new ArrayList<>();
        for (Tag t : tags) {
            if (containerId == -1 && (t instanceof DefineSceneAndFrameLabelDataTag)) {
                DefineSceneAndFrameLabelDataTag sceneFrameData = (DefineSceneAndFrameLabelDataTag) t;
                for (Long offset : sceneFrameData.sceneOffsets) {
                    sceneOffsets.add((int) (long) offset);
                }
            }
            if (t instanceof SoundStreamHeadTypeTag) {
                head = (SoundStreamHeadTypeTag) t;
                head.setCharacterId(containerId);
                ranges = new ArrayList<>();
                range = new SoundStreamFrameRange(head);
                range.startFrame = -1;
                range.endFrame = -1;
                numFramesNoSound = MIN_NUM_FRAMES_NO_SOUND;
                soundStreamRanges.put(containerId, ranges);
                continue;
            }
            if (t instanceof DefineExternalStreamSound) {
                DefineExternalStreamSound externalStream = (DefineExternalStreamSound) t;
                externalStream.setCharacterId(containerId);
                continue;
            }

            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) t;
                populateSoundStreamBlocks(sprite.getCharacterId(), sprite.getTags());
            }

            if (t instanceof ShowFrameTag) {
                frame++;
                if (frameHasSound) {
                    numFramesNoSound = 0;
                } else {
                    numFramesNoSound++;
                }
                frameHasSound = false;
                if (sceneOffsets.contains(frame) && range != null) {
                    if (range.endFrame > -1) {
                        ranges.add(range);
                    }
                    range = new SoundStreamFrameRange(head);
                    range.startFrame = -1;
                    range.endFrame = -1;
                }
            }

            if (ranges == null) {
                continue;
            }
            if (range == null) {
                continue;
            }

            if (t instanceof SoundStreamBlockTag) {
                if (numFramesNoSound >= MIN_NUM_FRAMES_NO_SOUND && range.endFrame > -1) {
                    ranges.add(range);
                    range = new SoundStreamFrameRange(head);
                    range.startFrame = -1;
                    range.endFrame = -1;
                }
                range.blocks.add((SoundStreamBlockTag) t);
                if (range.startFrame == -1) {
                    range.startFrame = frame;
                }
                range.endFrame = frame;
                frameHasSound = true;
            }
        }
        if (range != null && ranges != null && range.endFrame > -1) {
            ranges.add(range);
        }
    }

    /**
     * Gets needed characters for this timeline.
     *
     * @param usedCharacters Result
     */
    public void getNeededCharacters(Set<Integer> usedCharacters) {
        for (int i = 0; i < getFrameCount(); i++) {
            getNeededCharacters(i, usedCharacters);
        }
    }

    /**
     * Gets needed characters for this timeline.
     *
     * @param frames List of frames
     * @param usedCharacters Result
     */
    public void getNeededCharacters(List<Integer> frames, Set<Integer> usedCharacters) {
        for (int frame = 0; frame < getFrameCount(); frame++) {
            getNeededCharacters(frame, usedCharacters);
        }
    }

    /**
     * Gets needed characters for a frame.
     *
     * @param frame Frame index
     * @param usedCharacters Result
     */
    public void getNeededCharacters(int frame, Set<Integer> usedCharacters) {
        Frame frameObj = getFrame(frame);
        for (int depth : frameObj.layers.keySet()) {
            DepthState layer = frameObj.layers.get(depth);
            CharacterTag ch = layer.getCharacter();
            if (ch == null) {
                continue;
            }
            usedCharacters.add(ch.getCharacterId());
            ch.getNeededCharactersDeep(usedCharacters);
        }
    }

    /**
     * Replaces characterId with another.s
     *
     * @param oldCharacterId Old character id
     * @param newCharacterId New character id
     * @return True if modified
     */
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

    /**
     * Removes character.
     *
     * @param characterId Character id
     * @param listener Called on tag remove
     * @return True if removed
     */
    public boolean removeCharacter(int characterId, TagRemoveListener listener) {
        return swf.removeCharacterFromTimeline(characterId, this, listener);
    }

    /**
     * Rounds value to pixels.
     *
     * @param val Value
     * @return Rounded value
     */
    public double roundToPixel(double val) {
        return Math.rint(val / SWF.unitDivisor) * SWF.unitDivisor;
    }

    /**
     * Draws drawableTag
     *
     * @param swf SWF
     * @param strokeTransform Stroke transform
     * @param layer Layer
     * @param layerMatrix Layer matrix
     * @param g Graphics
     * @param colorTransForm Color transform
     * @param blendMode Blend mode
     * @param parentBlendMode Parent blend mode
     * @param clips Clips
     * @param transformation Transformation
     * @param isClip Is clip
     * @param clipDepth Clip depth
     * @param absMat Absolute matrix
     * @param time Time
     * @param ratio Ratio
     * @param renderContext Render context
     * @param image Image
     * @param fullImage Full image
     * @param drawable Drawable
     * @param filters Filters
     * @param unzoom Unzoom
     * @param mergedColorTransform Color transform
     * @param sameImage Same image
     * @param viewRect View rect
     * @param fullTransformation Full transformation
     * @param scaleStrokes Scale strokes
     * @param drawMode Draw mode
     * @param canUseSmoothing Can use smoothing
     */
    private void drawDrawable(SWF swf, Matrix strokeTransform, DepthState layer, Matrix layerMatrix, Graphics2D g, ColorTransform colorTransForm, int blendMode, int parentBlendMode, List<Clip> clips, Matrix transformation, boolean isClip, int clipDepth, Matrix absMat, int time, int ratio, RenderContext renderContext, SerializableImage image, SerializableImage fullImage, DrawableTag drawable, List<FILTER> filters, double unzoom, ColorTransform mergedColorTransform, boolean sameImage, ExportRectangle viewRect, Matrix fullTransformation, boolean scaleStrokes, int drawMode, boolean canUseSmoothing) {
        Matrix drawMatrix = new Matrix();
        int drawableFrameCount = drawable.getNumFrames();
        if (drawableFrameCount == 0) {
            drawableFrameCount = 1;
        }

        RECT boundRect = drawable.getRectWithStrokes();

        ExportRectangle rect = new ExportRectangle(boundRect);
        Matrix mat = transformation.concatenate(layerMatrix);

        rect = mat.transform(rect);
        ExportRectangle viewRectZoom = new ExportRectangle(viewRect);
        viewRectZoom.xMin *= unzoom;
        viewRectZoom.xMax *= unzoom;
        viewRectZoom.yMin *= unzoom;
        viewRectZoom.yMax *= unzoom;

        ExportRectangle fullRect = fullTransformation.concatenate(layerMatrix).transform(new ExportRectangle(boundRect));

        viewRectZoom.xMax -= viewRectZoom.xMin;
        viewRectZoom.xMin = 0;
        viewRectZoom.yMax -= viewRectZoom.yMin;
        viewRectZoom.yMin = 0;

        if (!viewRectZoom.intersects(fullRect)) {
            if (clipDepth > -1) {
                Clip clip = new Clip(new Area(), clipDepth);
                clips.add(clip);
            }
            return;
        }
        strokeTransform = strokeTransform.concatenate(layerMatrix);

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
            DisplayObjectCacheKey key = new DisplayObjectCacheKey(layer.placeObjectTag, unzoom, viewRect);
            try {
                img = renderContext.displayObjectCache.get(key);
            } catch (NullPointerException npe) {
                return;
            }
        }

        int stateCount = renderContext.stateUnderCursor == null ? 0 : renderContext.stateUnderCursor.size();
        int dframe;
        if (fontFrameNum != -1) {
            dframe = fontFrameNum;
        } else {
            dframe = time % drawableFrameCount;
        }
        int dtime = time - dframe;
        ExportRectangle viewRect2 = new ExportRectangle(viewRect);

        double deltaXMax = 0;
        double deltaYMax = 0;

        if (filters != null && filters.size() > 0) {
            // calculate size after applying the filters
            for (FILTER filter : filters) {
                double x = filter.getDeltaX();
                double y = filter.getDeltaY();
                deltaXMax = Math.max(x, deltaXMax);
                deltaYMax = Math.max(y, deltaYMax);
            }
            rect.xMin -= deltaXMax * unzoom * SWF.unitDivisor;
            rect.xMax += deltaXMax * unzoom * SWF.unitDivisor;
            rect.yMin -= deltaYMax * unzoom * SWF.unitDivisor;
            rect.yMax += deltaYMax * unzoom * SWF.unitDivisor;
            viewRect2.xMin -= deltaXMax * SWF.unitDivisor;
            viewRect2.xMax += deltaXMax * SWF.unitDivisor;
            viewRect2.yMin -= deltaYMax * SWF.unitDivisor;
            viewRect2.yMax += deltaYMax * SWF.unitDivisor;
        }

        drawMatrix.translate(rect.xMin, rect.yMin);
        drawMatrix.translateX /= SWF.unitDivisor;
        drawMatrix.translateY /= SWF.unitDivisor;

        boolean canUseSameImage = true;
        if (img == null) {
            int newWidth = (int) Math.ceil(rect.getWidth() / SWF.unitDivisor);
            int newHeight = (int) Math.ceil(rect.getHeight() / SWF.unitDivisor);
            int deltaX = (int) Math.ceil(rect.xMin / SWF.unitDivisor);
            int deltaY = (int) Math.ceil(rect.yMin / SWF.unitDivisor);
            newWidth = Math.min(image.getWidth() - deltaX, newWidth);
            newHeight = Math.min(image.getHeight() - deltaY, newHeight);

            if (newWidth <= 0 || newHeight <= 0) {
                return;
            }

            Matrix m = mat.preConcatenate(Matrix.getTranslateInstance(-rect.xMin, -rect.yMin));

            if (drawable instanceof ButtonTag) {
                dtime = time;
                dframe = ButtonTag.FRAME_UP;
                if (renderContext.cursorPosition != null && renderContext.enableButtons) {
                    int dx = (int) (viewRect.xMin * unzoom);
                    int dy = (int) (viewRect.yMin * unzoom);
                    Point cursorPositionInView = new Point((int) Math.round(renderContext.cursorPosition.x * unzoom) - dx, (int) Math.round(renderContext.cursorPosition.y * unzoom) - dy);

                    Shape buttonShape = ((ButtonTag) drawable).getOutline(true, ButtonTag.FRAME_HITTEST, dtime, ratio, renderContext, absMat, true, viewRect, unzoom);
                    if (buttonShape.contains(cursorPositionInView)) {
                        renderContext.mouseOverButton = (ButtonTag) drawable;
                        if (renderContext.mouseButton > 0) {
                            dframe = ButtonTag.FRAME_DOWN;
                        } else {
                            dframe = ButtonTag.FRAME_OVER;
                        }
                    }
                }
            }

            if (filters != null && !filters.isEmpty()) {
                canUseSameImage = false;
            }
            if (blendMode > 1) {
                canUseSameImage = false;
            }
            if (clipDepth > -1) {
                canUseSameImage = false;
            }

            if (cacheAsBitmap) {
                canUseSameImage = false;
            }

            Matrix mfull = fullTransformation.concatenate(layerMatrix);
            if (canUseSameImage && sameImage) {
                img = image;
                m = mat.clone();
                g.setTransform(new AffineTransform());
            } else {
                img = new SerializableImage(newWidth, newHeight, SerializableImage.TYPE_INT_ARGB_PRE);
                img.fillTransparent();
            }

            ColorTransform mergedColorTransform2 = mergedColorTransform;

            if (blendMode > 1) {
                mergedColorTransform2 = null;
            }

            if (filters != null && !filters.isEmpty()) {
                mergedColorTransform2 = null;
            }

            if (clipDepth > -1) {
                //Make transparent colors opaque, mask should be only made by shapes
                CXFORMWITHALPHA clrMask = new CXFORMWITHALPHA();
                clrMask.hasAddTerms = true;
                clrMask.hasMultTerms = true;
                clrMask.alphaAddTerm = 255;
                clrMask.redMultTerm = 0;
                clrMask.greenMultTerm = 0;
                clrMask.blueMultTerm = 0;
                mergedColorTransform2 = clrMask;
            }

            if (!(drawable instanceof ImageTag) || (swf.isAS3() && layer.hasImage)) {
                drawable.toImage(dframe, dtime, ratio, renderContext, img, fullImage, isClip || clipDepth > -1, m, strokeTransform, absMat, mfull, mergedColorTransform2, unzoom, sameImage, viewRect2, scaleStrokes, drawMode, layer.blendMode, canUseSmoothing);
            } else {
                // todo: show one time warning
            }

            if (cacheAsBitmap && layer.backGroundColor != null && (blendMode <= 1 || (filters != null && !filters.isEmpty()))) {
                Graphics2D g2 = (Graphics2D) img.getGraphics();
                g2.setComposite(AlphaComposite.DstOver);
                Color bgColor = layer.backGroundColor.toColor();
                g2.setColor(bgColor);
                g2.fillRect((int) Math.round(deltaXMax * unzoom),
                        (int) Math.round(deltaYMax * unzoom),
                        (int) Math.round(rect.getWidth() / SWF.unitDivisor - 2 * deltaXMax * unzoom),
                        (int) Math.round(rect.getHeight() / SWF.unitDivisor - 2 * deltaYMax * unzoom)
                );
            }

            if (filters != null) {
                for (FILTER filter : filters) {
                    img = filter.apply(img, unzoom, (int) deltaXMax, (int) deltaYMax, (int) Math.round(newWidth - 2 * deltaXMax), (int) Math.round(newHeight - 2 * deltaYMax));
                }
            }
            if (blendMode > 1) {
                if (colorTransForm != null) {
                    img = colorTransForm.apply(img);
                }
            }

            if (!sameImage && cacheAsBitmap && renderContext.displayObjectCache != null) {
                renderContext.clearPlaceObjectCache(layer.placeObjectTag);
                renderContext.displayObjectCache.put(new DisplayObjectCacheKey(layer.placeObjectTag, unzoom, viewRect), img);
            }
        }

        AffineTransform trans = drawMatrix.toTransform();

        if (g instanceof BlendModeSettable) {
            ((BlendModeSettable) g).setBlendMode(blendMode);
        } else {
            switch (blendMode) {
                case 0:
                case BlendMode.NORMAL:
                    g.setComposite(AlphaComposite.SrcOver);
                    break;
                case BlendMode.LAYER:
                    g.setComposite(AlphaComposite.SrcOver);
                    break;
                case BlendMode.MULTIPLY:
                    g.setComposite(BlendComposite.Multiply);
                    break;
                case BlendMode.SCREEN:
                    g.setComposite(BlendComposite.Screen);
                    break;
                case BlendMode.LIGHTEN:
                    g.setComposite(BlendComposite.Lighten);
                    break;
                case BlendMode.DARKEN:
                    g.setComposite(BlendComposite.Darken);
                    break;
                case BlendMode.DIFFERENCE:
                    g.setComposite(BlendComposite.Difference);
                    break;
                case BlendMode.ADD:
                    g.setComposite(BlendComposite.Add);
                    break;
                case BlendMode.SUBTRACT:
                    g.setComposite(BlendComposite.Subtract);
                    break;
                case BlendMode.INVERT:
                    g.setComposite(BlendComposite.Invert);
                    break;
                case BlendMode.ALPHA:
                    g.setComposite(BlendComposite.Alpha);
                    break;
                case BlendMode.ERASE:
                    g.setComposite(BlendComposite.Erase);
                    break;
                case BlendMode.OVERLAY:
                    g.setComposite(BlendComposite.Overlay);
                    break;
                case BlendMode.HARDLIGHT:
                    g.setComposite(BlendComposite.HardLight);
                    break;
                default: // Not implemented
                    g.setComposite(AlphaComposite.SrcOver);
                    break;
            }
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
                int dx = (int) Math.round(viewRect.xMin * unzoom);
                int dy = (int) Math.round(viewRect.yMin * unzoom);
                Point cursorPositionInView = new Point((int) Math.round(renderContext.cursorPosition.x * unzoom) - dx, (int) Math.round(renderContext.cursorPosition.y * unzoom) - dy);
                if (drawable instanceof DefineSpriteTag) {
                    if (renderContext.stateUnderCursor.size() > stateCount) {
                        renderContext.stateUnderCursor.add(layer);
                    }
                } else if (absMat.transform(new ExportRectangle(boundRect)).contains(cursorPositionInView)) {
                    if ((drawable instanceof ButtonTag) && !renderContext.enableButtons) {
                        dframe = ButtonTag.FRAME_HITTEST;
                    }
                    Shape shape = drawable.getOutline(true, dframe, dtime, layer.ratio, renderContext, absMat, true, viewRect, unzoom);
                    if (shape.contains(cursorPositionInView)) {
                        renderContext.stateUnderCursor.add(layer);
                    }
                }
            }

            if (renderContext.borderImage != null) {
                Graphics2D g2 = (Graphics2D) renderContext.borderImage.getGraphics();
                g2.setPaint(Color.red);
                g2.setStroke(new BasicStroke(2));
                Shape shape = drawable.getOutline(true, dframe, dtime, layer.ratio, renderContext, absMat.preConcatenate(Matrix.getScaleInstance(1 / SWF.unitDivisor)), true, viewRect, unzoom);
                g2.draw(shape);
            }
            if (!(sameImage && canUseSameImage)) {
                g.setTransform(drawMatrix.toTransform());

                if ((blendMode > 1 || (filters != null && !filters.isEmpty())) && mergedColorTransform != null) {
                    img = mergedColorTransform.apply(img);
                }

                if (blendMode > 1 && (filters == null || filters.isEmpty()) && cacheAsBitmap && layer.backGroundColor != null) {
                    Graphics2D g2 = (Graphics2D) img.getGraphics();
                    g2.setComposite(AlphaComposite.DstOver);
                    Color bgColor = layer.backGroundColor.toColor();
                    g2.setColor(bgColor);
                    g2.fillRect((int) Math.round(deltaXMax * unzoom),
                            (int) Math.round(deltaYMax * unzoom),
                            (int) Math.round(rect.getWidth() / SWF.unitDivisor - 2 * deltaXMax * unzoom),
                            (int) Math.round(rect.getHeight() / SWF.unitDivisor - 2 * deltaYMax * unzoom)
                    );
                }

                if (!((blendMode == 11 || blendMode == 12) && parentBlendMode <= 1)) { //alpha and erase modes require parent blendmode not normal
                    g.drawImage(img.getBufferedImage(), 0, 0, null);
                }
            }
            if (g instanceof BlendModeSettable) {
                ((BlendModeSettable) g).setBlendMode(0);
            } else {
                g.setComposite(AlphaComposite.SrcOver);
            }
        }
    }

    /**
     * Converts specified frame to an image.
     *
     * @param frame Frame
     * @param time Time
     * @param renderContext Render context
     * @param image Image
     * @param fullImage Full image
     * @param isClip Is clip
     * @param transformation Transformation
     * @param strokeTransformation Stroke transformation
     * @param absoluteTransformation Absolute transformation
     * @param colorTransform Color transform
     * @param unzoom Unzoom
     * @param sameImage Same image
     * @param viewRect View rect
     * @param fullTransformation Full transformation
     * @param scaleStrokes Scale strokes
     * @param drawMode Draw mode
     * @param blendMode Blend mode
     * @param canUseSmoothing Can use smoothing
     * @param ignoreDepths Ignore these depths when drawing
     */
    public void toImage(int frame, int time, RenderContext renderContext, SerializableImage image, SerializableImage fullImage, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, ColorTransform colorTransform, double unzoom, boolean sameImage, ExportRectangle viewRect, Matrix fullTransformation, boolean scaleStrokes, int drawMode, int blendMode, boolean canUseSmoothing, List<Integer> ignoreDepths) {
        if (getFrameCount() <= frame) {
            return;
        }

        Frame frameObj = getFrame(frame);
        Graphics2D g = (Graphics2D) image.getGraphics();
        Graphics2D fullG = (Graphics2D) fullImage.getGraphics();
        Shape prevClip = g.getClip();

        if (!sameImage) {
            g.setPaint(frameObj.backgroundColor.toColor());
            g.fill(new Rectangle(image.getWidth(), image.getHeight()));
        }
        sameImage = true;

        g.setTransform(transformation.toTransform());
        List<Clip> clips = new ArrayList<>();

        int maxDepth = getMaxDepth();
        int clipCount = 0;
        for (int i = 0; i <= maxDepth; i++) {
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
                    if (prevClip != null) {
                        clip = new Area(prevClip);
                    }
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
                } else {
                    g.setClip(prevClip);
                }

                clipCount = clips.size();
            }

            if (!frameObj.layers.containsKey(i)) {
                continue;
            }
            DepthState layer = frameObj.layers.get(i);
            if (!layer.isVisible) {
                continue;
            }

            CharacterTag character = layer.getCharacter();
            if (character == null) {
                continue;
            }
            Matrix layerMatrix = new Matrix(layer.getDrawingMatrix());

            Matrix mat = transformation.concatenate(layerMatrix);
            Matrix absMat = absoluteTransformation.concatenate(layerMatrix);

            ColorTransform clrTrans = colorTransform;
            if (layer.colorTransForm != null) {
                clrTrans = clrTrans == null ? layer.colorTransForm : colorTransform.merge(layer.colorTransForm);
            }

            boolean showPlaceholder = false;

            if (drawMode != DRAW_MODE_ALL && drawMode != DRAW_MODE_SPRITES && !(character instanceof ShapeTag)) {
                continue;
            }
            if (drawMode != DRAW_MODE_ALL && drawMode != DRAW_MODE_SHAPES && (character instanceof ShapeTag)) {
                continue;
            }

            if (ignoreDepths.contains(i)) {
                continue;
            }

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
                    DrawableTag dr = (DrawableTag) character;
                    ExportRectangle boundsRect = new ExportRectangle(dr.getRect());
                    ExportRectangle targetBoundsRect = layerMatrix.transform(boundsRect);
                    DefineScalingGridTag.getSlices(targetBoundsRect, boundsRect, new ExportRectangle(scalingRect), sourceRect, targetRect, transforms);
                    Shape c = g.getClip();
                    AffineTransform origTransform = g.getTransform();
                    int s = 0;
                    for (int sy = 0; sy < 3; sy++) {
                        for (int sx = 0; sx < 3; sx++) {
                            g.setTransform(new AffineTransform());
                            ExportRectangle p1 = transformation.transform(targetRect[s]);
                            if (sx == 0) {
                                p1.xMin = 0;
                            }
                            if (sy == 0) {
                                p1.yMin = 0;
                            }

                            if (sx == 2) {
                                p1.xMax = unzoom * viewRect.getWidth();
                            }
                            if (sy == 2) {
                                p1.yMax = unzoom * viewRect.getHeight();
                            }

                            p1.xMin /= SWF.unitDivisor;
                            p1.xMax /= SWF.unitDivisor;
                            p1.yMin /= SWF.unitDivisor;
                            p1.yMax /= SWF.unitDivisor;

                            Rectangle2D r = new Rectangle2D.Double(p1.xMin, p1.yMin, p1.getWidth(), p1.getHeight());

                            g.setClip(r);
                            drawDrawable(swf, strokeTransformation, layer, transforms[s], g, colorTransform, layer.blendMode, blendMode, clips, transformation, isClip, layer.clipDepth, absMat, layer.time + time, layer.ratio, renderContext, image, fullImage, (DrawableTag) character, layer.filters, unzoom, clrTrans, sameImage, viewRect, fullTransformation, false, DRAW_MODE_SHAPES, canUseSmoothing);
                            s++;
                        }
                    }
                    g.setClip(c);

                    g.setTransform(origTransform);

                    //draw all nonshapes (normally scaled) next
                    drawDrawable(swf, strokeTransformation, layer, layerMatrix, g, colorTransform, layer.blendMode, blendMode, clips, transformation, isClip, layer.clipDepth, absMat, layer.time + time, layer.ratio, renderContext, image, fullImage, (DrawableTag) character, layer.filters, unzoom, clrTrans, sameImage, viewRect, fullTransformation, scaleStrokes, DRAW_MODE_SPRITES, canUseSmoothing);
                } else {
                    boolean subScaleStrokes = scaleStrokes;
                    if (character instanceof DefineSpriteTag) {
                        subScaleStrokes = true;
                    }
                    drawDrawable(swf, strokeTransformation, layer, layerMatrix, g, colorTransform, layer.blendMode, blendMode, clips, transformation, isClip, layer.clipDepth, absMat, layer.time + time, layer.ratio, renderContext, image, fullImage, (DrawableTag) character, layer.filters, unzoom, clrTrans, sameImage, viewRect, fullTransformation, subScaleStrokes, DRAW_MODE_ALL, canUseSmoothing);
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

    /**
     * Converts specified frame to SVG.
     *
     * @param frame Frame
     * @param time Time
     * @param stateUnderCursor State under cursor
     * @param mouseButton Mouse button
     * @param exporter SVG exporter
     * @param colorTransform Color transform
     * @param level Level
     * @throws IOException On I/O error
     */
    public void toSVG(int frame, int time, DepthState stateUnderCursor, int mouseButton, SVGExporter exporter, ColorTransform colorTransform, int level) throws IOException {
        if (getFrameCount() <= frame) {
            return;
        }

        Frame frameObj = getFrame(frame);
        List<SvgClip> clips = new ArrayList<>();

        int maxDepth = getMaxDepth();
        int clipCount = 0;
        Element clipGroup = null;
        for (int i = 0; i <= maxDepth; i++) {
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
                    clipGroup = null;
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
            if (!layer.isVisible) {
                continue;
            }

            CharacterTag character = layer.getCharacter();
            if (character == null) {
                continue;
            }

            ColorTransform clrTrans = colorTransform;
            if (layer.colorTransForm != null && layer.blendMode <= 1) { // Normal blend mode
                clrTrans = clrTrans == null ? layer.colorTransForm : colorTransform.merge(layer.colorTransForm);
            }

            if (character instanceof DrawableTag) {
                DrawableTag drawable = (DrawableTag) character;

                String assetName;
                Tag drawableTag = (Tag) drawable;
                RECT boundRect = drawable.getRect();
                boolean createNew = false;
                if (exporter.exportedTags.containsKey(drawableTag)) {
                    assetName = exporter.exportedTags.get(drawableTag);
                } else {
                    assetName = getTagIdPrefix(drawableTag, exporter);
                    exporter.exportedTags.put(drawableTag, assetName);
                    createNew = true;
                }
                ExportRectangle rect = new ExportRectangle(boundRect);

                DefineScalingGridTag scalingGrid = character.getScalingGridTag();

                // TODO: if (layer.filters != null)
                // TODO: if (layer.blendMode > 1)                                               
                if (layer.clipDepth > -1) {
                    String clipName = exporter.getUniqueId("clipPath");
                    Matrix mat = new Matrix(layer.matrix);
                    exporter.createClipPath(mat, clipName);
                    SvgClip clip = new SvgClip(clipName, layer.clipDepth);
                    clips.add(clip);
                    drawable.toSVG(exporter, layer.ratio, clrTrans, level + 1);
                    exporter.endGroup();
                } else {
                    if (createNew) {
                        exporter.createDefGroup(new ExportRectangle(boundRect), assetName);
                        drawable.toSVG(exporter, layer.ratio, clrTrans, level + 1);
                        exporter.endGroup();
                    }
                    Matrix mat = Matrix.getTranslateInstance(rect.xMin, rect.yMin).preConcatenate(new Matrix(layer.matrix));
                    exporter.addUse(mat, boundRect, assetName, layer.instanceName, scalingGrid == null ? null : scalingGrid.splitter, String.valueOf(drawable.getCharacterId()), String.join("___", drawable.getClassNames()), layer.blendMode, layer.filters);
                }
            }
        }

        if (clipGroup != null) {
            exporter.endGroup();
        }
    }

    /**
     * Gets tag type prefix for SVG export.
     *
     * @param tag Tag
     * @param exporter SVG exporter
     * @return Tag type prefix
     */
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

    /**
     * Converts list of frames to HTML Canvas.
     *
     * @param result Result
     * @param unitDivisor Unit divisor
     * @param frames Frames
     */
    public void toHtmlCanvas(StringBuilder result, double unitDivisor, List<Integer> frames) {
        FrameExporter.framesToHtmlCanvas(result, unitDivisor, this, frames, 0, null, 0, displayRect, null, null);
    }

    /**
     * Gets all sounds from the frame.
     *
     * @param frame Frame
     * @param time Time
     * @param mouseOverButton Mouse over button
     * @param mouseButton Mouse button
     * @param sounds Sounds
     * @param soundClasses Sound classes
     * @param soundInfos Sound infos
     */
    public void getSounds(int frame, int time, ButtonTag mouseOverButton, int mouseButton, List<Integer> sounds, List<String> soundClasses, List<SOUNDINFO> soundInfos) {
        Frame fr = getFrame(frame);
        if (time == 0) {
            sounds.addAll(fr.sounds);
            soundClasses.addAll(fr.soundClasses);
            soundInfos.addAll(fr.soundInfos);
        }
        for (int d = maxDepth; d >= 0; d--) {
            DepthState ds = fr.layers.get(d);
            if (ds != null) {
                CharacterTag c = ds.getCharacter();
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
                    ((Timelined) c).getTimeline().getSounds(dframe, 0, mouseOverButton, mouseButton, sounds, soundClasses, soundInfos);
                }
            }
        }
    }

    /**
     * Gets outline of the frame.
     *
     * @param fast Fast
     * @param frame Frame
     * @param time Time
     * @param renderContext Render context
     * @param transformation Transformation
     * @param stroked Stroked
     * @param viewRect View rect
     * @param unzoom Unzoom
     * @return Outline
     */
    public Shape getOutline(boolean fast, int frame, int time, RenderContext renderContext, Matrix transformation, boolean stroked, ExportRectangle viewRect, double unzoom) {
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
            CharacterTag character = layer.getCharacter();
            if (character instanceof DrawableTag) {
                DrawableTag drawable = (DrawableTag) character;
                Matrix m = transformation.concatenate(new Matrix(layer.getDrawingMatrix()));

                int drawableFrameCount = drawable.getNumFrames();
                if (drawableFrameCount == 0) {
                    drawableFrameCount = 1;
                }

                int dframe = time % drawableFrameCount;
                if (character instanceof ButtonTag) {
                    dframe = ButtonTag.FRAME_UP;
                    if (renderContext.cursorPosition != null && renderContext.enableButtons) {
                        ButtonTag buttonTag = (ButtonTag) character;
                        Shape buttonShape = buttonTag.getOutline(fast, ButtonTag.FRAME_HITTEST, time, layer.ratio, renderContext, m, stroked, viewRect, unzoom);
                        int dx = (int) Math.round(viewRect.xMin * unzoom);
                        int dy = (int) Math.round(viewRect.yMin * unzoom);
                        Point cursorPositionInView = new Point((int) Math.round(renderContext.cursorPosition.x * unzoom) - dx, (int) Math.round(renderContext.cursorPosition.y * unzoom) - dy);
                        if (buttonShape.contains(cursorPositionInView)) {
                            if (renderContext.mouseButton > 0) {
                                dframe = ButtonTag.FRAME_DOWN;
                            } else {
                                dframe = ButtonTag.FRAME_OVER;
                            }
                        }
                    }
                }

                Shape cshape = ((DrawableTag) character).getOutline(fast, dframe, time, layer.ratio, renderContext, m, stroked, viewRect, unzoom);
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

    /**
     * Checks whether all frames are singleframe. Single frame = each frame has
     * no animated parts inside.
     *
     * @return True if all frames are single frame, otherwise false
     */
    public boolean isSingleFrame() {
        for (int i = 0; i < getFrameCount(); i++) {
            if (!isSingleFrame(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether specified frame is single frame. Single frame = has no
     * animated parts inside.
     *
     * @param frame Frame
     * @return True if frame is single frame, otherwise false
     */
    public boolean isSingleFrame(int frame) {
        Frame frameObj = getFrame(frame);
        for (int i = 0; i <= maxDepth; i++) {
            if (!frameObj.layers.containsKey(i)) {
                continue;
            }
            DepthState layer = frameObj.layers.get(i);
            if (!layer.isVisible) {
                continue;
            }
            CharacterTag character = layer.getCharacter();
            if (character == null) {
                continue;
            }
            if (!frameObj.sounds.isEmpty()) { //consider timelines with sound as not singleframe
                return false;
            }
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

    /**
     * Gets scenes list.
     *
     * @return Scenes list
     */
    public List<Scene> getScenes() {
        ensureInitialized();
        return new ArrayList<>(scenes);
    }
}
