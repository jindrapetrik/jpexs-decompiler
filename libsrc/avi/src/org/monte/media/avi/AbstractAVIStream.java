/*
 * @(#)AbstractAVIStream.java  
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.avi;

import org.monte.media.riff.RIFFChunk;
import java.util.Map;
import org.monte.media.Buffer;
import org.monte.media.Codec;
import org.monte.media.Format;
import org.monte.media.io.SubImageOutputStream;
import java.awt.Dimension;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javax.imageio.stream.ImageOutputStream;
import static org.monte.media.VideoFormatKeys.*;

/**
 * This is the base class for low-level AVI stream IO.
 *
 * @author Werner Randelshofer
 * @version $Id: AbstractAVIStream.java 299 2013-01-03 07:40:18Z werner $
 */
public abstract class AbstractAVIStream {

    /**
     * Chunk IDs.
     */
    /*
    protected final static int RIFF_ID = 0x46464952;//0x52494646;// "RIFF"
    protected final static int AVI_ID = 0x20495641; //0x41564920;// "AVI "
    protected final static int LIST_ID = 0x5453494c;//0x4c495354;// "LIST"
    protected final static int MOVI_ID = 0x69766f6d;//0x6d6f7669;// "movi"
    protected final static int HDRL_ID = 0x6c726468;//0x6864726c;// "hdrl"
    protected final static int AVIH_ID = 0x68697661;//0x61766968;// "avih"
    protected final static int STRL_ID = 0x6c727473;//0x7374726c;// "strl"
    protected final static int STRH_ID = 0x68727473;//0x73747268;// "strh"
    protected final static int STRN_ID = 0x6e727473;//0x7374726e;// "strn"
    protected final static int STRF_ID = 0x66727473;//0x73747266;// "strf"
    protected final static int STRD_ID = 0x64727473;//0x73747264;// "strd"
    protected final static int IDX1_ID = 0x31786469;//0x69647831;// "idx1"
    protected final static int REC_ID = 0x20636572;//0x72656320;// "rec "
    protected final static int PC_ID = 0x63700000;//0x00007063;// "??pc"
    protected final static int DB_ID = 0x62640000;//0x00006462;// "??db"
    protected final static int DC_ID = 0x63640000;//0x00006463;// "??dc"
    protected final static int WB_ID = 0x62770000;//0x00007762;// "??wb"
    */
    protected final static int RIFF_ID =0x52494646;// "RIFF"
    protected final static int AVI_ID = 0x41564920;// "AVI "
    protected final static int AVIX_ID = 0x41564958;// "AVIX"
    protected final static int LIST_ID = 0x4c495354;// "LIST"
    protected final static int MOVI_ID = 0x6d6f7669;// "movi"
    protected final static int HDRL_ID = 0x6864726c;// "hdrl"
    protected final static int AVIH_ID = 0x61766968;// "avih"
    protected final static int STRL_ID = 0x7374726c;// "strl"
    protected final static int STRH_ID = 0x73747268;// "strh"
    protected final static int STRN_ID = 0x7374726e;// "strn"
    protected final static int STRF_ID = 0x73747266;// "strf"
    protected final static int STRD_ID = 0x73747264;// "strd"
    protected final static int IDX1_ID = 0x69647831;// "idx1"
    protected final static int REC_ID = 0x72656320;// "rec "
    protected final static int CHUNK_SUBTYPE_MASK = 0xffff;// "??xx"
    protected final static int PC_ID = 0x00007063;// "??pc"
    protected final static int DB_ID = 0x00006462;// "??db"
    protected final static int DC_ID = 0x00006463;// "??dc"
    protected final static int WB_ID = 0x00007762;// "??wb"
    
    /**
     * Indicates the AVI file has an index.
     */
    public final static int AVIH_FLAG_HAS_INDEX = 0x00000010;
    /**
     * Indicates that application should use the index, rather than the physical
     * ordering of the chunks in the file, to determine the order of
     * presentation of the data. For example, this flag could be used to create
     * a list of frames for editing.
     */
    public final static int AVIH_FLAG_MUST_USE_INDEX = 0x00000020;
    /**
     * Indicates the AVI file is interleaved.
     */
    public final static int AVIH_FLAG_IS_INTERLEAVED = 0x00000100;
    /**
     * ??
     */
    public final static int AVIH_FLAG_TRUST_CK_TYPE = 0x00000800;
    /**
     * // Indicates the AVI file is a specially allocated file used for
     * capturing real-time video. Applications should warn the user before
     * writing over a file with this flag set because the user probably
     * defragmented this file.
     */
    public final static int AVIH_FLAG_WAS_CAPTURE_FILE = 0x00010000;
    /* Indicates the AVI file contains copyrighted data and 
     * software. When this flag is used, software should not
     * permit the data to be duplicated. */
    public final static int AVIH_FLAG_COPYRIGHTED = 0x00020000;
    /**
     * Indicates this stream should not be enabled by default.
     */
    public final static int STRH_FLAG_DISABLED = 0x00000001;
    /**
     * Indicates this video stream contains palette changes. This flag warns the
     * playback software that it will need to animate the palette.
     */
    public final static int STRH_FLAG_VIDEO_PALETTE_CHANGES = 0x00010000;
    /**
     * Underlying output stream.
     */
    protected ImageOutputStream out;
    /**
     * The offset in the underlying ImageOutputStream. Normally this is 0 unless
     * the underlying stream already contained data when it was passed to the
     * constructor.
     */
    protected long streamOffset;

    /**
     * Supported media types.
     */
    public static enum AVIMediaType {

        AUDIO("auds"),//
        MIDI("mids"),//
        TEXT("txts"),//
        VIDEO("vids")//
        ;
        protected final String fccType;

        @Override
        public String toString() {
            return fccType;
        }

        AVIMediaType(String fourCC) {
            this.fccType = fourCC;
        }
    }
    /**
     * The list of tracks in the file.
     */
    protected ArrayList<Track> tracks = new ArrayList<Track>();

    /**
     * Gets the position relative to the beginning of the QuickTime stream. <p>
     * Usually this value is equal to the stream position of the underlying
     * ImageOutputStream, but can be larger if the underlying stream already
     * contained data.
     *
     * @return The relative stream position.
     * @throws IOException
     */
    protected long getRelativeStreamPosition() throws IOException {
        return out.getStreamPosition() - streamOffset;
    }

    /**
     * Seeks relative to the beginning of the AVI stream. <p> Usually this equal
     * to seeking in the underlying ImageOutputStream, but can be different if
     * the underlying stream already contained data.
     *
     */
    protected void seekRelative(long newPosition) throws IOException {
        out.seek(newPosition + streamOffset);
    }

    /**
     * AVI stores media data in sample chunks. A sample chunk may contain one or
     * more media samples. A media sample is a single element in a sequence of
     * time-ordered data.
     */
    protected static class Sample {

        int chunkType;
        /**
         * Offset of the sample chunk relative to the startTime of the AVI file.
         */
        long offset;
        /**
         * Data length of the sample chunk.
         */
        long length;
        /**
         * The number of media samples in the sample chunk.
         */
        int duration;
        /**
         * Whether the sample is a sync-sample.
         */
        boolean isKeyframe;
        long timeStamp;
        /**
         * Palette change sample.
         */
        Sample header;

        /**
         * Creates a new sample.
         *
         * @param duration The number of media samples contained in the sample
         * chunk.
         * @param offset The offset in the AVI stream.
         * @param length The length in the AVI stream.
         */
        public Sample(int chunkId, int duration, long offset, long length, boolean isSync) {
            this.chunkType = chunkId;
            this.duration = duration;
            this.offset = offset;
            this.length = length;
            this.isKeyframe = isSync;
        }
    }

