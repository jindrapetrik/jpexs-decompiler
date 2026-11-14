/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.SoundStreamFrameRange;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Keeps timeline streams separate from event sounds and synchronized with the
 * frames rendered by ImagePanel. Each placed sprite has its own playback state.
 *
 * @author JPEXS
 */
final class SoundStreamPlayer {

    interface Playback {
        long getPosition();

        boolean isReady();

        void seek(long microsec);

        void close();
    }

    interface Factory {
        Playback create(SoundStreamFrameRange range, long microsec) throws Exception;
    }

    private static class Stream {
        final SoundStreamFrameRange range;
        final Playback playback;
        int lastFrame;

        Stream(SoundStreamFrameRange range, Playback playback) {
            this.range = range;
            this.playback = playback;
        }
    }

    private final Factory factory;
    private final Map<String, Stream> playing = new HashMap<>();
    private final Map<Timeline, List<SoundStreamFrameRange>> ranges = new IdentityHashMap<>();
    private final Map<Timeline, Frame> firstFrames = new IdentityHashMap<>();

    SoundStreamPlayer() {
        this((range, microsec) -> {
            SoundTagPlayer player = new SoundTagPlayer(null, range, 1, false, microsec);
            player.play();
            return new Playback() {
                @Override
                public long getPosition() {
                    return player.getPositionMicrosec();
                }

                @Override
                public boolean isReady() {
                    return player.isSoundReady();
                }

                @Override
                public void seek(long position) {
                    player.seekMicrosec(position);
                }

                @Override
                public void close() {
                    player.close();
                }
            };
        });
    }

    SoundStreamPlayer(Factory factory) {
        this.factory = factory;
    }

    synchronized void update(Timeline timeline, int frame, int time, boolean playRoot,
            ButtonTag mouseOverButton, int mouseButton) {
        Set<String> active = new HashSet<>();
        update(timeline, frame, time, playRoot, mouseOverButton, mouseButton, "", active, new HashSet<>());
        Iterator<Map.Entry<String, Stream>> iterator = playing.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Stream> entry = iterator.next();
            if (!active.contains(entry.getKey())) {
                close(entry.getValue());
                iterator.remove();
            }
        }
    }

    private void update(Timeline timeline, int frame, int time, boolean playStream,
            ButtonTag mouseOverButton, int mouseButton, String instance, Set<String> active, Set<Timeline> parents) {
        Frame current = timeline.getFrame(frame);
        if (current == null || !parents.add(timeline)) {
            return;
        }
        if (playStream && timeline.frameRate > 0) {
            for (SoundStreamFrameRange range : getRanges(timeline)) {
                if (frame < range.startFrame || frame > range.endFrame) {
                    continue;
                }
                // SeekSamples in the first MP3 block also accounts for encoder latency.
                long position = Math.round((frame - range.startFrame) * 1000000d / timeline.frameRate
                        + range.getSeekSamples() * 1000000d / range.getSoundFormat().samplingRate);
                if (position < 0) {
                    continue;
                }
                active.add(instance);
                Stream stream = playing.get(instance);
                if (stream == null || stream.range != range || frame < stream.lastFrame) {
                    close(stream);
                    try {
                        stream = new Stream(range, factory.create(range, position));
                    } catch (Exception ex) {
                        Logger.getLogger(SoundStreamPlayer.class.getName()).log(Level.WARNING, "Cannot play sound stream", ex);
                        // Remember a failed stream until it leaves the display list.
                        stream = new Stream(range, null);
                    }
                    playing.put(instance, stream);
                } else if (stream.playback != null) {
                    long tolerance = Math.max(200000, Math.round(2000000d / timeline.frameRate));
                    if (!stream.playback.isReady() || Math.abs(stream.playback.getPosition() - position) > tolerance) {
                        stream.playback.seek(position);
                    }
                }
                stream.lastFrame = frame;
                break;
            }
        }
        for (DepthState layer : current.layers.values()) {
            CharacterTag character = layer.getCharacter();
            if (!(character instanceof Timelined)) {
                continue;
            }
            Timeline child = ((Timelined) character).getTimeline();
            int count = child.getFrameCount();
            if (count == 0) {
                continue;
            }
            // Use the same frame/time calculation as Timeline.drawDrawable.
            int childTime = layer.time + time;
            int childFrame = childTime % count;
            if (character instanceof ButtonTag) {
                childFrame = ButtonTag.FRAME_UP;
                if (mouseOverButton == character) {
                    childFrame = mouseButton > 0 ? ButtonTag.FRAME_DOWN : ButtonTag.FRAME_OVER;
                }
            }
            String childInstance = instance + "/" + layer.instanceId + ":" + (childTime / count);
            update(child, childFrame, childTime - childFrame, true, mouseOverButton, mouseButton,
                    childInstance, active, parents);
        }
        parents.remove(timeline);
    }

    private List<SoundStreamFrameRange> getRanges(Timeline timeline) {
        List<SoundStreamFrameRange> result = ranges.get(timeline);
        if (result != null && firstFrames.get(timeline) == timeline.getFrame(0)) {
            return result;
        }
        result = new ArrayList<>();
        SoundStreamHeadTypeTag head = null;
        SoundStreamFrameRange range = null;
        for (int frame = 0; frame < timeline.getFrameCount(); frame++) {
            // Heads are not nested frame tags, so inspect allInnerTags.
            for (Tag tag : timeline.getFrame(frame).allInnerTags) {
                if (tag instanceof SoundStreamHeadTypeTag) {
                    head = (SoundStreamHeadTypeTag) tag;
                    range = null;
                } else if (tag instanceof SoundStreamBlockTag && head != null) {
                    // As in Timeline's export ranges, tolerate a missing block in
                    // one frame (MP3 packets need not match animation frames).
                    if (range == null || frame - range.endFrame > 2) {
                        range = new SoundStreamFrameRange(head);
                        range.startFrame = frame;
                        result.add(range);
                    }
                    range.blocks.add((SoundStreamBlockTag) tag);
                    range.endFrame = frame;
                }
            }
        }
        ranges.put(timeline, result);
        firstFrames.put(timeline, timeline.getFrame(0));
        return result;
    }

    synchronized void stop() {
        for (Stream stream : playing.values()) {
            close(stream);
        }
        playing.clear();
    }

    synchronized void reset() {
        stop();
        ranges.clear();
        firstFrames.clear();
    }

    private static void close(Stream stream) {
        if (stream != null && stream.playback != null) {
            stream.playback.close();
        }
    }
}
