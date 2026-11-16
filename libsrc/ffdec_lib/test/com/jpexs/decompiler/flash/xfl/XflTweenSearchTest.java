/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import java.io.FileInputStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.testng.Assert;
import org.testng.annotations.Test;

public class XflTweenSearchTest {

    private static final class CountingSamples extends AbstractList<ClassicTweenDetector.Sample> {
        private final List<ClassicTweenDetector.Sample> samples = new ArrayList<>();
        private long reads;

        @Override
        public ClassicTweenDetector.Sample get(int index) {
            reads++;
            return samples.get(index);
        }

        @Override
        public int size() {
            return samples.size();
        }
    }

    private CountingSamples samples(int count) {
        CountingSamples result = new CountingSamples();
        Random random = new Random(0xf1a);
        for (int f = 0; f < count; f++) {
            ClassicTweenDetector.Sample sample = new ClassicTweenDetector.Sample();
            sample.index = f;
            sample.duration = 1;
            sample.matrix = new double[]{1, 0, 0, 1};
            sample.values[0] = random.nextInt(100);
            sample.values[2] = sample.values[3] = 1;
            result.samples.add(sample);
        }
        return result;
    }

    @Test
    public void shortenedCandidatesReuseFailedSegmentsWithoutChangingTheirResults() throws Exception {
        CountingSamples samples = samples(90);
        List<String> expected = new ArrayList<>();
        for (int start = 0; start < 4; start++) {
            for (int end = samples.size() - 1; end >= start + 10; end--) {
                expected.add(MotionTweenDetector.fit(samples, start, end, 24, new double[]{0, 0}));
            }
        }
        long uncachedReads = samples.reads;
        samples.reads = 0;
        MotionTweenDetector.FitCache cache = new MotionTweenDetector.FitCache();
        int index = 0;
        for (int start = 0; start < 4; start++) {
            for (int end = samples.size() - 1; end >= start + 10; end--) {
                Assert.assertEquals(MotionTweenDetector.fit(samples, start, end, 24, new double[]{0, 0}, cache),
                        expected.get(index++));
            }
        }
        Assert.assertTrue(samples.reads < uncachedReads,
                "Failed cubic segments must be reused, not searched again for each shorter candidate");
    }

    @Test
    public void successfulPiecewiseFitsAreReusableWhenCandidateEndsMoveInEitherDirection() throws Exception {
        CountingSamples samples = samples(61);
        for (int f = 0; f < samples.size(); f++) {
            double value = f <= 30 ? f * f * f / 270.0
                    : 100 + Math.pow(f - 30, 3) / 135.0;
            samples.get(f).values[0] = Math.round(value * 20) / 20.0;
        }
        MotionTweenDetector.FitCache cache = new MotionTweenDetector.FitCache();
        Assert.assertNotNull(MotionTweenDetector.fit(samples, 0, 60, 24, new double[]{0, 0}, cache));
        for (int start : new int[]{0, 3, 5}) {
            for (int end : new int[]{60, 58, 49, 55, 60}) {
                String expected = MotionTweenDetector.fit(samples, start, end, 24, new double[]{0, 0});
                Assert.assertEquals(MotionTweenDetector.fit(samples, start, end, 24, new double[]{0, 0}, cache),
                        expected);
            }
        }
    }

    @Test(timeOut = 10000)
    public void arbitraryMotionWithALongFinalHoldDoesNotFitAndDiscardAllItsCubics() throws Exception {
        CountingSamples samples = samples(30);
        samples.get(samples.size() - 1).duration = 1000000;
        Assert.assertNull(MotionTweenDetector.fit(samples, 0, samples.size() - 1, 24, new double[]{0, 0}));
    }

    private static final class CountingShape extends DefineShape3Tag {
        private int reads;

        CountingShape(SWF swf) {
            super(swf);
        }

        @Override
        public synchronized SHAPEWITHSTYLE getShapes() {
            reads++;
            return super.getShapes();
        }
    }

    private static final class CountingMorph extends DefineMorphShapeTag {
        private final SHAPEWITHSTYLE endpoint;
        private int reads;

        CountingMorph(SWF swf, SHAPEWITHSTYLE endpoint) {
            super(swf);
            this.endpoint = endpoint;
        }

        @Override
        public SHAPEWITHSTYLE getShapeAtRatio(int ratio) {
            Assert.assertEquals(ratio, 65535);
            reads++;
            return endpoint;
        }
    }

    @Test
    public void ordinaryTimelineDoesNotComputeGeometryWithoutAMorphCandidate() {
        SWF swf = new SWF();
        CountingShape shape = new CountingShape(swf);
        shape.setCharacterId(1);
        swf.addTag(shape);
        List<Tag> tags = new ArrayList<>();
        for (int f = 0; f < 50; f++) {
            tags.add(new PlaceObject2Tag(swf, f != 0, 1, 1, new MATRIX(), null, -1, null, -1, null));
            tags.add(new ShowFrameTag(swf));
        }
        shape.reads = 0;
        ReadOnlyTagList restored = MorphShapeEndings.restore(new ReadOnlyTagList(tags), new MorphShapeSymbols());
        Assert.assertEquals(restored.toArrayList(), tags);
        Assert.assertEquals(shape.reads, 0);
    }

    @Test
    public void repeatedMorphEndpointsComputeEachCharactersGeometryOnlyOnce() throws Exception {
        SWF fixture;
        try (FileInputStream in = new FileInputStream("testdata/xfl/graphics-tweens.swf")) {
            fixture = new SWF(in, false);
        }
        SHAPEWITHSTYLE endpoint = ((ShapeTag) fixture.getCharacter(51)).getShapes();
        SWF swf = new SWF();
        CountingShape shape = new CountingShape(swf);
        shape.setCharacterId(1);
        shape.shapes = endpoint;
        swf.addTag(shape);
        CountingMorph morph = new CountingMorph(swf, endpoint);
        morph.setCharacterId(2);
        swf.addTag(morph);
        List<Tag> tags = new ArrayList<>();
        for (int f = 0; f < 20; f++) {
            tags.add(new PlaceObject2Tag(swf, false, 1, 2, new MATRIX(), null, -1, null, -1, null));
            tags.add(new ShowFrameTag(swf));
            tags.add(new PlaceObject2Tag(swf, true, 1, 1, new MATRIX(), null, -1, null, -1, null));
            tags.add(new ShowFrameTag(swf));
        }
        shape.reads = 0;
        ReadOnlyTagList restored = MorphShapeEndings.restore(new ReadOnlyTagList(tags), new MorphShapeSymbols());
        int endings = 0;
        for (Tag tag : restored) {
            if (tag instanceof MorphShapeEndings.EndPlacement) {
                endings++;
            }
        }
        Assert.assertEquals(endings, 20);
        Assert.assertEquals(morph.reads, 1);
        Assert.assertEquals(shape.reads, 1);
        MorphShapeEndings.restore(new ReadOnlyTagList(tags), new MorphShapeSymbols());
        Assert.assertEquals(morph.reads, 2, "Geometry caches must not leak between exports");
    }
}