    /**
     * Represents a track (or "stream") in an AVI file. <p> A track is defined
     * by an "strh" chunk, which contains an {@code AVISTREAMHEADER} struct.
     * Additional chunks can be provided depending on the media type of the
     * track. <p> See <a
     * href="http://msdn.microsoft.com/en-us/library/ms779638(VS.85).aspx">
     * http://msdn.microsoft.com/en-us/library/ms779638(VS.85).aspx</a> </p>
     * <pre>
     * -----------------
     * AVI Stream Header
     * -----------------
     *
     * enum {
     *    audioStream = "auds",
     *   midiStream = "mids",
     *   textStream = "txts",
     *   videoStream = "vids"
     * } aviStrhType;
     *
     * set {
     *    disabled = 0x00000001, // Indicates this stream should not be enabled by default.
     *    videoPaletteChanges = 0x00010000
     *        // Indicates this video stream contains palette changes. This flag
     *        // warns the playback software that it will need to animate the palette.
     *} aviStrhFlags;
     *
     *typedef struct {
     *    Int16 left;
     *    Int16 top;
     *    Int16 right;
     *    Int16 bottom;
     *} aviRectangle;
     *
     *typedef struct {
     *     FOURCC enum aviStrhType type;
     *        // Contains a FOURCC that specifies the type of the data contained in
     *        // the stream. The following standard AVI values for video and audio are
     *        // defined.
     *     FOURCC handler;
     *        // Optionally, contains a FOURCC that identifies a specific data
     *        // handler.
     *        // The data handler is the preferred handler for the stream. For audio
     *        // and video streams, this specifies the codec for decoding the stream.
     *     DWORD  set aviStrhFlags flags;
     *        // Contains any flags for the data stream. The bits in the high-order
     *        // word of these flags are specific to the type of data contained in the
     *        // stream.
     *     WORD   priority;
     *        // Specifies priority of a stream type. For example, in a file with
     *        // multiple audio streams, the one with the highest priority might be
     *        // the default stream.
     *     WORD   language;
     *     DWORD  initialFrames;
     *        // Specifies how far audio data is skewed ahead of the video frames in
     *        // interleaved files. Typically, this is about 0.75 seconds. If you are
     *        // creating interleaved files, specify the number of frames in the file
     *        // prior to the initial frame of the AVI sequence in this member. For
     *        // more information about the contents of this member, see "Special
     *        // Information for Interleaved Files" in the Video for Windows
     *        // Programmer's Guide.
     *     DWORD  scale;
     *        // Used with "rate" to specify the time scale that this stream will use.
     *        // Dividing "rate" by "scale" gives the number of samples per second.
     *        // For video streams, this is the frame rate. For audio streams, this
     *        // rate corresponds to the time needed to play blockAlign bytes of
     *        // audio, which for PCM audio is the just the sample rate.
     *     DWORD  rate;
     *        // See "scale".
     *     DWORD  startTime;
     *        // Specifies the starting time for this stream. The units are defined by
     *        // the "rate" and "scale" members in the main file header. Usually, this
     *        // is zero, but it can specify a delay time for a stream that does not
     *        // startTime concurrently with the file.
     *     DWORD  length;
     *        // Specifies the length of this stream. The units are defined by the
     *        // "rate" and "scale" members of the stream's header.
     *     DWORD  suggestedBufferSize;
     *        // Specifies how large a buffer should be used to read this stream.
     *        // Typically, this contains a value corresponding to the largest chunk
     *        // present in the stream. Using the correct buffer size makes playback
     *        // more efficient. Use zero if you do not know the correct buffer size.
     *     DWORD  quality;
     *        // Specifies an indicator of the quality of the data in the stream.
     *        // Quality is represented as a number between 0 and 10,000. For
     *        // compressed data, this typically represents the value of the quality
     *        // parameter passed to the compression software. If set to �1, drivers
     *        // use the default quality value.
     *     DWORD  sampleSize;
     *        // Specifies the size of a single sample of data. This is set to zero if
     *        // the samples can vary in size. If this number is nonzero, then
     *        // multiple samples of data can be grouped into a single chunk within
     *        // the file. If it is zero, each sample of data (such as a video frame)
     *        // must be in a separate chunk. For video streams, this number is
     *        // typically zero, although it can be nonzero if all video frames are
     *        // the same size. For audio streams, this number should be the same as
     *        // the blockAlign member of the WAVEFORMATEX structure describing the audio.
     *    aviRectangle frame;
     *        // Specifies the destination rectangle for a text or video stream within
     *        // the movie rectangle specified by the "frameWidth" and "frameHeight"
     *        // members of the AVI main header structure. The "frame" member is
     *        // typically used in support of multiple video streams. Set this
     *        // rectangle to the coordinates corresponding to the movie rectangle to
     *        // update the whole movie rectangle. Units for this member are pixels.
     *        // The upper-left corner of the destination rectangle is relative to the
     *        // upper-left corner of the movie rectangle.
     * } AVISTREAMHEADER; * </pre>
     */
    protected abstract class Track {

        /**
         * The media format.
         *
         * FIXME - AbstractAVIStream should have no dependencies to Format.
         */
        protected Format format;
        // Common metadata
        /**
         * The scale of the media in the track. <p> Used with rate to specify
         * the time scale that this stream will use. Dividing rate by scale
         * gives the number of samples per second. For video streams, this is
         * the frame rate. For audio streams, this rate corresponds to the time
         * needed to play blockAlign bytes of audio, which for PCM audio is just
         * the sample rate.
         */
        /**
         * The rate of the media in scale units. <p>
         *
         * @see scale
         */
        /**
         * List of samples.
         */
        protected ArrayList<Sample> samples;
        /**
         * Interval between sync samples (keyframes). 0 = automatic. 1 = write
         * all samples as sync samples. n = sync every n-th sample.
         */
        protected int syncInterval = 30;
        /**
         * The twoCC code is used for the ids of the chunks which hold the data
         * samples.
         */
        protected int twoCC;
        //
        // AVISTREAMHEADER structure
        // -------------------------
        /**
         * {@code mediaType.fccType} contains a FOURCC that specifies the type
         * of the data contained in the stream. The following standard AVI
         * values for video and audio are defined.
         *
         * FOURCC	Description 'auds'	Audio stream 'mids'	MIDI stream 'txts'	Text
         * stream 'vids'	Video stream
         *
         */
        protected final AVIMediaType mediaType;
        //protected String fccType;
        /**
         * Optionally, contains a FOURCC that identifies a specific data
         * handler. The data handler is the preferred handler for the stream.
         * For audio and video streams, this specifies the codec for decoding
         * the stream.
         */
        protected int fccHandler;
        /**
         * Contains any flags for the data stream. The bits in the high-order
         * word of these flags are specific to the type of data contained in the
         * stream. The following standard flags are defined.
         *
         * Value	Description
         *
         * AVISF_DISABLED	0x00000001 Indicates this stream should not be enabled
         * by default.
         *
         * AVISF_VIDEO_PALCHANGES 0x00010000 Indicates this video stream
         * contains palette changes. This flag warns the playback software that
         * it will need to animate the palette.
         */
        protected int flags;
        /**
         * Specifies priority of a stream type. For example, in a file with
         * multiple audio streams, the one with the highest priority might be
         * the default stream.
         */
        protected int priority = 0;
        /**
         * Language tag.
         */
        protected int language = 0;
        /**
         * Specifies how far audio data is skewed ahead of the video frames in
         * interleaved files. Typically, this is about 0.75 seconds. If you are
         * creating interleaved files, specify the number of frames in the file
         * prior to the initial frame of the AVI sequence in this member. For
         * more information, see the remarks for the initialFrames member of the
         * AVIMAINHEADER structure.
         */
        protected long initialFrames = 0;
        /**
         * Used with rate to specify the time scale that this stream will use.
         * Dividing rate by scale gives the number of samples per second. For
         * video streams, this is the frame rate. For audio streams, this rate
         * corresponds to the time needed to play blockAlign bytes of audio,
         * which for PCM audio is the just the sample rate.
         */
        protected long scale = 1;
        /**
         * The rate of the media in scale units.
         */
        protected long rate = 30;
        /**
         * Specifies the starting time for this stream. The units are defined by
         * the rate and scale members in the main file header. Usually, this is
         * zero, but it can specify a delay time for a stream that does not
         * startTime concurrently with the file.
         */
        protected long startTime = 0;
        /**
         * Specifies the length of this stream. The units are defined by the
         * rate and scale members of the stream's header.
         */
        protected long length;
        /**
         * Specifies how large a buffer should be used to read this stream.
         * Typically, this contains a value corresponding to the largest chunk
         * present in the stream. Using the correct buffer size makes playback
         * more efficient. Use zero if you do not know the correct buffer size.
         */
        //protected long dwSuggestedBufferSize; => this field is computed from tr.samples
        /**
         * Specifies an indicator of the quality of the data in the stream.
         * Quality is represented as a number between 0 and 10,000. For
         * compressed data, this typically represents the value of the quality
         * parameter passed to the compression software. If set to –1, drivers
         * use the default quality value.
         */
        protected int quality = -1;
        /**
         * Specifies the size of a single sample of data. This is set to zero if
         * the samples can vary in size. If this number is nonzero, then
         * multiple samples of data can be grouped into a single chunk within
         * the file. If it is zero, each sample of data (such as a video frame)
         * must be in a separate chunk. For video streams, this number is
         * typically zero, although it can be nonzero if all video frames are
         * the same size. For audio streams, this number should be the same as
         * the blockAlign member of the WAVEFORMATEX structure describing the
         * audio.
         */
        //protected long dwSampleSize; => computed from tr.samples
        /**
         * Specifies the destination rectangle for a text or video stream within
         * the movie rectangle specified by the dwWidth and dwHeight members of
         * the AVI main header structure. The rcFrame member is typically used
         * in support of multiple video streams. Set this rectangle to the
         * coordinates corresponding to the movie rectangle to update the whole
         * movie rectangle. Units for this member are pixels. The upper-left
         * corner of the destination rectangle is relative to the upper-left
         * corner of the movie rectangle.
         */
        int frameLeft;
        int frameTop;
        int frameRight;
        int frameBottom;
        // --------------------------------
        // End of AVISTREAMHEADER structure
        /**
         * This chunk holds the AVI Stream Header.
         */
        protected FixedSizeDataChunk strhChunk;
        /**
         * This chunk holds the AVI Stream Format Header.
         */
        protected FixedSizeDataChunk strfChunk;
        /**
         * The optional name of the track.
         */
        protected String name;
        /**
         * The codec.
         */
        protected Codec codec;
        /**
         * The output buffer is used to store the output of the codec.
         */
        protected Buffer outputBuffer;
        /**
         * The input buffer is used when one of the convenience methods without
         * a Buffer parameter is used.
         */
        protected Buffer inputBuffer;
        /**
         * The current chunk index of the reader.
         */
        protected long readIndex = 0;
        /**
         * List of additional header chunks.
         */
        protected ArrayList<RIFFChunk> extraHeaders;

