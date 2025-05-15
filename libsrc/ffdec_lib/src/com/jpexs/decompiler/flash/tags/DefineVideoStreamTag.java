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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.exporters.MovieExporter;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.modes.MovieExportMode;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.flash.types.filters.BlendComposite;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.video.FrameListener;
import com.jpexs.video.SimpleMediaPlayer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DefineVideoStream tag - defines video stream.
 *
 * @author JPEXS
 */
@SWFVersion(from = 6)
public class DefineVideoStreamTag extends DrawableTag implements BoundedTag, Timelined {

    public static final int ID = 60;

    public static final String NAME = "DefineVideoStream";

    @SWFType(BasicType.UI16)
    public int characterID;

    @SWFType(BasicType.UI16)
    public int numFrames;

    @SWFType(BasicType.UI16)
    public int width;

    @SWFType(BasicType.UI16)
    public int height;

    @Reserved
    @SWFType(value = BasicType.UB, count = 4)
    public int reserved;

    @SWFType(value = BasicType.UB, count = 3)
    @EnumValue(value = DefineVideoStreamTag.DEBLOCKING_USE_VIDEOPACKET_VALUE, text = "use VIDEOPACKET value")
    @EnumValue(value = DefineVideoStreamTag.DEBLOCKING_OFF, text = "off")
    @EnumValue(value = DefineVideoStreamTag.DEBLOCKING_LEVEL1, text = "Level 1 (Fast deblocking filter)")
    @EnumValue(value = DefineVideoStreamTag.DEBLOCKING_LEVEL2, text = "Level 2 (VP6 only, better deblocking filter)")
    @EnumValue(value = DefineVideoStreamTag.DEBLOCKING_LEVEL3, text = "Level 3 (VP6 only, better deblocking plus fast deringing filter)")
    @EnumValue(value = DefineVideoStreamTag.DEBLOCKING_LEVEL4, text = "Level 4 (VP6 only, better deblocking plus better deringing filter)")
    @EnumValue(value = DefineVideoStreamTag.DEBLOCKING_RESERVED1, text = "Reserved")
    @EnumValue(value = DefineVideoStreamTag.DEBLOCKING_RESERVED2, text = "Reserved")
    public int videoFlagsDeblocking;

    public boolean videoFlagsSmoothing;

    @SWFType(BasicType.UI8)
    @EnumValue(value = DefineVideoStreamTag.CODEC_JPEG, text = "JPEG (unused)")
    @EnumValue(value = DefineVideoStreamTag.CODEC_SORENSON_H263, text = "Sorenson H.263")
    @EnumValue(value = DefineVideoStreamTag.CODEC_SCREEN_VIDEO, text = "Screen video")
    @EnumValue(value = DefineVideoStreamTag.CODEC_VP6, text = "On2 VP6")
    @EnumValue(value = DefineVideoStreamTag.CODEC_VP6_ALPHA, text = "On2 VP6 video with alpha channel")
    @EnumValue(value = DefineVideoStreamTag.CODEC_SCREEN_VIDEO_V2, text = "Screen video version 2")
    @EnumValue(value = DefineVideoStreamTag.CODEC_AVC, text = "AVC")
    public int codecID;

    @Internal
    private SimpleMediaPlayer mediaPlayer;
    @Internal
    private final Object getFrameLock = new Object();

    @Internal
    private BufferedImage activeFrame = null;

    @Internal
    private ReadOnlyTagList tags;

    @Internal
    private int lastFrame = -1;

    @Internal
    private Timeline timeline;

    @Internal
    private Map<Integer, VideoFrameTag> frames;

    private static final List<SimpleMediaPlayer> players = new ArrayList<>();
    private static List<File> tempFiles = new ArrayList<>();

    @Internal
    private boolean renderingPaused = false;

    public static final int CODEC_JPEG = 1;
    public static final int CODEC_SORENSON_H263 = 2;
    public static final int CODEC_SCREEN_VIDEO = 3;
    public static final int CODEC_VP6 = 4;
    public static final int CODEC_VP6_ALPHA = 5;
    public static final int CODEC_SCREEN_VIDEO_V2 = 6;
    public static final int CODEC_AVC = 7; //Is this FLV only, or SWF too?

