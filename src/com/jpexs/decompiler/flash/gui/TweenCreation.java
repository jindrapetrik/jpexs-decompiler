/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.DepthTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

/**
 * A tween between two placements on the same timeline depth.
 */
public final class TweenCreation {

    private final Timelined timelined;
    private final int sourceFrame;
    private final int targetFrame;
    private final int depth;
    private final int characterId;
    private final MATRIX startMatrix;
    private final MATRIX endMatrix;
    private final CXFORMWITHALPHA startColorTransform;
    private final CXFORMWITHALPHA endColorTransform;
    private final boolean tweenColorTransform;
    private final int startRatio;
    private final int endRatio;
    private final boolean tweenRatio;

    private TweenCreation(Timelined timelined, int sourceFrame, int targetFrame, int depth,
            int characterId, MATRIX startMatrix, MATRIX endMatrix,
            CXFORMWITHALPHA startColorTransform, CXFORMWITHALPHA endColorTransform,
            boolean tweenColorTransform, int startRatio, int endRatio, boolean tweenRatio) {
        this.timelined = timelined;
        this.sourceFrame = sourceFrame;
        this.targetFrame = targetFrame;
        this.depth = depth;
        this.characterId = characterId;
        this.startMatrix = startMatrix;
        this.endMatrix = endMatrix;
        this.startColorTransform = startColorTransform;
        this.endColorTransform = endColorTransform;
        this.tweenColorTransform = tweenColorTransform;
        this.startRatio = startRatio;
        this.endRatio = endRatio;
        this.tweenRatio = tweenRatio;
    }

    public static TweenCreation find(PlaceObjectTypeTag source) {
        Timelined timelined = source.getTimelined();
        if (timelined == null) {
            return null;
        }
        Timeline timeline = timelined.getTimeline();
        for (int frameIndex = 0; frameIndex < timeline.getFrameCount(); frameIndex++) {
            if (timeline.getFrame(frameIndex).innerTags.contains(source)) {
                return find(timeline, frameIndex, source);
            }
        }
        return null;
    }

    public static TweenCreation find(Timeline timeline, int sourceFrame, PlaceObjectTypeTag source) {
        Timelined timelined = timeline.timelined;
        int depth = source.getDepth();
        DepthState sourceState = timeline.getDepthState(sourceFrame, depth);
        if (sourceState == null || sourceState.characterId < 0) {
            return null;
        }
        MATRIX startMatrix = sourceState.matrix == null ? new MATRIX() : new MATRIX(sourceState.matrix);

        for (int frameIndex = sourceFrame + 1; frameIndex < timeline.getFrameCount(); frameIndex++) {
            Frame frame = timeline.getFrame(frameIndex);
            for (Tag tag : frame.innerTags) {
                if (!(tag instanceof DepthTag) || ((DepthTag) tag).getDepth() != depth) {
                    continue;
                }
                if (!(tag instanceof PlaceObjectTypeTag) || frameIndex == sourceFrame + 1) {
                    return null;
                }

                PlaceObjectTypeTag candidate = (PlaceObjectTypeTag) tag;
                boolean sameCharacter = candidate.getCharacterId() == sourceState.characterId;
                boolean moveWithoutCharacter = candidate.flagMove() && candidate.getCharacterId() == -1;
                if (!sameCharacter && !moveWithoutCharacter) {
                    return null;
                }
                DepthState targetState = frame.layers.get(depth);
                if (targetState == null) {
                    return null;
                }
                boolean tweenColorTransform = source.getColorTransform() != null
                        || candidate.getColorTransform() != null;
                CXFORMWITHALPHA startColorTransform = sourceState.colorTransForm == null
                        ? null : new CXFORMWITHALPHA(sourceState.colorTransForm);
                CXFORMWITHALPHA endColorTransform = targetState.colorTransForm == null
                        ? null : new CXFORMWITHALPHA(targetState.colorTransForm);
                boolean tweenRatio = source.getRatio() > -1 || candidate.getRatio() > -1;
                int startRatio = tweenRatio ? Math.max(0, sourceState.ratio) : sourceState.ratio;
                int endRatio = tweenRatio ? Math.max(0, targetState.ratio) : targetState.ratio;
                return new TweenCreation(timelined, sourceFrame, frameIndex, depth,
                        sourceState.characterId, startMatrix,
                        targetState.matrix == null ? new MATRIX() : new MATRIX(targetState.matrix),
                        startColorTransform, endColorTransform, tweenColorTransform,
                        startRatio, endRatio, tweenRatio);
            }
        }
        return null;
    }

    public CreateTweenDialog createDialog(Window owner) {
        return new CreateTweenDialog(owner, timelined.getSwf(), characterId, targetFrame - sourceFrame + 1,
                startMatrix, endMatrix, startColorTransform, endColorTransform, tweenColorTransform,
                startRatio, endRatio, tweenRatio);
    }

    public Runnable createEdit(CreateTweenDialog dialog) {
        SWF swf = timelined.getSwf();
        List<PlaceObject2Tag> placements = new ArrayList<>();
        for (int frameIndex = sourceFrame + 1; frameIndex < targetFrame; frameIndex++) {
            double progress = (double) (frameIndex - sourceFrame) / (targetFrame - sourceFrame);
            double eased = dialog.getEasedProgress(progress);
            MATRIX matrix = dialog.getInterpolatedMatrix(eased);
            CXFORMWITHALPHA colorTransform = tweenColorTransform
                    ? TweenEasing.interpolate(startColorTransform, endColorTransform, eased) : null;
            int ratio = tweenRatio ? TweenEasing.interpolate(startRatio, endRatio, eased) : -1;
            PlaceObject2Tag place = new PlaceObject2Tag(swf, true, depth, -1,
                    matrix, colorTransform, ratio, null, -1, null);
            place.setTimelined(timelined);
            place.setModified(true);
            placements.add(place);
        }
        return () -> {
            Timeline timeline = timelined.getTimeline();
            for (int i = 0; i < placements.size(); i++) {
                Frame frame = timeline.getFrame(sourceFrame + 1 + i);
                int insertIndex = timelined.indexOfTag(frame.showFrameTag);
                timelined.addTag(insertIndex, placements.get(i));
            }
            swf.resetTimelines(timelined);
        };
    }
}