        public Track(int trackIndex, AVIMediaType mediaType, int fourCC) {
            this.mediaType = mediaType;
            twoCC = (('0'+trackIndex/10)<<24) | (('0'+trackIndex%10)<<16);
            
            this.fccHandler = fourCC;
            this.samples = new ArrayList<Sample>();
            this.extraHeaders = new ArrayList<RIFFChunk>();
        }

        public abstract long getSTRFChunkSize();

        public abstract int getSampleChunkFourCC(boolean isSync);

        public void addSample(Sample s) {
            if (!samples.isEmpty()) {
                s.timeStamp = samples.get(samples.size() - 1).timeStamp + samples.get(samples.size() - 1).duration;
            }
            samples.add(s);
            length++;
        }
    }

    /**
     * Represents a video track in an AVI file. <p> The format of a video track
     * is defined in a "strf" chunk, which contains a {@code BITMAPINFOHEADER}
     * struct.
     *
     * </pre> //---------------------- // AVI Bitmap Info Header //
     * ---------------------- typedef struct { BYTE blue; BYTE green; BYTE red;
     * BYTE reserved; } RGBQUAD;
     *
     * // Values for this enum taken from: //
     * http://www.fourcc.org/index.php?http%3A//www.fourcc.org/rgb.php enum {
     * BI_RGB = 0x00000000, RGB = 0x32424752, // Alias for BI_RGB BI_RLE8 =
     * 0x01000000, RLE8 = 0x38454C52, // Alias for BI_RLE8 BI_RLE4 = 0x00000002,
     * RLE4 = 0x34454C52, // Alias for BI_RLE4 BI_BITFIELDS = 0x00000003, raw =
     * 0x32776173, RGBA = 0x41424752, RGBT = 0x54424752, cvid = "cvid" }
     * bitmapCompression;
     *
     * typedef struct { DWORD structSize; // Specifies the number of bytes
     * required by the structure. LONG width; // Specifies the width of the
     * bitmap. // - For RGB formats, the width is specified in pixels. // - The
     * same is true for YUV formats if the bitdepth is an even power // of 2. //
     * - For YUV formats where the bitdepth is not an even power of 2, //
     * however, the width is specified in bytes. // Decoders and video sources
     * should propose formats where "width" is // the width of the image. If the
     * video renderer is using DirectDraw, it // modifies the format so that
     * "width" equals the stride of the surface, // and the "target" member of
     * the VIDEOINFOHEADER or VIDEOINFOHEADER2 // structure specifies the image
     * width. Then it proposes the modified // format using IPin::QueryAccept.
     * // For RGB and even-power-of-2 YUV formats, if the video renderer does //
     * not specify the stride, then round the width up to the nearst DWORD //
     * boundary to find the stride. LONG height; // Specifies the height of the
     * bitmap, in pixels. // - For uncompressed RGB bitmaps, if "height" is
     * positive, the bitmap // is a bottom-up DIB with the origin at the lower
     * left corner. If // "height" is negative, the bitmap is a top-down DIB
     * with the origin // at the upper left corner. // - For YUV bitmaps, the
     * bitmap is always top-down, regardless of the // sign of "height".
     * Decoders should offer YUV formats with postive // "height", but for
     * backward compatibility they should accept YUV // formats with either
     * positive or negative "height". // - For compressed formats, height must
     * be positive, regardless of // image orientation. WORD planes; //
     * Specifies the number of planes for the target device. This value must //
     * be set to 1. WORD bitCount; // Specifies the number of bits per pixel.
     * //DWORD enum bitmapCompression compression; FOURCC enum bitmapCompression
     * compression; // If the bitmap is compressed, this member is a FOURCC the
     * specifies // the compression. // Value Description // BI_RLE8 A
     * run-length encoded (RLE) format for bitmaps with 8 // bpp. The
     * compression format is a 2-byte format // consisting of a count byte
     * followed by a byte containing a color index. For more information, see
     * Bitmap Compression. // BI_RLE4 An RLE format for bitmaps with 4 bpp. The
     * compression // format is a 2-byte format consisting of a count byte //
     * followed by two word-length color indexes. For more // information, see
     * Bitmap Compression. // BI_JPEG Windows 98/Me, Windows 2000/XP: Indicates
     * that the // image is a JPEG image. // BI_PNG Windows 98/Me, Windows
     * 2000/XP: Indicates that the // image is a PNG image. // For uncompressed
     * formats, the following values are possible: // Value Description //
     * BI_RGB Uncompressed RGB. // BI_BITFIELDS Specifies that the bitmap is not
     * compressed and that // the color table consists of three DWORD color
     * masks // that specify the red, green, and blue components, //
     * respectively, of each pixel. This is valid when used // with 16- and
     * 32-bpp bitmaps. DWORD imageSizeInBytes; // Specifies the size, in bytes,
     * of the image. This can be set to 0 for // uncompressed RGB bitmaps. LONG
     * xPelsPerMeter; // Specifies the horizontal resolution, in pixels per
     * meter, of the // target device for the bitmap. LONG yPelsPerMeter; //
     * Specifies the vertical resolution, in pixels per meter, of the target //
     * device for the bitmap. DWORD numberOfColorsUsed; // Specifies the number
     * of color indices in the color table that are // actually used by the
     * bitmap DWORD numberOfColorsImportant; // Specifies the number of color
     * indices that are considered important // for displaying the bitmap. If
     * this value is zero, all colors are // important. RGBQUAD colors[]; // If
     * the bitmap is 8-bpp or less, the bitmap uses a color table, which //
     * immediately follows the BITMAPINFOHEADER. The color table consists of //
     * an array of RGBQUAD values. The size of the array is given by the //
     * "clrUsed" member. If "clrUsed" is zero, the array contains the // maximum
     * number of colors for the given bitdepth; that is, // 2^"bitCount" colors.
     * } BITMAPINFOHEADER;
     * </pre>
     */
    protected class VideoTrack extends Track {
        // Video metadata

