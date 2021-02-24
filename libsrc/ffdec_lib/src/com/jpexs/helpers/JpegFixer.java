package com.jpexs.helpers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fixes probles in some JPEGs to be readable by standard viewers.
 *
 * @author JPEXS
 */
public class JpegFixer {

    private static final int SOI = 0xD8;
    private static final int EOI = 0xD9;
    private static final int SOF0 = 0xC0;
    private static final int SOF1 = 0xC1;
    private static final int SOF2 = 0xC2;
    private static final int SOF3 = 0xC3;
    private static final int SOF5 = 0xC5;
    private static final int SOF6 = 0xC6;
    private static final int SOF7 = 0xC7;
    private static final int SOF9 = 0xC9;
    private static final int SOF10 = 0xCA;
    private static final int SOF11 = 0xCB;
    private static final int SOF13 = 0xCD;
    private static final int SOF14 = 0xCE;
    private static final int SOF15 = 0xCF;

    private static final int APP0 = 0xE0;


    public void fixJpeg(InputStream is, OutputStream os) throws IOException {
        List<byte[]> data = new ArrayList<>();
        List<Integer> markers = new ArrayList<>();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int lastMarker = -1;
        int val = is.read();
        if (val == -1) {
            return;
        }
        if (val == 0xFF) {
            val = is.read();
            if (val != SOI) {
                //not a JPEG file, proceed as is
                os.write(0xFF);
                os.write(val);
                while ((val = is.read()) > -1) {
                    os.write(val);
                }
                return;
            }
        } else {
            //not a JPEG file, proceed as is
            os.write(val);
            while ((val = is.read()) > -1) {
                os.write(val);
            }
            return;
        }
        while ((val = is.read()) > -1) {
            if (val == 0xFF) {
                val = is.read();
                if (val == 0) {
                    baos.write(0xff);
                    baos.write(val);
                    continue;
                }
            } else {
                baos.write(val);
                continue;
            }
            if (lastMarker > -1) {
                data.add(baos.toByteArray());
                markers.add(lastMarker);
                baos = new ByteArrayOutputStream();
            }
            lastMarker = val;
        }
        if (lastMarker > -1) {
            data.add(baos.toByteArray());
            markers.add(lastMarker);
        }

        boolean wasApp0 = false;
        for (int i = 0; i < data.size(); i++) {
            if (markers.get(i) == APP0) {
                wasApp0 = true;
            }
            if (i > 0 && markers.get(i) == SOI && markers.get(i - 1) == EOI && !wasApp0) {
                markers.remove(i);
                data.remove(i);
                markers.remove(i - 1);
                data.remove(i - 1);
                i--;
                List<byte[]> dataToMove = new ArrayList<>();
                List<Integer> markersToMove = new ArrayList<>();
                for (int j = i; j < data.size(); j++) {
                    //move these data up
                    if (markers.get(j) == APP0 || Arrays.asList(SOF0, SOF1, SOF2, SOF3, SOF5, SOF6, SOF7, SOF9, SOF10, SOF11, SOF13, SOF14, SOF15).contains(markers.get(j))) {
                        markersToMove.add(markers.get(j));
                        dataToMove.add(data.get(j));
                        data.remove(j);
                        markers.remove(j);
                        j--;
                    } else {
                        break;
                    }
                }
                data.addAll(1, dataToMove);
                markers.addAll(1, markersToMove);
                break;
            }
        }

        os.write(0xFF);
        os.write(SOI);
        for (int i = 0; i < data.size(); i++) {
            os.write(0xFF);
            os.write(markers.get(i));
            os.write(data.get(i));
        }
    }
}
