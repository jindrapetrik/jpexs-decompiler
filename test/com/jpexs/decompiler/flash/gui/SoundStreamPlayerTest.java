/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHead2Tag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.timeline.SoundStreamFrameRange;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class SoundStreamPlayerTest {

    private static class Playback implements SoundStreamPlayer.Playback {
        final SoundStreamFrameRange range;
        long position;
        boolean closed;
        boolean ready = true;
        int seeks;

        Playback(SoundStreamFrameRange range, long position) {
            this.range = range;
            this.position = position;
        }

        @Override
        public long getPosition() {
            return position;
        }

        @Override
        public boolean isReady() {
            return ready;
        }

        @Override
        public void seek(long microsec) {
            position = microsec;
            seeks++;
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    private static class Player {
        final List<Playback> opened = new ArrayList<>();
        final SoundStreamPlayer player = new SoundStreamPlayer((range, position) -> {
            Playback playback = new Playback(range, position);
            opened.add(playback);
            return playback;
        });

        void frame(Timelined timelined, int frame) {
            player.update(timelined.getTimeline(), frame, 0, true, null, 0);
        }
    }

    @Test
    public void mainTimelineStartsInMiddleAndDoesNotRestartOnRedraw() throws Exception {
        SWF swf = swf();
        swf.addTag(new ShowFrameTag(swf));
        SoundStreamHeadTypeTag head = head(swf, false, 3);
        swf.addTag(head);
        blocks(swf, 8);
        swf.addTag(new ShowFrameTag(swf));
        Player player = new Player();
        player.frame(swf, 4);
        Playback playback = player.opened.get(0);
        assertSame(playback.range.getHead(), head);
        assertEquals(playback.position, 300000L);
        assertEquals(playback.range.startFrame, 1);
        assertEquals(playback.range.endFrame, 8);
        player.frame(swf, 4);
        assertEquals(player.opened.size(), 1);
        assertEquals(playback.seeks, 0);
        playback.position = 400000;
        player.frame(swf, 5);
        assertEquals(playback.seeks, 0);
        player.frame(swf, 9);
        assertTrue(playback.closed);
    }

    @Test
    public void spritePreviewUsesItsOwnStreamHead2() throws Exception {
        SWF swf = swf();
        DefineSpriteTag sprite = sprite(swf, 6);
        Player player = new Player();
        player.frame(sprite, 3);
        assertEquals(player.opened.size(), 1);
        assertEquals(player.opened.get(0).position, 300000L);
        assertEquals(player.opened.get(0).range.blocks.size(), 6);
        assertTrue(player.opened.get(0).range.getHead() instanceof SoundStreamHead2Tag);
    }

    @Test
    public void nestedInstancesUsePlacementTimeAndStopWhenRemoved() throws Exception {
        SWF swf = swf();
        DefineSpriteTag sprite = sprite(swf, 6);
        swf.addTag(new PlaceObjectTag(swf, sprite.spriteId, 1, new MATRIX(), null));
        swf.addTag(new ShowFrameTag(swf));
        swf.addTag(new PlaceObjectTag(swf, sprite.spriteId, 2, new MATRIX(), null));
        swf.addTag(new ShowFrameTag(swf));
        RemoveObject2Tag remove = new RemoveObject2Tag(swf);
        remove.depth = 1;
        swf.addTag(remove);
        swf.addTag(new ShowFrameTag(swf));
        Player player = new Player();
        player.frame(swf, 1);
        assertEquals(player.opened.size(), 2);
        assertEquals(player.opened.get(0).position, 100000L);
        assertEquals(player.opened.get(1).position, 0L);
        player.frame(swf, 2);
        assertTrue(player.opened.get(0).closed);
        assertFalse(player.opened.get(1).closed);
        assertEquals(player.opened.size(), 2);
        player.player.stop();
        assertTrue(player.opened.get(1).closed);
    }

    @Test
    public void loopsAndResumeCreateNewPlaybackAtTheCurrentFrame() throws Exception {
        SWF swf = swf();
        swf.addTag(head(swf, false, 3));
        blocks(swf, 6);
        Player player = new Player();
        player.frame(swf, 4);
        player.frame(swf, 1); // includes a wrap where frame zero was dropped
        assertTrue(player.opened.get(0).closed);
        assertEquals(player.opened.size(), 2);
        assertEquals(player.opened.get(1).position, 100000L);
        player.player.stop(); // pause / mute / seek / close all use this path
        assertTrue(player.opened.get(1).closed);
        player.frame(swf, 3);
        assertEquals(player.opened.get(2).position, 300000L);
    }

    @Test
    public void driftAndSlowDecodingFollowTheTimeline() throws Exception {
        SWF swf = swf();
        swf.addTag(head(swf, true, 3));
        blocks(swf, 10);
        Player player = new Player();
        player.frame(swf, 0);
        Playback playback = player.opened.get(0);
        playback.ready = false;
        player.frame(swf, 1);
        assertEquals(playback.position, 100000L);
        playback.ready = true;
        player.frame(swf, 6);
        assertEquals(playback.position, 600000L);
        assertEquals(playback.seeks, 2);
        assertEquals(player.opened.size(), 1);
    }

    @Test
    public void multipleHeadsAndGapsDoNotMixStreams() throws Exception {
        SWF swf = swf();
        SoundStreamHeadTypeTag first = head(swf, false, 3);
        SoundStreamHeadTypeTag second = head(swf, true, 3);
        swf.addTag(first);
        blocks(swf, 2);
        swf.addTag(new ShowFrameTag(swf));
        swf.addTag(new ShowFrameTag(swf));
        blocks(swf, 1);
        swf.addTag(second);
        blocks(swf, 2);
        Player player = new Player();
        player.frame(swf, 0);
        assertEquals(player.opened.get(0).range.blocks.size(), 2);
        player.frame(swf, 2);
        assertTrue(player.opened.get(0).closed);
        player.frame(swf, 4);
        assertSame(player.opened.get(1).range.getHead(), first);
        assertEquals(player.opened.get(1).range.blocks.size(), 1);
        player.frame(swf, 5);
        assertTrue(player.opened.get(1).closed);
        assertSame(player.opened.get(2).range.getHead(), second);
    }

    @Test
    public void mp3HeadersAreRemovedAndInitialSeekSamplesApplied() throws Exception {
        SWF swf = swf();
        swf.addTag(head(swf, true, 2));
        SoundStreamBlockTag block = new SoundStreamBlockTag(swf);
        block.streamSoundData = new ByteArrayRange(new byte[]{0, 4, (byte) 0xb9, 1, 10, 20, 30}); // 441 seek samples
        swf.addTag(block);
        swf.addTag(new ShowFrameTag(swf));
        Player player = new Player();
        player.frame(swf, 0);
        Playback playback = player.opened.get(0);
        assertEquals(playback.position, 10000L);
        assertEquals(playback.range.getRawSoundData().get(0).getRangeData(), new byte[]{10, 20, 30});
    }

    @Test
    public void staticFramePreviewOnlyPlaysAnimatedChildren() throws Exception {
        SWF swf = swf();
        DefineSpriteTag sprite = sprite(swf, 3);
        swf.addTag(head(swf, true, 3));
        swf.addTag(new PlaceObjectTag(swf, sprite.spriteId, 1, new MATRIX(), null));
        blocks(swf, 1);
        Player player = new Player();
        player.player.update(swf.getTimeline(), 0, 1, false, null, 0);
        assertEquals(player.opened.size(), 1);
        assertEquals(player.opened.get(0).position, 100000L);
        player.player.update(swf.getTimeline(), 0, 3, false, null, 0);
        assertTrue(player.opened.get(0).closed);
        assertEquals(player.opened.get(1).position, 0L);
    }

    @Test
    public void timelineEditsInvalidateStreamRanges() throws Exception {
        SWF swf = swf();
        swf.addTag(head(swf, false, 3));
        blocks(swf, 2);
        Player player = new Player();
        player.frame(swf, 0);
        blocks(swf, 2);
        swf.resetTimeline();
        player.frame(swf, 0);
        assertTrue(player.opened.get(0).closed);
        assertEquals(player.opened.get(1).range.blocks.size(), 4);
    }

    @Test
    public void authoredMovieStreamsDecodeForMainTimelineAndSpritePreviews() throws Exception {
        SWF swf;
        try (FileInputStream input = new FileInputStream("libsrc/ffdec_lib/testdata/sounds/sounds.swf")) {
            swf = new SWF(input, false);
        }
        List<Timelined> timelines = new ArrayList<>();
        timelines.add(swf);
        for (Tag tag : swf.getTags()) {
            if (tag instanceof DefineSpriteTag) {
                timelines.add((DefineSpriteTag) tag);
            }
        }
        int[] decoded = {0};
        SoundStreamPlayer player = new SoundStreamPlayer((range, position) -> {
            ByteArrayOutputStream wav = new ByteArrayOutputStream();
            assertTrue(range.getSoundFormat().createWav(null, range.getRawSoundData(), wav, 0, true));
            try (AudioInputStream audio = SoundTagPlayer.createPlaybackStream(wav.toByteArray())) {
                assertTrue(audio.getFrameLength() > 0);
                assertTrue(audio.read(new byte[16]) > 0);
            }
            decoded[0]++;
            return new Playback(range, position);
        });
        for (Timelined timeline : timelines) {
            for (int frame = 0; frame < timeline.getTimeline().getFrameCount(); frame++) {
                player.update(timeline.getTimeline(), frame, 0, true, null, 0);
            }
            player.stop();
        }
        assertTrue(decoded[0] > 0);
        player.reset();
    }

    private static SWF swf() throws Exception {
        SWF swf = new SWF();
        swf.frameRate = 10;
        return swf;
    }

    private static SoundStreamHeadTypeTag head(SWF swf, boolean version2, int compression) {
        SoundStreamHeadTypeTag head = version2 ? new SoundStreamHead2Tag(swf) : new SoundStreamHeadTag(swf);
        head.setSoundCompression(compression);
        head.setSoundRate(3);
        head.setSoundSize(true);
        head.setSoundType(false);
        head.setSoundSampleCount(4410);
        return head;
    }

    private static void blocks(Timelined timeline, int count) {
        SWF swf = timeline.getSwf();
        for (int i = 0; i < count; i++) {
            SoundStreamBlockTag block = new SoundStreamBlockTag(swf);
            block.streamSoundData = new ByteArrayRange(new byte[]{1, 2, 3, 4});
            timeline.addTag(block);
            timeline.addTag(new ShowFrameTag(swf));
        }
    }

    private static DefineSpriteTag sprite(SWF swf, int frames) {
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        sprite.addTag(head(swf, true, 3));
        blocks(sprite, frames);
        sprite.frameCount = frames;
        swf.addTag(sprite);
        return sprite;
    }
}