        /**
         * The video compression quality.
         */
        protected float videoQuality = 0.97f;
        /**
         * Index color model for RAW_RGB4 and RAW_RGB8 formats.
         */
        protected IndexColorModel palette;
        protected IndexColorModel previousPalette;
        /**
         * Previous frame for delta compression.
         */
        protected Object previousData;
        //protected Rectangle rcFrame;
        // BITMAPINFOHEADER structure
        /**
         * Specifies the number of bytes required by the structure. This value
         * does not include the size of the color table or the size of the color
         * masks, if they are appended to the end of structure.
         */
        //protected long biSize; => computed when writing chukn
        /**
         * Specifies the width of the bitmap, in pixels. For information about
         * calculating the stride of the bitmap.
         *
         */
        int width;
        /**
         * Specifies the height of the bitmap, in pixels.
         *
         * For uncompressed RGB bitmaps, if height is positive, the bitmap is a
         * bottom-up DIB with the origin at the lower left corner. If height is
         * negative, the bitmap is a top-down DIB with the origin at the upper
         * left corner. For YUV bitmaps, the bitmap is always top-down,
         * regardless of the sign of height. Decoders should offer YUV formats
         * with positive height, but for backward compatibility they should
         * accept YUV formats with either positive or negative height. For
         * compressed formats, height must be positive, regardless of image
         * orientation.
         */
        int height;
        /**
         * Specifies the number of planes for the target device. This value must
         * be set to 1.
         */
        int planes;
        /**
         * Specifies the number of bits per pixel (bpp). For uncompressed
         * formats, this value is the average number of bits per pixel. For
         * compressed formats, this value is the implied bit depth of the
         * uncompressed image, after the image has been decoded.
         */
        int bitCount;
        /**
         * For compressed video and YUV formats, this member is a FOURCC code,
         * specified as a DWORD in little-endian order. For example, YUYV video
         * has the FOURCC 'VYUY' or 0x56595559.
         *
         * For uncompressed RGB formats, the following values are possible:
         *
         * Value	Description
         *
         * BI_RGB	Uncompressed RGB.
         *
         * BI_BITFIELDS	Uncompressed RGB with color masks. Valid for 16-bpp and
         * 32-bpp bitmaps.
         *
         * Note that BI_JPG and BI_PNG are not valid video formats.
         *
         * For 16-bpp bitmaps, if compression equals BI_RGB, the format is
         * always RGB 555. If compression equals BI_BITFIELDS, the format is
         * either RGB 555 or RGB 565. Use the subtype GUID in the AM_MEDIA_TYPE
         * structure to determine the specific RGB type.
         */
        String compression;
        /**
         * Specifies the size, in bytes, of the image. This can be set to 0 for
         * uncompressed RGB bitmaps.
         */
        long sizeImage;
        /**
         * Specifies the horizontal resolution, in pixels per meter, of the
         * target device for the bitmap.
         */
        long xPelsPerMeter;
        /**
         * Specifies the vertical resolution, in pixels per meter, of the target
         * device for the bitmap.
         */
        long yPelsPerMeter;
        /**
         * Specifies the number of color indices in the color table that are
         * actually used by the bitmap. See Remarks for more information.
         */
        long clrUsed;
        /**
         * Specifies the number of color indices that are considered important
         * for displaying the bitmap. If this value is zero, all colors are
         * important.
         */
        long clrImportant;
        private int sampleChunkFourCC;

        public VideoTrack(int trackIndex, int fourCC, Format videoFormat) {
            super(trackIndex, AVIMediaType.VIDEO, fourCC);
            this.format = videoFormat;
            sampleChunkFourCC = videoFormat != null && videoFormat.get(EncodingKey).equals(ENCODING_AVI_DIB) ? twoCC | DB_ID : twoCC | DC_ID;
        }

        @Override
        public long getSTRFChunkSize() {
            return palette == null ? 40 : 40 + palette.getMapSize() * 4;

        }

        @Override
        public int getSampleChunkFourCC(boolean isSync) {
            return sampleChunkFourCC;
        }
    }

