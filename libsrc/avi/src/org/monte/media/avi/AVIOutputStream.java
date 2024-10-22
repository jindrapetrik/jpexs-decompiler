/**
 * @(#)AVIOutputStream.java
 *
 * Copyright (c) 2011-2012 Werner Randelshofer, Goldau, Switzerland. All
 * rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer. For details see
 * accompanying license terms.
 */
package org.monte.media.avi;

import java.awt.image.ColorModel;
import org.monte.media.riff.RIFFChunk;
import org.monte.media.math.Rational;
import java.util.ArrayList;
import org.monte.media.Format;
import org.monte.media.riff.RIFFParser;
import java.awt.Dimension;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.nio.ByteOrder;
import javax.imageio.stream.*;
import static java.lang.Math.*;
import static org.monte.media.FormatKeys.*;
import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

/**
 * Provides low-level support for writing already encoded audio and video
 * samples into an AVI 1.0 file. <p> The length of an AVI 1.0 file is limited to
 * 1 GB. This class supports lengths of up to 4 GB, but such files may not work
 * on all players. <p> For detailed information about the AVI 1.0 file format
 * see:<br> <a
 * href="http://msdn.microsoft.com/en-us/library/ms779636.aspx">msdn.microsoft.com
 * AVI RIFF</a><br> <a
 * href="http://www.microsoft.com/whdc/archive/fourcc.mspx">www.microsoft.com
 * FOURCC for Video Compression</a><br> <a
 * href="http://www.saettler.com/RIFFMCI/riffmci.html">www.saettler.com
 * RIFF</a><br>
 *
 * @author Werner Randelshofer
 * @version $Id: AVIOutputStream.java 306 2013-01-04 16:19:29Z werner $
 */
public class AVIOutputStream extends AbstractAVIStream {

    /**
     * The states of the movie output stream.
     */
    protected static enum States {

        STARTED, FINISHED, CLOSED;
    }
    /**
     * The current state of the movie output stream.
     */
    protected States state = States.FINISHED;
    /**
     * This chunk holds the whole AVI content.
     */
    protected CompositeChunk aviChunk;
    /**
     * This chunk holds the movie frames.
     */
    protected CompositeChunk moviChunk;
    /**
     * This chunk holds the AVI Main Header.
     */
    protected FixedSizeDataChunk avihChunk;
    ArrayList<Sample> idx1 = new ArrayList<Sample>();

