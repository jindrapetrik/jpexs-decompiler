/*
 * 11/19/04 1.0 moved to LGPL.
 * 12/12/99 Original verion. mdm@techie.com.
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package javazoom.jl.converter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.Obuffer;


/**
 * The <code>Converter</code> class implements the conversion of
 * an MPEG audio file to a .WAV file. To convert an MPEG audio stream,
 * just create an instance of this class and call the convert()
 * method, passing in the names of the input and output files. You can
 * pass in optional <code>ProgressListener</code> and
 * <code>Decoder.Params</code> objects also to customize the conversion.
 *
 * @author MDM 12/12/99
 * @since 0.0.7
 */
public class Converter {

    /**
     * Creates a new converter instance.
     */
    public Converter() {
    }

    public synchronized void convert(String sourceName, String destName) throws JavaLayerException {
        convert(sourceName, destName, null, null);
    }

    public synchronized void convert(String sourceName,
                                     String destName,
                                     ProgressListener progressListener) throws JavaLayerException {
        convert(sourceName, destName, progressListener, null);
    }

    public void convert(String sourceName,
                        String destName,
                        ProgressListener progressListener,
                        Decoder.Params decoderParams) throws JavaLayerException {
        if (destName.length() == 0)
            destName = null;
        try {
            InputStream in = openInput(sourceName);
            convert(in, destName, progressListener, decoderParams);
            in.close();
        } catch (IOException ioe) {
            throw new JavaLayerException(ioe.getLocalizedMessage(), ioe);
        }
    }