    /**
     * <p> The format of a video track is defined in a "strf" chunk, which
     * contains a {@code WAVEFORMATEX} struct.
     * <pre>
     * ----------------------
     * AVI Wave Format Header
     * ----------------------
     * // values for this enum taken from mmreg.h
     * enum {
     *         WAVE_FORMAT_PCM = 0x0001,
     *         //  Microsoft Corporation
     *         WAVE_FORMAT_ADPCM = 0x0002,
     *         //  Microsoft Corporation
     *          *   IEEE754: range (+1, -1]
     *          *  32-bit/64-bit format as defined by
     *          *  MSVC++ float/double type
     *
     *         WAVE_FORMAT_IEEE_FLOAT = 0x0003,
     *         //  IBM Corporation
     *         WAVE_FORMAT_IBM_CVSD = 0x0005,
     *         //  Microsoft Corporation
     *         WAVE_FORMAT_ALAW = 0x0006,
     *         //  Microsoft Corporation
     *         WAVE_FORMAT_MULAW = 0x0007,
     *         //  OKI
     *         WAVE_FORMAT_OKI_ADPCM = 0x0010,
     *         //  Intel Corporation
     *         WAVE_FORMAT_DVI_ADPCM = 0x0011,
     *         //  Intel Corporation
     *         WAVE_FORMAT_IMA_ADPCM = 0x0011,
     *         //  Videologic
     *         WAVE_FORMAT_MEDIASPACE_ADPCM = 0x0012,
     *         //  Sierra Semiconductor Corp
     *         WAVE_FORMAT_SIERRA_ADPCM = 0x0013,
     *         //  Antex Electronics Corporation
     *         WAVE_FORMAT_G723_ADPCM = 0x0014,
     *         //  DSP Solutions, Inc.
     *         WAVE_FORMAT_DIGISTD = 0x0015,
     *         //  DSP Solutions, Inc.
     *         WAVE_FORMAT_DIGIFIX = 0x0016,
     *         //  Dialogic Corporation
     *         WAVE_FORMAT_DIALOGIC_OKI_ADPCM = 0x0017,
     *         //  Media Vision, Inc.
     *         WAVE_FORMAT_MEDIAVISION_ADPCM = 0x0018,
     *         //  Yamaha Corporation of America
     *         WAVE_FORMAT_YAMAHA_ADPCM = 0x0020,
     *         //  Speech Compression
     *         WAVE_FORMAT_SONARC = 0x0021,
     *         //  DSP Group, Inc
     *         WAVE_FORMAT_DSPGROUP_TRUESPEECH = 0x0022,
     *         //  Echo Speech Corporation
     *         WAVE_FORMAT_ECHOSC1 = 0x0023,
     *         //
     *         WAVE_FORMAT_AUDIOFILE_AF36 = 0x0024,
     *         //  Audio Processing Technology
     *         WAVE_FORMAT_APTX = 0x0025,
     *         //
     *         WAVE_FORMAT_AUDIOFILE_AF10 = 0x0026,
     *         //  Dolby Laboratories
     *         WAVE_FORMAT_DOLBY_AC2 = 0x0030,
     *         //  Microsoft Corporation
     *         WAVE_FORMAT_GSM610 = 0x0031,
     *         //  Microsoft Corporation
     *         WAVE_FORMAT_MSNAUDIO = 0x0032,
     *         //  Antex Electronics Corporation
     *         WAVE_FORMAT_ANTEX_ADPCME = 0x0033,
     *         //  Control Resources Limited
     *         WAVE_FORMAT_CONTROL_RES_VQLPC = 0x0034,
     *         //  DSP Solutions, Inc.
     *         WAVE_FORMAT_DIGIREAL = 0x0035,
     *         //  DSP Solutions, Inc.
     *         WAVE_FORMAT_DIGIADPCM = 0x0036,
     *         //  Control Resources Limited
     *         WAVE_FORMAT_CONTROL_RES_CR10 = 0x0037,
     *         //  Natural MicroSystems
     *         WAVE_FORMAT_NMS_VBXADPCM = 0x0038,
     *         // Crystal Semiconductor IMA ADPCM
     *         WAVE_FORMAT_CS_IMAADPCM = 0x0039,
     *         // Echo Speech Corporation
     *         WAVE_FORMAT_ECHOSC3 = 0x003A,
     *         // Rockwell International
     *         WAVE_FORMAT_ROCKWELL_ADPCM = 0x003B,
     *         // Rockwell International
     *         WAVE_FORMAT_ROCKWELL_DIGITALK = 0x003C,
     *         // Xebec Multimedia Solutions Limited
     *         WAVE_FORMAT_XEBEC = 0x003D,
     *         //  Antex Electronics Corporation
     *         WAVE_FORMAT_G721_ADPCM = 0x0040,
     *         //  Antex Electronics Corporation
     *         WAVE_FORMAT_G728_CELP = 0x0041,
     *         //  Microsoft Corporation
     *         WAVE_FORMAT_MPEG = 0x0050,
     *         //  ISO/MPEG Layer3 Format Tag
     *         WAVE_FORMAT_MPEGLAYER3 = 0x0055,
     *         //  Cirrus Logic
     *         WAVE_FORMAT_CIRRUS = 0x0060,
     *         //  ESS Technology
     *         WAVE_FORMAT_ESPCM = 0x0061,
     *         //  Voxware Inc
     *         WAVE_FORMAT_VOXWARE = 0x0062,
     *         //  Canopus, co., Ltd.
     *         WAVE_FORMAT_CANOPUS_ATRAC = 0x0063,
     *         //  APICOM
     *         WAVE_FORMAT_G726_ADPCM = 0x0064,
     *         //  APICOM
     *         WAVE_FORMAT_G722_ADPCM = 0x0065,
     *         //  Microsoft Corporation
     *         WAVE_FORMAT_DSAT = 0x0066,
     *         //  Microsoft Corporation
     *         WAVE_FORMAT_DSAT_DISPLAY = 0x0067,
     *         //  Softsound, Ltd.
     *         WAVE_FORMAT_SOFTSOUND = 0x0080,
     *         //  Rhetorex Inc
     *         WAVE_FORMAT_RHETOREX_ADPCM = 0x0100,
     *         //  Creative Labs, Inc
     *         WAVE_FORMAT_CREATIVE_ADPCM = 0x0200,
     *         //  Creative Labs, Inc
     *         WAVE_FORMAT_CREATIVE_FASTSPEECH8 = 0x0202,
     *         //  Creative Labs, Inc
     *         WAVE_FORMAT_CREATIVE_FASTSPEECH10 = 0x0203,
     *         //  Quarterdeck Corporation
     *         WAVE_FORMAT_QUARTERDECK = 0x0220,
     *         //  Fujitsu Corp.
     *         WAVE_FORMAT_FM_TOWNS_SND = 0x0300,
     *         //  Brooktree Corporation
     *         WAVE_FORMAT_BTV_DIGITAL = 0x0400,
     *         //  Ing C. Olivetti & C., S.p.A.
     *         WAVE_FORMAT_OLIGSM = 0x1000,
     *         //  Ing C. Olivetti & C., S.p.A.
     *         WAVE_FORMAT_OLIADPCM = 0x1001,
     *         //  Ing C. Olivetti & C., S.p.A.
     *         WAVE_FORMAT_OLICELP = 0x1002,
     *         //  Ing C. Olivetti & C., S.p.A.
     *         WAVE_FORMAT_OLISBC = 0x1003,
     *         //  Ing C. Olivetti & C., S.p.A.
     *         WAVE_FORMAT_OLIOPR = 0x1004,
     *         //  Lernout & Hauspie
     *         WAVE_FORMAT_LH_CODEC = 0x1100,
     *         //  Norris Communications, Inc.
     *         WAVE_FORMAT_NORRIS = 0x1400,
     *         //
     *          *  the WAVE_FORMAT_DEVELOPMENT format tag can be used during the
     *          *  development phase of a new wave format.  Before shipping, you MUST
     *          *  acquire an official format tag from Microsoft.
     *
     *         WAVE_FORMAT_DEVELOPMENT = 0xFFFF,
     * } wFormatTagEnum;
     *
     * typedef struct {
     *   WORD enum wFormatTagEnum formatTag;
     *     // Waveform-audio format type. Format tags are registered with Microsoft
     *     // Corporation for many compression algorithms. A complete list of format
     *     // tags can be found in the Mmreg.h header file. For one- or two-channel
     *     // Pulse Code Modulation (PCM) data, this value should be WAVE_FORMAT_PCM.
     *   WORD  numberOfChannels;
     *     // Number of channels in the waveform-audio data. Monaural data uses one
     *     // channel and stereo data uses two channels.
     *   DWORD samplesPerSec;
     *     // Sample rate, in samples per second (hertz). If "formatTag" is
     *     // "WAVE_FORMAT_PCM", then common values for "samplesPerSec" are 8.0 kHz,
     *     // 11.025 kHz, 22.05 kHz, and 44.1 kHz. For non-PCM formats, this member
     *     // must be computed according to the manufacturer's specification of the
     *     // format tag.
     *   DWORD avgBytesPerSec;
     *     // Required average data-transfer rate, in bytes per second, for the format
     *     // tag. If "formatTag" is "WAVE_FORMAT_PCM", "avgBytesPerSec" should be
     *     // equal to the product of "samplesPerSec" and "blockAlignment". For non-PCM
     *     // formats, this member must be computed according to the manufacturer's
     *     // specification of the format tag.
     *   WORD  blockAlignment;
     *     // Block alignment, in bytes. The block alignment is the minimum atomic unit
     *     // of data for the "formatTag" format type. If "formatTag" is
     *     // "WAVE_FORMAT_PCM" or "WAVE_FORMAT_EXTENSIBLE, "blockAlignment" must be equal
     *     // to the product of "numberOfChannels" and "bitsPerSample" divided by 8 (bits per
     *     // byte). For non-PCM formats, this member must be computed according to the
     *     // manufacturer's specification of the format tag.
     *     // Software must process a multiple of "blockAlignment" bytes of data at a
     *     // time. Data written to and read from a device must always start at the
     *     // beginning of a block. For example, it is illegal to start playback of PCM
     *     // data in the middle of a sample (that is, on a non-block-aligned boundary).
     *   WORD  bitsPerSample;
     *     // Bits per sample for the waveFormatTag format type. If "formatTag" is
     *     // "WAVE_FORMAT_PCM", then "bitsPerSample" should be equal to 8 or 16. For
     *     // non-PCM formats, this member must be set according to the manufacturer's
     *     // specification of the format tag. If "formatTag" is
     *     // "WAVE_FORMAT_EXTENSIBLE", this value can be any integer multiple of 8.
     *     // Some compression schemes cannot define a value for "bitsPerSample", so
     *     // this member can be zero.
     *   WORD  cbSize;
     *     // Size, in bytes, of extra format information appended to the end of the
     *     // WAVEFORMATEX structure. This information can be used by non-PCM formats
     *     // to store extra attributes for the "wFormatTag". If no extra information
     *     // is required by the "wFormatTag", this member must be set to zero. For
     *     // WAVE_FORMAT_PCM formats (and only WAVE_FORMAT_PCM formats), this member
     *     // is ignored.
     *   byte[cbSize] extra;
     * } WAVEFORMATEX;
     * </pre>
     */
    protected class AudioTrack extends Track {