    /**
     * use VIDEOPACKET value
     */
    public static final int DEBLOCKING_USE_VIDEOPACKET_VALUE = 0;
    /**
     * off
     */
    public static final int DEBLOCKING_OFF = 1;
    /**
     * Level 1 (Fast deblocking filter)
     */
    public static final int DEBLOCKING_LEVEL1 = 2;
    /**
     * Level 2 (VP6 only, better deblocking filter)
     */
    public static final int DEBLOCKING_LEVEL2 = 3;
    /**
     * Level 3 (VP6 only, better deblocking plus fast deringing filter)
     */
    public static final int DEBLOCKING_LEVEL3 = 4;
    /**
     * Level 4 (VP6 only, better deblocking plus better deringing filter)
     */
    public static final int DEBLOCKING_LEVEL4 = 5;
    public static final int DEBLOCKING_RESERVED1 = 6;
    public static final int DEBLOCKING_RESERVED2 = 7;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (SimpleMediaPlayer p : players) {
                    p.stop();
                }
                for (File f : tempFiles) {
                    f.delete();
                }
            }

        });
    }

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public DefineVideoStreamTag(SWF swf) {
        super(swf, ID, NAME, null);
        characterID = swf.getNextCharacterId();
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public DefineVideoStreamTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterID = sis.readUI16("characterID");
        numFrames = sis.readUI16("numFrames");
        width = sis.readUI16("width");
        height = sis.readUI16("height");
        reserved = (int) sis.readUB(4, "reserved");
        videoFlagsDeblocking = (int) sis.readUB(3, "videoFlagsDeblocking");
        videoFlagsSmoothing = sis.readUB(1, "videoFlagsSmoothing") == 1;
        codecID = sis.readUI8("codecID");
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(characterID);
        sos.writeUI16(numFrames);
        sos.writeUI16(width);
        sos.writeUI16(height);
        sos.writeUB(4, reserved);
        sos.writeUB(3, videoFlagsDeblocking);
        sos.writeUB(1, videoFlagsSmoothing ? 1 : 0);
        sos.writeUI8(codecID);
    }

    @Override
    public int getCharacterId() {
        return characterID;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.characterID = characterId;
    }

    @Override
    public RECT getRect() {
        return getRect(null); // parameter not used
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        return new RECT(0, (int) (SWF.unitDivisor * width), 0, (int) (SWF.unitDivisor * height));
    }

    @Override
    public RECT getRectWithStrokes() {
        return getRect();
    }

    public static boolean displayAvailable() {
        return SimpleMediaPlayer.isAvailable();
    }

    private void initPlayer() {
        if (mediaPlayer != null) { // && !mediaPlayer.isFinished()) {
            return;
        }
        MovieExporter exp = new MovieExporter();
        try {
            byte[] data = exp.exportMovie(this, MovieExportMode.FLV, true);
            if (data.length == 0) {
                return;
            }
            File tempFile = File.createTempFile("ffdec_video", ".flv");
            Helper.writeFile(tempFile.getAbsolutePath(), data);

            mediaPlayer = new SimpleMediaPlayer();
            players.add(mediaPlayer);
            mediaPlayer.addFrameListener(new FrameListener() {
                @Override
                public void newFrameReceived(BufferedImage image) {
                    synchronized (getFrameLock) {
                        activeFrame = image;
                        //System.out.println("received frame");
                        getFrameLock.notifyAll();
                        /*if (mediaPlayer.isFinished()) {
                            mediaPlayer.rewind();
                        }*/
                    }
                }
            });
            mediaPlayer.play(tempFile.getAbsolutePath());
            tempFiles.add(tempFile);
            //mediaPlayer.pause();

        } catch (IOException ex) {
            Logger.getLogger(DefineVideoStreamTag.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int getUsedParameters() {
        return PARAMETER_RATIO;
    }

    @Override
    public Shape getOutline(boolean fast, int frame, int time, int ratio, RenderContext renderContext, Matrix transformation, boolean stroked, ExportRectangle viewRect, double unzoom) {
        return transformation.toTransform().createTransformedShape(new Rectangle2D.Double(0, 0, width * SWF.unitDivisor, height * SWF.unitDivisor));
    }

    public synchronized void resetPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }

    public synchronized void setPauseRendering(boolean value) {
        this.renderingPaused = value;
        activeFrame = null;
    }

    @Override
    public synchronized void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, SerializableImage fullImage, boolean isClip, Matrix transformation, Matrix prevTransformation, Matrix absoluteTransformation, Matrix fullTransformation, ColorTransform colorTransform, double unzoom, boolean sameImage, ExportRectangle viewRect, ExportRectangle viewRectRaw, boolean scaleStrokes, int drawMode, int blendMode, boolean canUseSmoothing) {

        if (renderingPaused || !SimpleMediaPlayer.isAvailable()) {
            Graphics2D g = (Graphics2D) image.getBufferedImage().getGraphics();
            Matrix mat = transformation;
            AffineTransform trans = mat.preConcatenate(Matrix.getScaleInstance(1 / SWF.unitDivisor)).toTransform();
            g.setTransform(trans);
            BoundedTag b = (BoundedTag) this;
            g.setPaint(new Color(255, 255, 255, 128));
            g.setComposite(BlendComposite.Invert);
            g.setStroke(new BasicStroke((int) SWF.unitDivisor));
            RECT r = b.getRect();
            g.setFont(g.getFont().deriveFont((float) (12 * SWF.unitDivisor)));
            g.drawString(toString(), r.Xmin + (int) (3 * SWF.unitDivisor), r.Ymin + (int) (15 * SWF.unitDivisor));
            g.draw(new Rectangle(r.Xmin, r.Ymin, r.getWidth(), r.getHeight()));
            g.drawLine(r.Xmin, r.Ymin, r.Xmax, r.Ymax);
            g.drawLine(r.Xmax, r.Ymin, r.Xmin, r.Ymax);
            g.setComposite(AlphaComposite.Dst);
            return;
        }
        if (ratio == -1) {
            ratio = 0;
        }

        Set<Integer> keyFrames = getFrames().keySet();

        int f = 0;
        for (int i = 0; i <= ratio; i++) {
            if (keyFrames.contains(i)) {
                f = i;
            }
        }

        synchronized (DefineVideoStreamTag.class) {
            if (!(activeFrame != null && lastFrame == f)) {
                initPlayer();

                if (mediaPlayer == null) {
                    return;
                }

                float oneFr = 0; //1f / (getNumFrames() + 2);

                synchronized (getFrameLock) {
                    activeFrame = null;
                }
                mediaPlayer.setPosition(((float) f) / (getNumFrames() + 2) - (f == 0 ? 0 : oneFr / 10f));

                try {
                    synchronized (getFrameLock) {
                        if (activeFrame == null) {
                            //System.out.println("waiting...");
                            getFrameLock.wait();
                            Thread.sleep(10); //magic, but should work
                            mediaPlayer.pause();
                            //System.out.println("awakened");
                        }
                    }
                } catch (InterruptedException ex) {
                    //Logger.getLogger(DefineVideoStreamTag.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            lastFrame = f;
            synchronized (getFrameLock) {
                if (activeFrame != null) {
                    if (renderingPaused) {
                        return;
                    }
                    //System.out.println("drawed");
                    Graphics2D graphics = (Graphics2D) image.getBufferedImage().getGraphics();
                    AffineTransform at = transformation.toTransform();
                    at.preConcatenate(AffineTransform.getScaleInstance(1 / SWF.unitDivisor, 1 / SWF.unitDivisor));
                    graphics.setTransform(at);
                    //Point p = transformation.inverse().transform(0, 0);
                    graphics.drawImage(activeFrame, 0, 0,
                            (int) Math.round(width * SWF.unitDivisor),
                            (int) Math.round(height * SWF.unitDivisor),
                            0, 0, width, height,
                            null);
                }
            }
        }
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) throws IOException {
    }

    @Override
    public void toHtmlCanvas(StringBuilder result, double unitDivisor) {
    }

    @Override
    public int getNumFrames() {
        return numFrames;
    }

    @Override
    public boolean isSingleFrame() {
        return getNumFrames() == 1;
    }

    @Override
    public Timeline getTimeline() {
        initTimeline();
        return timeline;
    }

    private Map<Integer, VideoFrameTag> getFrames() {
        if (this.frames != null) {
            return this.frames;
        }
        Map<Integer, VideoFrameTag> frames = new HashMap<>();
        SWF.populateVideoFrames(characterID, swf.getTags(), frames);
        this.frames = frames;
        return frames;
    }

    private void initTimeline() {
        if (timeline != null) {
            return;
        }
        Set<Integer> frameNums = new TreeSet<>(getFrames().keySet());
        int maxFr = 0;
        for (int f : frameNums) {
            maxFr = f;
        }
        List<Tag> tags = new ArrayList<>();
        for (int f = 0; f <= maxFr; f++) {
            if (frames.containsKey(f)) {
                tags.add(frames.get(f));
            }
            tags.add(new PlaceObject2Tag(swf, f == 0 ? false : true, 1, f == 0 ? characterID : -1, new MATRIX(), null, f, null, -1, null));
            tags.add(new ShowFrameTag(swf));
        }
        this.tags = new ReadOnlyTagList(tags);

        timeline = new Timeline(swf, this, characterID, getRect()) {
            @Override
            public void getSounds(int frame, int time, ButtonTag mouseOverButton, int mouseButton, List<Integer> sounds, List<String> soundClasses, List<SOUNDINFO> soundInfos) {
            }
        };
    }

    @Override
    public void resetTimeline() {
        timeline = null;
        frames = null;
    }

    @Override
    public synchronized ReadOnlyTagList getTags() {
        initTimeline();
        return tags;
    }

    @Override
    public void removeTag(int index) {

    }

    @Override
    public void removeTag(Tag tag) {

    }

    @Override
    public void addTag(Tag tag) {
    }

    @Override
    public void addTag(int index, Tag tag) {
    }

    @Override
    public void replaceTag(int index, Tag newTag) {
    }

    @Override
    public void replaceTag(Tag oldTag, Tag newTag) {
    }

    @Override
    public int indexOfTag(Tag tag) {
        return tags.indexOf(tag);
    }

    @Override
    public void setFrameCount(int frameCount) {
    }

    @Override
    public int getFrameCount() {
        return numFrames;
    }

}