    public synchronized void convert(InputStream sourceStream,
                                     String destName,
                                     ProgressListener progressListener,
                                     Decoder.Params decoderParams) throws JavaLayerException {
        if (progressListener == null)
            progressListener = PrintWriterProgressListener.newStdOut(PrintWriterProgressListener.NO_DETAIL);
        try {
            if (!(sourceStream instanceof BufferedInputStream))
                sourceStream = new BufferedInputStream(sourceStream);
            int frameCount = -1;
            if (sourceStream.markSupported()) {
                sourceStream.mark(-1);
                frameCount = countFrames(sourceStream);
                sourceStream.reset();
            }
            progressListener.converterUpdate(ProgressListener.UPDATE_FRAME_COUNT, frameCount, 0);

            Obuffer output = null;
            Decoder decoder = new Decoder(decoderParams);
            Bitstream stream = new Bitstream(sourceStream);

            if (frameCount == -1)
                frameCount = Integer.MAX_VALUE;

            int frame = 0;
            long startTime = System.currentTimeMillis();

            try {
                for (; frame < frameCount; frame++) {
                    try {
                        Header header = stream.readFrame();
                        if (header == null)
                            break;

                        progressListener.readFrame(frame, header);

                        if (output == null) {
                            // REVIEW: Incorrect functionality.
                            // the decoder should provide decoded
                            // frequency and channels output as it may differ from
                            // the source (e.g. when downmixing stereo to mono.)
                            int channels = (header.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;
                            int freq = header.frequency();
                            output = new WaveFileObuffer(channels, freq, destName);
                            decoder.setOutputBuffer(output);
                        }

                        Obuffer decoderOutput = decoder.decodeFrame(header, stream);

                        // REVIEW: the way the output buffer is set
                        // on the decoder is a bit dodgy. Even though
                        // this exception should never happen, we test to be sure.
                        if (decoderOutput != output)
                            throw new InternalError("Output buffers are different.");

                        progressListener.decodedFrame(frame, header, output);

                        stream.closeFrame();

                    } catch (Exception ex) {
                        boolean stop = !progressListener.converterException(ex);

                        if (stop) {
                            throw new JavaLayerException(ex.getLocalizedMessage(), ex);
                        }
                    }
                }

            } finally {

                if (output != null)
                    output.close();
            }

            int time = (int) (System.currentTimeMillis() - startTime);
            progressListener.converterUpdate(ProgressListener.UPDATE_CONVERT_COMPLETE, time, frame);
        } catch (IOException ex) {
            throw new JavaLayerException(ex.getLocalizedMessage(), ex);
        }
    }

    protected int countFrames(InputStream in) {
        return -1;
    }

    protected InputStream openInput(String fileName) throws IOException {
        // ensure name is abstract path name
        File file = new File(fileName);
        InputStream fileIn = Files.newInputStream(file.toPath());
        BufferedInputStream bufIn = new BufferedInputStream(fileIn);

        return bufIn;
    }

    /**
     * This interface is used by the Converter to provide
     * notification of tasks being carried out by the converter,
     * and to provide new information as it becomes available.
     */
    public interface ProgressListener {
        int UPDATE_FRAME_COUNT = 1;

        /**
         * Conversion is complete. Param1 contains the time
         * to convert in milliseconds. Param2 contains the number
         * of MPEG audio frames converted.
         */
        int UPDATE_CONVERT_COMPLETE = 2;

        /**
         * Notifies the listener that new information is available.
         *
         * @param updateID Code indicating the information that has been
         *                 updated.
         * @param param1   Parameter whose value depends upon the update code.
         * @param param2   Parameter whose value depends upon the update code.
         *                 <p>
         *                 The <code>updateID</code> parameter can take these values:
         *                 <p>
         *                 UPDATE_FRAME_COUNT: param1 is the frame count, or -1 if
         *                 not known.
         *                 UPDATE_CONVERT_COMPLETE: param1 is the conversion time,
         *                 param2
         *                 is the number of frames converted.
         */
        void converterUpdate(int updateID, int param1, int param2);

        /**
         * If the converter wishes to make a first pass over the
         * audio frames, this is called as each frame is parsed.
         */
        void parsedFrame(int frameNo, Header header);

        /**
         * This method is called after each frame has been read,
         * but before it has been decoded.
         *
         * @param frameNo The 0-based sequence number of the frame.
         * @param header  The Header rerpesenting the frame just read.
         */
        void readFrame(int frameNo, Header header);

        /**
         * This method is called after a frame has been decoded.
         *
         * @param frameNo The 0-based sequence number of the frame.
         * @param header  The Header rerpesenting the frame just read.
         * @param o       The Obuffer the decoded data was written to.
         */
        void decodedFrame(int frameNo, Header header, Obuffer o);

        /**
         * Called when an exception is thrown during while converting
         * a frame.
         *
         * @param t The <code>Throwable</code> instance that
         *          was thrown.
         * @return <code>true</code> to continue processing, or false
         * to abort conversion.
         * <p>
         * If this method returns <code>false</code>, the exception
         * is propagated to the caller of the convert() method. If
         * <code>true</code> is returned, the exception is silently
         * ignored and the converter moves onto the next frame.
         */
        boolean converterException(Throwable t);
    }

    /**
     * Implementation of <code>ProgressListener</code> that writes
     * notification text to a <code>PrintWriter</code>.
     * <p>
     * REVIEW: i18n of text and order required.
     */
    static public class PrintWriterProgressListener implements ProgressListener {
        static public final int NO_DETAIL = 0;

        /**
         * Level of detail typically expected of expert
         * users.
         */
        static public final int EXPERT_DETAIL = 1;

        /**
         * Verbose detail.
         */
        static public final int VERBOSE_DETAIL = 2;

        /**
         * Debug detail. All frame read notifications are shown.
         */
        static public final int DEBUG_DETAIL = 7;

        static public final int MAX_DETAIL = 10;

        private PrintWriter pw;

        private int detailLevel;

        static public PrintWriterProgressListener newStdOut(int detail) {
            return new PrintWriterProgressListener(new PrintWriter(System.out, true), detail);
        }

        public PrintWriterProgressListener(PrintWriter writer, int detailLevel) {
            this.pw = writer;
            this.detailLevel = detailLevel;
        }

        public boolean isDetail(int detail) {
            return (this.detailLevel >= detail);
        }

        public void converterUpdate(int updateID, int param1, int param2) {
            if (isDetail(VERBOSE_DETAIL)) {
                switch (updateID) {
                case UPDATE_CONVERT_COMPLETE:
                    // catch divide by zero errors.
                    if (param2 == 0)
                        param2 = 1;

                    pw.println();
                    pw.println("Converted " + param2 + " frames in " + param1 + " ms (" + (param1 / param2)
                            + " ms per frame.)");
                }
            }
        }

        public void parsedFrame(int frameNo, Header header) {
            if ((frameNo == 0) && isDetail(VERBOSE_DETAIL)) {
                String headerString = header.toString();
                pw.println("File is a " + headerString);
            } else if (isDetail(MAX_DETAIL)) {
                String headerString = header.toString();
                pw.println("Prased frame " + frameNo + ": " + headerString);
            }
        }

        public void readFrame(int frameNo, Header header) {
            if ((frameNo == 0) && isDetail(VERBOSE_DETAIL)) {
                String headerString = header.toString();
                pw.println("File is a " + headerString);
            } else if (isDetail(MAX_DETAIL)) {
                String headerString = header.toString();
                pw.println("Read frame " + frameNo + ": " + headerString);
            }
        }

        public void decodedFrame(int frameNo, Header header, Obuffer o) {
            if (isDetail(MAX_DETAIL)) {
                String headerString = header.toString();
                pw.println("Decoded frame " + frameNo + ": " + headerString);
                pw.println("Output: " + o);
            } else if (isDetail(VERBOSE_DETAIL)) {
                if (frameNo == 0) {
                    pw.print("Converting.");
                    pw.flush();
                }

                if ((frameNo % 10) == 0) {
                    pw.print('.');
                    pw.flush();
                }
            }
        }

        public boolean converterException(Throwable t) {
            if (this.detailLevel > NO_DETAIL) {
                t.printStackTrace(pw);
                pw.flush();
            }
            return false;
        }
    }
}