    /**
     * Creates a new instance.
     *
     * @param file the output file
     */
    public AVIOutputStream(File file) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        this.out = new FileImageOutputStream(file);
        out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        this.streamOffset = 0;
    }

    /**
     * Creates a new instance.
     *
     * @param out the output stream.
     */
    public AVIOutputStream(ImageOutputStream out) throws IOException {
        this.out = out;
        this.streamOffset = out.getStreamPosition();
        out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Adds a video track.
     *
     * @param fccHandler The 4-character code of the format.
     * @param scale The numerator of the sample rate.
     * @param rate The denominator of the sample rate.
     * @param width The width of a video image. Must be greater than 0.
     * @param height The height of a video image. Must be greater than 0.
     * @param depth The number of bits per pixel. Must be greater than 0.
     * @param syncInterval Interval for sync-samples. 0=automatic. 1=all frames
     * are keyframes. Values larger than 1 specify that for every n-th frame is
     * a keyframe.
     *
     * @return Returns the track index.
     *
     * @throws IllegalArgumentException if the width or the height is smaller
     * than 1.
     */
    public int addVideoTrack(String fccHandler, long scale, long rate, int width, int height, int depth, int syncInterval) throws IOException {
        ensureFinished();
        if (fccHandler == null || fccHandler.length() != 4) {
            throw new IllegalArgumentException("fccHandler must be 4 characters long:" + fccHandler);
        }
        VideoTrack vt = new VideoTrack(tracks.size(), typeToInt(fccHandler),//
                new Format(MediaTypeKey, MediaType.VIDEO,
                MimeTypeKey, MIME_AVI,
                EncodingKey, fccHandler,
                DataClassKey, byte[].class,
                WidthKey, width, HeightKey, height, DepthKey, depth,
                FixedFrameRateKey, true,
                FrameRateKey, new Rational(rate, scale)));
        vt.scale = scale;
        vt.rate = rate;
        vt.syncInterval = syncInterval;
        vt.frameLeft = 0;
        vt.frameTop = 0;
        vt.frameRight = width;
        vt.frameBottom = height;
        vt.bitCount = depth;
        vt.planes = 1; // must be 1

        if (depth == 4) {
            byte[] gray = new byte[16];
            for (int i = 0; i < gray.length; i++) {
                gray[i] = (byte) ((i << 4) | i);
            }
            vt.palette = new IndexColorModel(4, 16, gray, gray, gray);
        } else if (depth == 8) {
            byte[] gray = new byte[256];
            for (int i = 0; i < gray.length; i++) {
                gray[i] = (byte) i;
            }
            vt.palette = new IndexColorModel(8, 256, gray, gray, gray);
        }

        tracks.add(vt);
        return tracks.size() - 1;
    }

    /**
     * Adds an audio track.
     *
     * @param waveFormatTag The format of the audio stream given in MMREG.H, for
     * example 0x0001 for WAVE_FORMAT_PCM.
     * @param scale The numerator of the sample rate.
     * @param rate The denominator of the sample rate.
     * @param numberOfChannels The number of channels: 1 for mono, 2 for stereo.
     * @param sampleSizeInBits The number of bits in a sample: 8 or 16.
     * @param isCompressed Whether the sound is compressed.
     * @param frameDuration The frame duration, expressed in the media’s
     * timescale, where the timescale is equal to the sample rate. For
     * uncompressed formats, this field is always 1.
     * @param frameSize For uncompressed audio, the number of bytes in a sample
     * for a single channel (sampleSize divided by 8). For compressed audio, the
     * number of bytes in a frame.
     *
     * @throws IllegalArgumentException if the format is not 4 characters long,
     * if the time scale is not between 1 and 2^32, if the integer portion of
     * the sampleRate is not equal to the scale, if numberOfChannels is not 1 or
     * 2.
     * @return Returns the track index.
     */
    public int addAudioTrack(int waveFormatTag, //
            long scale, long rate, //
            int numberOfChannels, int sampleSizeInBits, //
            boolean isCompressed, //
            int frameDuration, int frameSize) throws IOException {
        ensureFinished();

        if (scale < 1 || scale > (2L << 32)) {
            throw new IllegalArgumentException("timeScale must be between 1 and 2^32:" + scale);
        }
        if (numberOfChannels != 1 && numberOfChannels != 2) {
            throw new IllegalArgumentException("numberOfChannels must be 1 or 2: " + numberOfChannels);
        }
        if (sampleSizeInBits != 8 && sampleSizeInBits != 16) {
            throw new IllegalArgumentException("sampleSize must be 8 or 16: " + numberOfChannels);
        }

        AudioTrack t = new AudioTrack(tracks.size(), typeToInt("\u0000\u0000\u0000\u0000"));
        t.wFormatTag = waveFormatTag;

        float afSampleRate = (float) rate / (float) scale;

        t.format = new Format(MediaTypeKey, MediaType.AUDIO,
                MimeTypeKey, MIME_AVI,
                EncodingKey, RIFFParser.idToString(waveFormatTag),
                SampleRateKey, Rational.valueOf(afSampleRate),
                SampleSizeInBitsKey, sampleSizeInBits,
                ChannelsKey, numberOfChannels,
                FrameSizeKey, frameSize,
                FrameRateKey, Rational.valueOf(afSampleRate),
                SignedKey, sampleSizeInBits != 8,
                ByteOrderKey, ByteOrder.LITTLE_ENDIAN);

        t.scale = scale;
        t.rate = rate;
        t.samplesPerSec = rate / scale;
        t.channels = numberOfChannels;
        t.avgBytesPerSec = t.samplesPerSec * frameSize;
        t.blockAlign = t.channels * sampleSizeInBits / 8;
        t.bitsPerSample = sampleSizeInBits;
        tracks.add(t);
        return tracks.size() - 1;
    }

    /**
     * Sets the global color palette.
     */
    public void setPalette(int track, ColorModel palette) {
        if (palette instanceof IndexColorModel) {
            ((VideoTrack) tracks.get(track)).palette = (IndexColorModel) palette;
        }
    }

    /**
     * Gets the dimension of a track.
     */
    public Dimension getVideoDimension(int track) {
        Track tr = tracks.get(track);
        if (tr instanceof VideoTrack) {
            VideoTrack vt = (VideoTrack) tr;
            Format fmt = vt.format;
            return new Dimension(fmt.get(WidthKey), fmt.get(HeightKey));
        } else {
            return new Dimension(0, 0);
        }
    }

    /**
     * Returns the contents of the extra track header. Returns null if the
     * header is not present. <p> Note: this method can only be performed before
     * media data has been written into the tracks.
     *
     * @param track
     * @param fourcc
     * @param data the extra header as a byte array
     * @throws IOException
     */
    public void putExtraHeader(int track, String fourcc, byte[] data) throws IOException {
        if (state == States.STARTED) {
            throw new IllegalStateException("Stream headers have already been written!");
        }
        Track tr = tracks.get(track);
        int id = RIFFParser.stringToID(fourcc);
        // Remove duplicate entries
        for (int i = tr.extraHeaders.size() - 1; i >= 0; i--) {
            if (tr.extraHeaders.get(i).getID() == id) {
                tr.extraHeaders.remove(i);
            }
        }

        // Add new entry
        RIFFChunk chunk = new RIFFChunk(STRH_ID, id, data.length, -1);
        chunk.setData(data);
        tr.extraHeaders.add(chunk);
    }

    /**
     * Returns the fourcc's of all extra stream headers.
     *
     * @param track
     * @return An array of fourcc's of all extra stream headers.
     * @throws IOException
     */
    public String[] getExtraHeaderFourCCs(int track) throws IOException {
        Track tr = tracks.get(track);
        String[] fourccs = new String[tr.extraHeaders.size()];
        for (int i = 0; i < fourccs.length; i++) {
            fourccs[i] = RIFFParser.idToString(tr.extraHeaders.get(i).getID());
        }
        return fourccs;
    }

    public void setName(int track, String name) {
        tracks.get(track).name = name;
    }

    /**
     * Sets the compression quality of a track. <p> A value of 0 stands for
     * "high compression is important" a value of 1 for "high image quality is
     * important". <p> Changing this value affects the encoding of video frames
     * which are subsequently written into the track. Frames which have already
     * been written are not changed. <p> This value has no effect on videos
     * encoded with lossless encoders such as the PNG format. <p> The default
     * value is 0.97.
     *
     * @param newValue
     */
    public void setCompressionQuality(int track, float newValue) {
        VideoTrack vt = (VideoTrack) tracks.get(track);
        vt.videoQuality = newValue;
    }

    /**
     * Returns the compression quality of a track.
     *
     * @return compression quality
     */
    public float getCompressionQuality(int track) {
        return ((VideoTrack) tracks.get(track)).videoQuality;
    }    /**
     * Sets the state of the QuickTimeOutputStream to started. <p> If the state
     * is changed by this method, the prolog is written.
     */
    protected void ensureStarted() throws IOException {
        if (state != States.STARTED) {
            writeProlog();
            state = States.STARTED;
        }
    }

    /**
     * Sets the state of the QuickTimeOutputStream to finished. <p> If the state
     * is changed by this method, the prolog is written.
     */
    protected void ensureFinished() throws IOException {
        if (state != States.FINISHED) {
            throw new IllegalStateException("Writer is in illegal state for this operation.");
        }
    }

    /**
     * Writes an already encoded palette change into the specified track. <p> If
     * a track contains palette changes, then all key frames must be immediately
     * preceded by a palette change chunk which also is a key frame. If a key
     * frame is not preceded by a key frame palette change chunk, it will be
     * downgraded to a delta frame.
     *
     * @throws IllegalArgumentException if the track is not a video track.
     */
    public void writePalette(int track, byte[] data, int off, int len, boolean isKeyframe) throws IOException {
        Track tr = tracks.get(track);
        if (!(tr instanceof VideoTrack)) {
            throw new IllegalArgumentException("Error: track " + track + " is not a video track.");
        }
        if (!isKeyframe && tr.samples.isEmpty()) {
            throw new IllegalStateException("The first sample in a track must be a keyframe.");
        }

        VideoTrack vt = (VideoTrack) tr;
        tr.flags |= STRH_FLAG_VIDEO_PALETTE_CHANGES;

        DataChunk paletteChangeChunk = new DataChunk(vt.twoCC | PC_ID);
        long offset = getRelativeStreamPosition();
        ImageOutputStream pOut = paletteChangeChunk.getOutputStream();
        pOut.write(data, off, len);
        moviChunk.add(paletteChangeChunk);
        paletteChangeChunk.finish();
        long length = getRelativeStreamPosition() - offset;
        Sample s = new Sample(paletteChangeChunk.chunkType, 0, offset, length, isKeyframe);
        tr.addSample(s);
        idx1.add(s);
        //tr.length+=0;  Length is not affected by this chunk!
        offset = getRelativeStreamPosition();
    }

    /**
     * Writes an already encoded sample from a file to the specified track. <p>
     * This method does not inspect the contents of the file. For example, Its
     * your responsibility to only append JPG files if you have chosen the JPEG
     * video format. <p> If you append all frames from files or from input
     * streams, then you have to explicitly set the dimension of the video track
     * before you call finish() or close().
     *
     * @param file The file which holds the sample data.
     *
     * @throws IllegalStateException if the duration is less than 1.
     * @throws IOException if writing the sample data failed.
     */
    public void writeSample(int track, File file, boolean isKeyframe) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            writeSample(track, in, isKeyframe);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Writes an already encoded sample from an input stream to the specified
     * track. <p> This method does not inspect the contents of the file. For
     * example, its your responsibility to only append JPG files if you have
     * chosen the JPEG video format. <p> If you append all frames from files or
     * from input streams, then you have to explicitly set the dimension of the
     * video track before you call finish() or close().
     *
     * @param track The track number.
     * @param in The input stream which holds the sample data.
     * @param isKeyframe True if the sample is a key frame.
     *
     * @throws IllegalArgumentException if the duration is less than 1.
     * @throws IOException if writing the sample data failed.
     */
    public void writeSample(int track, InputStream in, boolean isKeyframe) throws IOException {
        ensureStarted();

        Track tr = tracks.get(track);

        if (!isKeyframe && tr.samples.isEmpty()) {
            throw new IllegalStateException("The first sample in a track must be a keyframe.");
        }

        // If a stream has palette changes, then only palette change samples can
        // be marked as keyframe.
        if (isKeyframe && 0 != (tr.flags & STRH_FLAG_VIDEO_PALETTE_CHANGES)) {
            // If a keyframe sample is immediately preceded by a palette change
            // we can raise the palette change to a keyframe.
            if (tr.samples.size() > 0) {
                Sample s = tr.samples.get(tr.samples.size() - 1);
                if ((s.chunkType & 0xffff) == PC_ID) {
                    s.isKeyframe = true;
                }
            }
            isKeyframe = false;
        }


        DataChunk dc = new DataChunk(tr.getSampleChunkFourCC(isKeyframe));
        moviChunk.add(dc);
        ImageOutputStream mdatOut = dc.getOutputStream();
        long offset = getRelativeStreamPosition();
        byte[] buf = new byte[512];
        int len;
        while ((len = in.read(buf)) != -1) {
            mdatOut.write(buf, 0, len);
        }
        long length = getRelativeStreamPosition() - offset;
        dc.finish();
        Sample s = new Sample(dc.chunkType, 1, offset, length, isKeyframe);
        tr.addSample(s);
        idx1.add(s);
        tr.length++;
        if (getRelativeStreamPosition() > 1L << 32) {
            throw new IOException("AVI file is larger than 4 GB");
        }
    }

    /**
     * Writes an already encoded sample from a byte array into a track. <p> This
     * method does not inspect the contents of the samples. The contents has to
     * match the format and dimensions of the media in this track. <p> If a
     * track contains palette changes, then all key frames must be immediately
     * preceded by a palette change chunk. If a key frame is not preceded by a
     * palette change chunk, it will be downgraded to a delta frame.
     *
     * @param track The track index.
     * @param data The encoded sample data.
     * @param off The startTime offset in the data.
     * @param len The number of bytes to write.
     * @param isKeyframe Whether the sample is a sync sample (keyframe).
     *
     * @throws IllegalArgumentException if the duration is less than 1.
     * @throws IOException if writing the sample data failed.
     */
    public void writeSample(int track, byte[] data, int off, int len, boolean isKeyframe) throws IOException {
        ensureStarted();
        Track tr = tracks.get(track);

        // The first sample in a track is always a key frame
        if (!isKeyframe && tr.samples.isEmpty()) {
            throw new IllegalStateException("The first sample in a track must be a keyframe.\nTrack="+track+", "+tr.format);
        }

        // If a stream has palette changes, then only palette change samples can
        // be marked as keyframe.
        if (isKeyframe && 0 != (tr.flags & STRH_FLAG_VIDEO_PALETTE_CHANGES)) {
            throw new IllegalStateException("Only palette changes can be marked as keyframe.\nTrack="+track+", "+tr.format);
        }

        DataChunk dc = new DataChunk(tr.getSampleChunkFourCC(isKeyframe), len);
        moviChunk.add(dc);
        ImageOutputStream mdatOut = dc.getOutputStream();
        long offset = getRelativeStreamPosition();
        mdatOut.write(data, off, len);
        long length = getRelativeStreamPosition() - offset;
        dc.finish();
        Sample s = new Sample(dc.chunkType, 1, offset, length, isKeyframe);
        tr.addSample(s);
        idx1.add(s);
        if (getRelativeStreamPosition() > 1L << 32) {
            throw new IOException("AVI file is larger than 4 GB");
        }
    }

    /**
     * Writes multiple already encoded samples from a byte array into a track.
     * <p> This method does not inspect the contents of the data. The contents
     * has to match the format and dimensions of the media in this track.
     *
     * @param track The track index.
     * @param sampleCount The number of samples.
     * @param data The encoded sample data.
     * @param off The startTime offset in the data.
     * @param len The number of bytes to write. Must be dividable by
     * sampleCount.
     * @param isKeyframe Whether the samples are sync samples. All samples must
     * either be sync samples or non-sync samples.
     *
     * @throws IllegalArgumentException if the duration is less than 1.
     * @throws IOException if writing the sample data failed.
     */
    public void writeSamples(int track, int sampleCount, byte[] data, int off, int len, boolean isKeyframe) throws IOException {
        ensureStarted();
        Track tr = tracks.get(track);
        if (tr.mediaType == AVIMediaType.AUDIO) {
            DataChunk dc = new DataChunk(tr.getSampleChunkFourCC(isKeyframe), len);
            moviChunk.add(dc);
            ImageOutputStream mdatOut = dc.getOutputStream();
            long offset = getRelativeStreamPosition();
            mdatOut.write(data, off, len);
            long length = getRelativeStreamPosition() - offset;
            dc.finish();
            Sample s = new Sample(dc.chunkType, sampleCount, offset, length, isKeyframe | tr.samples.isEmpty());
            tr.addSample(s);
            idx1.add(s);
            tr.length += sampleCount;
            if (getRelativeStreamPosition() > 1L << 32) {
                throw new IOException("AVI file is larger than 4 GB");
            }
        } else {
            for (int i = 0; i < sampleCount; i++) {
                writeSample(track, data, off, len / sampleCount, isKeyframe);
                off += len / sampleCount;
            }
        }
    }

    /**
     * Returns the duration of the track in media time scale.
     */
    public long getMediaDuration(int track) {
        Track tr = tracks.get(track);
        long duration = tr.startTime;
        if (!tr.samples.isEmpty()) {
            Sample s = tr.samples.get(tr.samples.size() - 1);
            duration += s.timeStamp + s.duration;
        }
        return duration;
    }

    /**
     * Closes the stream.
     *
     * @exception IOException if an I/O error has occurred
     */
    public void close() throws IOException {
        if (state == States.STARTED) {
            finish();
        }
        if (state != States.CLOSED) {
            out.close();
            state = States.CLOSED;
        }
    }

    /**
     * Finishes writing the contents of the AVI output stream without closing
     * the underlying stream. Use this method when applying multiple filters in
     * succession to the same output stream.
     *
     * @exception IllegalStateException if the dimension of the video track has
     * not been specified or determined yet.
     * @exception IOException if an I/O exception has occurred
     */
    public void finish() throws IOException {
        ensureOpen();
        if (state != States.FINISHED) {
            moviChunk.finish();
            writeEpilog();
            state = States.FINISHED;
        }
    }

    /**
     * Check to make sure that this stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (state == States.CLOSED) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * Returns true if the limit for media samples has been reached. If this
     * limit is reached, no more samples should be added to the movie. <p> AVI
     * 1.0 files have a file size limit of 2 GB. This method returns true if a
     * file size of 1.8 GB has been reached.
     */
    public boolean isDataLimitReached() {
        try {
            return getRelativeStreamPosition() > (long) (1.8 * 1024 * 1024 * 1024);
        } catch (IOException ex) {
            return true;
        }
    }

    private void writeProlog() throws IOException {
        // The file has the following structure:
        //
        // .RIFF AVI
        // ..avih (AVI Header Chunk)
        // ..LIST strl (for each track)
        // ...strh (Stream Header Chunk)
        // ...strf (Stream Format Chunk)
        // ...**** (Extra Stream Header Chunks)
        // ...strn (Stream Name Chunk)
        // ..LIST movi
        // ...00dc (Compressed video data chunk in Track 00, repeated for each frame)
        // ..idx1 (List of video data chunks and their location in the file)

        // The RIFF AVI Chunk holds the complete movie
        aviChunk = new CompositeChunk(RIFF_ID, AVI_ID);
        CompositeChunk hdrlChunk = new CompositeChunk(LIST_ID, HDRL_ID);

        // Write empty AVI Main Header Chunk - we fill the data in later
        aviChunk.add(hdrlChunk);
        avihChunk = new FixedSizeDataChunk(AVIH_ID, 56);
        avihChunk.seekToEndOfChunk();
        hdrlChunk.add(avihChunk);

        // Write empty AVI Stream Header Chunk - we fill the data in later
        for (Track tr : tracks) {

            CompositeChunk strlChunk = new CompositeChunk(LIST_ID, STRL_ID);
            hdrlChunk.add(strlChunk);

            tr.strhChunk = new FixedSizeDataChunk(STRH_ID, 56);
            tr.strhChunk.seekToEndOfChunk();
            strlChunk.add(tr.strhChunk);

            tr.strfChunk = new FixedSizeDataChunk(STRF_ID, tr.getSTRFChunkSize());
            tr.strfChunk.seekToEndOfChunk();
            strlChunk.add(tr.strfChunk);

            for (RIFFChunk c : tr.extraHeaders) {
                DataChunk d = new DataChunk(c.getID(),
                        c.getSize());
                ImageOutputStream dout = d.getOutputStream();
                dout.write(c.getData());
                d.finish();
                strlChunk.add(d);
            }

            if (tr.name != null) {
                byte[] data = (tr.name + "\u0000").getBytes("ASCII");
                DataChunk d = new DataChunk(STRN_ID,
                        data.length);
                ImageOutputStream dout = d.getOutputStream();
                dout.write(data);
                d.finish();
                strlChunk.add(d);
            }
        }

        moviChunk = new CompositeChunk(LIST_ID, MOVI_ID);
        aviChunk.add(moviChunk);


    }

    private void writeEpilog() throws IOException {

        ImageOutputStream d;

        /* Create Idx1 Chunk and write data
         * -------------
         typedef struct _avioldindex {
         FOURCC  fcc;
         DWORD   cb;
         struct _avioldindex_entry {
         DWORD   dwChunkId;
         DWORD   flags;
         DWORD   dwOffset;
         DWORD   dwSize;
         } aIndex[];
         } AVIOLDINDEX;
         */
        {
            DataChunk idx1Chunk = new DataChunk(IDX1_ID);
            aviChunk.add(idx1Chunk);
            d = idx1Chunk.getOutputStream();
            long moviListOffset = moviChunk.offset + 8 + 8;

            {
                double movieTime = 0;
                int nTracks = tracks.size();
                int[] trackSampleIndex = new int[nTracks];
                long[] trackSampleCount = new long[nTracks];
                for (Sample s : idx1) {
                    d.setByteOrder(ByteOrder.BIG_ENDIAN);
                    d.writeInt(s.chunkType); // dwChunkId
                    d.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                    // Specifies a FOURCC that identifies a stream in the AVI file. The
                    // FOURCC must have the form 'xxyy' where xx is the stream number and yy
                    // is a two-character code that identifies the contents of the stream:
                    //
                    // Two-character code   Description
                    //  db                  Uncompressed video frame
                    //  dc                  Compressed video frame
                    //  header                  Palette change
                    //  wb                  Audio data

                    d.writeInt(((s.chunkType & 0xffff) == PC_ID ? 0x100 : 0x0)//
                            | (s.isKeyframe ? 0x10 : 0x0)); // flags
                    // Specifies a bitwise combination of zero or more of the following
                    // flags:
                    //
                    // Value    Name            Description
                    // 0x10     AVIIF_KEYFRAME  The data chunk is a key frame.
                    // 0x1      AVIIF_LIST      The data chunk is a 'rec ' list.
                    // 0x100    AVIIF_NO_TIME   The data chunk does not affect the timing of the
                    //                          stream. For example, this flag should be set for
                    //                          palette changes.

                    d.writeInt((int) (s.offset - moviListOffset)); // dwOffset
                    // Specifies the location of the data chunk in the file. The value
                    // should be specified as an offset, in bytes, from the startTime of the
                    // 'movi' list; however, in some AVI files it is given as an offset from
                    // the startTime of the file.

                    d.writeInt((int) (s.length)); // dwSize
                    // Specifies the size of the data chunk, in bytes.
                }

            }

            idx1Chunk.finish();
        }

        /* Write Data into AVI Main Header Chunk
         * -------------
         * The AVIMAINHEADER structure defines global information in an AVI file.
         * see http://msdn.microsoft.com/en-us/library/ms779632(VS.85).aspx
         typedef struct _avimainheader {
         FOURCC fcc;
         DWORD  cb;
         DWORD  dwMicroSecPerFrame;
         DWORD  dwMaxBytesPerSec;
         DWORD  dwPaddingGranularity;
         DWORD  flags;
         DWORD  dwTotalFrames;
         DWORD  initialFrames;
         DWORD  dwStreams;
         DWORD  dwSuggestedBufferSize;
         DWORD  dwWidth;
         DWORD  dwHeight;
         DWORD  dwReserved[4];
         } AVIMAINHEADER; */
        {
            avihChunk.seekToStartOfData();
            d = avihChunk.getOutputStream();

            // compute largest buffer size
            long largestBufferSize = 0;
            long duration = 0;
            for (Track tr : tracks) {
                long trackDuration = 0;
                for (Sample s : tr.samples) {
                    trackDuration += s.duration;
                }
                duration = max(duration, trackDuration);
                for (Sample s : tr.samples) {
                    if (s.length > largestBufferSize) {
                        largestBufferSize = s.length;
                    }
                }
            }



            // FIXME compute dwMicroSecPerFrame properly!
            Track tt = tracks.get(0);

            d.writeInt((int) ((1000000L * tt.scale) / tt.rate)); // dwMicroSecPerFrame
            // Specifies the number of microseconds between frames.
            // This value indicates the overall timing for the file.

            d.writeInt((int)largestBufferSize); // dwMaxBytesPerSec
            // Specifies the approximate maximum data rate of the file.
            // This value indicates the number of bytes per second the system
            // must handle to present an AVI sequence as specified by the other
            // parameters contained in the main header and stream header chunks.

            d.writeInt(0); // dwPaddingGranularity
            // Specifies the alignment for data, in bytes. Pad the data to multiples
            // of this value.

            d.writeInt(0x10|0x100|0x800); // flags 
            // Contains a bitwise combination of zero or more of the following
            // flags:
            //
            // Value   Name         Description
            // 0x10    AVIF_HASINDEX Indicates the AVI file has an index.
            // 0x20    AVIF_MUSTUSEINDEX Indicates that application should use the
            //                      index, rather than the physical ordering of the
            //                      chunks in the file, to determine the order of
            //                      presentation of the data. For example, this flag
            //                      could be used to create a list of frames for
            //                      editing.
            // 0x100   AVIF_ISINTERLEAVED Indicates the AVI file is interleaved.
            // 0x800   AVIF_TRUST_CK_TYPE ???  
            // 0x1000  AVIF_WASCAPTUREFILE Indicates the AVI file is a specially
            //                      allocated file used for capturing real-time
            //                      video. Applications should warn the user before
            //                      writing over a file with this flag set because
            //                      the user probably defragmented this file.
            // 0x20000 AVIF_COPYRIGHTED Indicates the AVI file contains copyrighted
            //                      data and software. When this flag is used,
            //                      software should not permit the data to be
            //                      duplicated.

            /*long dwTotalFrames = 0;
             for (Track t : tracks) {
             dwTotalFrames += t.samples.size();
             }*/
            d.writeInt(tt.samples.size()); // dwTotalFrames
            // Specifies the total number of frames of data in the file.

            d.writeInt(0); // initialFrames
            // Specifies the initial frame for interleaved files. Noninterleaved
            // files should specify zero. If you are creating interleaved files,
            // specify the number of frames in the file prior to the initial frame
            // of the AVI sequence in this member.
            // To give the audio driver enough audio to work with, the audio data in
            // an interleaved file must be skewed from the video data. Typically,
            // the audio data should be moved forward enough frames to allow
            // approximately 0.75 seconds of audio data to be preloaded. The
            // dwInitialRecords member should be set to the number of frames the
            // audio is skewed. Also set the same value for the initialFrames
            // member of the AVISTREAMHEADER structure in the audio stream header

            d.writeInt(tracks.size()); // dwStreams
            // Specifies the number of streams in the file. For example, a file with
            // audio and video has two streams.

            d.writeInt((int) largestBufferSize); // dwSuggestedBufferSize
            // Specifies the suggested buffer size for reading the file. Generally,
            // this size should be large enough to contain the largest chunk in the
            // file. If set to zero, or if it is too small, the playback software
            // will have to reallocate memory during playback, which will reduce
            // performance. For an interleaved file, the buffer size should be large
            // enough to read an entire record, and not just a chunk.
            {
                VideoTrack vt = null;
                int width = 0, height = 0;
                // FIXME - Maybe we should support a global video dimension property
                for (Track tr : tracks) {
                    width = max(width, max(tr.frameLeft, tr.frameRight));
                    height = max(height, max(tr.frameTop, tr.frameBottom));
                }
                d.writeInt(width); // dwWidth
                // Specifies the width of the AVI file in pixels.

                d.writeInt(height); // dwHeight
                // Specifies the height of the AVI file in pixels.
            }
            d.writeInt(0); // dwReserved[0]
            d.writeInt(0); // dwReserved[1]
            d.writeInt(0); // dwReserved[2]
            d.writeInt(0); // dwReserved[3]
            // Reserved. Set this array to zero.
        }

        for (Track tr : tracks) {
            /* Write Data into AVI Stream Header Chunk
             * -------------
             * The AVISTREAMHEADER structure contains information about one stream
             * in an AVI file.
             * see http://msdn.microsoft.com/en-us/library/ms779638(VS.85).aspx
             typedef struct _avistreamheader {
             FOURCC fcc;
             DWORD  cb;
             FOURCC fccType;
             FOURCC fccHandler;
             DWORD  flags;
             WORD   priority;
             WORD   language;
             DWORD  initialFrames;
             DWORD  scale;
             DWORD  rate;
             DWORD  startTime;
             DWORD  dwLength;
             DWORD  dwSuggestedBufferSize;
             DWORD  quality;
             DWORD  dwSampleSize;
             struct {
             short int left;
             short int top;
             short int right;
             short int bottom;
             }  rcFrame;
             } AVISTREAMHEADER;
             */
            tr.strhChunk.seekToStartOfData();
            d = tr.strhChunk.getOutputStream();
            d.setByteOrder(ByteOrder.BIG_ENDIAN);
            d.writeInt(typeToInt(tr.mediaType.fccType)); // fccType: "vids" for video stream
            d.writeInt(tr.fccHandler); // fccHandler: specifies the codec
            d.setByteOrder(ByteOrder.LITTLE_ENDIAN);

            d.writeInt(tr.flags);
            // Contains any flags for the data stream. The bits in the high-order
            // word of these flags are specific to the type of data contained in the
            // stream. The following standard flags are defined:
            //
            // Value    Name        Description
            //          AVISF_DISABLED 0x00000001 Indicates this stream should not
            //                      be enabled by default.
            //          AVISF_VIDEO_PALCHANGES 0x00010000
            //                      Indicates this video stream contains
            //                      palette changes. This flag warns the playback
            //                      software that it will need to animate the
            //                      palette.

            d.writeShort(tr.priority); // priority: highest priority denotes default stream
            d.writeShort(tr.language); // language: language code (?)
            d.writeInt((int) tr.initialFrames); // initialFrames: how far audio data is ahead of the video frames
            d.writeInt((int) tr.scale); // scale: time scale
            d.writeInt((int) tr.rate); // rate: sample rate in scale units
            d.writeInt((int) tr.startTime); // startTime: starting time of stream
            d.writeInt((int) tr.length); // dwLength: length of stream ! WRONG

            long dwSuggestedBufferSize = 0;
            long dwSampleSize = -1; // => -1 indicates unknown
            for (Sample s : tr.samples) {
                if (s.length > dwSuggestedBufferSize) {
                    dwSuggestedBufferSize = s.length;
                }
                if (dwSampleSize == -1) {
                    dwSampleSize = s.length;
                } else if (dwSampleSize != s.length) {
                    dwSampleSize = 0;
                }
            }
            if (dwSampleSize == -1) {
                dwSampleSize = 0;
            }

            d.writeInt((int) dwSuggestedBufferSize); // dwSuggestedBufferSize
            // Specifies how large a buffer should be used to read this stream.
            // Typically, this contains a value corresponding to the largest chunk
            // present in the stream. Using the correct buffer size makes playback
            // more efficient. Use zero if you do not know the correct buffer size.

            d.writeInt(tr.quality); // quality
            // Specifies an indicator of the quality of the data in the stream.
            // Quality is represented as a number between 0 and 10,000.
            // For compressed data, this typically represents the value of the
            // quality parameter passed to the compression software. If set to –1,
            // drivers use the default quality value.

            d.writeInt(tr instanceof AudioTrack ? ((AudioTrack) tr).blockAlign : (int) dwSampleSize); // dwSampleSize
            // Specifies the size of a single sample of data. This is set to zero
            // if the samples can vary in size. If this number is nonzero, then
            // multiple samples of data can be grouped into a single chunk within
            // the file. If it is zero, each sample of data (such as a video frame)
            // must be in a separate chunk. For video streams, this number is
            // typically zero, although it can be nonzero if all video frames are
            // the same size. For audio streams, this number should be the same as
            // the blockAlign member of the WAVEFORMATEX structure describing the
            // audio.

            d.writeShort(tr.frameLeft); // rcFrame.left
            d.writeShort(tr.frameTop); // rcFrame.top
            d.writeShort(tr.frameRight); // rcFrame.right
            d.writeShort(tr.frameBottom); // rcFrame.bottom
            // Specifies the destination rectangle for a text or video stream within
            // the movie rectangle specified by the dwWidth and dwHeight members of
            // the AVI main header structure. The rcFrame member is typically used
            // in support of multiple video streams. Set this rectangle to the
            // coordinates corresponding to the movie rectangle to update the whole
            // movie rectangle. Units for this member are pixels. The upper-left
            // corner of the destination rectangle is relative to the upper-left
            // corner of the movie rectangle.

            if (tr instanceof VideoTrack) {
                VideoTrack vt = (VideoTrack) tr;
                Format vf = tr.format;

                /* Write BITMAPINFOHEADR Data into AVI Stream Format Chunk
                 /* -------------
                 * see http://msdn.microsoft.com/en-us/library/ms779712(VS.85).aspx
                 typedef struct tagBITMAPINFOHEADER {
                 DWORD  biSize;
                 LONG   width;
                 LONG   height;
                 WORD   planes;
                 WORD   bitCount;
                 DWORD  compression;
                 DWORD  sizeImage;
                 LONG   xPelsPerMeter;
                 LONG   yPelsPerMeter;
                 DWORD  clrUsed;
                 DWORD  clrImportant;
                 } BITMAPINFOHEADER;
                 */
                tr.strfChunk.seekToStartOfData();
                d = tr.strfChunk.getOutputStream();
                d.writeInt(40); // biSize: number of bytes required by the structure.
                d.writeInt(vf.get(WidthKey)); // width
                d.writeInt(vf.get(HeightKey)); // height
                d.writeShort(1); // planes
                d.writeShort(vf.get(DepthKey)); // bitCount

                String enc = vf.get(EncodingKey);
                if (enc.equals(ENCODING_AVI_DIB)) {
                    d.writeInt(0); // compression - BI_RGB for uncompressed RGB
                } else if (enc.equals(ENCODING_AVI_RLE)) {
                    if (vf.get(DepthKey) == 8) {
                        d.writeInt(1); // compression - BI_RLE8
                    } else if (vf.get(DepthKey) == 4) {
                        d.writeInt(2); // compression - BI_RLE4
                    } else {
                        throw new UnsupportedOperationException("RLE only supports 4-bit and 8-bit images");
                    }
                } else {
            d.setByteOrder(ByteOrder.BIG_ENDIAN);
                    d.writeInt(typeToInt(vt.format.get(EncodingKey))); // compression
            d.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                }

                if (enc.equals(ENCODING_AVI_DIB)) {
                    d.writeInt(0); // sizeImage
                } else {
                    if (vf.get(DepthKey) == 4) {
                        d.writeInt(vf.get(WidthKey) * vf.get(HeightKey) / 2); // sizeImage
                    } else {
                        int bytesPerPixel = Math.max(1, vf.get(DepthKey) / 8);
                        d.writeInt(vf.get(WidthKey) * vf.get(HeightKey) * bytesPerPixel); // sizeImage
                    }
                }

                d.writeInt(0); // xPelsPerMeter
                d.writeInt(0); // yPelsPerMeter

                d.writeInt(vt.palette == null ? 0 : vt.palette.getMapSize()); // clrUsed

                d.writeInt(0); // clrImportant

                if (vt.palette != null) {
                    for (int i = 0, n = vt.palette.getMapSize(); i < n; ++i) {
                        /*
                         * typedef struct tagRGBQUAD {
                         BYTE rgbBlue;
                         BYTE rgbGreen;
                         BYTE rgbRed;
                         BYTE rgbReserved; // This member is reserved and must be zero.
                         } RGBQUAD;
                         */
                        d.write(vt.palette.getBlue(i));
                        d.write(vt.palette.getGreen(i));
                        d.write(vt.palette.getRed(i));
                        d.write(0);
                    }
                }
            } else if (tr instanceof AudioTrack) {
                AudioTrack at = (AudioTrack) tr;

                /* Write WAVEFORMATEX Data into AVI Stream Format Chunk
                 /* -------------
                 * see http://msdn.microsoft.com/en-us/library/dd757720(v=vs.85).aspx
                 typedef struct {
                 WORD  wFormatTag;
                 WORD  channels;
                 DWORD samplesPerSec;
                 DWORD avgBytesPerSec;
                 WORD  blockAlign;
                 WORD  bitsPerSample;
                 WORD  cbSize;
                 } WAVEFORMATEX;
                 */
                tr.strfChunk.seekToStartOfData();
                d = tr.strfChunk.getOutputStream();

                d.writeShort(at.wFormatTag); // wFormatTag: WAVE_FORMAT_PCM=0x0001
                d.writeShort(at.channels); // channels
                d.writeInt((int) at.samplesPerSec);// samplesPerSec
                d.writeInt((int) at.avgBytesPerSec); // avgBytesPerSec
                d.writeShort(at.blockAlign); //  blockAlign
                d.writeShort(at.bitsPerSample); // bitsPerSample

                d.writeShort(0); //cbSize
                // cbSize: Size, in bytes, of extra format information appended
                // to the end of the WAVEFORMATEX structure. This information 
                // can be used by non-PCM formats to store extra attributes for
                // the wFormatTag. If no extra information is required by the 
                // wFormatTag, this member must be set to zero. If this value is
                // 22, the format is most likely described using the 
                // WAVEFORMATEXTENSIBLE structure, of which WAVEFORMATEX is the
                // first member.
            }
        }

        // -----------------
        aviChunk.finish();
    }
}
