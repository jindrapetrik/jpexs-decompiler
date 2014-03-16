/*
 * @(#)PNGCodec.java  
 *
 * Copyright (c) 2011-2012 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.png;

import org.monte.media.Format;
import org.monte.media.AbstractVideoCodec;
import org.monte.media.Buffer;
import org.monte.media.io.ByteArrayImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import static org.monte.media.VideoFormatKeys.*;
import static org.monte.media.BufferFlag.*;

/**
 * {@code PNGCodec} encodes a BufferedImage as a byte[] array..
 * <p>
 * Supported input formats:
 * <ul>
 * {@code VideoFormat} with {@code BufferedImage.class}, any width, any height,
 * any depth.
 * </ul>
 * Supported output formats:
 * <ul>
 * {@code VideoFormat} with {@code byte[].class}, same width and height as input
 * format, depth=24.
 * </ul>
 *
 * @author Werner Randelshofer
 * @version $Id: PNGCodec.java 299 2013-01-03 07:40:18Z werner $
 */
public class PNGCodec extends AbstractVideoCodec {

    public PNGCodec() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE), //
                },
                new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    DepthKey, 24,
                    EncodingKey, ENCODING_QUICKTIME_PNG, DataClassKey, byte[].class), //
                    //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    DepthKey, 24,
                    EncodingKey, ENCODING_AVI_PNG, DataClassKey, byte[].class), //
                });
    }

    @Override
    public Format setOutputFormat(Format f) {
        String mimeType = f.get(MimeTypeKey, MIME_QUICKTIME);
        if (mimeType != null && !mimeType.equals(MIME_AVI)) {
            return super.setOutputFormat(
                    f.prepend(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_QUICKTIME_PNG, DataClassKey,
                    byte[].class, DepthKey, 24));
        } else {
            return super.setOutputFormat(
                    f.prepend(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_PNG, DataClassKey,
                    byte[].class, DepthKey, 24));
        }
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        BufferedImage image = getBufferedImage(in);
        if (image == null) {
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }

        ByteArrayImageOutputStream tmp;
        if (out.data instanceof byte[]) {
            tmp = new ByteArrayImageOutputStream((byte[]) out.data);
        } else {
            tmp = new ByteArrayImageOutputStream();
        }

        try {
            ImageWriter iw = ImageIO.getImageWritersByMIMEType("image/png").next();
            ImageWriteParam iwParam = iw.getDefaultWriteParam();
            iw.setOutput(tmp);
            IIOImage img = new IIOImage(image, null, null);
            iw.write(null, img, iwParam);
            iw.dispose();

            out.setFlag(KEYFRAME);
            out.header = null;
            out.data = tmp.getBuffer();
            out.offset = 0;
            out.length = (int) tmp.getStreamPosition();
            return CODEC_OK;
        } catch (IOException ex) {
            ex.printStackTrace();
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
    }
}