        // WAVEFORMATEX Structure
        /**
         * Waveform-audio format type. Format tags are registered with Microsoft
         * Corporation for many compression algorithms. A complete list of
         * format tags can be found in the Mmreg.h header file. For one- or
         * two-channel Pulse Code Modulation (PCM) data, this value should be
         * WAVE_FORMAT_PCM=0x0001.
         *
         * If wFormatTag equals WAVE_FORMAT_EXTENSIBLE=0xFFFE, the structure is
         * interpreted as a WAVEFORMATEXTENSIBLE structure. If wFormatTag equals
         * WAVE_FORMAT_MPEG, the structure is interpreted as an MPEG1WAVEFORMAT
         * structure. If wFormatTag equals MPEGLAYER3WAVEFORMAT, the structure
         * is interpreted as an MPEGLAYER3WAVEFORMAT structure. Before
         * reinterpreting a WAVEFORMATEX structure as one of these extended
         * structures, verify that the actual structure size is sufficiently
         * large and that the cbSize member indicates a valid size.
         */
        protected int wFormatTag;
        /**
         * Number of channels in the waveform-audio data. Monaural data uses one
         * channel and stereo data uses two channels.
         */
        protected int channels;
        /**
         * Sample rate, in samples per second (hertz). If wFormatTag is
         * WAVE_FORMAT_PCM, then common values for samplesPerSec are 8.0 kHz,
         * 11.025 kHz, 22.05 kHz, and 44.1 kHz. For non-PCM formats, this member
         * must be computed according to the manufacturer's specification of the
         * format tag.
         */
        protected long samplesPerSec;
        /**
         * Required average data-transfer rate, in bytes per second, for the
         * format tag. If wFormatTag is WAVE_FORMAT_PCM, avgBytesPerSec should
         * be equal to the product of samplesPerSec and blockAlign. For non-PCM
         * formats, this member must be computed according to the manufacturer's
         * specification of the format tag.
         */
        protected long avgBytesPerSec;
        /**
         * Block alignment, in bytes. The block alignment is the minimum atomic
         * unit of data for the wFormatTag format type. If wFormatTag is
         * WAVE_FORMAT_PCM or WAVE_FORMAT_EXTENSIBLE, blockAlign must be equal
         * to the product of channels and bitsPerSample divided by 8 (bits per
         * byte). For non-PCM formats, this member must be computed according to
         * the manufacturer's specification of the format tag.
         *
         * Software must process a multiple of blockAlign bytes of data at a
         * time. Data written to and read from a device must always startTime at
         * the beginning of a block. For example, it is illegal to startTime
         * playback of PCM data in the middle of a sample (that is, on a
         * non-block-aligned boundary).
         */
        protected int blockAlign;
        /**
         * Bits per sample for the wFormatTag format type. If wFormatTag is
         * WAVE_FORMAT_PCM, then bitsPerSample should be equal to 8 or 16. For
         * non-PCM formats, this member must be set according to the
         * manufacturer's specification of the format tag. If wFormatTag is
         * WAVE_FORMAT_EXTENSIBLE, this value can be any integer multiple of 8.
         * Some compression schemes cannot define a value for bitsPerSample, so
         * this member can be zero.
         */
        protected int bitsPerSample;
        /**
         * Size, in bytes, of extra format information appended to the end of
         * the WAVEFORMATEX structure. This information can be used by non-PCM
         * formats to store extra attributes for the wFormatTag. If no extra
         * information is required by the wFormatTag, this member must be set to
         * zero. For WAVE_FORMAT_PCM formats (and only WAVE_FORMAT_PCM formats),
         * this member is ignored.
         */
        //int cbSize; => this value is computed
        // Well known wave format tags
        /**
         * Microsoft Corporation
         */
        protected final static int WAVE_FORMAT_PCM = 0x0001;
        /**
         * Microsoft Corporation
         */
        protected final static int WAVE_FORMAT_ADPCM = 0x0002;
        /**
         * Microsoft Corporation IEEE754: range (+1, -1] 32-bit/64-bit format as
         * defined by MSVC++ float/double type
         */
        protected final static int WAVE_FORMAT_IEEE_FLOAT = 0x0003;
        /**
         * IBM Corporation
         */
        protected final static int WAVE_FORMAT_IBM_CVSD = 0x0005;
        /**
         * Microsoft Corporation
         */
        protected final static int WAVE_FORMAT_ALAW = 0x0006;
        /**
         * Microsoft Corporation
         */
        protected final static int WAVE_FORMAT_MULAW = 0x0007;
        /**
         * OKI
         */
        protected final static int WAVE_FORMAT_OKI_ADPCM = 0x0010;
        /**
         * Intel Corporation
         */
        protected final static int WAVE_FORMAT_DVI_ADPCM = 0x0011;
        /**
         * Intel Corporation
         */
        protected final static int WAVE_FORMAT_IMA_ADPCM = WAVE_FORMAT_DVI_ADPCM;
        /**
         * Videologic
         */
        protected final static int WAVE_FORMAT_MEDIASPACE_ADPCM = 0x0012;
        /**
         * Sierra Semiconductor Corp
         */
        protected final static int WAVE_FORMAT_SIERRA_ADPCM = 0x0013;
        /**
         * Antex Electronics Corporation
         */
        protected final static int WAVE_FORMAT_G723_ADPCM = 0x0014;
        /**
         * DSP Solutions, Inc.
         */
        protected final static int WAVE_FORMAT_DIGISTD = 0x0015;
        /**
         * DSP Solutions, Inc.
         */
        protected final static int WAVE_FORMAT_DIGIFIX = 0x0016;
        /**
         * Dialogic Corporation
         */
        protected final static int WAVE_FORMAT_DIALOGIC_OKI_ADPCM = 0x0017;
        /**
         * Media Vision, Inc.
         */
        protected final static int WAVE_FORMAT_MEDIAVISION_ADPCM = 0x0018;
        /**
         * Yamaha Corporation of America
         */
        protected final static int WAVE_FORMAT_YAMAHA_ADPCM = 0x0020;
        /**
         * Speech Compression
         */
        protected final static int WAVE_FORMAT_SONARC = 0x0021;
        /**
         * DSP Group, Inc
         */
        protected final static int WAVE_FORMAT_DSPGROUP_TRUESPEECH = 0x0022;
        /**
         * Echo Speech Corporation
         */
        protected final static int WAVE_FORMAT_ECHOSC1 = 0x0023;
        /**
         *
         */
        protected final static int WAVE_FORMAT_AUDIOFILE_AF36 = 0x0024;
        /**
         * Audio Processing Technology
         */
        protected final static int WAVE_FORMAT_APTX = 0x0025;
        /**
         *
         */
        protected final static int WAVE_FORMAT_AUDIOFILE_AF10 = 0x0026;
        /**
         * Dolby Laboratories
         */
        protected final static int WAVE_FORMAT_DOLBY_AC2 = 0x0030;
        /**
         * Microsoft Corporation
         */
        protected final static int WAVE_FORMAT_GSM610 = 0x0031;
        /**
         * Microsoft Corporation
         */
        protected final static int WAVE_FORMAT_MSNAUDIO = 0x0032;
        /**
         * Antex Electronics Corporation
         */
        protected final static int WAVE_FORMAT_ANTEX_ADPCME = 0x0033;
        /**
         * Control Resources Limited
         */
        protected final static int WAVE_FORMAT_CONTROL_RES_VQLPC = 0x0034;
        /**
         * DSP Solutions, Inc.
         */
        protected final static int WAVE_FORMAT_DIGIREAL = 0x0035;
        /**
         * DSP Solutions, Inc.
         */
        protected final static int WAVE_FORMAT_DIGIADPCM = 0x0036;
        /**
         * Control Resources Limited
         */
        protected final static int WAVE_FORMAT_CONTROL_RES_CR10 = 0x0037;
        /**
         * Natural MicroSystems
         */
        protected final static int WAVE_FORMAT_NMS_VBXADPCM = 0x0038;
        /**
         * Crystal Semiconductor IMA ADPCM
         */
        protected final static int WAVE_FORMAT_CS_IMAADPCM = 0x0039;
        /**
         * Echo Speech Corporation
         */
        protected final static int WAVE_FORMAT_ECHOSC3 = 0x003A;
        /**
         * Rockwell International
         */
        protected final static int WAVE_FORMAT_ROCKWELL_ADPCM = 0x003B;
        /**
         * Rockwell International
         */
        protected final static int WAVE_FORMAT_ROCKWELL_DIGITALK = 0x003C;
        /**
         * Xebec Multimedia Solutions Limited
         */
        protected final static int WAVE_FORMAT_XEBEC = 0x003D;
        /**
         * Antex Electronics Corporation
         */
        protected final static int WAVE_FORMAT_G721_ADPCM = 0x0040;
        /**
         * Antex Electronics Corporation
         */
        protected final static int WAVE_FORMAT_G728_CELP = 0x0041;
        /**
         * Microsoft Corporation
         */
        protected final static int WAVE_FORMAT_MPEG = 0x0050;
        /**
         * ISO/MPEG Layer3 Format Tag
         */
        protected final static int WAVE_FORMAT_MPEGLAYER3 = 0x0055;
        /**
         * Cirrus Logic
         */
        protected final static int WAVE_FORMAT_CIRRUS = 0x0060;
        /**
         * ESS Technology
         */
        protected final static int WAVE_FORMAT_ESPCM = 0x0061;
        /**
         * Voxware Inc
         */
        protected final static int WAVE_FORMAT_VOXWARE = 0x0062;
        /**
         * Canopus, co., Ltd.
         */
        protected final static int WAVE_FORMAT_CANOPUS_ATRAC = 0x0063;
        /**
         * APICOM
         */
        protected final static int WAVE_FORMAT_G726_ADPCM = 0x0064;
        /**
         * APICOM
         */
        protected final static int WAVE_FORMAT_G722_ADPCM = 0x0065;
        /**
         * Microsoft Corporation
         */
        protected final static int WAVE_FORMAT_DSAT = 0x0066;
        /**
         * Microsoft Corporation
         */
        protected final static int WAVE_FORMAT_DSAT_DISPLAY = 0x0067;
        /**
         * Softsound, Ltd.
         */
        protected final static int WAVE_FORMAT_SOFTSOUND = 0x0080;
        /**
         * Rhetorex Inc
         */
        protected final static int WAVE_FORMAT_RHETOREX_ADPCM = 0x0100;
        /**
         * Creative Labs, Inc
         */
        protected final static int WAVE_FORMAT_CREATIVE_ADPCM = 0x0200;
        /**
         * Creative Labs, Inc
         */
        protected final static int WAVE_FORMAT_CREATIVE_FASTSPEECH8 = 0x0202;
        /**
         * Creative Labs, Inc
         */
        protected final static int WAVE_FORMAT_CREATIVE_FASTSPEECH10 = 0x0203;
        /**
         * Quarterdeck Corporation
         */
        protected final static int WAVE_FORMAT_QUARTERDECK = 0x0220;
        /**
         * Fujitsu Corp.
         */
        protected final static int WAVE_FORMAT_FM_TOWNS_SND = 0x0300;
        /**
         * Brooktree Corporation
         */
        protected final static int WAVE_FORMAT_BTV_DIGITAL = 0x0400;
        /**
         * Ing C. Olivetti & C., S.p.A.
         */
        protected final static int WAVE_FORMAT_OLIGSM = 0x1000;
        /**
         * Ing C. Olivetti & C., S.p.A.
         */
        protected final static int WAVE_FORMAT_OLIADPCM = 0x1001;
        /**
         * Ing C. Olivetti & C., S.p.A.
         */
        protected final static int WAVE_FORMAT_OLICELP = 0x1002;
        /**
         * Ing C. Olivetti & C., S.p.A.
         */
        protected final static int WAVE_FORMAT_OLISBC = 0x1003;
        /**
         * Ing C. Olivetti & C., S.p.A.
         */
        protected final static int WAVE_FORMAT_OLIOPR = 0x1004;
        /**
         * Lernout & Hauspie
         */
        protected final static int WAVE_FORMAT_LH_CODEC = 0x1100;
        /**
         * Norris Communications, Inc.
         */
        protected final static int WAVE_FORMAT_NORRIS = 0x1400;
        /**
         * the WAVE_FORMAT_DEVELOPMENT format tag can be used during the
         * development phase of a new wave format. Before shipping, you MUST
         * acquire an official format tag from Microsoft.
         */
        protected final static int WAVE_FORMAT_DEVELOPMENT = 0xFFFF;
        private int sampleChunkFourCC;

        public AudioTrack(int trackIndex, int fourCC) {
            super(trackIndex, AVIMediaType.AUDIO, fourCC);
            sampleChunkFourCC = twoCC | WB_ID;

        }

        @Override
        public long getSTRFChunkSize() {
            return 18;

        }

        @Override
        public int getSampleChunkFourCC(boolean isSync) {
            return sampleChunkFourCC;
        }
    }

    /**
     * Chunk base class.
     */
    protected abstract class Chunk {

        /**
         * The chunkType of the chunk. A String with the length of 4 characters.
         */
        protected int chunkType;
        /**
         * The offset of the chunk relative to the startTime of the
         * ImageOutputStream.
         */
        protected long offset;

        /**
         * Creates a new Chunk at the current position of the ImageOutputStream.
         *
         * @param chunkType The chunkType of the chunk. A string with a length
         * of 4 characters.
         */
        public Chunk(int chunkType) throws IOException {
            this.chunkType = chunkType;
            offset = getRelativeStreamPosition();
        }

        /**
         * Writes the chunk to the ImageOutputStream and disposes it.
         */
        public abstract void finish() throws IOException;

        /**
         * Returns the size of the chunk including the size of the chunk header.
         *
         * @return The size of the chunk.
         */
        public abstract long size();
    }

    /**
     * A CompositeChunk contains an ordered list of Chunks.
     */
    protected class CompositeChunk extends Chunk {

        /**
         * The type of the composite. A String with the length of 4 characters.
         */
        protected int compositeType;
        protected LinkedList<Chunk> children;
        protected boolean finished;

        /**
         * Creates a new CompositeChunk at the current position of the
         * ImageOutputStream.
         *
         * @param compositeType The type of the composite.
         * @param chunkType The type of the chunk.
         */
        public CompositeChunk(int compositeType, int chunkType) throws IOException {
            super(chunkType);
            this.compositeType = compositeType;
            //out.write
            out.writeLong(0); // make room for the chunk header
            out.writeInt(0); // make room for the chunk header
            children = new LinkedList<Chunk>();
        }

        public void add(Chunk child) throws IOException {
            if (children.size() > 0) {
                children.getLast().finish();
            }
            children.add(child);
        }

        /**
         * Writes the chunk and all its children to the ImageOutputStream and
         * disposes of all resources held by the chunk.
         *
         * @throws java.io.IOException
         */
        @Override
        public void finish() throws IOException {
            if (!finished) {
                if (size() > 0xffffffffL) {
                    throw new IOException("CompositeChunk \"" + chunkType + "\" is too large: " + size());
                }

                long pointer = getRelativeStreamPosition();
                seekRelative(offset);

                out.setByteOrder(ByteOrder.BIG_ENDIAN);
                out.writeInt(compositeType);
                out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                out.writeInt((int) (size() - 8));
                out.setByteOrder(ByteOrder.BIG_ENDIAN);
                out.writeInt(chunkType);
                out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                for (Chunk child : children) {
                    child.finish();
                }
                seekRelative(pointer);
                if (size() % 2 == 1) {
                    out.writeByte(0); // write pad byte
                }
                finished = true;
            }
        }

        @Override
        public long size() {
            long length = 12;
            for (Chunk child : children) {
                length += child.size() + child.size() % 2;
            }
            return length;
        }
    }

    /**
     * Data Chunk.
     */
    protected class DataChunk extends Chunk {

        //protected SubImageOutputStream data;
        protected boolean finished;
        private long finishedSize;

        /**
         * Creates a new DataChunk at the current position of the
         * ImageOutputStream.
         *
         * @param name The name of the chunk.
         */
        public DataChunk(int name) throws IOException {
            this(name, -1);
        }

        /**
         * Creates a new DataChunk at the current position of the
         * ImageOutputStream.
         *
         * @param name The name of the chunk.
         * @param dataSize The size of the chunk data, or -1 if not known.
         */
        public DataChunk(int name, long dataSize) throws IOException {
            super(name);
            /*
             data = new SubImageOutputStream(out, ByteOrder.LITTLE_ENDIAN, false);
             data.writeInt(typeToInt(chunkType));
             data.writeInt((int)Math.max(0, dataSize)); */
                out.setByteOrder(ByteOrder.BIG_ENDIAN);
            out.writeInt(chunkType);
                out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            out.writeInt((int) Math.max(0, dataSize));
            finishedSize = dataSize == -1 ? -1 : dataSize + 8;
        }

        public ImageOutputStream getOutputStream() {
            if (finished) {
                throw new IllegalStateException("DataChunk is finished");
            }
            //return data;
            return out;
        }

        /**
         * Returns the offset of this chunk to the beginning of the random
         * access file
         */
        public long getOffset() {
            return offset;
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                if (finishedSize == -1) {
                    finishedSize = size();

                    if (finishedSize > 0xffffffffL) {
                        throw new IOException("DataChunk \"" + chunkType + "\" is too large: " + size());
                    }

                    seekRelative(offset + 4);
                    out.writeInt((int) (finishedSize - 8));
                    seekRelative(offset + finishedSize);
                } else {
                    if (size() != finishedSize) {
                        throw new IOException("DataChunk \"" + chunkType + "\" actual size differs from given size: actual size:" + size() + " given size:" + finishedSize);
                    }
                }
                if (size() % 2 == 1) {
                    out.writeByte(0); // write pad byte
                }


                //data.dispose();
                //data = null;
                finished = true;
            }
        }

        @Override
        public long size() {
            if (finished) {
                return finishedSize;
            }

            try {
                //               return data.length();
                return out.getStreamPosition() - offset;
            } catch (IOException ex) {
                InternalError ie = new InternalError("IOException");
                ie.initCause(ex);
                throw ie;
            }
        }
    }

    /**
     * A DataChunk with a fixed size.
     */
    protected class FixedSizeDataChunk extends Chunk {

        protected boolean finished;
        protected long fixedSize;

        /**
         * Creates a new DataChunk at the current position of the
         * ImageOutputStream.
         *
         * @param chunkType The chunkType of the chunk.
         */
        public FixedSizeDataChunk(int chunkType, long fixedSize) throws IOException {
            super(chunkType);
            this.fixedSize = fixedSize;
                out.setByteOrder(ByteOrder.BIG_ENDIAN);
            out.writeInt(chunkType);
                out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            out.writeInt((int) fixedSize);

            // Fill fixed size with nulls
            byte[] buf = new byte[(int) Math.min(512, fixedSize)];
            long written = 0;
            while (written < fixedSize) {
                out.write(buf, 0, (int) Math.min(buf.length, fixedSize - written));
                written += Math.min(buf.length, fixedSize - written);
            }
            if (fixedSize % 2 == 1) {
                out.writeByte(0); // write pad byte
            }
            seekToStartOfData();
        }

        public ImageOutputStream getOutputStream() {
            /*if (finished) {
             throw new IllegalStateException("DataChunk is finished");
             }*/
            return out;
        }

        /**
         * Returns the offset of this chunk to the beginning of the random
         * access file
         */
        public long getOffset() {
            return offset;
        }

        public void seekToStartOfData() throws IOException {
            seekRelative(offset + 8);

        }

        public void seekToEndOfChunk() throws IOException {
            seekRelative(offset + 8 + fixedSize + fixedSize % 2);
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                finished = true;
            }
        }

        @Override
        public long size() {
            return 8 + fixedSize;
        }
    }

    protected class MidiTrack extends Track {

        private final int sampleChunkFourCC;

        public MidiTrack(int trackIndex, int fourCC) {
            super(trackIndex, AVIMediaType.MIDI, fourCC);
            sampleChunkFourCC = twoCC | WB_ID;

        }

        @Override
        public long getSTRFChunkSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getSampleChunkFourCC(boolean isSync) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    protected class TextTrack extends Track {

        private final int sampleChunkFourCC;

        public TextTrack(int trackIndex, int fourCC) {
            super(trackIndex, AVIMediaType.TEXT, fourCC);
            sampleChunkFourCC = twoCC | WB_ID;

        }

        @Override
        public long getSTRFChunkSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getSampleChunkFourCC(boolean isSync) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /**
     * <p>Holds information about the entire movie. </p>
     *
     * <pre>
     * ---------------
     * AVI Main Header
     * ---------------
     *
     * Set values taken from
     * http://graphics.cs.uni-sb.de/NMM/dist-0.4.0/Docs/Doxygen/html/avifmt_8h.html
     *
     * typedef struct {
     *     DWORD  microSecPerFrame;
     *             // Specifies the number of microseconds between frames.
     *             // This value indicates the overall timing for the file.
     *     DWORD  maxBytesPerSec;
     *             // Specifies the approximate maximum data rate of the file. This
     *             // value indicates the number of bytes per second the system must
     *             // handle to present an AVI sequence as specified by the other
     *             // parameters contained in the main header and stream header chunks.
     *     DWORD  paddingGranularity;
     *             // Specifies the alignment for data, in bytes. Pad the data to
     *             // multiples of this value.
     *     DWORD set avihFlags  flags;
     *             // Contains a bitwise combination of zero or more of the following flags:
     *     DWORD  totalFrames;
     *             // Specifies the total number of frames of data in the file.
     *     DWORD  initialFrames;
     *             // Specifies the initial frame for interleaved files. Noninterleaved
     *             // files should specify zero. If you are creating interleaved files,
     *             // specify the number of frames in the file prior to the initial
     *             // frame of the AVI sequence in this member. For more information
     *             // about the contents of this member, see "Special Information for
     *             // Interleaved Files" in the Video for Windows Programmer's Guide.
     *     DWORD  streams;
     *             // Specifies the number of streams in the file. For example, a file
     *             // with audio and video has two streams.
     *     DWORD  suggestedBufferSize;
     *             // Specifies the suggested buffer size for reading the file.
     *             // Generally, this size should be large enough to contain the
     *             // largest chunk in the file. If set to zero, or if it is too small,
     *             // the playback software will have to reallocate memory during
     *             // playback, which will reduce performance. For an interleaved file,
     *             // the buffer size should be large enough to read an entire record,
     *             // and not just a chunk.
     *     DWORD  width;
     *             // Specifies the width of the AVI file in pixels.
     *     DWORD  height;
     *             // Specifies the height of the AVI file in pixels.
     *     DWORD[]  reserved;
     *             // Reserved. Set this array to zero.
     * } AVIMAINHEADER;
     * </pre>
     */
    protected static class MainHeader {

        /**
         * Specifies the number of microseconds (=10E-6 seconds) between frames.
         * This value indicates the overall timing for the file.
         */
        protected long microSecPerFrame;
        protected long maxBytesPerSec;
        protected long paddingGranularity;
        protected int flags;
        protected long totalFrames;
        protected long initialFrames;
        protected long streams;
        protected long suggestedBufferSize;
        /**
         * Width and height of the movie. Null if not specified.
         */
        protected Dimension size;
    }

    protected static int typeToInt(String str) {
        int value = ((str.charAt(0) & 0xff) << 24) | ((str.charAt(1) & 0xff) << 16) | ((str.charAt(2) & 0xff) << 8) | (str.charAt(3) & 0xff);
        return value;
    }

    protected static String intToType(int id) {
        char[] b=new char[4];
        
            b[0] = (char) ((id >>> 24) & 0xff);
            b[1] = (char) ((id >>> 16) & 0xff);
            b[2] = (char) ((id >>> 8) & 0xff);
            b[3] = (char) (id & 0xff);
            return String.valueOf(b);
    }

    /**
     * Returns true, if the specified mask is set on the flag.
     */
    protected static boolean isFlagSet(int flag, int mask) {
        return (flag & mask) == mask;
    }
}
